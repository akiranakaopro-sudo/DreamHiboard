package gd.app.hiboard.ui.grid

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.PathInterpolator
import android.widget.ScrollView
import com.coui.appcompat.pressfeedback.COUIPressFeedbackHelper
import gd.app.hiboard.model.CardInstance
import gd.app.hiboard.model.CardSize
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
    private val elevationPx = 12 * resources.displayMetrics.density
    private val scrollEdgePx = (64 * resources.displayMetrics.density).roundToInt()
    private val cornerPx = 16 * resources.displayMetrics.density
    private val outlineInsetPx = 2 * resources.displayMetrics.density
    private val reflowMs = 250L
    private val outlineInMs = 900L
    private val outlineOutMs = 400L
    private val slop = ViewConfiguration.get(context).scaledTouchSlop
    private val reflowInterpolator = PathInterpolator(0.33f, 0f, 0.1f, 1f)
    private val outlineInterpolator = DecelerateInterpolator(2.5f)
    private var cards: List<CardInstance> = emptyList()
    private val cardViews = mutableListOf<View>()

    var isDragging: Boolean = false
        private set
    private var originCards: List<CardInstance> = emptyList()
    private var originViews: List<View> = emptyList()
    private var draggedId: String? = null
    private var lastHitId: String? = null
    private var lastSwapX = 0f
    private var lastSwapY = 0f
    private var dragIndex: Int = -1
    private var pendingIndex: Int = -1
    private var grabOffsetX = 0f
    private var grabOffsetY = 0f
    private var dragX = 0f
    private var dragY = 0f
    private var downX = 0f
    private var downY = 0f
    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private var pressHelper: COUIPressFeedbackHelper? = null
    private var outlineHost: View? = null
    private var outlineAlpha = 0f
    private var outlineAnimator: ValueAnimator? = null
    private val outlineRect = RectF()
    private val outlineFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0x26FFFFFF
    }
    private val outlineStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = outlineInsetPx
        color = 0x59FFFFFF
    }

    private val longPressRunnable = Runnable { beginDrag() }

    init {
        clipChildren = false
        clipToPadding = false
        setWillNotDraw(false)
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
        val cell = cellWidth(width)
        cards.forEach { card ->
            val place = placements.firstOrNull { it.instanceId == card.instanceId } ?: return@forEach
            val child = viewFor(card.instanceId) ?: return@forEach
            child.measure(
                MeasureSpec.makeMeasureSpec(spanPx(cell, place.columns), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(spanPx(cell, place.rows), MeasureSpec.EXACTLY),
            )
        }
        val maxRow = placements.maxOfOrNull { it.row + it.rows } ?: 0
        val height = if (maxRow == 0) 0 else spanPx(cell, maxRow)
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l
        val placements = packCards(cards, columns)
        val cell = cellWidth(width)
        val dragged = draggedId
        cards.forEach { card ->
            val place = placements.firstOrNull { it.instanceId == card.instanceId } ?: return@forEach
            val child = viewFor(card.instanceId) ?: return@forEach
            val x = place.column * (cell + gutterPx)
            val y = place.row * (cell + gutterPx)
            if (isDragging && card.instanceId != dragged) {
                layoutReflow(child, x, y)
            } else {
                child.layout(x, y, x + child.measuredWidth, y + child.measuredHeight)
            }
        }
        if (isDragging) followPointer()
    }

    override fun dispatchDraw(canvas: Canvas) {
        drawSeatOutline(canvas)
        super.dispatchDraw(canvas)
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
                if (pendingIndex >= 0) {
                    cardViews.getOrNull(pendingIndex)?.let { startPressFeedback(it) }
                    if (cards.getOrNull(pendingIndex)?.canDrag == true) {
                        postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout().toLong())
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val (x, y) = localPoint(event)
                if (!isDragging && hypot(x - downX, y - downY) > slop) {
                    removeCallbacks(longPressRunnable)
                    endPressFeedback(restore = true)
                }
                if (isDragging) return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                removeCallbacks(longPressRunnable)
                if (!isDragging) endPressFeedback(restore = true)
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
                endPressFeedback(restore = true)
            }
            MotionEvent.ACTION_CANCEL -> {
                removeCallbacks(longPressRunnable)
                if (isDragging) {
                    endDrag(commit = false)
                    return true
                }
                endPressFeedback(restore = true)
            }
        }
        return isDragging || pendingIndex >= 0
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(longPressRunnable)
        endPressFeedback(restore = false)
        outlineAnimator?.cancel()
        if (isDragging) endDrag(commit = false)
        super.onDetachedFromWindow()
    }

    private fun startPressFeedback(child: View) {
        if (isDragging) return
        val helper = pressHelper ?: COUIPressFeedbackHelper(child).also { pressHelper = it }
        helper.setTargetView(child)
        helper.setScaleEnable(true)
        child.animate().cancel()
        helper.executeFeedbackAnimator(true)
        helper.springAnimation?.spring?.setBounce(0.2f)
    }

    private fun endPressFeedback(restore: Boolean) {
        val helper = pressHelper ?: return
        if (restore) {
            helper.executeFeedbackAnimator(false)
        } else {
            helper.springAnimation?.cancel()
            helper.setScaleEnable(false)
            helper.setTargetView(null)
        }
    }

    private fun beginDrag() {
        val index = pendingIndex
        val child = cardViews.getOrNull(index) ?: return
        val card = cards.getOrNull(index) ?: return
        if (card.canDrag != true) return
        isDragging = true
        dragIndex = index
        draggedId = card.instanceId
        lastHitId = null
        lastSwapX = downX
        lastSwapY = downY
        originCards = cards
        originViews = cardViews.toList()
        grabOffsetX = downX - child.left
        grabOffsetY = downY - child.top
        dragX = downX
        dragY = downY
        endPressFeedback(restore = false)
        val scale = if (card.size == CardSize.TwoByTwo) 0.92f else 0.96f
        child.animate().cancel()
        child.animate().scaleX(scale).scaleY(scale).setDuration(120).start()
        child.translationZ = elevationPx
        child.alpha = 0.94f
        child.isPressed = false
        child.cancelPendingInputEvents()
        child.bringToFront()
        outlineHost = child
        captureSeatRect(child)
        animateOutline(show = true)
        parent.requestDisallowInterceptTouchEvent(true)
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        onDragStarted?.invoke()
        followPointer()
        invalidate()
    }

    private fun onDrag(x: Float, y: Float, rawY: Float) {
        dragX = x
        dragY = y
        autoScroll(rawY)
        followPointer()
        tryMoveToTarget()
    }

    private fun tryMoveToTarget() {
        val dragged = draggedId ?: return
        val child = viewFor(dragged) ?: return
        val draggedCard = cards.firstOrNull { it.instanceId == dragged } ?: return
        val stride = (cellWidth(width.coerceAtLeast(1)) + gutterPx).toFloat()
        val visualLeft = child.left + child.translationX
        val visualTop = child.top + child.translationY
        val column = (visualLeft + child.width / 2f) / stride
        val row = (visualTop + child.height / 2f) / stride
        val next = previewCardsForDrop(originCards, cards, dragged, column, row, columns)
        if (next.map { it.instanceId } == cards.map { it.instanceId }) return
        val restoring = next.map { it.instanceId } == originCards.map { it.instanceId }
        if (!restoring && lastHitId != null &&
            hypot(dragX - lastSwapX, dragY - lastSwapY) < swapTravelPx()
        ) {
            return
        }
        if (restoring) {
            applyOrder(originCards)
            lastHitId = dragged
        } else {
            val hitId = dropTargetId(
                packCards(cards, columns),
                dragged,
                column,
                row,
                draggedColumns = draggedCard.size.columns,
            ) ?: return
            applyOrder(next)
            lastHitId = hitId
        }
        dragIndex = cards.indexOfFirst { it.instanceId == dragged }
        lastSwapX = dragX
        lastSwapY = dragY
        requestLayout()
    }

    private fun applyOrder(next: List<CardInstance>) {
        val byTag = cardViews.associateBy { it.tag as String }
        cards = next
        cardViews.clear()
        cardViews.addAll(next.mapNotNull { byTag[it.instanceId] })
    }

    private fun endDrag(commit: Boolean) {
        val dragged = viewFor(draggedId.orEmpty())
        cardViews.forEach { child ->
            child.animate().cancel()
            if (child !== dragged) {
                child.translationX = 0f
                child.translationY = 0f
            }
        }
        dragged?.animate()?.cancel()
        parent.requestDisallowInterceptTouchEvent(false)
        val nextCards = if (commit) cards else originCards
        val nextViews = if (commit) cardViews.toList() else originViews
        val changed = commit && nextCards.map { it.catalogId } != originCards.map { it.catalogId }
        isDragging = false
        dragIndex = -1
        draggedId = null
        lastHitId = null
        lastSwapX = 0f
        lastSwapY = 0f
        pendingIndex = -1
        activePointerId = MotionEvent.INVALID_POINTER_ID
        cards = nextCards
        cardViews.clear()
        cardViews.addAll(nextViews)
        originCards = emptyList()
        originViews = emptyList()
        dragged?.translationX = 0f
        dragged?.translationY = 0f
        dragged?.scaleX = 1f
        dragged?.scaleY = 1f
        dragged?.translationZ = 0f
        dragged?.alpha = 1f
        animateOutline(show = false)
        requestLayout()
        if (changed) {
            onReorder?.invoke(nextCards.map { it.catalogId })
        }
        onDragEnded?.invoke()
    }

    private fun animateOutline(show: Boolean) {
        outlineAnimator?.cancel()
        val start = outlineAlpha
        val end = if (show) 1f else 0f
        if (start == end) {
            if (!show) outlineHost = null
            return
        }
        outlineAnimator = ValueAnimator.ofFloat(start, end).apply {
            duration = if (show) outlineInMs else outlineOutMs
            interpolator = outlineInterpolator
            addUpdateListener { animator ->
                outlineAlpha = animator.animatedValue as Float
                if (!show && outlineAlpha <= 0f) outlineHost = null
                invalidate()
            }
            start()
        }
    }

    private fun captureSeatRect(host: View) {
        val inset = outlineInsetPx
        outlineRect.set(
            host.left + inset,
            host.top + inset,
            host.right - inset,
            host.bottom - inset,
        )
    }

    private fun drawSeatOutline(canvas: Canvas) {
        if (outlineAlpha <= 0f || outlineRect.isEmpty) return
        val host = outlineHost
        if (host != null && isDragging) captureSeatRect(host)
        outlineFill.alpha = (0x26 * outlineAlpha).roundToInt().coerceIn(0, 255)
        outlineStroke.alpha = (0x59 * outlineAlpha).roundToInt().coerceIn(0, 255)
        canvas.drawRoundRect(outlineRect, cornerPx, cornerPx, outlineFill)
        canvas.drawRoundRect(outlineRect, cornerPx, cornerPx, outlineStroke)
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
        val child = viewFor(draggedId.orEmpty()) ?: return
        child.translationX = dragX - grabOffsetX - child.left
        child.translationY = dragY - grabOffsetY - child.top
    }

    private fun viewFor(instanceId: String): View? =
        cardViews.firstOrNull { it.tag == instanceId }

    private fun swapTravelPx(): Float =
        ((cellWidth(width.coerceAtLeast(1)) + gutterPx) * 0.4f).coerceAtLeast(slop * 3f)

    private fun hitIndex(x: Float, y: Float): Int {
        for (index in cardViews.indices.reversed()) {
            val child = cardViews[index]
            if (x >= child.left && x < child.right && y >= child.top && y < child.bottom) {
                return index
            }
        }
        return -1
    }

    private fun spanPx(cell: Int, spans: Int): Int =
        cell * spans + gutterPx * (spans - 1).coerceAtLeast(0)

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
