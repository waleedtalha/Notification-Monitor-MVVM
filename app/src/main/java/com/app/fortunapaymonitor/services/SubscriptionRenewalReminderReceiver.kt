package com.app.fortunapaymonitor.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.fortunapaymonitor.R
import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper
import com.app.fortunapaymonitor.ui.activity.MainActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class SubscriptionRenewalReminderReceiver : BroadcastReceiver(), KoinComponent {

    private val prefs: PreferenceHelper by inject()
    override fun onReceive(context: Context, intent: Intent) {
        showRenewalNotification(context)
        prefs.subscriptionExpired = true
    }

    @SuppressLint("MissingPermission")
    private fun showRenewalNotification(context: Context) {
        // Create an Intent that launches your main activity (modify this as necessary)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Create the PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val builder = NotificationCompat.Builder(context, "notify_channel_id")
            .setSmallIcon(R.mipmap.ic_launcher_app_round)
            .setContentTitle(context.getString(R.string.renewal_reminder))
            .setContentText(context.getString(R.string.renewal_reminder_desc))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)  // Set the PendingIntent into the notification
            .setAutoCancel(true)  // Auto-cancel the notification when it's tapped

        // Issue the notification
        with(NotificationManagerCompat.from(context)) {
            notify(0, builder.build())  // Ensure the ID is unique if multiple notifications can be present
        }
    }
}