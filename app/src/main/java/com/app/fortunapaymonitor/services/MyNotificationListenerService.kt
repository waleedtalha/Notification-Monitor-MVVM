package com.app.fortunapaymonitor.services

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper
import com.app.fortunapaymonitor.utils.extensions.NOTIFICATION_TEXT
import com.app.fortunapaymonitor.utils.extensions.NOTIFICATION_TITLE
import com.app.fortunapaymonitor.utils.extensions.toast
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MyNotificationListenerService : NotificationListenerService(),KoinComponent {
    private val pref: PreferenceHelper by inject()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val appPackages = pref.enabledAppList
        val packageName = sbn?.packageName
        if (appPackages.contains(packageName)){
            val notification = sbn?.notification
            val extras = notification?.extras
            val title = extras?.getString("android.title")
            val text = extras?.getCharSequence("android.text").toString()

//            Log.wtf("NotificationListener", "Notification from $packageName - Title: $title, Text: $text")
            if (title != null && text != null) {
                if ((title.contains("pix", ignoreCase = true) || text.contains("pix", ignoreCase = true)) &&
                    (title.contains("R$", ignoreCase = true) || text.contains("R$", ignoreCase = true))) {
                    if (!(title.contains("enviou", ignoreCase = true) || text.contains("enviou", ignoreCase = true) ||
                                title.contains("enviado", ignoreCase = true) || text.contains("enviado", ignoreCase = true))) {

                        if (pref.monitoringTime24 == true){
                            sendDataToActivity(title,text)
                        }else{
                            if (isCurrentTimeWithinUserSettings()) {
                                sendDataToActivity(title,text)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Handle notification removal if needed
    }
    private fun sendDataToActivity(title:String,text:String) {
        val sendLevel = Intent(this, NotificationForegroundService::class.java)
        sendLevel.putExtra(NOTIFICATION_TITLE, title)
        sendLevel.putExtra(NOTIFICATION_TEXT, text)
        startService(sendLevel)
    }




    private fun isCurrentTimeWithinUserSettings(): Boolean {
        val currentCalendar = Calendar.getInstance()
        val currentDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK)
        val adjustedDayOfWeek = (currentDayOfWeek - 1) % 7  // Adjusting Sunday from 1 to 0, Monday from 2 to 1, etc.

        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentCalendar.time)

        // Check if today is one of the selected days
        if (pref.selectedDaysList.contains(adjustedDayOfWeek.toString())) {
            val fromTime = pref.fromTime ?: "00:00"
            val toTime = pref.toTime ?: "23:00"

            // Parse current, from, and to times for comparison
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentTimeParsed = timeFormat.parse(currentTime)
            val fromTimeParsed = timeFormat.parse(fromTime)
            val toTimeParsed = timeFormat.parse(toTime)

            if (currentTimeParsed != null && fromTimeParsed != null && toTimeParsed != null) {
                return if (fromTimeParsed.before(toTimeParsed)) {
                    currentTimeParsed.after(fromTimeParsed) && currentTimeParsed.before(toTimeParsed)
                } else {
                    currentTimeParsed.after(fromTimeParsed) || currentTimeParsed.before(toTimeParsed)
                }
            }
        }
        return false
    }


    private fun normalizeTime(time: String): String {
        return time.removeSuffix("am").removeSuffix("pm")
    }


    private fun convertTo24HourFormat(time: String): String {
        // Ensure the time string includes minutes
        val standardizedTime = if (time.matches(Regex("\\d{1,2}[aApP][mM]"))) {
            // Add ":00" for times without minutes
            time.replace(Regex("(\\d{1,2})([aApP][mM])"), "$1:00$2")
        } else {
            time
        }

        val timeFormat12 = SimpleDateFormat("hh:mma", Locale.getDefault())
        val timeFormat24 = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = timeFormat12.parse(standardizedTime)
        return if (date != null) timeFormat24.format(date) else time
    }

    private fun toTimeConverter(toTime:String):String{
        return if (toTime == "12pm") {
            "11:59pm"
        } else {
            toTime
        }
    }



}