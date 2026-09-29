package gd.app.hiboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.LinearLayout
import kotlin.math.abs

/**
 * Vertical sheet that takes over a vertical drag from any child once it passes touch slop and
 * [Gesture.claim] accepts it; taps and horizontal moves still reach the children.
 */
class DragSheetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    interface Gesture {
        /** Whether to take a drag of [dy] (positive = down) that started at screen point ([x], [y]). */
        fun claim(dy: Float, x: Float, y: Float): Boolean
        fun move(dy: Float)
        fun release(velocityY: Float, canceled: Boolean)
    }

    var gesture: Gesture? = null

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var tracker: VelocityTracker? = null
    private var downX = 0f
    private var downY = 0f
    private var startY = 0f
    private var dragging = false

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // The sheet itself moves while dragging, so velocity has to come from screen coordinates.
        val screen = MotionEvent.obtain(event).apply { setLocation(event.rawX, event.rawY) }
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            tracker?.recycle()
            tracker = VelocityTracker.obtain()
            downX = event.rawX
            downY = event.rawY
            dragging = false
        }
        tracker?.addMovement(screen)
        screen.recycle()
        return super.dispatchTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_MOVE) maybeStart(event)
        return dragging
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_MOVE -> {
                if (!dragging) maybeStart(event)
                if (dragging) gesture?.move(event.rawY - startY)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (dragging) {
                    val velocity = tracker?.let { it.computeCurrentVelocity(1000); it.yVelocity } ?: 0f
                    gesture?.release(velocity, event.actionMasked == MotionEvent.ACTION_CANCEL)
                }
                dragging = false
                tracker?.recycle()
                tracker = null
            }
        }
        return true
    }

    private fun maybeStart(event: MotionEvent) {
        if (dragging) return
        val dy = event.rawY - downY
        val dx = event.rawX - downX
        if (abs(dy) <= touchSlop || abs(dy) <= abs(dx)) return
        if (gesture?.claim(dy, downX, downY) != true) return
        dragging = true
        startY = downY + if (dy > 0) touchSlop else -touchSlop
        parent?.requestDisallowInterceptTouchEvent(true)
    }
}
