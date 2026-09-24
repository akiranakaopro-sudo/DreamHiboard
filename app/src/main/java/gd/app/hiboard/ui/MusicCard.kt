package gd.app.hiboard.ui

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.coui.appcompat.cardview.COUICardView
import gd.app.hiboard.R
import gd.app.hiboard.engine.MusicNow
import gd.app.hiboard.engine.MusicSessions
import gd.app.hiboard.engine.musicClock

/** Oppo music aggregate card: cover, source, progress, and transport controls. */
class MusicCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val backgroundView: ImageView
    private val scrim: View
    private val cover: ImageView
    private val source: View
    private val sourceIcon: ImageView
    private val titleView: TextView
    private val rhythm: MusicRhythmView
    private val artistView: TextView
    private val positionView: TextView
    private val progress: ProgressBar
    private val durationView: TextView
    private val playlist: ImageView
    private val previous: ImageView
    private val play: ImageView
    private val next: ImageView
    private val favorite: ImageView

    private var live = false
    private var raw = MusicNow.SAMPLE
    private var shown = MusicNow.SAMPLE
    private val liked = mutableSetOf<String>()
    private var artApplied = false
    private var appliedArt: Bitmap? = null
    private var sourceApplied = false
    private var appliedPackage: String? = null

    private val tick = object : Runnable {
        override fun run() {
            if (!live) return
            render(MusicSessions.current(context) ?: MusicNow.SAMPLE)
            val delay = if (shown.playing) 400L else 1000L
            postDelayed(this, delay)
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.card_music, this, true)
        backgroundView = findViewById(R.id.musicBackground)
        scrim = findViewById(R.id.musicScrim)
        cover = findViewById(R.id.musicCover)
        source = findViewById(R.id.musicSource)
        sourceIcon = findViewById(R.id.musicSourceIcon)
        titleView = findViewById(R.id.musicTitle)
        rhythm = findViewById(R.id.musicRhythm)
        artistView = findViewById(R.id.musicArtist)
        positionView = findViewById(R.id.musicPosition)
        progress = findViewById(R.id.musicProgress)
        durationView = findViewById(R.id.musicDuration)
        playlist = findViewById(R.id.musicPlaylist)
        previous = findViewById(R.id.musicPrevious)
        play = findViewById(R.id.musicPlay)
        next = findViewById(R.id.musicNext)
        favorite = findViewById(R.id.musicFavorite)
        cover.outlineProvider = roundOutline(8f)
        cover.clipToOutline = true
        sourceIcon.outlineProvider = roundOutline(8f)
        sourceIcon.clipToOutline = true
        titleView.isSelected = true
        render(MusicNow.SAMPLE)
    }

    fun setLive(enabled: Boolean) {
        live = enabled
        if (!enabled) {
            removeCallbacks(tick)
            render(MusicNow.SAMPLE)
            clearClicks()
            return
        }
        wireClicks()
        removeCallbacks(tick)
        if (isAttachedToWindow) post(tick)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (live) {
            removeCallbacks(tick)
            post(tick)
        }
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(tick)
        rhythm.playing = false
        super.onDetachedFromWindow()
    }

    private fun wireClicks() {
        val open = OnClickListener { openPlayer() }
        cover.setOnClickListener(open)
        titleView.setOnClickListener(open)
        artistView.setOnClickListener(open)
        playlist.setOnClickListener(open)
        source.setOnClickListener(open)
        previous.setOnClickListener { MusicSessions.previous(context) }
        next.setOnClickListener { MusicSessions.next(context) }
        play.setOnClickListener { MusicSessions.togglePlay(context) }
        favorite.setOnClickListener {
            val key = raw.title
            if (key in liked) liked.remove(key) else liked.add(key)
            render(raw)
        }
    }

    private fun clearClicks() {
        listOf(cover, titleView, artistView, playlist, source, previous, next, play, favorite)
            .forEach { it.setOnClickListener(null) }
    }

    private fun openPlayer() {
        launchIntent(this, MusicSessions.launch(context, shown.packageName))
    }

    private fun render(now: MusicNow) {
        raw = now
        val favoriteOn = now.favorite || now.title in liked
        shown = now.copy(favorite = favoriteOn)
        titleView.text = shown.title
        artistView.text = shown.artist
        positionView.text = musicClock(shown.positionMs)
        durationView.text = musicClock(shown.durationMs)
        progress.progress = if (shown.durationMs <= 0L) {
            0
        } else {
            ((shown.positionMs.coerceAtMost(shown.durationMs) * 1000L) / shown.durationMs).toInt()
        }
        play.setImageResource(if (shown.playing) R.drawable.ic_music_pause else R.drawable.ic_music_play)
        play.contentDescription = context.getString(if (shown.playing) R.string.music_pause else R.string.music_play)
        favorite.setImageResource(if (shown.favorite) R.drawable.ic_music_heart_on else R.drawable.ic_music_heart)
        rhythm.playing = shown.playing
        bindArt(shown.art)
        bindSource(shown.packageName)
    }

    private fun bindArt(art: Bitmap?) {
        if (artApplied && art === appliedArt) return
        artApplied = true
        appliedArt = art
        if (art == null) {
            backgroundView.setImageResource(R.drawable.bg_music_card)
            clearBlur()
            cover.setImageResource(R.drawable.cover_music_sample)
            scrim.setBackgroundColor(0x14000000)
            return
        }
        backgroundView.setImageBitmap(art)
        blurBackground()
        cover.setImageBitmap(art)
        scrim.setBackgroundColor(0x33000000)
    }

    private fun bindSource(packageName: String?) {
        if (sourceApplied && packageName == appliedPackage) return
        sourceApplied = true
        appliedPackage = packageName
        val icon = packageName?.let { name ->
            try {
                context.packageManager.getApplicationIcon(name)
            } catch (_: PackageManager.NameNotFoundException) {
                null
            }
        }
        if (icon == null) sourceIcon.setImageResource(R.drawable.ic_music_source)
        else sourceIcon.setImageDrawable(icon)
    }

    private fun blurBackground() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val radius = 28f * resources.displayMetrics.density
        backgroundView.setRenderEffect(RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP))
    }

    private fun clearBlur() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        backgroundView.setRenderEffect(null)
    }

    private fun roundOutline(radiusDp: Float) = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val radius = radiusDp * resources.displayMetrics.density
            outline.setRoundRect(0, 0, view.width, view.height, radius)
        }
    }
}

/** Three bars beside the title while a track is playing. */
class MusicRhythmView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xE6FFFFFF.toInt() }
    var playing: Boolean = false
        set(value) {
            field = value
            if (value) invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        val gap = width * 0.18f
        val bar = (width - gap * 2f) / 3f
        val phase = (System.currentTimeMillis() % 900L) / 900f
        for (index in 0 until 3) {
            val wave = if (playing) {
                val shifted = (phase + index * 0.22f) % 1f
                val peak = if (shifted < 0.5f) shifted * 2f else (1f - shifted) * 2f
                0.35f + 0.65f * peak
            } else {
                0.45f
            }
            val top = height * (1f - wave) * 0.72f
            val left = index * (bar + gap)
            val radius = bar / 2f
            canvas.drawRoundRect(left, top, left + bar, height.toFloat(), radius, radius, paint)
        }
        if (playing) postInvalidateOnAnimation()
    }
}

fun bindMusicCard(card: COUICardView, body: LinearLayout, live: Boolean) {
    card.setCardBackgroundColor(0xFF6E5338.toInt())
    card.setContentPadding(0, 0, 0, 0)
    card.clipToOutline = true
    val music = MusicCardView(body.context)
    music.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT,
    )
    body.addView(music)
    music.setLive(live)
}
