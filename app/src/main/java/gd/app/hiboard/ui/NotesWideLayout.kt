package gd.app.hiboard.ui

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import gd.app.hiboard.R
import gd.app.hiboard.model.NoteItem
import kotlin.math.roundToInt

/** Fills the two note rows; with no notes the first row shows the Title/Content placeholders. */
internal fun bindNotesWide(view: View, notes: List<NoteItem>, onOpenNote: ((Long) -> Unit)?) {
    val context = view.context
    val rows = notes.take(2).ifEmpty {
        listOf(NoteItem(0L, context.getString(R.string.notes_default_title), context.getString(R.string.notes_default_content)))
    }
    val titles = listOf(R.id.notesTitle, R.id.notesTitle2)
    val snippets = listOf(R.id.notesSnippet, R.id.notesSnippet2)
    val hits = listOf(R.id.notesRowHit, R.id.notesRowHit2)
    for (i in 0 until 2) {
        val note = rows.getOrNull(i)
        view.findViewById<TextView>(titles[i]).text = note?.title.orEmpty()
        view.findViewById<TextView>(snippets[i]).text = note?.snippet?.lineSequence()?.firstOrNull { it.isNotBlank() }.orEmpty()
        val hit = view.findViewById<View>(hits[i])
        if (note != null && onOpenNote != null) {
            hit.visibility = View.VISIBLE
            hit.setOnClickListener { onOpenNote(note.id) }
        } else {
            hit.visibility = View.GONE
        }
    }
    view.findViewById<View>(R.id.notesDivider).visibility = if (rows.size > 1) View.VISIBLE else View.INVISIBLE
}

/**
 * Lays out the 4×2 notes card by fractions of its height so text and icons scale with the card.
 * Shows the two latest notes, each a title over one snippet line, split by a hairline.
 * Vertical positions are text baselines, measured on the ColorOS reference card.
 */
class NotesWideLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    private val icon: View by lazy { findViewById(R.id.notesIcon) }
    private val label: TextView by lazy { findViewById(R.id.notesLabel) }
    private val add: View by lazy { findViewById(R.id.notesAdd) }
    private val titles: List<TextView> by lazy { listOf(findViewById(R.id.notesTitle), findViewById(R.id.notesTitle2)) }
    private val snippets: List<TextView> by lazy { listOf(findViewById(R.id.notesSnippet), findViewById(R.id.notesSnippet2)) }
    private val hits: List<View> by lazy { listOf(findViewById(R.id.notesRowHit), findViewById(R.id.notesRowHit2)) }
    private val divider: View by lazy { findViewById(R.id.notesDivider) }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            (w * 0.48f).roundToInt()
        } else {
            MeasureSpec.getSize(heightMeasureSpec)
        }
        setMeasuredDimension(w, h)
        if (w <= 0 || h <= 0) return
        val hf = h.toFloat()
        exact(icon, hf * ICON_WIDTH, hf * ICON_HEIGHT)
        exact(add, hf * ADD_BOX, hf * ADD_BOX)
        label.sizePx(hf * LABEL_TEXT)
        val labelMax = (w - hf * (ADD_RIGHT + ADD_BOX + LABEL_LEFT + 0.04f)).roundToInt().coerceAtLeast(0)
        label.measure(atMost(labelMax), unspecified())
        val textMax = (w - hf * (NOTE_LEFT + NOTE_RIGHT)).roundToInt().coerceAtLeast(0)
        (titles + snippets).forEach { text ->
            text.sizePx(hf * NOTE_TEXT)
            text.measure(atMost(textMax), unspecified())
        }
        exact(divider, w - hf * (DIVIDER_LEFT + DIVIDER_RIGHT), hf * DIVIDER_THICKNESS)
        exact(hits[0], w.toFloat(), hf * (DIVIDER_Y - ROW_TOP))
        exact(hits[1], w.toFloat(), hf * (1f - DIVIDER_Y))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val w = r - l
        val hf = (b - t).toFloat()
        val iconLeft = (hf * ICON_LEFT).roundToInt()
        val iconTop = (hf * ICON_TOP).roundToInt()
        icon.layout(iconLeft, iconTop, iconLeft + icon.measuredWidth, iconTop + icon.measuredHeight)
        placeByBaseline(label, (hf * LABEL_LEFT).roundToInt(), hf * LABEL_BASELINE)
        val addRight = (w - hf * ADD_RIGHT).roundToInt()
        val addTop = (hf * ADD_CENTER_Y - add.measuredHeight / 2f).roundToInt()
        add.layout(addRight - add.measuredWidth, addTop, addRight, addTop + add.measuredHeight)
        val textLeft = (hf * NOTE_LEFT).roundToInt()
        ROW_BASELINES.forEachIndexed { i, baseline ->
            placeByBaseline(titles[i], textLeft, hf * baseline)
            val snippetBaseline = if (titles[i].text.isNullOrEmpty()) baseline else baseline + LINE_STEP
            placeByBaseline(snippets[i], textLeft, hf * snippetBaseline)
        }
        val dividerLeft = (hf * DIVIDER_LEFT).roundToInt()
        val dividerTop = (hf * DIVIDER_Y).roundToInt()
        divider.layout(dividerLeft, dividerTop, dividerLeft + divider.measuredWidth, dividerTop + divider.measuredHeight)
        val rowTop = (hf * ROW_TOP).roundToInt()
        hits[0].layout(0, rowTop, w, rowTop + hits[0].measuredHeight)
        hits[1].layout(0, dividerTop, w, dividerTop + hits[1].measuredHeight)
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
            MeasureSpec.makeMeasureSpec(w.roundToInt().coerceAtLeast(0), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(h.roundToInt().coerceAtLeast(1), MeasureSpec.EXACTLY),
        )
    }

    private fun atMost(size: Int) = MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST)

    private fun unspecified() = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)

    private companion object {
        const val ICON_LEFT = 0.1200f
        const val ICON_TOP = 0.1147f
        const val ICON_WIDTH = 0.0787f
        const val ICON_HEIGHT = 0.0955f
        const val LABEL_TEXT = 0.0955f
        const val LABEL_LEFT = 0.2415f
        const val LABEL_BASELINE = 0.1987f
        const val ADD_BOX = 0.1603f
        const val ADD_RIGHT = 0.0812f
        const val ADD_CENTER_Y = 0.1614f
        const val NOTE_TEXT = 0.081f
        const val NOTE_LEFT = 0.106f
        const val NOTE_RIGHT = 0.109f
        const val LINE_STEP = 0.087f
        val ROW_BASELINES = floatArrayOf(0.3996f, 0.7031f)
        const val ROW_TOP = 0.27f
        const val DIVIDER_Y = 0.5695f
        const val DIVIDER_LEFT = 0.107f
        const val DIVIDER_RIGHT = 0.109f
        const val DIVIDER_THICKNESS = 0.0025f
    }
}
