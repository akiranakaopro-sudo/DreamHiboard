package gd.app.hiboard.ui

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import gd.app.hiboard.R
import kotlin.math.min
import kotlin.math.roundToInt

/** Lays out the recorder card by fractions of its width, so the 2×2 face scales like the reference. */
class RecorderCardLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    private val time: TextView by lazy { findViewById(R.id.recorderTime) }
    private val wave: View by lazy { findViewById(R.id.recorderWave) }
    private val mark: View by lazy { findViewById(R.id.recorderMark) }
    private val primary: View by lazy { findViewById(R.id.recorderPrimary) }
    private val save: View by lazy { findViewById(R.id.recorderSave) }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            w
        } else {
            MeasureSpec.getSize(heightMeasureSpec)
        }
        setMeasuredDimension(w, h)
        val unit = min(w, h).toFloat()
        if (unit <= 0f) return
        val textPx = unit * TIME_TEXT
        if (time.textSize != textPx) time.setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx)
        time.measure(
            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
        )
        wave.measure(
            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec((unit * WAVE_HEIGHT).roundToInt(), MeasureSpec.EXACTLY),
        )
        val big = MeasureSpec.makeMeasureSpec((unit * PRIMARY_SIZE).roundToInt(), MeasureSpec.EXACTLY)
        primary.measure(big, big)
        val small = MeasureSpec.makeMeasureSpec((unit * SIDE_SIZE).roundToInt(), MeasureSpec.EXACTLY)
        mark.measure(small, small)
        save.measure(small, small)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val w = r - l
        val h = b - t
        val unit = min(w, h).toFloat()
        val top = (h - unit) / 2f
        val cx = w / 2f
        val timeTop = (top + unit * TIME_BASELINE - time.baseline).roundToInt()
        time.layout(0, timeTop, w, timeTop + time.measuredHeight)
        val waveTop = (top + unit * WAVE_CENTER - wave.measuredHeight / 2f).roundToInt()
        wave.layout(0, waveTop, w, waveTop + wave.measuredHeight)
        val buttonCy = top + unit * BUTTON_CENTER
        place(primary, cx, buttonCy)
        place(mark, cx - unit * SIDE_OFFSET, buttonCy)
        place(save, cx + unit * SIDE_OFFSET, buttonCy)
    }

    private fun place(view: View, cx: Float, cy: Float) {
        val left = (cx - view.measuredWidth / 2f).roundToInt()
        val top = (cy - view.measuredHeight / 2f).roundToInt()
        view.layout(left, top, left + view.measuredWidth, top + view.measuredHeight)
    }

    private companion object {
        const val TIME_TEXT = 0.215f
        const val TIME_BASELINE = 0.28f
        const val WAVE_CENTER = 0.454f
        const val WAVE_HEIGHT = 0.26f
        const val BUTTON_CENTER = 0.781f
        const val PRIMARY_SIZE = 0.282f
        const val SIDE_SIZE = 0.19f
        const val SIDE_OFFSET = 0.335f
    }
}
