package gd.app.hiboard.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
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
import kotlin.math.pow

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
    private var pendingProgress: Float? = null
    private var progressApplyScheduled = false
    private var lastNotifiedProgress = -1f
    private var settleVelocityPx = 0f
    private var closeDragging = false
    private var settleTarget = -1f
    private var windowInteractive = false
    /** Estimated horizontal velocity (px/s) from recent onScroll samples. */
    private var scrollVelocityPx = 0f
    private var lastScrollSampleProgress = 0f
    private var lastScrollSampleTime = 0L
    /**
     * True when this finger session started from a mostly-open panel (close /
     * reverse). Uses Oppo scrollOut keep-open threshold instead of open 0.25.
     */
    private var sessionFromOpen = false
    private var panelBg: ColorDrawable? = null
    /** Widget/chrome layer only — slides; full-screen scrim stays fixed (Oppo Assist). */
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
        // If onScroll already auto-started this gesture, keep its sessionFromOpen.
        // Late begin+end on finger-up used to re-read progress (e.g. 0.74) and
        // wrongly mark the session as close-from-open → settle closed.
        if (!acceptingUserScroll) {
            sessionFromOpen = progress >= SESSION_FROM_OPEN_PROGRESS
        }
        acceptingUserScroll = true
        scrolling = true
        mainHandler.post {
            if (gen != scrollGen.get()) return@post
            if (closeDragging) return@post
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
        val clamped = p.coerceIn(0f, 1f)
        // Oppo streams onScrollChange even before begin succeeds. Accept progress
        // and implicitly open the session so early finger travel is not dropped.
        if (!acceptingUserScroll) {
            if (clamped <= 0f && progress <= 0f) return
            acceptingUserScroll = true
            scrolling = true
            sessionFromOpen = progress >= SESSION_FROM_OPEN_PROGRESS || clamped >= SESSION_FROM_OPEN_PROGRESS
            mainHandler.post {
                if (closeDragging) return@post
                cancelSettle(/* keepProgress = */ true)
                settleTarget = -1f
                scrolling = true
                acceptingUserScroll = true
                sessionFromOpen =
                    progress >= SESSION_FROM_OPEN_PROGRESS || sessionFromOpen
                ensureEntered()
                if (progress < PANEL_INTERACTIVE_THRESHOLD) {
                    syncTouchable(false)
                    syncCloseGestureEnabled(false)
                }
            }
        }
        sampleScrollVelocity(clamped)
        pendingProgress = clamped
        if (Looper.myLooper() == mainHandler.looper) {
            if (progressApplyScheduled) {
                mainHandler.removeCallbacks(applyPendingProgressRunnable)
                progressApplyScheduled = false
            }
            pendingProgress = null
            applyProgress(clamped, fromUser = true)
            return
        }
        if (progressApplyScheduled) return
        progressApplyScheduled = true
        mainHandler.post(applyPendingProgressRunnable)
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
                hostController?.resume()
            }
        }
    }

    override fun onPause() {
        mainHandler.post {
            resumed = false
            hostController?.pause()
        }
    }

    override fun onStop() {
        mainHandler.post {
            if (progress > 0f) {
                cancelSettle(keepProgress = false)
                applyProgress(0f, fromUser = false)
                hostController?.exit()
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

        // Launcher may call windowAttached2 several times while the token settles.
        // Do NOT treat "addView posted but isAttachedToWindow still false" as stale —
        // that tore the window down in a loop and made glance look dead.
        if (attached && hostView != null) {
            windowParams = buildParams(attrs)
            try {
                windowManager.updateViewLayout(hostView, windowParams)
                notifyStatus(OverlayContract.STATUS_CONNECTED)
                return
            } catch (e: Exception) {
                Log.w(TAG, "updateViewLayout failed — recreating", e)
                detachWindowPreservingCallback(cb)
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
        // Oppo: full-screen translucent sheet stays put; only chrome/widgets slide.
        contentSlide = view.findViewById(R.id.boardRoot)
        storeSlide = view.findViewById(R.id.storeRoot)
        // Ensure no opaque layout bg fights the progress-driven ColorDrawable scrim.
        (view as? android.view.ViewGroup)?.let { root ->
            for (i in 0 until root.childCount) {
                root.getChildAt(i)?.setBackgroundColor(Color.TRANSPARENT)
            }
        }
        view.translationX = 0f
        view.visibility = View.VISIBLE
        view.alpha = 1f
        view.isFocusable = true
        view.isFocusableInTouchMode = true
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
            contentSlide = null
            storeSlide = null
            attached = false
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
        pendingProgress = null
        progressApplyScheduled = false
        acceptingUserScroll = false
        windowInteractive = false
        mainHandler.removeCallbacks(applyPendingProgressRunnable)
        lastNotifiedProgress = -1f
        try {
            hostView?.let { windowManager.removeViewImmediate(it) }
        } catch (_: Exception) {
        }
        hostController?.destroy()
        hostController = null
        hostView = null
        contentSlide = null
        storeSlide = null
        windowParams = null
        attached = false
        callback = cb
    }

    private fun buildParams(source: WindowManager.LayoutParams): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams()
        params.copyFrom(source)
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
        return (WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
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
        try {
            windowManager.updateViewLayout(view, params)
            if (interactive) {
                view.isFocusable = true
                view.isFocusableInTouchMode = true
                view.requestFocus()
            }
        } catch (e: Exception) {
            Log.w(TAG, "syncTouchable failed", e)
        }
    }

    private fun detachWindow() {
        cancelSettle(keepProgress = false)
        scrolling = false
        closeDragging = false
        settleTarget = -1f
        progress = 0f
        contentEntered = false
        pendingProgress = null
        progressApplyScheduled = false
        acceptingUserScroll = false
        sessionFromOpen = false
        windowInteractive = false
        panelBg = null
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
        val view = hostView ?: return
        view.visibility = View.VISIBLE
        if (contentEntered) return
        contentEntered = true
        hostController?.enter()
        if (resumed) {
            hostController?.resume()
        }
    }

    private fun beginCloseDrag() {
        cancelSettle(keepProgress = true)
        settleTarget = -1f
        scrolling = true
        closeDragging = true
        sessionFromOpen = true
        acceptingUserScroll = false
        lastScrollSampleProgress = progress
        lastScrollSampleTime = 0L
        ensureEntered()
        syncTouchable(true)
        syncCloseGestureEnabled(true)
    }

    private fun onCloseDragProgress(p: Float) {
        if (!closeDragging) beginCloseDrag()
        val clamped = p.coerceIn(0f, 1f)
        sampleScrollVelocity(clamped)
        applyProgress(clamped, fromUser = true)
    }

    private fun endCloseDrag(velocityX: Float) {
        closeDragging = false
        scrolling = false
        val v = if (abs(velocityX) >= 50f) velocityX else scrollVelocityPx
        finishScroll(v)
    }

    private fun sampleScrollVelocity(p: Float) {
        val now = android.os.SystemClock.uptimeMillis()
        if (lastScrollSampleTime > 0L) {
            val dt = (now - lastScrollSampleTime).coerceAtLeast(1L)
            val dPx = (p - lastScrollSampleProgress) * windowWidth.toFloat().coerceAtLeast(1f)
            scrollVelocityPx = dPx / dt * 1000f
        }
        lastScrollSampleProgress = p
        lastScrollSampleTime = now
    }

    private fun applyProgress(p: Float, fromUser: Boolean) {
        progress = p
        val view = hostView ?: return
        val w = windowWidth.toFloat().coerceAtLeast(1f)
        // Keep host fixed so the translucent sheet covers home on the right.
        // Only board/store chrome slides in from the left (Oppo Assist).
        view.translationX = 0f
        val slideX = w * (p - 1f)
        contentSlide?.translationX = slideX
        storeSlide?.translationX = slideX
        view.visibility = View.VISIBLE
        updatePanelBackground(view, p)

        // Oppo: launcher owns the open finger; once glance is far enough on-screen,
        // the panel must accept swipe-left immediately (including mid open-settle).
        val panelInteractive = shouldPanelBeInteractive(p)
        syncTouchable(panelInteractive)
        syncCloseGestureEnabled(panelInteractive)

        if (!fromUser && p <= 0f) {
            if (contentEntered) {
                contentEntered = false
                hostController?.exit()
            }
        }
        if (p >= 1f && resumed) {
            hostController?.resume()
        }
        notifyScroll(p)
    }

    /**
     * Oppo Assist: full-screen translucent tint while dragging (workspace shows
     * through on the right); opaque at progress=1 so home icons are hidden.
     * Alpha must reach 0 at p=0 now that the scrim no longer translates off-screen.
     */
    private fun updatePanelBackground(view: View, p: Float) {
        val rgb = panelBgRgb()
        val bg = panelBg ?: ColorDrawable(rgb).also {
            panelBg = it
            view.background = it
        }
        if (bg.color != rgb) bg.color = rgb
        val alpha = when {
            p <= 0.001f -> 0
            p >= 0.995f -> 255
            else -> {
                val mid = MID_SWIPE_BG_ALPHA
                val a = if (p < 0.35f) {
                    mid * (p / 0.35f)
                } else {
                    mid + (1f - mid) *
                        ((p - 0.35f) / 0.65f).toDouble().pow(1.4).toFloat()
                }
                (a * 255f).toInt().coerceIn(0, 255)
            }
        }
        bg.alpha = alpha
    }

    private fun panelBgRgb(): Int {
        return try {
            val c = ContextCompat.getColor(appContext, R.color.hiboard_background)
            Color.rgb(Color.red(c), Color.green(c), Color.blue(c))
        } catch (_: Exception) {
            Color.rgb(0x83, 0x97, 0xCC)
        }
    }

    /**
     * Panel takes horizontal close/reopen once it covers enough of the screen.
     * Not while launcher finger is still driving [acceptingUserScroll].
     */
    private fun shouldPanelBeInteractive(p: Float): Boolean {
        if (closeDragging) return true
        if (acceptingUserScroll) return false
        if (p <= 0.001f) return false
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
        // frame). Prefer a real fling; else fall back to onScroll sample.
        val v = when {
            velocity != null && abs(velocity) > 1f -> velocity
            else -> scrollVelocityPx
        }
        settleVelocityPx = v
        // Oppo:
        // - Open (WIDTH_THRESHOLD): progress >= 0.25 commits open when slow.
        // - Close from open (scrollOut): must keep progress > ~0.75 to restore;
        //   a slow left drag past that exits instead of springing back open.
        val fromOpen = sessionFromOpen
        val shouldOpen = when {
            abs(v) > VELOCITY_THRESHOLD -> v > 0f
            fromOpen -> progress > KEEP_OPEN_THRESHOLD
            else -> progress >= OPEN_THRESHOLD
        }
        Log.i(
            TAG,
            "finishScroll progress=$progress velocity=$v fromOpen=$fromOpen open=$shouldOpen",
        )
        sessionFromOpen = false
        scrollVelocityPx = 0f
        lastScrollSampleTime = 0L
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
            if (target <= 0f && contentEntered) {
                contentEntered = false
                hostController?.exit()
            } else if (target >= 1f && resumed) {
                hostController?.resume()
            }
            settleVelocityPx = 0f
            lastNotifiedProgress = -1f
            notifyScroll(target)
            return
        }
        if (target > 0f) {
            ensureEntered()
        }

        val w = windowWidth.toFloat().coerceAtLeast(1f)
        // Convert px/s → progress/s for the spring.
        var startVelocity = (if (abs(velocityPx) > 1f) velocityPx else settleVelocityPx) / w
        settleVelocityPx = 0f
        // Quick flick: keep directional velocity strong enough that soft spring
        // + AIDL lag does not feel like a late open/exit.
        val fling = abs(velocityPx) > VELOCITY_THRESHOLD
        if (fling) {
            val minFlingProg = FLING_MIN_START_VELOCITY
            if (target >= 1f && startVelocity < minFlingProg) {
                startVelocity = minFlingProg
            } else if (target <= 0f && startVelocity > -minFlingProg) {
                startVelocity = -minFlingProg
            }
        }

        val holder = FloatValueHolder(start)
        // AssistantScreen SETTLE_ANIM_SPRING_*: response=0.4 idle; snappier on fling.
        val response = if (fling) SETTLE_RESPONSE_FLING else SETTLE_RESPONSE
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
                applyProgress(value.coerceIn(0f, 1f), fromUser = false)
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
                if (target <= 0f) {
                    if (contentEntered) {
                        contentEntered = false
                        hostController?.exit()
                    }
                } else if (resumed) {
                    hostController?.resume()
                }
                lastNotifiedProgress = -1f
                notifyScroll(target)
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
        /** Mid-swipe full-screen cover before ramping to opaque at p=1. */
        private const val MID_SWIPE_BG_ALPHA = 0.72f
        /**
         * From AssistantScreen string pool SETTLE_ANIM_SPRING_* — same convention as
         * [com.coui.appcompat.panel.COUIBottomSheetBehavior] (bounce=0, response=0.4).
         */
        private const val SETTLE_BOUNCE = 0.0f
        private const val SETTLE_RESPONSE = 0.4f
        /** Faster settle when finger left with a real fling. */
        private const val SETTLE_RESPONSE_FLING = 0.28f
        /** progress/s floor so a quick flick is not eaten by lag. */
        private const val FLING_MIN_START_VELOCITY = 3.2f

        @Volatile
        var active: HiboardOverlayBinder? = null
            private set
    }
}
