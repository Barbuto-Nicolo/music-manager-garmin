package it.phoneaudio.bridge

import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState

/** Controls the active player on the phone, whichever app it is. */
class PhoneMedia(private val context: Context) {

    private val sessions = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val listener = ComponentName(context, MediaAccessService::class.java)

    /** Prefers the player that is playing, otherwise the most recent one. */
    fun activeController(): MediaController? = try {
        val all = sessions.getActiveSessions(listener)
        all.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING } ?: all.firstOrNull()
    } catch (e: SecurityException) {
        null // notification access not granted
    }

    fun hasAccess(): Boolean = try {
        sessions.getActiveSessions(listener); true
    } catch (e: SecurityException) {
        false
    }

    fun playPause() {
        val c = activeController() ?: return
        if (c.playbackState?.state == PlaybackState.STATE_PLAYING) c.transportControls.pause()
        else c.transportControls.play()
    }

    fun next() { activeController()?.transportControls?.skipToNext() }

    fun previous() { activeController()?.transportControls?.skipToPrevious() }

    fun volumeUp() = audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)

    fun volumeDown() = audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0)

    fun volumePercent(): Int {
        val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        return audio.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / max
    }

    /** State to send to the watch. */
    fun snapshot(): HashMap<String, Any> {
        val c = activeController()
        val meta = c?.metadata
        return hashMapOf(
            "title" to (meta?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""),
            "artist" to (meta?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""),
            "playing" to (c?.playbackState?.state == PlaybackState.STATE_PLAYING),
            "vol" to volumePercent(),
        )
    }
}
