package gd.app.hiboard.ui

import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.coui.appcompat.cardview.COUICardView
import gd.app.hiboard.R
import gd.app.hiboard.engine.CalendarCell
import gd.app.hiboard.engine.MonthPage

fun bindCalendarCard(
    card: COUICardView,
    body: LinearLayout,
    page: MonthPage,
    onOpen: (() -> Unit)?,
) {
    val context = body.context
    val density = body.resources.displayMetrics.density
    val pad = (10 * density).toInt()
    card.setCardBackgroundColor(context.getColor(R.color.hiboard_calendar_card))
    card.setContentPadding(pad, pad, pad, (6 * density).toInt())
    val view = android.view.LayoutInflater.from(context).inflate(R.layout.card_calendar, body, true)
    view.findViewById<TextView>(R.id.calendarTitle).text = page.title
    val weekdays = view.findViewById<LinearLayout>(R.id.calendarWeekdays)
    val weeks = view.findViewById<LinearLayout>(R.id.calendarWeeks)
    weekdays.removeAllViews()
    weeks.removeAllViews()
    val weekdayColor = context.getColor(R.color.hiboard_calendar_weekday)
    page.weekdays.forEach { label ->
        weekdays.addView(dayLabel(context, label, weekdayColor, 9f, bold = false))
    }
    val inMonth = context.getColor(R.color.hiboard_calendar_title)
    val outside = context.getColor(R.color.hiboard_calendar_outside)
    val todayColor = context.getColor(R.color.hiboard_calendar_today)
    page.weeks.forEach { week ->
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f,
            )
        }
        week.forEach { cell ->
            row.addView(dayCell(context, cell, density, inMonth, outside, todayColor))
        }
        weeks.addView(row)
    }
    val open = View.OnClickListener { onOpen?.invoke() }
    view.findViewById<View>(R.id.calendarRoot).setOnClickListener(open)
    card.setOnClickListener(open)
}

private fun dayLabel(
    context: android.content.Context,
    text: String,
    color: Int,
    sizeSp: Float,
    bold: Boolean,
): TextView {
    return TextView(context).apply {
        this.text = text
        setTextColor(color)
        textSize = sizeSp
        gravity = Gravity.CENTER
        includeFontPadding = false
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }
}

private fun dayCell(
    context: android.content.Context,
    cell: CalendarCell,
    density: Float,
    inMonth: Int,
    outside: Int,
    todayColor: Int,
): FrameLayout {
    val frame = FrameLayout(context).apply {
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
    }
    val size = (16 * density).toInt()
    val label = TextView(context).apply {
        text = cell.day.toString()
        gravity = Gravity.CENTER
        includeFontPadding = false
        textSize = 10f
        minWidth = size
        minHeight = size
        layoutParams = FrameLayout.LayoutParams(size, size, Gravity.CENTER)
    }
    if (cell.today) {
        label.setTextColor(context.getColor(android.R.color.white))
        label.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(todayColor)
        }
    } else {
        label.setTextColor(if (cell.inMonth) inMonth else outside)
    }
    frame.addView(label)
    return frame
}
