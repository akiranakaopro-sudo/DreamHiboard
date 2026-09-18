package gd.app.hiboard.ui.grid

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.HapticFeedbackConstants
import android.widget.ScrollView
import gd.app.hiboard.model.CardInstance
import kotlin.math.hypot
import kotlin.math.roundToInt

class PackedCardLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    var columns: Int = 4
    var onDragStarted: (() -> Unit)? = null
    var onDragEnded: (() -> Unit)? = null
    var onReorder: ((List<String>) -> Unit)? = null

    private val gutterPx = (10 * resources.displayMetrics.density).roundToInt()
    private val rowHeightPx = (52 * resources.displayMetrics.density).roundToInt()
    private val elevationPx = 12 * resources.displayMetrics.density
    private val scrollEdgePx = (64 * resources.displayMetrics.density).roundToInt()
    private val slop = ViewConfiguration.get(context).scaledTouchSlop
    private var cards: List<CardInstance> = emptyList()
    private val cardViews = mutableListOf<View>()

    var isDragging: Boolean = false
        private set
    private var originCards: List<CardInstance> = emptyList()
    private var dragIndex: Int = -1
    private var pendingIndex: Int = -1
    private var grabOffsetX = 0f
    private var grabOffsetY = 0f
    private var dragX = 0f
    private var dragY = 0f
    private var downX = 0f
    private var downY = 0f
    private var activePointerId = MotionEvent.INVALID_POINTER_ID

    private val longPressRunnable = Runnable { beginDrag() }

    init {
        clipChildren = false
        clipToPadding = false
    }

    fun setCards(cards: List<CardInstance>, factory: (CardInstance) -> View) {
        if (isDragging) return
        this.cards = cards
        removeAllViews()
        cardViews.clear()
        cards.forEach { card ->
            val child = factory(card)
            child.tag = card.instanceId
            cardViews.add(child)
            addView(child)
        }
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val placements = packCards(cards, columns)
        val cellWidth = cellWidth(width)
        cards.forEachIndexed { index, _ ->
            val place = placements.getOrNull(index) ?: return@forEachIndexed
            val child = cardViews.getOrNull(index) ?: return@forEachIndexed
            val childWidth = cellWidth * place.columns + gutterPx * (place.columns - 1)
            val childHeight = rowHeightPx * place.rows + gutterPx * (place.rows - 1)
            child.measure(
                MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY),
            )
        }
        val maxRow = placements.maxOfOrNull { it.row + it.rows } ?: 0
        val height = if (maxRow == 0) 0 else rowHeightPx * maxRow + gutterPx * (maxRow - 1)
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l
        val placements = packCards(cards, columns)
        val cellWidth = cellWidth(width)
        cards.forEachIndexed { index, _ ->
            val place = placements.getOrNull(index) ?: return@forEachIndexed
            val child = cardViews.getOrNull(index) ?: return@forEachIndexed
            if (isDragging && index == dragIndex) {
                layoutDragged(child)
            } else {
                val x = place.column * (cellWidth + gutterPx)
                val y = place.row * (rowHeightPx + gutterPx)
                child.layout(x, y, x + child.measuredWidth, y + child.measuredHeight)
            }
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = event.getPointerId(0)
                val (x, y) = localPoint(event)
                downX = x
                downY = y
                dragX = x
                dragY = y
                pendingIndex = hitIndex(x, y)
                removeCallbacks(longPressRunnable)
                if (pendingIndex >= 0 && cards.getOrNull(pendingIndex)?.canDrag == true) {
                    postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout().toLong())
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val (x, y) = localPoint(event)
                if (!isDragging && hypot(x - downX, y - downY) > slop) {
                    removeCallbacks(longPressRunnable)
                }
                if (isDragging) return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                removeCallbacks(longPressRunnable)
                activePointerId = MotionEvent.INVALID_POINTER_ID
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (event.getPointerId(event.actionIndex) == activePointerId) {
                    removeCallbacks(longPressRunnable)
                }
            }
        }
        return isDragging
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (pendingIndex < 0 && !isDragging) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val (x, y) = localPoint(event)
                    onDrag(x, y, event.rawY)
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                removeCallbacks(longPressRunnable)
                if (isDragging) {
                    endDrag(commit = true)
                    return true
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                removeCallbacks(longPressRunnable)
                if (isDragging) {
                    endDrag(commit = false)
                    return true
                }
            }
        }
        return isDragging || pendingIndex >= 0
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(longPressRunnable)
        if (isDragging) endDrag(commit = false)
        super.onDetachedFromWindow()
    }

    private fun beginDrag() {
        val index = pendingIndex
        val child = cardViews.getOrNull(index) ?: return
        if (cards.getOrNull(index)?.canDrag != true) return
        isDragging = true
        dragIndex = index
        originCards = cards
        grabOffsetX = downX - child.left
        grabOffsetY = downY - child.top
        dragX = downX
        dragY = downY
        child.animate().scaleX(1.04f).scaleY(1.04f).setDuration(120).start()
        child.translationZ = elevationPx
        child.alpha = 0.94f
        child.isPressed = false
        child.cancelPendingInputEvents()
        child.bringToFront()
        parent.requestDisallowInterceptTouchEvent(true)
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        onDragStarted?.invoke()
        layoutDragged(child)
        invalidate()
    }

    private fun onDrag(x: Float, y: Float, rawY: Float) {
        dragX = x
        dragY = y
        autoScroll(rawY)
        val dragged = cardViews.getOrNull(dragIndex)
        if (dragged != null) layoutDragged(dragged)
        val target = targetIndexForPointer(x, y)
        if (target != dragIndex && target in cards.indices) {
            cards = moveItem(cards, dragIndex, target)
            val movedViews = moveItem(cardViews, dragIndex, target)
            cardViews.clear()
            cardViews.addAll(movedViews)
            dragIndex = target
            requestLayout()
        }
        invalidate()
    }

    private fun endDrag(commit: Boolean) {
        val dragged = cardViews.getOrNull(dragIndex)
        dragged?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(120)?.start()
        dragged?.translationZ = 0f
        dragged?.alpha = 1f
        parent.requestDisallowInterceptTouchEvent(false)
        val next = if (commit) cards else originCards
        val changed = commit && next.map { it.catalogId } != originCards.map { it.catalogId }
        isDragging = false
        dragIndex = -1
        pendingIndex = -1
        activePointerId = MotionEvent.INVALID_POINTER_ID
        if (!commit) {
            val byId = cardViews.associateBy { it.tag as? String }
            cards = originCards
            cardViews.clear()
            originCards.forEach { card ->
                byId[card.instanceId]?.let(cardViews::add)
            }
        }
        originCards = emptyList()
        requestLayout()
        if (changed) {
            onReorder?.invoke(next.map { it.catalogId })
        }
        onDragEnded?.invoke()
    }

    private fun layoutDragged(child: View) {
        val left = (dragX - grabOffsetX).roundToInt()
        val top = (dragY - grabOffsetY).roundToInt()
        child.layout(left, top, left + child.measuredWidth, top + child.measuredHeight)
    }

    private fun targetIndexForPointer(x: Float, y: Float): Int {
        val width = width.coerceAtLeast(1)
        val cellWidth = cellWidth(width).toFloat()
        val column = x / (cellWidth + gutterPx)
        val row = y / (rowHeightPx + gutterPx)
        return targetIndexAt(packCards(cards, columns), column, row)
    }

    private fun hitIndex(x: Float, y: Float): Int {
        for (index in cardViews.indices.reversed()) {
            val child = cardViews[index]
            if (x >= child.left && x < child.right && y >= child.top && y < child.bottom) {
                return index
            }
        }
        return -1
    }

    private fun cellWidth(width: Int): Int =
        ((width - gutterPx * (columns - 1)) / columns.toFloat()).roundToInt().coerceAtLeast(1)

    private fun localPoint(event: MotionEvent): Pair<Float, Float> {
        val loc = IntArray(2)
        getLocationOnScreen(loc)
        return event.rawX - loc[0] to event.rawY - loc[1]
    }

    private fun autoScroll(rawY: Float) {
        val scroller = scrollParent() ?: return
        val loc = IntArray(2)
        scroller.getLocationOnScreen(loc)
        val yInScroll = rawY - loc[1]
        val step = (12 * resources.displayMetrics.density).roundToInt()
        when {
            yInScroll < scrollEdgePx -> scroller.scrollBy(0, -step)
            yInScroll > scroller.height - scrollEdgePx -> scroller.scrollBy(0, step)
        }
    }

    private fun scrollParent(): View? {
        var current = parent
        while (current is View) {
            if (current is ScrollView || current.canScrollVertically(1) || current.canScrollVertically(-1)) {
                return current
            }
            current = current.parent
        }
        return null
    }
}
