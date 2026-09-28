package gd.app.hiboard.ui

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import gd.app.hiboard.R
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Lays out the 2×2 notes card by fractions of its width so text and icons scale with the card.
 * Vertical positions are text baselines, measured on the ColorOS reference card.
 */
class NotesCardLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    private val icon: View by lazy { findViewById(R.id.notesIcon) }
    private val label: TextView by lazy { findViewById(R.id.notesLabel) }
    private val add: View by lazy { findViewById(R.id.notesAdd) }
    private val title: TextView by lazy { findViewById(R.id.notesTitle) }
    private val snippet: TextView by lazy { findViewById(R.id.notesSnippet) }
    private val whenText: TextView by lazy { findViewById(R.id.notesWhen) }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            w
        } else {
            MeasureSpec.getSize(heightMeasureSpec)
        }
        setMeasuredDimension(w, h)
        if (w <= 0 || h <= 0) return
        val wf = w.toFloat()
        exact(icon, wf * ICON_WIDTH, wf * ICON_HEIGHT)
        exact(add, wf * ADD_BOX, wf * ADD_BOX)
        label.sizePx(wf * LABEL_TEXT)
        title.sizePx(wf * NOTE_TEXT)
        snippet.sizePx(wf * NOTE_TEXT)
        whenText.sizePx(wf * WHEN_TEXT)

        val labelMax = (wf * (ADD_CENTER_X - ADD_BOX / 2f - LABEL_LEFT - 0.02f)).roundToInt().coerceAtLeast(0)
        label.measure(atMost(labelMax), unspecified())
        val textMax = (wf * (1f - NOTE_LEFT - NOTE_RIGHT)).roundToInt()
        title.measure(atMost(textMax), unspecified())
        whenText.measure(atMost(textMax), unspecified())

        val step = wf * LINE_STEP
        val natural = snippet.paint.getFontMetrics(null)
        val extra = step - natural
        if (snippet.lineSpacingExtra != extra) snippet.setLineSpacing(extra, 1f)
        val firstBaseline = snippetFirstBaseline(wf)
        val whenTop = h * WHEN_BASELINE_OF_HEIGHT - (-whenText.paint.fontMetrics.ascent)
        val room = whenTop - wf * SNIPPET_BOTTOM_GAP - firstBaseline
        val lines = (floor(room / step).toInt() + 1).coerceAtLeast(0)
        if (snippet.maxLines != lines) snippet.maxLines = lines
        if (lines == 0 || snippet.text.isNullOrEmpty()) {
            snippet.measure(atMost(textMax), MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY))
        } else {
            snippet.measure(atMost(textMax), unspecified())
        }
    }

    private fun snippetFirstBaseline(wf: Float): Float =
        if (title.text.isNullOrEmpty()) wf * NOTE_BASELINE else wf * (NOTE_BASELINE + LINE_STEP)

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val wf = (r - l).toFloat()
        val h = b - t
        val iconLeft = (wf * ICON_LEFT).roundToInt()
        val iconTop = (wf * ICON_TOP).roundToInt()
        icon.layout(iconLeft, iconTop, iconLeft + icon.measuredWidth, iconTop + icon.measuredHeight)
        placeByBaseline(label, (wf * LABEL_LEFT).roundToInt(), wf * LABEL_BASELINE)
        val addLeft = (wf * ADD_CENTER_X - add.measuredWidth / 2f).roundToInt()
        val addTop = (wf * ADD_CENTER_Y - add.measuredHeight / 2f).roundToInt()
        add.layout(addLeft, addTop, addLeft + add.measuredWidth, addTop + add.measuredHeight)
        val textLeft = (wf * NOTE_LEFT).roundToInt()
        placeByBaseline(title, textLeft, wf * NOTE_BASELINE)
        placeByBaseline(snippet, textLeft, snippetFirstBaseline(wf))
        placeByBaseline(whenText, textLeft, h * WHEN_BASELINE_OF_HEIGHT)
    }

    private fun placeByBaseline(view: TextView, left: Int, baseline: Float) {
        val top = (baseline - view.baseline.coerceAtLeast(0)).roundToInt()
        view.layout(left, top, left + view.measuredWidth, top + view.measuredHeight)
    }

    private fun TextView.sizePx(px: Float) {
        if (textSize != px) setTextSize(TypedValue.COMPLEX_UNIT_PX, px)
    }

    private fun exact(view: View, w: Float, h: Float) {
        view.measure(
            MeasureSpec.makeMeasureSpec(w.roundToInt(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(h.roundToInt(), MeasureSpec.EXACTLY),
        )
    }

    private fun atMost(size: Int) = MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST)

    private fun unspecified() = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)

    private companion object {
        const val ICON_LEFT = 0.1265f
        const val ICON_TOP = 0.1150f
        const val ICON_WIDTH = 0.0815f
        const val ICON_HEIGHT = 0.0990f
        const val LABEL_TEXT = 0.099f
        const val LABEL_LEFT = 0.250f
        const val LABEL_BASELINE = 0.2009f
        const val ADD_BOX = 0.1667f
        const val ADD_CENTER_X = 0.8323f
        const val ADD_CENTER_Y = 0.1656f
        const val NOTE_TEXT = 0.081f
        const val NOTE_LEFT = 0.112f
        const val NOTE_RIGHT = 0.085f
        const val NOTE_BASELINE = 0.4017f
        const val LINE_STEP = 0.087f
        const val WHEN_TEXT = 0.096f
        const val WHEN_BASELINE_OF_HEIGHT = 0.8718f
        const val SNIPPET_BOTTOM_GAP = 0.03f
    }
}
