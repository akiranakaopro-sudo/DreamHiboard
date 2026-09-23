package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.widget.OverScroller
import android.widget.ScrollView
import androidx.dynamicanimation.animation.FloatPropertyCompat
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce
import com.coui.appcompat.scrollview.COUIScrollView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * DreamRecorder overscroll.
 * The stretch and the return are the same soft spring, so a pull eases home
 * instead of snapping back.
 */
open class SpringScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : COUIScrollView(context, attrs) {

    private var lastY = 0f
    private var overscroll = 0f
    private var fingerDown = false
    private var correcting = false
    private var handoff = 0f
    private var tracker: VelocityTracker? = null
    private val edgeSpring: COUISpringAnimation

    init {
        overScrollMode = OVER_SCROLL_NEVER
        val force = COUISpringForce(0f)
            .setResponse(0.9f)
            .setDampingRatio(SPRING_DAMPING)
        edgeSpring = COUISpringAnimation(this, OVERSCROLL).setSpring(force)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            lastY = ev.y
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                cancelReturn()
                lastY = ev.y
                handoff = 0f
                fingerDown = true
                tracker?.recycle()
                tracker = VelocityTracker.obtain()
                tracker?.addMovement(ev)
            }
            MotionEvent.ACTION_MOVE -> {
                tracker?.addMovement(ev)
                val dy = ev.y - lastY
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
                tracker?.addMovement(ev)
                tracker?.computeCurrentVelocity(1000)
                val velocity = tracker?.yVelocity ?: 0f
                tracker?.recycle()
                tracker = null
                fingerDown = false
                val cancelled = ev.actionMasked == MotionEvent.ACTION_CANCEL
                val handled = super.onTouchEvent(ev)
                if (overscroll != 0f) springHome(if (cancelled) 0f else velocity)
                return handled
            }
        }
        return super.onTouchEvent(ev)
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
        if (fingerDown || correcting || overscroll != 0f) return
        val range = scrollRange()
        val hitTop = top <= 0 && oldTop > 0
        val hitBottom = range > 0 && top >= range && oldTop < range
        if (!hitTop && !hitBottom) return
        val speed = edgeSpeed().coerceAtMost(MAX_SPRING_SPEED)
        if (speed < 200f) return
        springHome(if (hitTop) speed else -speed)
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
        if (overscroll != 0f) cancelReturn()
        val atTop = scrollY <= 0
        val atBottom = scrollY >= scrollRange()
        val top = overscroll > 0f || (overscroll == 0f && atTop && dy > 0f && scrolled <= 0)
        val bottom = overscroll < 0f || (overscroll == 0f && atBottom && dy < 0f && scrolled >= 0)
        if (!top && !bottom) return
        correcting = true
        try {
            if (overscroll != 0f && scrolled != 0) scrollBy(0, -scrolled)
            if (top) shiftTop(dy) else shiftBottom(dy)
        } finally {
            correcting = false
        }
    }

    /** Same gentle resistance pulling out and coming back, so the stretch does not pop. */
    private fun shiftTop(dy: Float) {
        val maxDrag = height * MAX_DRAG_FRACTION
        val progress = (abs(overscroll) / maxDrag.coerceAtLeast(1f)).coerceAtMost(1f)
        val damp = TOP_DAMPING / max(MIN_DAMP_SCALE, 1f - min(DAMP_PROGRESS_CAP, progress))
        val delta = dy / damp
        var next = overscroll + delta
        if (next > maxDrag) next = maxDrag
        if (overscroll > 0f && next < 0f && delta != 0f) {
            scrollHandoff(-dy * (next / delta))
            next = 0f
        }
        setOverscroll(next)
    }

    /** Same resistance as COUIPhysicalAnimationUtil.calcRealOverScrollDist, kept in float. */
    private fun shiftBottom(dy: Float) {
        val max = height.coerceAtLeast(1).toFloat()
        val ratio = (abs(overscroll) / max).coerceAtMost(1f)
        val delta = dy * (1f - ratio) / 5f * 2f
        var next = overscroll + delta
        val limit = -max
        if (next < limit) next = limit
        if (overscroll < 0f && next > 0f && delta != 0f) {
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

    /** A fling that arrives at an end starts here too, from offset 0, carried by [velocity]. */
    private fun springHome(velocity: Float) {
        if (overscroll == 0f && velocity == 0f) return
        edgeSpring.cancelComplete()
        val speed = velocity.coerceIn(-MAX_SPRING_SPEED, MAX_SPRING_SPEED)
        edgeSpring.setStartValue(overscroll).setStartVelocity(speed).animateToFinalPosition(0f)
    }

    private fun cancelReturn() {
        edgeSpring.cancelComplete()
    }

    private fun setOverscroll(value: Float) {
        if (overscroll == value) return
        overscroll = value
        invalidate()
    }

    private fun scrollRange(): Int {
        if (childCount == 0) return 0
        val child = getChildAt(0)
        return (child.height - (height - paddingTop - paddingBottom)).coerceAtLeast(0)
    }

    private fun edgeSpeed(): Float {
        val field = scrollerField ?: return 0f
        val scroller = runCatching { field.get(this) as? OverScroller }.getOrNull() ?: return 0f
        return abs(scroller.currVelocity)
    }

    private companion object {
        const val TOP_DAMPING = 2.5f
        const val MIN_DAMP_SCALE = 0.15f
        const val DAMP_PROGRESS_CAP = 0.95f
        const val MAX_DRAG_FRACTION = 0.45f
        /** About a 0.9s glide, slightly overdamped so the return eases instead of kicking. */
        const val SPRING_DAMPING = 1.08f
        /** A fling's full speed would slam the edge. This caps the stretch. */
        const val MAX_SPRING_SPEED = 1600f

        val scrollerField = runCatching {
            ScrollView::class.java.getDeclaredField("mScroller").apply { isAccessible = true }
        }.getOrNull()

        val OVERSCROLL = object : FloatPropertyCompat<SpringScrollView>("overscroll") {
            override fun getValue(view: SpringScrollView): Float = view.overscroll

            override fun setValue(view: SpringScrollView, value: Float) {
                view.setOverscroll(value)
            }
        }
    }
}
