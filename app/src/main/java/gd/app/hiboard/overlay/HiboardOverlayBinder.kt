package gd.app.hiboard.overlay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.PathInterpolator
import gd.app.hiboard.host.HiboardViewController
import gd.app.hiboard.ui.HiboardView
import kotlin.math.abs

/**
 * Server-side [ILauncherOverlay] implementation.
 * Hosts [HiboardViewController] in a remote window attached via launcher LayoutParams token.
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
    private var settleAnimator: ValueAnimator? = null
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
        mainHandler.post {
            // Hiboard owns the session while open/closing — ignore Workspace overscroll
            // (that fight caused exit shake). Re-open only when fully closed.
            if (closeDragging) return@post
            if (settleAnimator?.isRunning == true) return@post
            if (progress > 0.05f) return@post
            settleAnimator?.cancel()
            settleTarget = -1f
            scrolling = true
            acceptingUserScroll = true
            scrollVelocityPx = 0f
            lastScrollSampleTime = 0L
            lastScrollSampleProgress = progress
            ensureEntered()
        }
    }

    override fun onScroll(p: Float) {
        if (!acceptingUserScroll) return
        val clamped = p.coerceIn(0f, 1f)
        sampleScrollVelocity(clamped)
        pendingProgress = clamped
        if (progressApplyScheduled) return
        progressApplyScheduled = true
        mainHandler.post(applyPendingProgressRunnable)
    }

    override fun endScroll() {
        mainHandler.post {
            if (!acceptingUserScroll && !scrolling) return@post
            acceptingUserScroll = false
            pendingProgress?.let {
                pendingProgress = null
                progressApplyScheduled = false
                mainHandler.removeCallbacks(applyPendingProgressRunnable)
                applyProgress(it, fromUser = true)
            }
            // Prefer tracked fling velocity so short/quick swipes still commit.
            finishScroll(scrollVelocityPx)
        }
    }

    override fun endScrollWithVelocity(velocity: Float) {
        mainHandler.post {
            if (!acceptingUserScroll && !scrolling) return@post
            acceptingUserScroll = false
            pendingProgress?.let {
                pendingProgress = null
                progressApplyScheduled = false
                mainHandler.removeCallbacks(applyPendingProgressRunnable)
                applyProgress(it, fromUser = true)
            }
            finishScroll(velocity)
        }
    }

    override fun openOverlay(flags: Int) {
        mainHandler.post {
            ensureEntered()
            animateTo(1f)
        }
    }

    override fun closeOverlay(flags: Int) {
        mainHandler.post { animateTo(0f) }
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
        if (attached) {
            windowParams = buildParams(attrs)
            hostView?.let { view ->
                try {
                    windowManager.updateViewLayout(view, windowParams)
                } catch (e: Exception) {
                    Log.w(TAG, "updateViewLayout failed", e)
                }
            }
            notifyStatus(OverlayContract.STATUS_CONNECTED)
            return
        }

        val controller = HiboardViewController(appContext)
        val view = controller.create(onNavigateHome = {
            mainHandler.post { animateTo(0f) }
        })
        if (view is HiboardView) {
            view.onCloseScrollBegin = { beginCloseDrag() }
            view.onCloseScroll = { onCloseDragProgress(it) }
            view.onCloseScrollEnd = { endCloseDrag(it) }
        }
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
            applyProgress(0f, fromUser = false)
            notifyStatus(OverlayContract.STATUS_CONNECTED)
            Log.i(TAG, "overlay window attached width=$windowWidth")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay window", e)
            controller.destroy()
            hostController = null
            hostView = null
            attached = false
            notifyStatus(0)
        }
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
        settleAnimator?.cancel()
        settleAnimator = null
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
        settleAnimator?.cancel()
        settleAnimator = null
        settleTarget = -1f
        scrolling = true
        closeDragging = true
        acceptingUserScroll = false
        scrollVelocityPx = 0f
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
        // HiboardView velocityX: swipe-left (close) is negative; positive = reopen.
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
        view.translationX = w * (p - 1f)
        view.visibility = View.VISIBLE

        // Only flip window flags at open/close boundaries — never every settle frame
        // (updateViewLayout/focus thrash was a major exit-shake source).
        when {
            p >= 0.999f -> {
                syncTouchable(true)
                syncCloseGestureEnabled(true)
            }
            p <= 0.001f && !closeDragging -> {
                syncTouchable(false)
                syncCloseGestureEnabled(false)
            }
            closeDragging -> {
                syncTouchable(true)
                syncCloseGestureEnabled(true)
            }
        }

        if (!fromUser && p <= 0f) {
            if (contentEntered) {
                contentEntered = false
                hostController?.exit()
            }
        }
        if (p >= 1f && resumed) {
            hostController?.resume()
        }
        notifyScrollThrottled(p)
    }

    private fun syncCloseGestureEnabled(enabled: Boolean) {
        val view = hostView as? HiboardView ?: return
        view.closeGestureEnabled = enabled
    }

    private fun finishScroll(velocity: Float?) {
        scrolling = false
        val v = velocity ?: scrollVelocityPx
        settleVelocityPx = v
        // Quick short fling commits by velocity; slow drag uses distance threshold.
        val shouldOpen = when {
            abs(v) > VELOCITY_THRESHOLD -> v > 0f
            else -> progress >= OPEN_THRESHOLD
        }
        Log.i(TAG, "finishScroll progress=$progress velocity=$v open=$shouldOpen")
        scrollVelocityPx = 0f
        lastScrollSampleTime = 0L
        animateTo(if (shouldOpen) 1f else 0f)
    }

    private fun animateTo(target: Float) {
        settleAnimator?.cancel()
        closeDragging = false
        settleTarget = target
        acceptingUserScroll = false
        val start = progress
        if (abs(start - target) < 0.001f) {
            applyProgress(target, fromUser = false)
            settleTarget = -1f
            syncTouchable(target >= 0.999f)
            syncCloseGestureEnabled(target >= 0.999f)
            if (target <= 0f && contentEntered) {
                contentEntered = false
                hostController?.exit()
            } else if (target >= 1f && resumed) {
                hostController?.resume()
            }
            settleVelocityPx = 0f
            // Always publish endpoints so launcher home FX / gates reset.
            lastNotifiedProgress = -1f
            notifyScroll(target)
            return
        }
        if (target > 0f) {
            ensureEntered()
        }
        val distance = abs(target - start)
        val velocityBased = if (abs(settleVelocityPx) > 1f) {
            ((distance * windowWidth) / abs(settleVelocityPx) * 1000f)
                .toLong()
                .coerceIn(120L, SETTLE_DURATION_MS)
        } else {
            (distance * SETTLE_DURATION_MS).toLong().coerceIn(120L, SETTLE_DURATION_MS)
        }
        settleVelocityPx = 0f
        val anim = ValueAnimator.ofFloat(start, target)
        anim.duration = velocityBased
        anim.interpolator = PathInterpolator(0.22f, 0.05f, 0.1f, 1f)
        anim.addUpdateListener { applyProgress(it.animatedValue as Float, fromUser = false) }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                settleTarget = -1f
                applyProgress(target, fromUser = false)
                syncTouchable(target >= 0.999f)
                syncCloseGestureEnabled(target >= 0.999f)
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
            }

            override fun onAnimationCancel(animation: Animator) {
                settleTarget = -1f
            }
        })
        settleAnimator = anim
        anim.start()
    }

    private fun notifyScrollThrottled(p: Float) {
        if (lastNotifiedProgress >= 0f &&
            abs(p - lastNotifiedProgress) < 0.008f &&
            p > 0.01f &&
            p < 0.99f
        ) {
            return
        }
        lastNotifiedProgress = p
        notifyScroll(p)
    }

    private fun notifyScroll(p: Float) {
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
        /** Distance fallback when fling is weak (ColorOS-ish: easy to complete). */
        private const val OPEN_THRESHOLD = 0.22f
        /** px/s — short quick flicks should still open/close. */
        private const val VELOCITY_THRESHOLD = 350f
        private const val SETTLE_DURATION_MS = 320L

        @Volatile
        var active: HiboardOverlayBinder? = null
            private set
    }
}
