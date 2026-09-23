package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.animation.PathInterpolator
import android.widget.OverScroller
import android.widget.ScrollView
import android.widget.Scroller
import androidx.dynamicanimation.animation.FloatPropertyCompat
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce
import com.coui.appcompat.scrollview.COUIScrollView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * DreamRecorder overscroll.
 * A pull down at the top is the BounceLayout rubber band.
 * A pull past the bottom, and a fling into either end, uses the COUI list spring.
 */
open class SpringScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : COUIScrollView(context, attrs) {

    private var lastY = 0f
    private var overscroll = 0f
    private var pulling = false
    private var pullDown = false
    private var tracker: VelocityTracker? = null
    private val settle = Scroller(context, PathInterpolator(0.3f, 0f, 0.1f, 1f))
    private val edgeSpring: COUISpringAnimation

    init {
        overScrollMode = OVER_SCROLL_NEVER
        val force = COUISpringForce(0f)
            .setDampingRatio(EDGE_DAMPING)
            .setStiffness(EDGE_STIFFNESS)
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
                pulling = overscroll != 0f
                pullDown = overscroll > 0f
                tracker?.recycle()
                tracker = VelocityTracker.obtain()
                tracker?.addMovement(ev)
            }
            MotionEvent.ACTION_MOVE -> {
                tracker?.addMovement(ev)
                val dy = ev.y - lastY
                lastY = ev.y
                if (pulling || shouldPull(dy)) {
                    if (!pulling) {
                        val cancel = MotionEvent.obtain(ev)
                        cancel.action = MotionEvent.ACTION_CANCEL
                        super.onTouchEvent(cancel)
                        cancel.recycle()
                        pulling = true
                    }
                    pull(dy)
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                tracker?.addMovement(ev)
                tracker?.computeCurrentVelocity(1000)
                val velocity = tracker?.yVelocity ?: 0f
                tracker?.recycle()
                tracker = null
                if (pulling || overscroll != 0f) {
                    release(velocity)
                    pulling = false
                    return true
                }
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun onScrollChanged(left: Int, top: Int, oldLeft: Int, oldTop: Int) {
        super.onScrollChanged(left, top, oldLeft, oldTop)
        if (pulling || overscroll != 0f) return
        val range = scrollRange()
        val hitTop = top <= 0 && oldTop > 0
        val hitBottom = range > 0 && top >= range && oldTop < range
        if (!hitTop && !hitBottom) return
        val speed = edgeSpeed()
        if (speed < 200f) return
        pullDown = false
        settle.abortAnimation()
        edgeSpring.setStartVelocity(if (hitTop) speed else -speed).animateToFinalPosition(0f)
    }

    override fun computeScroll() {
        if (settle.computeScrollOffset()) {
            setOverscroll(settle.currY.toFloat())
            if (settle.currY == 0) pullDown = false
            postInvalidateOnAnimation()
        }
        super.computeScroll()
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

    private fun shouldPull(dy: Float): Boolean {
        if (dy == 0f || childCount == 0) return false
        if (dy > 0f && scrollY <= 0) return true
        return dy < 0f && scrollY >= scrollRange()
    }

    private fun pull(dy: Float) {
        if (pullDown || (overscroll >= 0f && scrollY <= 0 && dy > 0f)) {
            pullDown = true
            pullFromTop(dy)
        } else {
            pullDown = false
            pullFromBottom(dy)
        }
    }

    /** BounceLayout: dy is divided by a damping that rises toward 45% of the height. */
    private fun pullFromTop(dy: Float) {
        val maxDrag = height * MAX_DRAG_FRACTION
        val progress = (abs(overscroll) / maxDrag.coerceAtLeast(1f)).coerceAtMost(1f)
        val damp = TOP_DAMPING / max(MIN_DAMP_SCALE, 1f - min(DAMP_PROGRESS_CAP, progress))
        var next = overscroll + dy / damp
        if (next > maxDrag) next = maxDrag
        if (overscroll > 0f && next < 0f) {
            scrollBy(0, -next.toInt())
            next = 0f
            pullDown = false
        }
        setOverscroll(next)
    }

    /** Same resistance as COUIPhysicalAnimationUtil.calcRealOverScrollDist, kept in float. */
    private fun pullFromBottom(dy: Float) {
        val max = height.coerceAtLeast(1).toFloat()
        val ratio = (abs(overscroll) / max).coerceAtMost(1f)
        val delta = dy * (1f - ratio) / 5f * 2f
        var next = overscroll + delta
        if ((overscroll > 0f && next < 0f) || (overscroll < 0f && next > 0f)) {
            scrollBy(0, -next.toInt())
            next = 0f
        }
        setOverscroll(next)
    }

    private fun release(velocity: Float) {
        if (overscroll == 0f) return
        if (pullDown && overscroll > 0f) {
            edgeSpring.cancel()
            val start = overscroll.toInt()
            if (start == 0) {
                setOverscroll(0f)
                return
            }
            settle.startScroll(0, start, 0, -start, TOP_SETTLE_MS)
            invalidate()
            return
        }
        settle.abortAnimation()
        edgeSpring.setStartVelocity(velocity).animateToFinalPosition(0f)
    }

    private fun cancelReturn() {
        edgeSpring.cancel()
        settle.abortAnimation()
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
        const val TOP_SETTLE_MS = 417
        /** Origami friction 12.19, tension 16, as SpringOverScroller stores them. */
        const val EDGE_STIFFNESS = 143.32f
        const val EDGE_DAMPING = 1f

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
