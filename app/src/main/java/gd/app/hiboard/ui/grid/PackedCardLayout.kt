package gd.app.hiboard.ui.grid

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import gd.app.hiboard.model.CardInstance
import kotlin.math.roundToInt

class PackedCardLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    var columns: Int = 4
    private val gutterPx = (10 * resources.displayMetrics.density).roundToInt()
    private val rowHeightPx = (52 * resources.displayMetrics.density).roundToInt()
    private var cards: List<CardInstance> = emptyList()
    private var factory: ((CardInstance) -> View)? = null

    fun setCards(cards: List<CardInstance>, factory: (CardInstance) -> View) {
        this.cards = cards
        this.factory = factory
        removeAllViews()
        cards.forEach { addView(factory(it)) }
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val placements = packCards(cards, columns)
        val cellWidth = ((width - gutterPx * (columns - 1)) / columns.toFloat())
            .roundToInt()
            .coerceAtLeast(1)
        cards.forEachIndexed { index, _ ->
            val place = placements.getOrNull(index) ?: return@forEachIndexed
            val childWidth = cellWidth * place.columns + gutterPx * (place.columns - 1)
            val childHeight = rowHeightPx * place.rows + gutterPx * (place.rows - 1)
            getChildAt(index).measure(
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
        val cellWidth = ((width - gutterPx * (columns - 1)) / columns.toFloat())
            .roundToInt()
            .coerceAtLeast(1)
        cards.forEachIndexed { index, _ ->
            val place = placements.getOrNull(index) ?: return@forEachIndexed
            val child = getChildAt(index)
            val x = place.column * (cellWidth + gutterPx)
            val y = place.row * (rowHeightPx + gutterPx)
            child.layout(x, y, x + child.measuredWidth, y + child.measuredHeight)
        }
    }
}
