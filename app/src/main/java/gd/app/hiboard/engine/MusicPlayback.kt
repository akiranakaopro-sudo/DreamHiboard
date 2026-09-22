package gd.app.hiboard.engine

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.Rating
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import android.view.KeyEvent
import java.util.Locale

/** Now-playing row for the music aggregate card. */
data class MusicNow(
    val title: String,
    val artist: String,
    val playing: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    val favorite: Boolean,
    val art: Bitmap?,
    val packageName: String?,
) {
    companion object {
        val SAMPLE = MusicNow(
            title = "跟世界说晚安",
            artist = "胡彦斌 - 前途无量",
            playing = false,
            positionMs = 0L,
            durationMs = 0L,
            favorite = false,
            art = null,
            packageName = null,
        )
    }
}

fun musicClock(positionMs: Long): String {
    val total = (positionMs.coerceAtLeast(0L) / 1000L).toInt()
    val seconds = total % 60
    val minutes = total / 60
    return if (minutes >= 60) {
        String.format(Locale.US, "%d:%02d:%02d", minutes / 60, minutes % 60, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

object MusicSessions {
    private val players = listOf(
        "com.heytap.music",
        "com.tencent.qqmusic",
        "com.netease.cloudmusic",
        "com.kugou.android",
        "cn.kuwo.player",
        "com.google.android.apps.youtube.music",
        "com.android.music",
    )

    fun current(context: Context): MusicNow? {
        val controller = controller(context) ?: return null
        val metadata = controller.metadata
        val state = controller.playbackState
        val title = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE).orEmpty()
        if (title.isBlank() && state == null) return null
        val artist = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST).orEmpty()
        val album = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ALBUM).orEmpty()
        val subtitle = listOf(artist, album).filter { it.isNotBlank() }.joinToString(" - ")
        val duration = metadata?.getLong(android.media.MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val art = metadata?.getBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(android.media.MediaMetadata.METADATA_KEY_ART)
        return MusicNow(
            title = title.ifBlank { MusicNow.SAMPLE.title },
            artist = subtitle.ifBlank { MusicNow.SAMPLE.artist },
            playing = state.isActive(),
            positionMs = state.extrapolatedPosition(),
            durationMs = duration.coerceAtLeast(0L),
            favorite = metadata.isFavorite(),
            art = art,
            packageName = controller.packageName,
        )
    }

    fun togglePlay(context: Context) {
        val controller = controller(context)
        val state = controller?.playbackState
        if (controller != null && state != null) {
            if (state.isActive()) controller.transportControls.pause()
            else controller.transportControls.play()
            return
        }
        dispatch(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    }

    fun previous(context: Context) {
        controller(context)?.transportControls?.skipToPrevious()
            ?: dispatch(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }

    fun next(context: Context) {
        controller(context)?.transportControls?.skipToNext()
            ?: dispatch(context, KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    fun launch(context: Context, packageName: String?): Intent? {
        val manager = context.packageManager
        if (!packageName.isNullOrBlank()) {
            manager.getLaunchIntentForPackage(packageName)?.let { return it }
        }
        val category = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC)
        if (manager.resolveActivity(category, PackageManager.MATCH_DEFAULT_ONLY) != null) return category
        players.forEach { name ->
            manager.getLaunchIntentForPackage(name)?.let { return it }
        }
        return null
    }

    private fun controller(context: Context): MediaController? {
        val manager = context.getSystemService(MediaSessionManager::class.java) ?: return null
        val listener = ComponentName(context, MusicNotificationListener::class.java)
        val sessions = sessions(manager, null) ?: sessions(manager, listener) ?: return null
        return sessions.firstOrNull { it.playbackState.isActive() } ?: sessions.firstOrNull()
    }

    private fun sessions(manager: MediaSessionManager, listener: ComponentName?): List<MediaController>? {
        return try {
            manager.getActiveSessions(listener)
        } catch (_: SecurityException) {
            null
        }
    }

    private fun dispatch(context: Context, keyCode: Int) {
        val audio = context.getSystemService(AudioManager::class.java) ?: return
        val now = SystemClock.uptimeMillis()
        audio.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0))
        audio.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0))
    }
}

private fun PlaybackState?.isActive(): Boolean {
    val state = this?.state ?: return false
    return state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING
}

private fun PlaybackState?.extrapolatedPosition(): Long {
    val state = this ?: return 0L
    if (!state.isActive()) return state.position.coerceAtLeast(0L)
    val speed = if (state.playbackSpeed > 0f) state.playbackSpeed else 1f
    val elapsed = ((SystemClock.elapsedRealtime() - state.lastPositionUpdateTime) * speed).toLong()
    return (state.position + elapsed).coerceAtLeast(0L)
}

private fun android.media.MediaMetadata?.isFavorite(): Boolean {
    val rating = this?.getRating(android.media.MediaMetadata.METADATA_KEY_USER_RATING)
        ?: this?.getRating(android.media.MediaMetadata.METADATA_KEY_RATING)
        ?: return false
    if (!rating.isRated) return false
    return when (rating.ratingStyle) {
        Rating.RATING_HEART -> rating.hasHeart()
        Rating.RATING_THUMB_UP_DOWN -> rating.isThumbUp
        Rating.RATING_3_STARS, Rating.RATING_4_STARS, Rating.RATING_5_STARS -> rating.starRating > 0f
        else -> false
    }
}
