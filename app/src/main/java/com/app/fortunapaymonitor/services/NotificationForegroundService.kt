package com.app.fortunapaymonitor.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.app.fortunapaymonitor.R
import com.app.fortunapaymonitor.utils.extensions.NOTIFICATION_TEXT
import com.app.fortunapaymonitor.utils.extensions.NOTIFICATION_TITLE
import com.app.fortunapaymonitor.viewmodel.AuthViewModel
import org.koin.android.ext.android.inject

class NotificationForegroundService : Service() {
    private val authViewModel: AuthViewModel by inject()
    private var wakeLock: PowerManager.WakeLock? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)

        // Acquire WakeLock
        acquireWakeLock()

        // Handle any intents that started the service here if needed
        processIntent(intent)

        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification {
        val notificationChannelId = "notify_channel_id"
        val channelName = "apps_monitoring"
        val channel = NotificationChannel(
            notificationChannelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_app_round)
            .setContentTitle(getString(R.string.apps_monitoring))
            .setContentText(getString(R.string.apps_monitoring_desc))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)

        return notificationBuilder.build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
        stopForeground(true)
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "App:MyWakeLockTag")
        wakeLock?.acquire(10 * 60 * 1000L /*10 minutes*/)
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    private fun processIntent(intent: Intent?) {
        intent?.let {
            val title = it.getStringExtra(NOTIFICATION_TITLE)
            val text = it.getStringExtra(NOTIFICATION_TEXT)
            if (title != null && text != null) {
                val textToSend = "Titulo: $title \nMensagem: $text"
                Log.wtf("textToSend", textToSend)
                //call api
                authViewModel.sendMessageToWP(textToSend)
            }
        }
        // Release WakeLock after processing is complete
        releaseWakeLock()
    }
}