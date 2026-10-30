package it.phoneaudio.bridge

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class MainActivity : Activity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (20 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            gravity = Gravity.TOP
        }
        status = TextView(this).apply { textSize = 16f }
        root.addView(status)

        fun button(label: String, action: () -> Unit) = root.addView(Button(this).apply {
            text = label
            setOnClickListener { action() }
        })

        button("1. Grant notification access") {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        button("2. Start the bridge") { startBridge(true) }
        button("Stop the bridge") { startBridge(false) }
        button("Battery optimization exemption") {
            startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    .setData(Uri.parse("package:$packageName"))
            )
        }
        setContentView(root)

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        if (Build.VERSION.SDK_INT >= 31 &&
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 2)
    }

    override fun onResume() {
        super.onResume()
        val access = PhoneMedia(this).hasAccess()
        status.text = "Media control access: " + (if (access) "OK" else "NOT granted") +
            "\n\nRequires Garmin Connect installed and the watch paired." +
            "\nOn the watch, open the \"Phone Audio\" app."
    }

    private fun startBridge(start: Boolean) {
        getSharedPreferences("prefs", MODE_PRIVATE).edit().putBoolean("autostart", start).apply()
        val intent = Intent(this, BridgeService::class.java)
        if (start) ContextCompat.startForegroundService(this, intent) else stopService(intent)
    }
}
