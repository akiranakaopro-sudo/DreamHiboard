package gd.app.hiboard.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.coui.appcompat.cardview.COUICardView
import gd.app.hiboard.R
import java.util.Calendar
import java.util.Locale

/** Square system clock: hour over minute, with tick marks around the edge. */
class LocalTimeClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val hourView: TextView
    private val minuteView: TextView

    private val tick = object : Runnable {
        override fun run() {
            render()
            val delay = 1000L - (System.currentTimeMillis() % 1000L)
            postDelayed(this, delay.coerceAtLeast(16L))
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.card_local_time_clock, this, true)
        hourView = findViewById(R.id.localClockHour)
        minuteView = findViewById(R.id.localClockMinute)
        render()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        removeCallbacks(tick)
        post(tick)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(tick)
        super.onDetachedFromWindow()
    }

    private fun render() {
        val now = Calendar.getInstance()
        hourView.text = String.format(Locale.US, "%02d", now.get(Calendar.HOUR_OF_DAY))
        minuteView.text = String.format(Locale.US, "%02d", now.get(Calendar.MINUTE))
    }
}

fun bindLocalTimeClock(
    card: COUICardView,
    body: LinearLayout,
    onOpen: (() -> Unit)?,
) {
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_calendar_card))
    card.setContentPadding(0, 0, 0, 0)
    card.clipToOutline = true
    val clock = LocalTimeClockView(body.context)
    clock.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT,
    )
    body.addView(clock)
    if (onOpen != null) {
        val open = View.OnClickListener { onOpen.invoke() }
        clock.setOnClickListener(open)
        card.setOnClickListener(open)
    }
}
