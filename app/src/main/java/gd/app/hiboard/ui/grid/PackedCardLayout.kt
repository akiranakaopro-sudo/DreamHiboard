package gd.app.hiboard.ui.grid

import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ScrollView
import gd.app.hiboard.model.CardInstance
import gd.app.hiboard.model.GridPlacement
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
    private val reflowMs = 200L
    private val slop = ViewConfiguration.get(context).scaledTouchSlop
    private val reflowInterpolator = DecelerateInterpolator()
    private var cards: List<CardInstance> = emptyList()
    private val cardViews = mutableListOf<View>()

    var isDragging: Boolean = false
        private set
    private var originCards: List<CardInstance> = emptyList()
    private var originPlacements: List<GridPlacement> = emptyList()
    private var originIndex: Int = -1
    private var dropIndex: Int = -1
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
        val source = visibleCards()
        val placements = packCards(source, columns)
        val cellWidth = cellWidth(width)
        source.forEach { card ->
            val place = placements.firstOrNull { it.instanceId == card.instanceId } ?: return@forEach
            val child = viewFor(card.instanceId) ?: return@forEach
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
        val source = visibleCards()
        val placements = packCards(source, columns)
        val cellWidth = cellWidth(width)
        val draggedId = draggedInstanceId()
        source.forEach { card ->
            val place = placements.firstOrNull { it.instanceId == card.instanceId } ?: return@forEach
            val child = viewFor(card.instanceId) ?: return@forEach
            val x = place.column * (cellWidth + gutterPx)
            val y = place.row * (rowHeightPx + gutterPx)
            if (isDragging && card.instanceId != draggedId) {
                layoutReflow(child, x, y)
            } else {
                child.layout(x, y, x + child.measuredWidth, y + child.measuredHeight)
            }
        }
        if (isDragging) followPointer()
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
        originIndex = index
        dropIndex = index
        dragIndex = index
        originCards = cards
        originPlacements = packCards(cards, columns)
        grabOffsetX = downX - child.left
        grabOffsetY = downY - child.top
        dragX = downX
        dragY = downY
        child.animate().cancel()
        child.animate().scaleX(1.04f).scaleY(1.04f).setDuration(120).start()
        child.translationZ = elevationPx
        child.alpha = 0.94f
        child.isPressed = false
        child.cancelPendingInputEvents()
        child.bringToFront()
        parent.requestDisallowInterceptTouchEvent(true)
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        onDragStarted?.invoke()
        followPointer()
    }

    private fun onDrag(x: Float, y: Float, rawY: Float) {
        dragX = x
        dragY = y
        autoScroll(rawY)
        followPointer()
        updateDropIndex()
    }

    private fun updateDropIndex() {
        val child = cardViews.getOrNull(dragIndex) ?: return
        val strideX = (cellWidth(width.coerceAtLeast(1)) + gutterPx).toFloat()
        val strideY = (rowHeightPx + gutterPx).toFloat()
        val centerX = child.left + child.translationX + child.width / 2f
        val centerY = child.top + child.translationY + child.height / 2f
        val next = dropIndexFromOrigin(
            originPlacements,
            originIndex,
            dragX / strideX,
            dragY / strideY,
            centerX / strideX,
            centerY / strideY,
        )
        if (next != dropIndex) {
            dropIndex = next
            requestLayout()
        }
    }

    private fun endDrag(commit: Boolean) {
        val dragged = cardViews.getOrNull(dragIndex)
        cardViews.forEach { child ->
            child.animate().cancel()
            if (child !== dragged) {
                child.translationX = 0f
                child.translationY = 0f
            }
        }
        dragged?.animate()?.cancel()
        parent.requestDisallowInterceptTouchEvent(false)
        val from = originIndex
        val to = dropIndex
        val nextCards = if (commit) reorderCards(originCards, from, to) else originCards
        val nextViews = if (commit) {
            reorderItems(cardViews.toList(), originCards, from, to)
        } else {
            cardViews.toList()
        }
        val changed = commit && nextCards.map { it.catalogId } != originCards.map { it.catalogId }
        isDragging = false
        dragIndex = -1
        originIndex = -1
        dropIndex = -1
        pendingIndex = -1
        activePointerId = MotionEvent.INVALID_POINTER_ID
        cards = nextCards
        cardViews.clear()
        cardViews.addAll(nextViews)
        originCards = emptyList()
        originPlacements = emptyList()
        dragged?.translationX = 0f
        dragged?.translationY = 0f
        dragged?.scaleX = 1f
        dragged?.scaleY = 1f
        dragged?.translationZ = 0f
        dragged?.alpha = 1f
        requestLayout()
        if (changed) {
            onReorder?.invoke(nextCards.map { it.catalogId })
        }
        onDragEnded?.invoke()
    }

    private fun layoutReflow(child: View, x: Int, y: Int) {
        if (child.left == x && child.top == y) return
        val visualX = child.left + child.translationX
        val visualY = child.top + child.translationY
        child.animate().cancel()
        child.layout(x, y, x + child.measuredWidth, y + child.measuredHeight)
        if (visualX == x.toFloat() && visualY == y.toFloat()) {
            child.translationX = 0f
            child.translationY = 0f
            return
        }
        child.translationX = visualX - x
        child.translationY = visualY - y
        child.animate()
            .translationX(0f)
            .translationY(0f)
            .setDuration(reflowMs)
            .setInterpolator(reflowInterpolator)
            .start()
    }

    private fun followPointer() {
        val child = cardViews.getOrNull(dragIndex) ?: return
        child.translationX = dragX - grabOffsetX - child.left
        child.translationY = dragY - grabOffsetY - child.top
    }

    private fun visibleCards(): List<CardInstance> {
        if (!isDragging) return cards
        return reorderCards(originCards, originIndex, dropIndex)
    }

    private fun draggedInstanceId(): String? = originCards.getOrNull(originIndex)?.instanceId

    private fun viewFor(instanceId: String): View? =
        cardViews.firstOrNull { it.tag == instanceId }

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
