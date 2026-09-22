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

/**
 * Scroll view that rubber-bands past the top and bottom, then springs back.
 */
open class SpringScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : COUIScrollView(context, attrs) {

    private var lastY = 0f
    private var overscroll = 0f
    private var pulling = false
    private var tracker: VelocityTracker? = null
    private val spring: COUISpringAnimation

    init {
        overScrollMode = OVER_SCROLL_NEVER
        val force = COUISpringForce(0f)
            .setDampingRatio(COUISpringForce.DAMPING_RATIO_LOW_BOUNCY)
            .setStiffness(COUISpringForce.STIFFNESS_MEDIUM)
        spring = COUISpringAnimation(this, OVERSCROLL).setSpring(force)
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
                spring.cancel()
                lastY = ev.y
                pulling = overscroll != 0f
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
                    if (overscroll != 0f) {
                        spring.setStartVelocity(velocity).animateToFinalPosition(0f)
                    }
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
        spring.setStartVelocity(if (hitTop) speed else -speed).animateToFinalPosition(0f)
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

    /** Same resistance as [com.coui.appcompat.animation.COUIPhysicalAnimationUtil.calcRealOverScrollDist], kept in float so short moves still travel. */
    private fun pull(dy: Float) {
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
