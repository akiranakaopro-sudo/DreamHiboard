package gd.app.hiboard.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.dynamicanimation.animation.FloatValueHolder
import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce
import gd.app.hiboard.R
import gd.app.hiboard.host.HiboardViewController
import gd.app.hiboard.ui.HiboardView
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

/**
 * Server-side [ILauncherOverlay] implementation.
 *
 * Oppo AssistantScreen parity:
 * - Finger-down: launcher owns progress via startScroll / onScroll.
 * - Finger-up: COUI spring settle (response/bounce) owns progress; overlayScrollChanged every frame.
 * - New startScroll cancels settle (interruptible reverse).
 * - Panel interactive once progress >= 0.25 and launcher finger is up.
 */
class HiboardOverlayBinder(
    private val appContext: Context,
) : ILauncherOverlay.Stub() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val windowManager =
        appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var callback: ILauncherOverlayCallback? = null
    private var hostController: HiboardViewController? = null
    private var hostView: View? = null
    private var windowParams: WindowManager.LayoutParams? = null
    private var windowWidth = 0
    private var progress = 0f
    private var scrolling = false
    /** True only between startScroll and endScroll; drops late oneway onScroll after finger up. */
    private var acceptingUserScroll = false
    private var settleSpring: COUISpringAnimation? = null
    private var attached = false
    private var resumed = false
    private var contentEntered = false
    /** True after [hostController.resume] until pause/exit — never resume every frame. */
    private var contentResumed = false
    private var pendingProgress: Float? = null
    private var progressApplyScheduled = false
    private var lastNotifiedProgress = -1f
    private var settleVelocityPx = 0f
    private var closeDragging = false
    private var settleTarget = -1f
    private var windowInteractive = false
    /** Estimated horizontal velocity (px/s) from recent onScroll samples. */
    private var scrollVelocityPx = 0f
    /**
     * Most leftward (negative) px/s seen this gesture — ACTION_UP often decelerates
     * to ~0, which wrongly cancelled short-quick close flicks.
     */
    private var peakCloseVelocityPx = 0f
    /** Most rightward (positive) px/s — interrupt-close reopen flicks. */
    private var peakOpenVelocityPx = 0f
    /** Finger grabbed the panel while a close settle was running — reopen easier. */
    private var interruptedCloseSettle = false
    private var lastScrollSampleProgress = 0f
    private var lastScrollSampleTime = 0L
    /** Gesture clock for open-from-home short-flick velocity estimate. */
    private var sessionStartTimeMs = 0L
    private var sessionStartProgress = 0f
    /**
     * True when this finger session started from a mostly-open panel (close /
     * reverse). Uses Oppo scrollOut keep-open threshold instead of open 0.25.
     */
    private var sessionFromOpen = false
    private var panelBgRgbCached = 0
    private var overscrollLayersOn = false
    /**
     * True while the WM window is parked off-screen ([LayoutParams.x] = -width),
     * matching Oppo AssistantScreenWindow.updateCloseLayoutX / OverlayWindow
     * (p < 0.01 → x = -width).
     */
    private var windowParked = true
    /**
     * Oppo fixed Assist plate (DecorView / blur bg). Opaque #8397cc color;
     * translucency via [View.setAlpha] — NOT ColorDrawable.setAlpha (that flips
     * window opaque↔translucent composition and blinks while dragging).
     */
    private var plateView: View? = null
    /**
     * Content layers only — Oppo slides EventProcessor via setScrollX; the
     * plate stays fixed full-bleed under them.
     */
    private var contentSlide: View? = null
    private var storeSlide: View? = null
    /**
     * Bumps on every startScroll so a delayed endScroll from a prior gesture cannot
     * spring-open after the user already reversed (AIDL oneway race).
     */
    private val scrollGen = AtomicInteger(0)

    private val applyPendingProgressRunnable = Runnable {
        progressApplyScheduled = false
        val p = pendingProgress ?: return@Runnable
        pendingProgress = null
        applyProgress(p, fromUser = true)
    }

    init {
        active = this
    }

    override fun windowAttached2(bundle: Bundle?, cb: ILauncherOverlayCallback?) {
        @Suppress("DEPRECATION")
        val attrs = bundle?.getParcelable<WindowManager.LayoutParams>(
            OverlayContract.EXTRA_LAYOUT_PARAMS,
        )
        mainHandler.post { attachWindow(attrs, cb) }
    }

    override fun windowDetached(isChangingConfigurations: Boolean) {
        mainHandler.post { detachWindow() }
    }

    override fun startScroll() {
        val gen = scrollGen.incrementAndGet()
        // Panel close/reopen owns the finger — do not steal session flags (rapid
        // left/right chatter was leaving acceptingUserScroll stuck true).
        if (closeDragging) {
            Log.i(TAG, "startScroll ignored — panel owns gesture gen=$gen")
            return
        }
        // If onScroll already auto-started this gesture, keep its sessionFromOpen.
        // Late begin+end on finger-up used to re-read progress (e.g. 0.74) and
        // wrongly mark the session as close-from-open → settle closed.
        if (!acceptingUserScroll) {
            sessionFromOpen = progress >= SESSION_FROM_OPEN_PROGRESS
            markScrollSessionStart()
        }
        acceptingUserScroll = true
        scrolling = true
        mainHandler.post {
            if (gen != scrollGen.get()) return@post
            if (closeDragging) {
                acceptingUserScroll = false
                scrolling = false
                return@post
            }
            cancelSettle(/* keepProgress = */ true)
            settleTarget = -1f
            scrolling = true
            acceptingUserScroll = true
            ensureEntered()
            if (progress < PANEL_INTERACTIVE_THRESHOLD) {
                syncTouchable(false)
                syncCloseGestureEnabled(false)
            }
        }
    }

    override fun onScroll(p: Float) {
        if (closeDragging) return
        // Oppo launcher sends undamped |amount|/width (may be > 1). Map past-open
        // through COUI closed-form damp; finger-driven close already sends damped p.
        val visual = mapLauncherScrollToVisual(p)
        // Post-finger workspace rubber-band must not yank an open settle to 0.
        if (!acceptingUserScroll && settleTarget >= 1f && visual < progress) {
            return
        }
        // Oppo streams onScrollChange even before begin succeeds. Accept progress
        // and implicitly open the session so early finger travel is not dropped.
        if (!acceptingUserScroll) {
            if (visual <= 0f && progress <= 0f) return
            acceptingUserScroll = true
            scrolling = true
            // Session kind is fixed at finger-down progress — never from [visual].
            // Fast open frames (visual≥0.5) used to mark fromOpen and then a
            // weak left tip at past-open wrongly settled closed.
            sessionFromOpen = progress >= SESSION_FROM_OPEN_PROGRESS
            markScrollSessionStart()
            mainHandler.post {
                if (closeDragging) {
                    acceptingUserScroll = false
                    scrolling = false
                    return@post
                }
                cancelSettle(/* keepProgress = */ true)
                settleTarget = -1f
                scrolling = true
                acceptingUserScroll = true
                ensureEntered()
                if (progress < PANEL_INTERACTIVE_THRESHOLD) {
                    syncTouchable(false)
                    syncCloseGestureEnabled(false)
                }
            }
        }
        sampleScrollVelocity(visual)
        pendingProgress = visual
        if (Looper.myLooper() == mainHandler.looper) {
            if (progressApplyScheduled) {
                mainHandler.removeCallbacks(applyPendingProgressRunnable)
                progressApplyScheduled = false
            }
            pendingProgress = null
            applyProgress(visual, fromUser = true)
            return
        }
        if (progressApplyScheduled) return
        progressApplyScheduled = true
        mainHandler.post(applyPendingProgressRunnable)
    }

    /** Stamp gesture clock once per finger session (open or close). */
    private fun markScrollSessionStart(force: Boolean = false) {
        if (!force && sessionStartTimeMs != 0L) return
        sessionStartTimeMs = android.os.SystemClock.uptimeMillis()
        sessionStartProgress = progress
        peakOpenVelocityPx = 0f
        peakCloseVelocityPx = 0f
        scrollVelocityPx = 0f
        lastScrollSampleTime = 0L
    }

    /**
     * Launcher open path: undamped p → visual with COUI soft past-open.
     * Values already in (1, 1+MAX] from the panel finger path pass through.
     */
    private fun mapLauncherScrollToVisual(p: Float): Float {
        val raw = p.coerceAtLeast(0f)
        if (raw <= 1f) return raw
        val w = windowWidth.toFloat().coerceAtLeast(1f)
        val maxOver = w * CouiOverscroll.MAX_FRACTION
        val undampedOver = (raw - 1f) * w
        val visualOver = CouiOverscroll.visualFromUndamped(undampedOver, maxOver)
        return 1f + visualOver / w
    }

    override fun endScroll() {
        scheduleFinishScroll(scrollGen.get(), scrollVelocityPx)
    }

    override fun endScrollWithVelocity(velocity: Float) {
        scheduleFinishScroll(scrollGen.get(), velocity)
    }

    /**
     * Finger-up must settle immediately. Previously endScroll was posted to the
     * front of the queue while startScroll still had `scrolling=true` pending —
     * finish ran first, saw !scrolling, and dropped the settle (late/no open).
     */
    private fun scheduleFinishScroll(gen: Int, velocity: Float) {
        acceptingUserScroll = false
        val run = Runnable {
            if (gen != scrollGen.get()) {
                Log.i(TAG, "drop stale endScroll gen=$gen now=${scrollGen.get()}")
                return@Runnable
            }
            if (closeDragging) return@Runnable
            pendingProgress?.let {
                pendingProgress = null
                progressApplyScheduled = false
                mainHandler.removeCallbacks(applyPendingProgressRunnable)
                applyProgress(it, fromUser = true)
            }
            // Settle even if startScroll's main post has not run yet.
            scrolling = false
            enablePanelIfReady(gen)
            finishScroll(velocity)
        }
        if (Looper.myLooper() == mainHandler.looper) {
            run.run()
        } else {
            mainHandler.post(run)
        }
    }

    private fun enablePanelIfReady(gen: Int) {
        if (gen != scrollGen.get()) return
        if (closeDragging) return
        if (acceptingUserScroll) return
        val p = (pendingProgress ?: progress).coerceIn(0f, 1f)
        if (p < PANEL_INTERACTIVE_THRESHOLD) return
        syncTouchable(true)
        syncCloseGestureEnabled(true)
        Log.i(TAG, "panel interactive early p=$p")
    }

    override fun openOverlay(flags: Int) {
        mainHandler.post {
            ensureEntered()
            animateTo(1f, 0f)
        }
    }

    override fun closeOverlay(flags: Int) {
        mainHandler.post { animateTo(0f, 0f) }
    }

    override fun onStart() {}

    override fun onResume() {
        mainHandler.post {
            resumed = true
            if (progress >= 1f) {
                ensureContentResumed()
            }
        }
    }

    override fun onPause() {
        mainHandler.post {
            resumed = false
            ensureContentPaused()
        }
    }

    override fun onStop() {
        mainHandler.post {
            if (progress > 0f) {
                cancelSettle(keepProgress = false)
                applyProgress(0f, fromUser = false)
                clearContentEntered()
            }
        }
    }

    override fun onDestroy() {
        mainHandler.post { detachWindow() }
    }

    override fun hasOverlayContent(): Boolean = attached

    private fun attachWindow(attrs: WindowManager.LayoutParams?, cb: ILauncherOverlayCallback?) {
        callback = cb
        if (attrs == null) {
            Log.w(TAG, "windowAttached without LayoutParams")
            notifyStatus(0)
            return
        }

        // Already attached: only refresh token if it changed. Do NOT updateViewLayout
        // + STATUS_CONNECTED on every windowAttached2 — that relayout/focus-thrashed
        // and blinked the home page under a full-bleed empty blue plate.
        if (attached && hostView != null) {
            val existing = windowParams
            val newToken = attrs.token
            if (existing != null && newToken != null && existing.token != newToken) {
                existing.token = newToken
                try {
                    windowManager.updateViewLayout(hostView, existing)
                } catch (e: Exception) {
                    Log.w(TAG, "token update failed — recreating", e)
                    detachWindowPreservingCallback(cb)
                }
            }
            if (attached && hostView != null) {
                // Already connected — skip STATUS_CONNECTED spam.
                return
            }
        } else if (attached) {
            // Flag said attached but view is gone.
            Log.w(TAG, "overlay attached flag without hostView — recreating")
            detachWindowPreservingCallback(cb)
        }

        val controller = HiboardViewController(appContext)
        val view = controller.create(onNavigateHome = {
            mainHandler.post { animateTo(0f, 0f) }
        })
        if (view is HiboardView) {
            view.onCloseScrollBegin = {
                view.seedCloseProgress(progress)
                beginCloseDrag()
            }
            view.onCloseScroll = { onCloseDragProgress(it) }
            view.onCloseScrollEnd = { endCloseDrag(it) }
        }
        // Oppo AssistantScreenWindow: fixed full-bleed plate + content slides
        // (setScrollX). Park WM when closed (updateCloseLayoutX).
        contentSlide = view.findViewById(R.id.boardRoot)
        storeSlide = view.findViewById(R.id.storeRoot)
        (view as? android.view.ViewGroup)?.let { root ->
            for (i in 0 until root.childCount) {
                root.getChildAt(i)?.setBackgroundColor(Color.TRANSPARENT)
            }
            // Plate behind chrome — solid color, alpha via View.setAlpha (Oppo
            // BackgroundController → DecorView.setAlpha).
            val plate = View(appContext).apply {
                setBackgroundColor(panelBgRgb() or 0xFF000000.toInt())
                alpha = 0f
                isClickable = false
                isFocusable = false
                // Stable alpha compositing while dragging (avoids SoftLayer blink).
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
            }
            root.addView(
                plate,
                0,
                android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                ),
            )
            plateView = plate
        }
        view.background = null
        view.translationX = 0f
        view.visibility = View.INVISIBLE
        view.alpha = 1f
        contentSlide?.translationX = -appContext.resources.displayMetrics.widthPixels.toFloat()
        storeSlide?.translationX = -appContext.resources.displayMetrics.widthPixels.toFloat()
        view.isFocusable = false
        view.isFocusableInTouchMode = false
        view.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK &&
                event.action == android.view.KeyEvent.ACTION_UP
            ) {
                controller.onBackPressed()
            } else {
                false
            }
        }

        val params = buildParams(attrs)
        windowWidth = if (params.width > 0) {
            params.width
        } else {
            appContext.resources.displayMetrics.widthPixels
        }
        // Park before addView so the first composite frame is off-screen.
        params.x = -windowWidth
        windowParked = true

        try {
            windowManager.addView(view, params)
            hostController = controller
            hostView = view
            windowParams = params
            attached = true
            windowInteractive = false
            // Mid-gesture attach: keep launcher progress; otherwise start closed.
            val resumeProgress = if (acceptingUserScroll) {
                (pendingProgress ?: progress).coerceIn(0f, 1f)
            } else {
                0f
            }
            pendingProgress = null
            progressApplyScheduled = false
            mainHandler.removeCallbacks(applyPendingProgressRunnable)
            if (resumeProgress > 0f) {
                ensureEntered()
            }
            applyProgress(resumeProgress, fromUser = acceptingUserScroll)
            notifyStatus(OverlayContract.STATUS_CONNECTED)
            Log.i(TAG, "overlay window attached width=$windowWidth p=$resumeProgress")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay window", e)
            controller.destroy()
            hostController = null
            hostView = null
            plateView = null
            contentSlide = null
            storeSlide = null
            attached = false
            windowParked = true
            notifyStatus(0)
        }
    }

    private fun detachWindowPreservingCallback(cb: ILauncherOverlayCallback?) {
        cancelSettle(keepProgress = false)
        scrolling = false
        closeDragging = false
        settleTarget = -1f
        progress = 0f
        contentEntered = false
        contentResumed = false
        pendingProgress = null
        progressApplyScheduled = false
        acceptingUserScroll = false
        windowInteractive = false
        overscrollLayersOn = false
        windowParked = true
        mainHandler.removeCallbacks(applyPendingProgressRunnable)
        lastNotifiedProgress = -1f
        try {
            hostView?.let { windowManager.removeViewImmediate(it) }
        } catch (_: Exception) {
        }
        hostController?.destroy()
        hostController = null
        hostView = null
        plateView = null
        contentSlide = null
        storeSlide = null
        windowParams = null
        attached = false
        callback = cb
    }

    private fun buildParams(source: WindowManager.LayoutParams): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams()
        // Do not copyFrom(launcher) — inherits FORCE_DRAW_STATUS_BAR_BACKGROUND and
        // other private flags that make the panel composite like an opaque plate.
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        params.x = 0
        params.y = 0
        params.gravity = Gravity.START or Gravity.TOP
        params.format = PixelFormat.TRANSLUCENT
        params.title = "HiboardOverlay"
        params.token = source.token
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
        params.flags = closedWindowFlags()
        // Draw behind status / nav / cutout. Content pads via WindowInsets; the sheet
        // ColorDrawable must fill the full display (mtk.png was clipped above the pill).
        params.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            params.setFitInsetsTypes(0)
        }
        return params
    }

    private fun closedWindowFlags(): Int {
        return (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED) and
            WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
    }

    private fun openWindowFlags(): Int {
        // Keep NOT_FOCUSABLE so the panel never steals focus from launcher (focus
        // thrash HiboardOverlay ↔ CustomizeLauncher blinked the home page).
        return (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED) and
            WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
    }

    private fun syncTouchable(interactive: Boolean) {
        if (windowInteractive == interactive) return
        windowInteractive = interactive
        val view = hostView ?: return
        val params = windowParams ?: return
        params.flags = if (interactive) openWindowFlags() else closedWindowFlags()
        // Preserve park x — flag-only update must not unpark the window.
        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            Log.w(TAG, "syncTouchable failed", e)
        }
    }

    /**
     * Oppo OverlayWindow: `wmLp.x = if (p < 0.01) -width else 0`.
     * Only crosses the threshold — never per-frame updateViewLayout.
     */
    private fun syncWindowParked(parked: Boolean) {
        if (windowParked == parked) return
        windowParked = parked
        val view = hostView ?: return
        val params = windowParams ?: return
        val w = windowWidth.coerceAtLeast(
            appContext.resources.displayMetrics.widthPixels,
        )
        params.x = if (parked) -w else 0
        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            Log.w(TAG, "syncWindowParked failed", e)
        }
    }

    private fun detachWindow() {
        cancelSettle(keepProgress = false)
        scrolling = false
        closeDragging = false
        settleTarget = -1f
        progress = 0f
        contentEntered = false
        contentResumed = false
        pendingProgress = null
        progressApplyScheduled = false
        acceptingUserScroll = false
        sessionFromOpen = false
        windowInteractive = false
        overscrollLayersOn = false
        windowParked = true
        plateView = null
        contentSlide = null
        storeSlide = null
        mainHandler.removeCallbacks(applyPendingProgressRunnable)
        lastNotifiedProgress = -1f
        try {
            hostView?.let { windowManager.removeViewImmediate(it) }
        } catch (e: Exception) {
            Log.w(TAG, "removeView failed", e)
        }
        hostController?.destroy()
        hostController = null
        hostView = null
        windowParams = null
        attached = false
        notifyStatus(0)
        callback = null
        if (active === this) {
            active = null
        }
    }

    private fun ensureEntered() {
        if (contentEntered) return
        contentEntered = true
        hostController?.enter()
        if (resumed) {
            ensureContentResumed()
        }
    }

    /** Resume card engines once when fully open — never every overscroll frame. */
    private fun ensureContentResumed() {
        if (contentResumed || !resumed) return
        contentResumed = true
        hostController?.resume()
    }

    private fun ensureContentPaused() {
        if (!contentResumed) return
        contentResumed = false
        hostController?.pause()
    }

    private fun clearContentEntered() {
        if (!contentEntered) return
        contentEntered = false
        contentResumed = false
        hostController?.exit()
    }

    private fun beginCloseDrag() {
        // Invalidate in-flight launcher endScroll so rapid chatter cannot settle
        // the previous AIDL gesture after panel takeover.
        scrollGen.incrementAndGet()
        // Only true when interrupting a close settle — NOT an open settle.
        // (Any-settle was biasing short-quick left at ~half back to open.)
        interruptedCloseSettle = settleTarget == 0f
        cancelSettle(keepProgress = true)
        settleTarget = -1f
        scrolling = true
        closeDragging = true
        sessionFromOpen = true
        acceptingUserScroll = false
        markScrollSessionStart(force = true)
        ensureEntered()
        syncTouchable(true)
        syncCloseGestureEnabled(true)
    }

    private fun onCloseDragProgress(p: Float) {
        if (!closeDragging) beginCloseDrag()
        // Finger path already COUI-damped; allow soft past-open, never below 0.
        val clamped = p.coerceAtLeast(0f).coerceAtMost(1f + CouiOverscroll.MAX_FRACTION)
        sampleScrollVelocity(clamped)
        applyProgress(clamped, fromUser = true)
    }

    private fun endCloseDrag(velocityX: Float) {
        closeDragging = false
        scrolling = false
        val tip = if (abs(velocityX) >= 50f) velocityX else scrollVelocityPx
        finishScroll(tip)
    }

    private fun sampleScrollVelocity(p: Float) {
        val now = android.os.SystemClock.uptimeMillis()
        if (lastScrollSampleTime > 0L) {
            val dt = (now - lastScrollSampleTime).coerceAtLeast(1L)
            val dPx = (p - lastScrollSampleProgress) * windowWidth.toFloat().coerceAtLeast(1f)
            scrollVelocityPx = dPx / dt * 1000f
            if (scrollVelocityPx < peakCloseVelocityPx) {
                peakCloseVelocityPx = scrollVelocityPx
            }
            if (scrollVelocityPx > peakOpenVelocityPx) {
                peakOpenVelocityPx = scrollVelocityPx
            }
        }
        lastScrollSampleProgress = p
        lastScrollSampleTime = now
    }

    /**
     * Short-flick intent: tip + peaks + whole-gesture estimate.
     * VelocityTracker / AIDL tip is often ~0 on 1–2 frame flicks — [estimateSessionVelocityPx]
     * recovers that.
     *
     * Tip direction is honored for a real reverse flick, but a weak reverse tip
     * must not erase a dominant opposite peak (open past-overscroll often ends
     * tip≈-400 while peakOpen≫1000 — that used to flingClose and snap shut).
     */
    private fun effectiveFlingVelocity(tip: Float): Float {
        val estimated = estimateSessionVelocityPx()
        val open = maxOf(
            peakOpenVelocityPx,
            scrollVelocityPx.coerceAtLeast(0f),
            estimated.coerceAtLeast(0f),
        )
        val close = minOf(
            peakCloseVelocityPx,
            scrollVelocityPx.coerceAtMost(0f),
            estimated.coerceAtMost(0f),
        )
        return when {
            tip > 0f -> {
                val openIntent = maxOf(tip, open)
                // Weak right tip after a dominant left peak = release noise on close.
                if (abs(close) >= SHORT_FLING_VELOCITY && openIntent < abs(close)) {
                    close
                } else {
                    openIntent
                }
            }
            tip < 0f -> {
                val closeIntent = minOf(tip, close)
                if (open >= SHORT_FLING_VELOCITY && abs(closeIntent) < open) {
                    open
                } else {
                    closeIntent
                }
            }
            else -> if (abs(open) >= abs(close)) open else close
        }
    }

    /** Signed px/s from session start → now (works with a single onScroll/drag sample). */
    private fun estimateSessionVelocityPx(): Float {
        if (sessionStartTimeMs <= 0L) return 0f
        val dt = (android.os.SystemClock.uptimeMillis() - sessionStartTimeMs).coerceAtLeast(1L)
        val dP = progress - sessionStartProgress
        if (dP == 0f) return 0f
        return dP * windowWidth.toFloat().coerceAtLeast(1f) / dt * 1000f
    }

    private fun applyProgress(p: Float, fromUser: Boolean) {
        // Allow rubber-band past open; launcher freeze callbacks stay in [0, 1].
        val visual = p.coerceAtLeast(0f).coerceAtMost(1f + CouiOverscroll.MAX_FRACTION)
        progress = visual
        val view = hostView ?: return
        val w = windowWidth.toFloat().coerceAtLeast(1f)

        // Oppo AssistantScreen:
        // - Fixed full-bleed plate; alpha via View.setAlpha (BackgroundController).
        // - Content slides (setScrollX ≈ translationX on board/store).
        // - WM params.x parks off-screen only when closed.
        view.translationX = 0f

        if (visual <= 0.001f) {
            // Never toggle HW layers / flags on the way out of a drag frame.
            syncOverscrollLayers(false)
            setPlateAlpha(0f)
            contentSlide?.translationX = -w
            storeSlide?.translationX = -w
            view.visibility = View.INVISIBLE
            syncWindowParked(true)
        } else if (visual <= 1f) {
            // Finger path: do not flip LAYER_TYPE (crossing 1.0 blinked solid↔clear).
            if (!fromUser) syncOverscrollLayers(false)
            val contentX = w * (visual - 1f)
            contentSlide?.translationX = contentX
            storeSlide?.translationX = contentX
            setPlateAlpha(visual.coerceIn(0f, 1f))
            syncWindowParked(false)
            view.visibility = View.VISIBLE
        } else {
            if (!fromUser) syncOverscrollLayers(true)
            val slideX = w * (visual - 1f)
            contentSlide?.translationX = slideX
            storeSlide?.translationX = slideX
            setPlateAlpha(1f)
            syncWindowParked(false)
            view.visibility = View.VISIBLE
        }

        val panelInteractive = shouldPanelBeInteractive(visual)
        syncTouchable(panelInteractive)
        syncCloseGestureEnabled(panelInteractive)

        if (!fromUser && visual <= 0f) {
            clearContentEntered()
            syncOverscrollLayers(false)
        }
        if (visual >= 1f && resumed) {
            ensureContentResumed()
        }
        if (visual <= 1f) {
            notifyScroll(visual)
        } else if (lastNotifiedProgress < 0.999f) {
            notifyScroll(1f)
        }
    }

    /**
     * Oppo BackgroundController → DecorView.setAlpha(progress).
     * Keep an opaque color on [plateView] and fade with View alpha so the window
     * stays on the translucent composition path (ColorDrawable.setAlpha on the
     * root flipped opaque↔translucent and blinked while dragging).
     */
    private fun setPlateAlpha(alpha: Float) {
        val plate = plateView ?: return
        val a = alpha.coerceIn(0f, 1f)
        if (abs(plate.alpha - a) < 0.001f) return
        plate.alpha = a
    }

    private fun panelBgRgb(): Int {
        if (panelBgRgbCached != 0) return panelBgRgbCached
        panelBgRgbCached = try {
            val c = ContextCompat.getColor(appContext, R.color.hiboard_background)
            Color.rgb(Color.red(c), Color.green(c), Color.blue(c))
        } catch (_: Exception) {
            Color.rgb(0x83, 0x97, 0xCC)
        }
        return panelBgRgbCached
    }

    private fun syncOverscrollLayers(enabled: Boolean) {
        if (overscrollLayersOn == enabled) return
        overscrollLayersOn = enabled
        val type = if (enabled) View.LAYER_TYPE_HARDWARE else View.LAYER_TYPE_NONE
        contentSlide?.setLayerType(type, null)
        storeSlide?.setLayerType(type, null)
    }

    /**
     * Panel takes horizontal close/reopen once it covers enough of the screen.
     * Not while launcher finger is still driving [acceptingUserScroll].
     * Oppo: settle is interruptible — keep touch + close-gesture alive while the
     * spring runs so swipe-right mid-close can reopen.
     */
    private fun shouldPanelBeInteractive(p: Float): Boolean {
        if (closeDragging) return true
        if (acceptingUserScroll) return false
        if (p <= 0.001f) return false
        if (settleSpring?.isRunning == true || settleTarget >= 0f) return true
        return p >= PANEL_INTERACTIVE_THRESHOLD
    }

    private fun syncCloseGestureEnabled(enabled: Boolean) {
        val view = hostView as? HiboardView ?: return
        view.closeGestureEnabled = enabled
    }

    private fun finishScroll(velocity: Float?) {
        if (closeDragging) return
        scrolling = false
        // Launcher VelocityTracker is often 0 on quick overscroll (begin+end same
        // frame). Prefer tip; [effectiveFlingVelocity] fills gaps via peak + estimate.
        val tip = when {
            velocity != null && abs(velocity) > 1f -> velocity
            else -> scrollVelocityPx
        }
        val fromOpen = sessionFromOpen
        val v = effectiveFlingVelocity(tip)
        settleVelocityPx = v
        val flingOpen = v >= SHORT_FLING_VELOCITY
        val flingClose = v <= -SHORT_FLING_VELOCITY
        // Oppo:
        // - Past-open rubber-band springs back to 1 on slow release.
        // - Clear left fling closes even above keep-open / past-open.
        // - Clear right fling opens even under WIDTH_THRESHOLD 0.25.
        // - Slow drag: 0.25 open / 0.75 keep-open.
        val shouldOpen = when {
            progress > 1f && !flingClose -> true
            flingClose -> false
            flingOpen -> true
            abs(v) > VELOCITY_THRESHOLD -> v > 0f
            // Finger direction wins during rapid reverse (Oppo interruptible settle).
            fromOpen && v > 0f -> progress >= OPEN_THRESHOLD
            fromOpen && v < 0f -> progress > KEEP_OPEN_THRESHOLD
            // Interrupted close settle, no clear tip: reopen if still past open threshold.
            fromOpen && interruptedCloseSettle -> progress >= OPEN_THRESHOLD
            fromOpen -> progress > KEEP_OPEN_THRESHOLD
            else -> progress >= OPEN_THRESHOLD
        }
        Log.i(
            TAG,
            "finishScroll progress=$progress tip=$tip v=$v est=${estimateSessionVelocityPx()} " +
                "peakClose=$peakCloseVelocityPx peakOpen=$peakOpenVelocityPx " +
                "fromOpen=$fromOpen interrupted=$interruptedCloseSettle open=$shouldOpen",
        )
        sessionFromOpen = false
        interruptedCloseSettle = false
        scrollVelocityPx = 0f
        peakCloseVelocityPx = 0f
        peakOpenVelocityPx = 0f
        lastScrollSampleTime = 0L
        sessionStartTimeMs = 0L
        sessionStartProgress = 0f
        animateTo(if (shouldOpen) 1f else 0f, v)
    }

    private fun cancelSettle(keepProgress: Boolean) {
        val spring = settleSpring
        if (spring != null && spring.isRunning) {
            spring.cancel()
        }
        settleSpring = null
        settleTarget = -1f
        if (!keepProgress) {
            settleVelocityPx = 0f
        }
    }

    private fun animateTo(target: Float, velocityPx: Float) {
        cancelSettle(keepProgress = true)
        closeDragging = false
        settleTarget = target
        acceptingUserScroll = false
        val start = progress
        if (abs(start - target) < 0.001f) {
            applyProgress(target, fromUser = false)
            settleTarget = -1f
            syncOverscrollLayers(false)
            if (target <= 0f) {
                clearContentEntered()
            } else if (target >= 1f && resumed) {
                ensureContentResumed()
            }
            settleVelocityPx = 0f
            lastNotifiedProgress = -1f
            notifyScroll(target.coerceIn(0f, 1f))
            return
        }
        if (target > 0f) {
            ensureEntered()
        }

        val w = windowWidth.toFloat().coerceAtLeast(1f)
        // Convert px/s → progress/s for the spring.
        var startVelocity = (if (abs(velocityPx) > 1f) velocityPx else settleVelocityPx) / w
        settleVelocityPx = 0f
        val fling = abs(velocityPx) > VELOCITY_THRESHOLD
        if (fling) {
            // Floor so AIDL lag does not eat intent; cap so close/open never teleports.
            startVelocity = when {
                // Overscroll spring-back: keep natural direction, only cap magnitude.
                start > 1f && target >= 1f ->
                    startVelocity.coerceIn(-FLING_MAX_START_VELOCITY, FLING_MAX_START_VELOCITY)
                target >= 1f ->
                    startVelocity.coerceIn(FLING_MIN_START_VELOCITY, FLING_MAX_START_VELOCITY)
                else ->
                    // Close: softer ceiling so the opaque sheet slide is always readable.
                    startVelocity.coerceIn(-FLING_MAX_CLOSE_VELOCITY, -FLING_MIN_CLOSE_VELOCITY)
            }
        } else {
            startVelocity = startVelocity.coerceIn(-FLING_MAX_START_VELOCITY, FLING_MAX_START_VELOCITY)
        }

        val holder = FloatValueHolder(start)
        val response = when {
            start > 1f && target >= 1f -> SETTLE_RESPONSE_OVERSCROLL
            target <= 0f -> SETTLE_RESPONSE_CLOSE
            fling -> SETTLE_RESPONSE_FLING
            else -> SETTLE_RESPONSE
        }
        val spring = COUISpringAnimation(holder).setSpring(
            COUISpringForce(target)
                .setBounce(SETTLE_BOUNCE)
                .setResponse(response),
        )
        spring.setStartValue(start)
        spring.setStartVelocity(startVelocity)
        spring.setMinimumVisibleChange(0.001f)
        spring.addUpdateListener(
            COUIDynamicAnimation.OnAnimationUpdateListener { _, value, _ ->
                applyProgress(
                    value.coerceAtLeast(0f).coerceAtMost(1f + CouiOverscroll.MAX_FRACTION),
                    fromUser = false,
                )
            },
        )
        spring.addEndListener(
            COUIDynamicAnimation.OnAnimationEndListener { _, canceled, value, _ ->
                settleSpring = null
                if (canceled) {
                    // Finger takeover — keep current progress.
                    settleTarget = -1f
                    return@OnAnimationEndListener
                }
                val end = value.coerceIn(0f, 1f)
                settleTarget = -1f
                applyProgress(if (abs(end - target) < 0.02f) target else end, fromUser = false)
                syncOverscrollLayers(false)
                if (target <= 0f) {
                    clearContentEntered()
                } else if (resumed) {
                    ensureContentResumed()
                }
                lastNotifiedProgress = -1f
                notifyScroll(target.coerceIn(0f, 1f))
            },
        )
        settleSpring = spring
        Log.i(TAG, "spring settle start=$start target=$target vProg=$startVelocity")
        spring.start()
    }

    private fun notifyScroll(p: Float) {
        if (lastNotifiedProgress >= 0f && abs(p - lastNotifiedProgress) < 0.0005f) {
            return
        }
        lastNotifiedProgress = p
        try {
            callback?.overlayScrollChanged(p)
        } catch (e: RemoteException) {
            Log.w(TAG, "overlayScrollChanged failed", e)
        }
    }

    private fun notifyStatus(status: Int) {
        try {
            callback?.overlayStatusChanged(status)
        } catch (e: RemoteException) {
            Log.w(TAG, "overlayStatusChanged failed", e)
        }
    }

    fun asBinderPublic(): IBinder = this

    companion object {
        private const val TAG = "HiboardOverlayBinder"
        /** Oppo WIDTH_THRESHOLD = 0.25 — short pulls still commit with velocity. */
        private const val OPEN_THRESHOLD = 0.25f
        /**
         * Oppo scrollOut: when closing from open, slow release restores only if
         * progress stayed above ~0.75 (dragged less than 25% closed).
         */
        private const val KEEP_OPEN_THRESHOLD = 0.75f
        /** Treat session as close/reverse when already this far open at begin. */
        private const val SESSION_FROM_OPEN_PROGRESS = 0.5f
        /**
         * Once progress is at/above this and launcher finger is up, panel accepts
         * swipe-left/right (Oppo: no wait for spring idle at 1.0).
         */
        private const val PANEL_INTERACTIVE_THRESHOLD = 0.25f
        /** px/s — short quick flicks still open/close. */
        private const val VELOCITY_THRESHOLD = 250f
        /**
         * px/s — short-flick floor (open or close). Below system minFling often;
         * tip is unreliable on 1–2 frame AIDL/overscroll, so [effectiveFlingVelocity]
         * also uses whole-gesture estimate.
         */
        private const val SHORT_FLING_VELOCITY = 80f
        /**
         * From AssistantScreen string pool SETTLE_ANIM_SPRING_* — same convention as
         * [com.coui.appcompat.panel.COUIBottomSheetBehavior] (bounce=0, response=0.4).
         */
        private const val SETTLE_BOUNCE = 0.0f
        private const val SETTLE_RESPONSE = 0.4f
        /** Faster settle when finger left with a real fling — still capped for visibility. */
        private const val SETTLE_RESPONSE_FLING = 0.32f
        /** Close settle — readable opaque-sheet slide (Oppo scrollOut). */
        private const val SETTLE_RESPONSE_CLOSE = 0.42f
        /** Softer ease when releasing past-open rubber-band. */
        private const val SETTLE_RESPONSE_OVERSCROLL = 0.45f
        /** progress/s floor so a quick flick is not eaten by lag. */
        private const val FLING_MIN_START_VELOCITY = 1.8f
        /**
         * progress/s ceiling so close/open from ~1.0 always reads as a slide
         * (uncapped flings finished in a few frames ≈ "no animation").
         */
        private const val FLING_MAX_START_VELOCITY = 2.6f
        private const val FLING_MIN_CLOSE_VELOCITY = 1.0f
        private const val FLING_MAX_CLOSE_VELOCITY = 1.7f

        @Volatile
        var active: HiboardOverlayBinder? = null
            private set
    }
}
