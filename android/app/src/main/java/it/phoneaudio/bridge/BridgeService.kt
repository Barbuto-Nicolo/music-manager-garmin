package it.phoneaudio.bridge

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice
import com.garmin.android.connectiq.exception.InvalidStateException
import com.garmin.android.connectiq.exception.ServiceUnavailableException

/**
 * Stays alive in the foreground: receives commands from the watch via Connect IQ,
 * runs them on the phone's player and sends the state back (track, play/pause, volume).
 */
class BridgeService : Service() {

    companion object {
        const val WATCH_APP_ID = "8f1b2c3d4e5f4a6b9c7d0e1f2a3b4c5d"
        private const val TAG = "PhoneAudioBridge"
        private const val CHANNEL = "bridge"
        private const val STATE_PUSH_MS = 2000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var media: PhoneMedia
    private lateinit var ciq: ConnectIQ
    private val watchApp = IQApp(WATCH_APP_ID)
    private val devices = mutableListOf<IQDevice>()
    private var sdkReady = false
    private var lastSent: Map<String, Any>? = null

    private val pushState = object : Runnable {
        override fun run() {
            pushStateIfChanged()
            handler.postDelayed(this, STATE_PUSH_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        media = PhoneMedia(this)
        startInForeground()

        ciq = ConnectIQ.getInstance(this, ConnectIQ.IQConnectType.WIRELESS)
        ciq.initialize(this, true, object : ConnectIQ.ConnectIQListener {
            override fun onSdkReady() {
                sdkReady = true
                registerDevices()
            }

            override fun onInitializeError(status: ConnectIQ.IQSdkErrorStatus) {
                Log.e(TAG, "Connect IQ init error: $status")
            }

            override fun onSdkShutDown() { sdkReady = false }
        })
        handler.postDelayed(pushState, STATE_PUSH_MS)
    }

    private fun registerDevices() {
        try {
            devices.clear()
            devices.addAll(ciq.knownDevices.orEmpty())
            for (device in devices) {
                ciq.registerForDeviceEvents(device) { _, _ -> }
                ciq.registerForAppEvents(device, watchApp) { dev, _, message, status ->
                    if (status == ConnectIQ.IQMessageStatus.SUCCESS) {
                        message.orEmpty().forEach { handleMessage(dev, it) }
                    }
                }
            }
        } catch (e: InvalidStateException) {
            Log.e(TAG, "registerDevices", e)
        } catch (e: ServiceUnavailableException) {
            Log.e(TAG, "Garmin Connect not available", e)
        }
    }

    private fun handleMessage(device: IQDevice, payload: Any?) {
        val cmd = (payload as? Map<*, *>)?.get("cmd") as? String ?: return
        handler.post {
            when (cmd) {
                "playpause" -> media.playPause()
                "next" -> media.next()
                "prev" -> media.previous()
                "volup" -> media.volumeUp()
                "voldown" -> media.volumeDown()
                "sync" -> lastSent = null
            }
            // give Android time to update the session, then send the state
            handler.postDelayed({ pushStateIfChanged() }, 250)
        }
    }

    private fun pushStateIfChanged() {
        if (!sdkReady) return
        val state = media.snapshot()
        if (state == lastSent) return
        lastSent = state
        for (device in devices) {
            try {
                ciq.sendMessage(device, watchApp, state) { _, _, _ -> }
            } catch (e: Exception) {
                Log.w(TAG, "sendMessage", e)
            }
        }
    }

    private fun startInForeground() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Watch bridge", NotificationManager.IMPORTANCE_MIN)
        )
        val notification = Notification.Builder(this, CHANNEL)
            .setContentTitle("Phone Audio Bridge attivo")
            .setContentText("Audio control from the Garmin watch")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        try { ciq.shutdown(this) } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
