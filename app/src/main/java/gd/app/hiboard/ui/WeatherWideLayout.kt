package gd.app.hiboard.ui

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import gd.app.hiboard.R
import kotlin.math.roundToInt

/**
 * Lays out the 4×2 weather card by fractions of its size so text and icons scale with the card.
 * Vertical positions are text baselines, measured on the ColorOS reference card.
 */
class WeatherWideLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    private val background: View by lazy { findViewById(R.id.weatherBackground) }
    private val pin: View by lazy { findViewById(R.id.weatherPin) }
    private val location: TextView by lazy { findViewById(R.id.weatherLocation) }
    private val summary: TextView by lazy { findViewById(R.id.weatherSummary) }
    private val conditionIcon: View by lazy { findViewById(R.id.weatherConditionIcon) }
    private val temp: TextView by lazy { findViewById(R.id.weatherTemp) }
    private val forecast: ViewGroup by lazy { findViewById(R.id.weatherForecast) }

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
        exact(background, w, h)
        square(pin, hf * PIN_BOX)
        square(conditionIcon, hf * CONDITION_BOX)
        location.sizePx(hf * TITLE_TEXT)
        summary.sizePx(hf * TITLE_TEXT)
        temp.sizePx(hf * TEMP_TEXT)
        val summaryMax = (w * 0.45f).roundToInt()
        summary.measure(MeasureSpec.makeMeasureSpec(summaryMax, MeasureSpec.AT_MOST), unspecified())
        val locationLeft = w * LOCATION_LEFT
        val locationMax = (w * CONDITION_RIGHT - hf * CONDITION_BOX - summary.measuredWidth - locationLeft - w * 0.04f)
            .roundToInt().coerceAtLeast(0)
        location.measure(MeasureSpec.makeMeasureSpec(locationMax, MeasureSpec.AT_MOST), unspecified())
        temp.measure(unspecified(), unspecified())
        measureForecast(w, hf)
    }

    private fun measureForecast(w: Int, hf: Float) {
        val iconBox = (hf * DAY_ICON_BOX).roundToInt()
        var labelTop = 0f
        var bottom = 0f
        for (i in 0 until forecast.childCount) {
            val item = forecast.getChildAt(i) as? ViewGroup ?: continue
            val label = item.findViewById<TextView>(R.id.weatherDayLabel)
            val icon = item.findViewById<View>(R.id.weatherDayIcon)
            val range = item.findViewById<TextView>(R.id.weatherDayRange)
            label.sizePx(hf * DAY_LABEL_TEXT)
            range.sizePx(hf * DAY_RANGE_TEXT)
            val labelAscent = -label.paint.fontMetrics.ascent
            val labelHeight = label.paint.fontMetrics.let { it.descent - it.ascent }
            val rangeAscent = -range.paint.fontMetrics.ascent
            labelTop = hf * DAY_LABEL_BASELINE - labelAscent
            val iconTop = hf * DAY_ICON_CENTER - iconBox / 2f
            val rangeTop = hf * DAY_RANGE_BASELINE - rangeAscent
            (icon.layoutParams as MarginLayoutParams).apply {
                width = iconBox
                height = iconBox
                topMargin = (iconTop - labelTop - labelHeight).roundToInt()
            }
            (range.layoutParams as MarginLayoutParams).topMargin = (rangeTop - iconTop - iconBox).roundToInt()
            bottom = rangeTop + range.paint.fontMetrics.let { it.descent - it.ascent }
        }
        forecastTop = labelTop
        val inset = w * FORECAST_INSET
        forecast.measure(
            MeasureSpec.makeMeasureSpec((w - inset * 2).roundToInt(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec((bottom - labelTop).roundToInt().coerceAtLeast(0) + 2, MeasureSpec.EXACTLY),
        )
    }

    private var forecastTop = 0f

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val w = r - l
        val hf = (b - t).toFloat()
        background.layout(0, 0, w, b - t)
        val pinBox = pin.measuredWidth
        val pinLeft = (w * PIN_LEFT - pinBox * PIN_GLYPH_LEFT).roundToInt()
        val pinTop = (hf * TITLE_BASELINE - pinBox * PIN_GLYPH_BOTTOM).roundToInt()
        pin.layout(pinLeft, pinTop, pinLeft + pinBox, pinTop + pinBox)
        placeByBaseline(location, (w * LOCATION_LEFT).roundToInt(), hf * TITLE_BASELINE)
        val iconBox = conditionIcon.measuredWidth
        val iconRight = (w * CONDITION_RIGHT + iconBox * CONDITION_GLYPH_RIGHT_PAD).roundToInt()
        val iconTop = (hf * CONDITION_CENTER - iconBox / 2f).roundToInt()
        conditionIcon.layout(iconRight - iconBox, iconTop, iconRight, iconTop + iconBox)
        val summaryRight = (w * CONDITION_RIGHT - iconBox * CONDITION_GLYPH_WIDTH - w * SUMMARY_GAP).roundToInt()
        placeByBaseline(summary, summaryRight - summary.measuredWidth, hf * TITLE_BASELINE)
        placeByBaseline(temp, (w * TEMP_LEFT).roundToInt(), hf * TEMP_BASELINE)
        val inset = (w * FORECAST_INSET).roundToInt()
        val top = forecastTop.roundToInt()
        forecast.layout(inset, top, inset + forecast.measuredWidth, top + forecast.measuredHeight)
    }

    private fun placeByBaseline(view: TextView, left: Int, baseline: Float) {
        val top = (baseline - view.baseline).roundToInt()
        view.layout(left, top, left + view.measuredWidth, top + view.measuredHeight)
    }

    private fun TextView.sizePx(px: Float) {
        if (textSize != px) setTextSize(TypedValue.COMPLEX_UNIT_PX, px)
    }

    private fun exact(view: View, w: Int, h: Int) {
        view.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY))
    }

    private fun square(view: View, size: Float) {
        val spec = MeasureSpec.makeMeasureSpec(size.roundToInt(), MeasureSpec.EXACTLY)
        view.measure(spec, spec)
    }

    private fun unspecified() = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)

    private companion object {
        const val TITLE_TEXT = 0.093f
        const val TITLE_BASELINE = 0.19f
        const val PIN_BOX = 0.095f
        const val PIN_LEFT = 0.0525f
        const val PIN_GLYPH_LEFT = 3.3f / 16f
        const val PIN_GLYPH_BOTTOM = 14.4f / 16f
        const val LOCATION_LEFT = 0.097f
        const val CONDITION_BOX = 0.15f
        const val CONDITION_RIGHT = 0.9496f
        const val CONDITION_CENTER = 0.153f
        const val CONDITION_GLYPH_RIGHT_PAD = 1.1f / 24f
        const val CONDITION_GLYPH_WIDTH = 19.9f / 24f
        const val SUMMARY_GAP = 0.018f
        const val TEMP_TEXT = 0.271f
        const val TEMP_BASELINE = 0.4637f
        const val TEMP_LEFT = 0.04f
        const val FORECAST_INSET = 0.0275f
        const val DAY_LABEL_TEXT = 0.063f
        const val DAY_LABEL_BASELINE = 0.6132f
        const val DAY_ICON_BOX = 0.115f
        const val DAY_ICON_CENTER = 0.70f
        const val DAY_RANGE_TEXT = 0.078f
        const val DAY_RANGE_BASELINE = 0.8504f
    }
}
