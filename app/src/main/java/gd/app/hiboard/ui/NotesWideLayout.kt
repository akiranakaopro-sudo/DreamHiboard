package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import gd.app.hiboard.R
import gd.app.hiboard.model.NoteItem
import kotlin.math.roundToInt

/** Fills the note rows; with no notes the first row shows the Title/Content placeholders. */
internal fun bindNotesWide(view: View, notes: List<NoteItem>, onOpenNote: ((Long) -> Unit)?) {
    val context = view.context
    val rows = notes.ifEmpty {
        listOf(NoteItem(0L, context.getString(R.string.notes_default_title), context.getString(R.string.notes_default_content)))
    }
    view.findViewById<NotesWideLayout>(R.id.notesRoot).setNotes(rows, onOpenNote)
}

/**
 * Lays out the 4×2 and 4×4 notes cards by fractions of their width so text and icons scale with
 * the card. Rows are a title over one snippet line, split by hairlines; the card shows as many
 * rows as fit its height. Vertical positions are text baselines, measured on the ColorOS cards.
 */
class NotesWideLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    private class Row(val title: TextView, val snippet: TextView, val divider: View, val hit: View)

    private val icon: View by lazy { findViewById(R.id.notesIcon) }
    private val label: TextView by lazy { findViewById(R.id.notesLabel) }
    private val add: View by lazy { findViewById(R.id.notesAdd) }
    private val rows = mutableListOf<Row>()
    private var rowCount = 0
    private var shownRows = 0

    fun setNotes(notes: List<NoteItem>, onOpenNote: ((Long) -> Unit)?) {
        while (rows.size < notes.size) rows += createRow()
        rows.forEachIndexed { i, row ->
            val note = notes.getOrNull(i)
            row.title.text = note?.title.orEmpty()
            row.snippet.text = note?.snippet?.lineSequence()?.firstOrNull { it.isNotBlank() }.orEmpty()
            if (note != null && onOpenNote != null) {
                row.hit.setOnClickListener { onOpenNote(note.id) }
                row.hit.isClickable = true
            } else {
                row.hit.setOnClickListener(null)
                row.hit.isClickable = false
            }
        }
        rowCount = notes.size
        requestLayout()
    }

    private fun createRow(): Row {
        val titleColor = context.getColor(R.color.hiboard_notes_title)
        fun text(medium: Boolean) = TextView(context).apply {
            setTextColor(titleColor)
            includeFontPadding = false
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            if (medium) typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        val row = Row(
            title = text(medium = true),
            snippet = text(medium = false),
            divider = View(context).apply { setBackgroundColor(context.getColor(R.color.hiboard_notes_divider)) },
            hit = View(context).apply { isFocusable = true },
        )
        val addIndex = indexOfChild(add)
        addView(row.title, addIndex)
        addView(row.snippet, addIndex + 1)
        addView(row.divider, addIndex + 2)
        addView(row.hit, addIndex + 3)
        return row
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            (w / WIDTH_UNITS).roundToInt()
        } else {
            MeasureSpec.getSize(heightMeasureSpec)
        }
        setMeasuredDimension(w, h)
        if (w <= 0 || h <= 0) return
        val u = w / WIDTH_UNITS
        exact(icon, u * ICON_WIDTH, u * ICON_HEIGHT)
        exact(add, u * ADD_BOX, u * ADD_BOX)
        label.sizePx(u * LABEL_TEXT)
        val labelMax = (w - u * (ADD_RIGHT + ADD_BOX + LABEL_LEFT + 0.04f)).roundToInt().coerceAtLeast(0)
        label.measure(atMost(labelMax), unspecified())
        val fit = ((h / u - BOTTOM_ROOM - FIRST_BASELINE - LINE_STEP) / ROW_PITCH).toInt() + 1
        shownRows = rowCount.coerceAtMost(fit.coerceAtLeast(1))
        val textMax = (w - u * (NOTE_LEFT + NOTE_RIGHT)).roundToInt().coerceAtLeast(0)
        rows.forEachIndexed { i, row ->
            val shown = i < shownRows
            row.title.sizePx(u * NOTE_TEXT)
            row.snippet.sizePx(u * NOTE_TEXT)
            if (shown) {
                row.title.measure(atMost(textMax), unspecified())
                row.snippet.measure(atMost(textMax), unspecified())
                exact(row.hit, w.toFloat(), u * ROW_PITCH)
            } else {
                listOf(row.title, row.snippet, row.hit).forEach { exact(it, 0f, 0f) }
            }
            exact(row.divider, if (shown && i > 0) w - u * (DIVIDER_LEFT + DIVIDER_RIGHT) else 0f, u * DIVIDER_THICKNESS)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val w = r - l
        val u = w / WIDTH_UNITS
        val iconLeft = (u * ICON_LEFT).roundToInt()
        val iconTop = (u * ICON_TOP).roundToInt()
        icon.layout(iconLeft, iconTop, iconLeft + icon.measuredWidth, iconTop + icon.measuredHeight)
        placeByBaseline(label, (u * LABEL_LEFT).roundToInt(), u * LABEL_BASELINE)
        val addRight = (w - u * ADD_RIGHT).roundToInt()
        val addTop = (u * ADD_CENTER_Y - add.measuredHeight / 2f).roundToInt()
        add.layout(addRight - add.measuredWidth, addTop, addRight, addTop + add.measuredHeight)
        val textLeft = (u * NOTE_LEFT).roundToInt()
        val dividerLeft = (u * DIVIDER_LEFT).roundToInt()
        rows.forEachIndexed { i, row ->
            if (i >= shownRows) {
                listOf(row.title, row.snippet, row.divider, row.hit).forEach { it.layout(0, 0, 0, 0) }
                return@forEachIndexed
            }
            val baseline = FIRST_BASELINE + i * ROW_PITCH
            placeByBaseline(row.title, textLeft, u * baseline)
            val snippetBaseline = if (row.title.text.isNullOrEmpty()) baseline else baseline + LINE_STEP
            placeByBaseline(row.snippet, textLeft, u * snippetBaseline)
            val dividerTop = (u * (baseline - ROW_PITCH + DIVIDER_AFTER_BASELINE)).roundToInt()
            row.divider.layout(dividerLeft, dividerTop, dividerLeft + row.divider.measuredWidth, dividerTop + row.divider.measuredHeight)
            row.hit.layout(0, dividerTop, w, dividerTop + row.hit.measuredHeight)
        }
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
            MeasureSpec.makeMeasureSpec(h.roundToInt().coerceAtLeast(if (w > 0f) 1 else 0), MeasureSpec.EXACTLY),
        )
    }

    private fun atMost(size: Int) = MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST)

    private fun unspecified() = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)

    private companion object {
        /** Card width in layout units; one unit is the height of a 4×2 board card. */
        const val WIDTH_UNITS = 2.062f
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
        const val FIRST_BASELINE = 0.3996f
        const val LINE_STEP = 0.109f
        const val ROW_PITCH = 0.3035f
        const val DIVIDER_AFTER_BASELINE = 0.1699f
        const val BOTTOM_ROOM = 0.10f
        const val DIVIDER_LEFT = 0.107f
        const val DIVIDER_RIGHT = 0.109f
        const val DIVIDER_THICKNESS = 0.0025f
    }
}
