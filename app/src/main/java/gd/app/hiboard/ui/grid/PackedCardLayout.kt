package gd.app.hiboard.ui.grid

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.PathInterpolator
import android.widget.ScrollView
import com.coui.appcompat.pressfeedback.COUIPressFeedbackHelper
import gd.app.hiboard.R
import gd.app.hiboard.model.CardInstance
import gd.app.hiboard.model.CardSize
import gd.app.hiboard.model.GridPlacement
import kotlin.math.abs
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
    var onAddSlotClick: (() -> Unit)? = null
    var onCardLongPress: ((CardInstance, View) -> Unit)? = null

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
    private val addSlotViews = mutableListOf<View>()
    private var addSlots: List<GridPlacement> = emptyList()

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
    private var pendingAddSlot: Int = -1
    private var dragArmed: Boolean = false
    private var stoleStream: Boolean = false
    private var grabOffsetX = 0f
    private var grabOffsetY = 0f
    private var dragX = 0f
    private var dragY = 0f
    private var downX = 0f
    private var downY = 0f
    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private var pressHelper: COUIPressFeedbackHelper? = null
    private var outlineAlpha = 0f
    private var outlineAnimator: ValueAnimator? = null
    private var seatAnimator: ValueAnimator? = null
    private val outlineRect = RectF()
    private val seatFrom = RectF()
    private val seatTo = RectF()
    private val outlineFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0x26FFFFFF
    }
    private val outlineStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = outlineInsetPx
        color = 0x59FFFFFF
    }

    private val longPressRunnable = Runnable { armLongPress() }

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
        addSlotViews.clear()
        cards.forEach { card ->
            val child = factory(card)
            child.tag = card.instanceId
            cardViews.add(child)
            addView(child)
        }
        syncAddSlots()
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
        addSlots.forEachIndexed { index, place ->
            val child = addSlotViews.getOrNull(index) ?: return@forEachIndexed
            if (child.visibility == View.GONE) return@forEachIndexed
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
        addSlots.forEachIndexed { index, place ->
            val child = addSlotViews.getOrNull(index) ?: return@forEachIndexed
            if (child.visibility == View.GONE) return@forEachIndexed
            val x = place.column * (cell + gutterPx)
            val y = place.row * (cell + gutterPx)
            child.layout(x, y, x + child.measuredWidth, y + child.measuredHeight)
        }
        if (isDragging) {
            followPointer()
            syncSeatOutline(animate = true)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        drawSeatOutline(canvas)
        super.dispatchDraw(canvas)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            stoleStream = false
        }
        if ((isDragging || dragArmed) && event.actionMasked != MotionEvent.ACTION_DOWN) {
            parent.requestDisallowInterceptTouchEvent(true)
            if (!stoleStream) {
                stoleStream = true
                cancelChildTouches(event)
            }
            return onTouchEvent(event)
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = event.getPointerId(0)
                dragArmed = false
                stoleStream = false
                val (x, y) = localPoint(event)
                downX = x
                downY = y
                dragX = x
                dragY = y
                pendingIndex = hitIndex(x, y)
                pendingAddSlot = if (pendingIndex < 0) hitAddSlot(x, y) else -1
                removeCallbacks(longPressRunnable)
                if (pendingIndex >= 0) {
                    val card = cards.getOrNull(pendingIndex)
                    if (card?.canDrag == true || card?.canEdit == true) {
                        cardViews.getOrNull(pendingIndex)?.let { startPressFeedback(it) }
                        postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout().toLong())
                    }
                } else if (pendingAddSlot >= 0) {
                    addSlotViews.getOrNull(pendingAddSlot)?.let { startPressFeedback(it) }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val (x, y) = localPoint(event)
                if (!isDragging && !dragArmed && hypot(x - downX, y - downY) > slop) {
                    abandonPress()
                    pendingIndex = -1
                    pendingAddSlot = -1
                }
                if (isDragging || dragArmed) return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                removeCallbacks(longPressRunnable)
                if (isDragging || dragArmed) return true
                abandonPress()
                pendingAddSlot = -1
                activePointerId = MotionEvent.INVALID_POINTER_ID
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (event.getPointerId(event.actionIndex) == activePointerId) {
                    removeCallbacks(longPressRunnable)
                }
            }
        }
        return isDragging || dragArmed
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (pendingIndex < 0 && !isDragging && !dragArmed) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val (x, y) = localPoint(event)
                if (!isDragging && !dragArmed && hypot(x - downX, y - downY) > slop) {
                    abandonPress()
                    pendingIndex = -1
                    pendingAddSlot = -1
                    return false
                }
                if (dragArmed && !isDragging && hypot(x - downX, y - downY) > slop) {
                    beginDrag()
                }
                if (isDragging) {
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
                if (dragArmed) {
                    clearArm()
                    return true
                }
                abandonPress()
                pendingIndex = -1
                pendingAddSlot = -1
            }
            MotionEvent.ACTION_CANCEL -> {
                removeCallbacks(longPressRunnable)
                if (isDragging) {
                    endDrag(commit = false)
                    return true
                }
                if (dragArmed) {
                    clearArm()
                    return true
                }
                abandonPress()
                pendingIndex = -1
                pendingAddSlot = -1
            }
        }
        return isDragging || dragArmed || pendingIndex >= 0
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(longPressRunnable)
        endPressFeedback(restore = false)
        outlineAnimator?.cancel()
        seatAnimator?.cancel()
        dragArmed = false
        stoleStream = false
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

    private fun armLongPress() {
        val index = pendingIndex
        val card = cards.getOrNull(index) ?: return
        val child = cardViews.getOrNull(index) ?: return
        if (isDragging) return
        if (card.canDrag != true && card.canEdit != true) return
        dragArmed = true
        parent.requestDisallowInterceptTouchEvent(true)
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        cancelChildTouches()
        stoleStream = true
        endPressFeedback(restore = true)
        if (card.canEdit) {
            onCardLongPress?.invoke(card, child)
        }
    }

    private fun abandonPress() {
        removeCallbacks(longPressRunnable)
        parent.requestDisallowInterceptTouchEvent(false)
        endPressFeedback(restore = true)
    }

    private fun clearArm() {
        dragArmed = false
        pendingIndex = -1
        pendingAddSlot = -1
        stoleStream = false
        abandonPress()
        activePointerId = MotionEvent.INVALID_POINTER_ID
    }

    private fun cancelChildTouches(event: MotionEvent? = null) {
        val cancel = if (event != null) {
            MotionEvent.obtain(event).apply { action = MotionEvent.ACTION_CANCEL }
        } else {
            MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_CANCEL,
                0f,
                0f,
                0,
            )
        }
        for (index in 0 until childCount) {
            getChildAt(index).dispatchTouchEvent(cancel)
        }
        cancel.recycle()
    }

    private fun beginDrag() {
        val index = pendingIndex
        val child = cardViews.getOrNull(index) ?: return
        val card = cards.getOrNull(index) ?: return
        if (card.canDrag != true) return
        isDragging = true
        dragArmed = false
        addSlotViews.forEach { it.visibility = View.GONE }
        dragIndex = index
        draggedId = card.instanceId
        lastHitId = null
        lastSwapX = downX
        lastSwapY = downY
        originCards = cards
        originViews = cardViews.toList()
        grabOffsetX = downX - child.left
        grabOffsetY = downY - child.top
        endPressFeedback(restore = false)
        val scale = if (card.size == CardSize.TwoByTwo) 0.92f else 0.96f
        child.animate().cancel()
        child.animate().scaleX(scale).scaleY(scale).setDuration(120).start()
        child.translationZ = elevationPx
        child.alpha = 0.94f
        child.isPressed = false
        child.cancelPendingInputEvents()
        child.bringToFront()
        syncSeatOutline(animate = false)
        animateOutline(show = true)
        parent.requestDisallowInterceptTouchEvent(true)
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
        if (lastHitId != null &&
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
            val hitPlace = packCards(cards, columns).firstOrNull { it.instanceId == hitId }
            if (hitId == lastHitId && (hitPlace == null || hitPlace.rows >= 4)) return
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
        cards = pinLockedCards(next)
        cardViews.clear()
        cardViews.addAll(cards.mapNotNull { byTag[it.instanceId] })
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
        dragArmed = false
        dragIndex = -1
        draggedId = null
        lastHitId = null
        lastSwapX = 0f
        lastSwapY = 0f
        pendingIndex = -1
        pendingAddSlot = -1
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
        seatAnimator?.cancel()
        animateOutline(show = false)
        syncAddSlots()
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
        if (start == end) return
        outlineAnimator = ValueAnimator.ofFloat(start, end).apply {
            duration = if (show) outlineInMs else outlineOutMs
            interpolator = outlineInterpolator
            addUpdateListener { animator ->
                outlineAlpha = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun syncSeatOutline(animate: Boolean) {
        val target = seatRectForDragged() ?: return
        if (sameSeat(outlineRect, target) && sameSeat(seatTo, target)) return
        if (sameSeat(seatTo, target) && seatAnimator?.isRunning == true) return
        if (!animate || outlineRect.isEmpty) {
            seatAnimator?.cancel()
            outlineRect.set(target)
            seatTo.set(target)
            invalidate()
            return
        }
        seatAnimator?.cancel()
        seatFrom.set(outlineRect)
        seatTo.set(target)
        seatAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = reflowMs
            interpolator = reflowInterpolator
            addUpdateListener { animator ->
                val t = animator.animatedValue as Float
                outlineRect.set(
                    seatFrom.left + (seatTo.left - seatFrom.left) * t,
                    seatFrom.top + (seatTo.top - seatFrom.top) * t,
                    seatFrom.right + (seatTo.right - seatFrom.right) * t,
                    seatFrom.bottom + (seatTo.bottom - seatFrom.bottom) * t,
                )
                invalidate()
            }
            start()
        }
    }

    private fun seatRectForDragged(): RectF? {
        val dragged = draggedId ?: return null
        val place = packCards(cards, columns).firstOrNull { it.instanceId == dragged } ?: return null
        val cell = cellWidth(width.coerceAtLeast(1))
        val x = place.column * (cell + gutterPx)
        val y = place.row * (cell + gutterPx)
        val w = spanPx(cell, place.columns)
        val h = spanPx(cell, place.rows)
        val inset = outlineInsetPx
        return RectF(x + inset, y + inset, x + w - inset, y + h - inset)
    }

    private fun sameSeat(a: RectF, b: RectF): Boolean {
        val epsilon = 0.5f
        return abs(a.left - b.left) < epsilon &&
            abs(a.top - b.top) < epsilon &&
            abs(a.right - b.right) < epsilon &&
            abs(a.bottom - b.bottom) < epsilon
    }

    private fun drawSeatOutline(canvas: Canvas) {
        if (outlineAlpha <= 0f || outlineRect.isEmpty) return
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

    fun findCard(catalogId: String): View? {
        val instanceId = cards.firstOrNull { it.catalogId == catalogId }?.instanceId ?: return null
        return viewFor(instanceId)
    }

    private fun syncAddSlots() {
        addSlots = if (isDragging || cards.isEmpty()) {
            emptyList()
        } else {
            emptyAddSlots(packCards(cards, columns), columns)
        }
        while (addSlotViews.size > addSlots.size) {
            removeView(addSlotViews.removeAt(addSlotViews.lastIndex))
        }
        while (addSlotViews.size < addSlots.size) {
            addSlotViews.add(inflateAddSlot())
        }
        addSlotViews.forEach { child ->
            child.visibility = if (isDragging) View.GONE else View.VISIBLE
        }
    }

    private fun inflateAddSlot(): View {
        val child = LayoutInflater.from(context).inflate(R.layout.item_add_slot, this, false)
        child.setOnClickListener { onAddSlotClick?.invoke() }
        addView(child)
        return child
    }

    private fun swapTravelPx(): Float =
        ((cellWidth(width.coerceAtLeast(1)) + gutterPx) * 0.22f).coerceAtLeast(slop * 2f)

    private fun hitIndex(x: Float, y: Float): Int {
        for (index in cardViews.indices.reversed()) {
            val child = cardViews[index]
            if (x >= child.left && x < child.right && y >= child.top && y < child.bottom) {
                return index
            }
        }
        return -1
    }

    private fun hitAddSlot(x: Float, y: Float): Int {
        for (index in addSlotViews.indices.reversed()) {
            val child = addSlotViews[index]
            if (child.visibility != View.VISIBLE) continue
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
