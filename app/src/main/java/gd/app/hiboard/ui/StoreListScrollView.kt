package gd.app.hiboard.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.abs

/**
 * Vertical list inside the store pager. An up or down drag stays here.
 * Only a clearly sideways drag is left for the tab pager.
 */
class StoreListScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : SpringScrollView(context, attrs) {

    private var downX = 0f
    private var downY = 0f
    private val slop = ViewConfiguration.get(context).scaledTouchSlop

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = abs(ev.x - downX)
                val dy = abs(ev.y - downY)
                if (dy >= dx) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                } else if (dx > slop * 2 && dx > dy * 2f) {
                    parent?.requestDisallowInterceptTouchEvent(false)
                    return false
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }
}
