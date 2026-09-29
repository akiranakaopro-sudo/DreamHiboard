package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import com.coui.appcompat.scroll.SpringOverScroller
import com.coui.appcompat.scrollview.COUIScrollView
import com.coui.appcompat.uiutil.UIUtil
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * ColorOS assistant screen scrolling: flings, edge rebounds and the pull release all run on
 * COUI's SpringOverScroller, tuned the way the system COUIScrollView tunes it.
 * The stretch past an end is drawn as a translation, so scrollY never leaves the content.
 */
open class SpringScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : COUIScrollView(context, attrs) {

    private var lastY = 0f
    private var overscroll = 0f
    private var fingerDown = false
    private var handoff = 0f
    private val scroller = SpringOverScroller(context).apply {
        setSpringBackTensionMultiple(SPRING_BACK_TENSION)
        setIsScrollView(true)
        setEnableFlingSpeedIncrease(true)
    }
    private var mode = Mode.Idle
    private var edgeNotified = false

    /** Called for every scroll step and overscroll stretch frame. */
    var onScrollMoved: (() -> Unit)? = null

    private enum class Mode { Idle, Fling, Return }

    init {
        overScrollMode = OVER_SCROLL_NEVER
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            lastY = ev.y
            if (mode != Mode.Idle) {
                // A touch that catches a moving list only stops it; it must not click a card.
                stopAnimation()
                super.onInterceptTouchEvent(ev)
                return true
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                stopAnimation()
                lastY = ev.y
                handoff = 0f
                fingerDown = true
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = (ev.y.toInt() - lastY.toInt()).toFloat()
                lastY = ev.y
                fingerDown = true
                val before = scrollY
                val handled = super.onTouchEvent(ev)
                // The list already followed the finger. A stretch past the end is only visual,
                // and a reversal keeps this same drag so it does not have to start over.
                followEdge(dy, scrollY - before)
                return handled
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                fingerDown = false
                val handled = super.onTouchEvent(ev)
                if (overscroll != 0f) springBack()
                return handled
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun fling(velocityY: Int) {
        // A release while stretched always springs home first, as on ColorOS.
        if (overscroll != 0f || childCount == 0) return
        scroller.abortAnimation()
        scroller.fling(scrollX, scrollY, 0, velocityY)
        mode = Mode.Fling
        edgeNotified = false
        postInvalidateOnAnimation()
    }

    override fun computeScroll() {
        if (mode == Mode.Idle) {
            super.computeScroll()
            return
        }
        if (!scroller.computeScrollOffset()) {
            finishAnimation()
            return
        }
        applyVirtual(scroller.getCOUICurrY())
        postInvalidateOnAnimation()
    }

    /** [y] is scrollY as if the list could leave its range; the excess becomes the stretch. */
    private fun applyVirtual(y: Int) {
        val range = scrollRange()
        if (y in 0..range) {
            setOverscroll(0f)
            if (y != scrollY) scrollTo(scrollX, y)
            return
        }
        val edge = if (y < 0) 0 else range
        var excess = y - edge
        if (mode == Mode.Fling) {
            excess = flingResistance(excess)
            if (!edgeNotified) {
                edgeNotified = true
                scroller.notifyVerticalEdgeReached(edge + excess, edge, overflingDistance())
            }
        }
        if (scrollY != edge) scrollTo(scrollX, edge)
        setOverscroll(-excess.toFloat())
    }

    private fun springBack() {
        val range = scrollRange()
        val edge = if (overscroll > 0f) 0 else range
        val virtual = edge - overscroll.roundToInt()
        scroller.abortAnimation()
        if (scroller.springBack(scrollX, virtual, 0, 0, 0, range)) {
            mode = Mode.Return
            postInvalidateOnAnimation()
        } else {
            setOverscroll(0f)
        }
    }

    private fun stopAnimation() {
        if (mode == Mode.Idle) return
        scroller.abortAnimation()
        mode = Mode.Idle
    }

    private fun finishAnimation() {
        val returning = mode == Mode.Return || overscroll != 0f
        mode = Mode.Idle
        if (returning && overscroll != 0f && !fingerDown) {
            if (abs(overscroll) < 1f) setOverscroll(0f) else springBack()
        }
    }

    override fun overScrollBy(
        deltaX: Int,
        deltaY: Int,
        scrollX: Int,
        scrollY: Int,
        scrollRangeX: Int,
        scrollRangeY: Int,
        maxOverScrollX: Int,
        maxOverScrollY: Int,
        isTouchEvent: Boolean,
    ): Boolean {
        super.overScrollBy(
            deltaX,
            deltaY,
            scrollX,
            scrollY,
            scrollRangeX,
            scrollRangeY,
            0,
            0,
            isTouchEvent,
        )
        // Hitting the end must not wipe the drag speed, or a quick reversal cannot fling.
        return false
    }

    override fun onScrollChanged(left: Int, top: Int, oldLeft: Int, oldTop: Int) {
        super.onScrollChanged(left, top, oldLeft, oldTop)
        onScrollMoved?.invoke()
    }

    override fun onDetachedFromWindow() {
        stopAnimation()
        super.onDetachedFromWindow()
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (overscroll == 0f) {
            super.dispatchDraw(canvas)
            return
        }
        val save = canvas.save()
        canvas.translate(0f, overscroll)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(save)
    }

    private fun followEdge(dy: Float, scrolled: Int) {
        if (dy == 0f && overscroll == 0f) return
        val atTop = scrollY <= 0
        val atBottom = scrollY >= scrollRange()
        val top = overscroll > 0f || (overscroll == 0f && atTop && dy > 0f && scrolled <= 0)
        val bottom = overscroll < 0f || (overscroll == 0f && atBottom && dy < 0f && scrolled >= 0)
        if (!top && !bottom) return
        if (overscroll != 0f && scrolled != 0) scrollBy(0, -scrolled)
        stretch(dy)
    }

    /**
     * COUIPhysicalAnimationUtil.calcRealOverScrollDist. Each event drops its fraction of a pixel
     * like the system list does, which is what makes a slow pull feel heavier than a quick one.
     */
    private fun stretch(dy: Float) {
        val max = resources.displayMetrics.heightPixels.toFloat()
        val ratio = (abs(overscroll) / max).coerceAtMost(1f)
        val delta = (dy * (1f - ratio) / 5f * 2f).toInt().toFloat()
        if (delta == 0f) return
        var next = (overscroll + delta).coerceIn(-max, max)
        if (overscroll != 0f && next != 0f && (next > 0f) != (overscroll > 0f) && delta != 0f) {
            scrollHandoff(-dy * (next / delta))
            next = 0f
        }
        setOverscroll(next)
    }

    private fun scrollHandoff(pixels: Float) {
        handoff += pixels
        val whole = handoff.toInt()
        if (whole == 0) return
        val before = scrollY
        scrollBy(0, whole)
        handoff -= (scrollY - before).toFloat()
    }

    /** COUIPhysicalAnimationUtil's soft cap for a fling running past an end. */
    private fun flingResistance(excess: Int): Int {
        val cap = resources.displayMetrics.heightPixels * FLING_STRETCH_CAP
        return (excess * cap / sqrt(cap * cap + excess.toFloat() * excess)).toInt()
    }

    private fun overflingDistance(): Int = resources.displayMetrics.heightPixels

    private fun setOverscroll(value: Float) {
        if (overscroll == value) return
        overscroll = value
        invalidate()
        onScrollMoved?.invoke()
    }

    private fun scrollRange(): Int {
        if (childCount == 0) return 0
        val child = getChildAt(0)
        return (child.height - (height - paddingTop - paddingBottom)).coerceAtLeast(0)
    }

    private companion object {
        const val SPRING_BACK_TENSION = 2.15f
        const val FLING_STRETCH_CAP = 0.3731f

        init {
            useColorOsFlingFriction()
        }

        /**
         * COUI picks its fling friction from persist.sys.oplus.anim_level, which ColorOS phones
         * set to 4. Without it flings glide about half again as far, so use the level 4 values.
         * Must run before the first SpringOverScroller is built.
         */
        fun useColorOsFlingFriction() {
            if (UIUtil.getAnimLevel() >= 3) return
            runCatching {
                val rebound = Class.forName("com.coui.appcompat.scroll.SpringOverScroller\$ReboundOverScroller")
                fun set(name: String, value: Any) {
                    rebound.getDeclaredField(name).apply { isAccessible = true }.set(null, value)
                }
                set("sMidFlingBaseFriction", 4.5)
                set("sSlowFlingBaseFriction", 4.0)
                set("sCouiFlingFrictionNormal", 0.24f)
            }
        }
    }
}
