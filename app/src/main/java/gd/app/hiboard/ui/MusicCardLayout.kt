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
 * Lays out the 4×2 music card by fractions of its height so it scales with the card.
 * Text is placed by baseline and icons by glyph size, measured on the ColorOS reference card;
 * the five controls share the card width equally.
 */
class MusicCardLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    private val cover: View by lazy { findViewById(R.id.musicCover) }
    private val source: View by lazy { findViewById(R.id.musicSource) }
    private val sourceIcon: View by lazy { findViewById(R.id.musicSourceIcon) }
    private val chevron: View by lazy { findViewById(R.id.musicSourceChevron) }
    private val title: TextView by lazy { findViewById(R.id.musicTitle) }
    private val rhythm: View by lazy { findViewById(R.id.musicRhythm) }
    private val artist: TextView by lazy { findViewById(R.id.musicArtist) }
    private val position: TextView by lazy { findViewById(R.id.musicPosition) }
    private val progress: View by lazy { findViewById(R.id.musicProgress) }
    private val duration: TextView by lazy { findViewById(R.id.musicDuration) }
    private val controls: List<View> by lazy {
        listOf(R.id.musicPlaylist, R.id.musicPrevious, R.id.musicPlay, R.id.musicNext, R.id.musicFavorite)
            .map { findViewById(it) }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            (w / 2.062f).roundToInt()
        } else {
            MeasureSpec.getSize(heightMeasureSpec)
        }
        setMeasuredDimension(w, h)
        if (w <= 0 || h <= 0) return
        val hf = h.toFloat()
        exact(cover, hf * COVER_SIZE, hf * COVER_SIZE)
        exact(source, hf * CHIP_WIDTH, hf * CHIP_HEIGHT)
        exact(sourceIcon, hf * CHIP_ICON, hf * CHIP_ICON)
        exact(chevron, hf * CHEVRON_BOX, hf * CHEVRON_BOX)
        exact(rhythm, hf * RHYTHM_BOX, hf * RHYTHM_BOX)
        title.sizePx(hf * TITLE_TEXT)
        artist.sizePx(hf * ARTIST_TEXT)
        position.sizePx(hf * TIME_TEXT)
        duration.sizePx(hf * TIME_TEXT)
        val titleMax = (w - hf * (CHIP_RIGHT + CHIP_WIDTH + TITLE_LEFT + RHYTHM_GAP + RHYTHM_BOX + 0.03f))
            .roundToInt().coerceAtLeast(0)
        title.measure(atMost(titleMax), unspecified())
        artist.measure(atMost((w - hf * (TITLE_LEFT + TIME_LEFT)).roundToInt().coerceAtLeast(0)), unspecified())
        position.measure(unspecified(), unspecified())
        duration.measure(unspecified(), unspecified())
        exact(progress, w - hf * (BAR_LEFT + BAR_RIGHT), hf * BAR_THICKNESS)
        controls.forEachIndexed { i, view ->
            val box = if (i == PLAY_INDEX) PLAY_BOX else CONTROL_BOX
            exact(view, w / 5f, hf * box)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val w = r - l
        val hf = (b - t).toFloat()
        val coverLeft = (hf * COVER_LEFT).roundToInt()
        val coverTop = (hf * COVER_TOP).roundToInt()
        cover.layout(coverLeft, coverTop, coverLeft + cover.measuredWidth, coverTop + cover.measuredHeight)

        val chipRight = (w - hf * CHIP_RIGHT).roundToInt()
        val chipTop = (hf * CHIP_TOP).roundToInt()
        val chipLeft = chipRight - source.measuredWidth
        source.layout(chipLeft, chipTop, chipRight, chipTop + source.measuredHeight)
        val chipCenterY = chipTop + source.measuredHeight / 2f
        val iconLeft = (chipLeft + hf * CHIP_ICON_INSET).roundToInt()
        val iconTop = (chipCenterY - sourceIcon.measuredHeight / 2f).roundToInt()
        sourceIcon.layout(iconLeft, iconTop, iconLeft + sourceIcon.measuredWidth, iconTop + sourceIcon.measuredHeight)
        val chevronLeft = (chipRight - hf * CHEVRON_FROM_RIGHT - chevron.measuredWidth / 2f).roundToInt()
        val chevronTop = (chipCenterY - chevron.measuredHeight / 2f).roundToInt()
        chevron.layout(chevronLeft, chevronTop, chevronLeft + chevron.measuredWidth, chevronTop + chevron.measuredHeight)

        val textLeft = (hf * TITLE_LEFT).roundToInt()
        placeByBaseline(title, textLeft, hf * TITLE_BASELINE)
        val rhythmLeft = (title.right + hf * RHYTHM_GAP).roundToInt()
        val rhythmBottom = (hf * TITLE_BASELINE).roundToInt()
        rhythm.layout(rhythmLeft, rhythmBottom - rhythm.measuredHeight, rhythmLeft + rhythm.measuredWidth, rhythmBottom)
        placeByBaseline(artist, textLeft, hf * ARTIST_BASELINE)

        placeByBaseline(position, (hf * TIME_LEFT).roundToInt(), hf * TIME_BASELINE)
        placeByBaseline(duration, (w - hf * TIME_LEFT).roundToInt() - duration.measuredWidth, hf * TIME_BASELINE)
        val barLeft = (hf * BAR_LEFT).roundToInt()
        val barTop = (hf * BAR_CENTER - progress.measuredHeight / 2f).roundToInt()
        progress.layout(barLeft, barTop, barLeft + progress.measuredWidth, barTop + progress.measuredHeight)

        val column = w / 5f
        controls.forEachIndexed { i, view ->
            val left = (column * i).roundToInt()
            val top = (hf * CONTROLS_CENTER - view.measuredHeight / 2f).roundToInt()
            view.layout(left, top, left + view.measuredWidth, top + view.measuredHeight)
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
            MeasureSpec.makeMeasureSpec(h.roundToInt().coerceAtLeast(0), MeasureSpec.EXACTLY),
        )
    }

    private fun atMost(size: Int) = MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST)

    private fun unspecified() = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)

    companion object {
        /** Cover corner radius as a fraction of the cover size. */
        const val COVER_CORNER = 0.104f
        private const val COVER_LEFT = 0.1026f
        private const val COVER_TOP = 0.1026f
        private const val COVER_SIZE = 0.346f
        private const val CHIP_RIGHT = 0.1047f
        private const val CHIP_TOP = 0.1026f
        private const val CHIP_WIDTH = 0.267f
        private const val CHIP_HEIGHT = 0.152f
        private const val CHIP_ICON = 0.1004f
        private const val CHIP_ICON_INSET = 0.0257f
        private const val CHEVRON_BOX = 0.0716f
        private const val CHEVRON_FROM_RIGHT = 0.0758f
        private const val TITLE_LEFT = 0.527f
        private const val TITLE_TEXT = 0.0994f
        private const val TITLE_BASELINE = 0.2534f
        private const val RHYTHM_BOX = 0.07f
        private const val RHYTHM_GAP = 0.012f
        private const val ARTIST_TEXT = 0.090f
        private const val ARTIST_BASELINE = 0.3803f
        private const val TIME_TEXT = 0.078f
        private const val TIME_LEFT = 0.103f
        private const val TIME_BASELINE = 0.5769f
        private const val BAR_LEFT = 0.3846f
        private const val BAR_RIGHT = 0.3867f
        private const val BAR_CENTER = 0.546f
        private const val BAR_THICKNESS = 0.0363f
        private const val CONTROLS_CENTER = 0.781f
        private const val CONTROL_BOX = 0.20f
        private const val PLAY_BOX = 0.2286f
        private const val PLAY_INDEX = 2
    }
}
