package it.phoneaudio.bridge

import android.service.notification.NotificationListenerService

/**
 * Does nothing on its own: notification access is the permission Android requires
 * to read and control the media sessions of other apps (Spotify, YouTube, ...).
 */
class MediaAccessService : NotificationListenerService()
