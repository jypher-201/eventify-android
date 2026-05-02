package com.j4.eventify

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.j4.eventify.data.local.EventEntity

// 1. The Receiver: This wakes up when the alarm goes off and builds the visual notification
class EventNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EVENT_TITLE") ?: "Event Reminder"
        val desc = intent.getStringExtra("EVENT_DESC") ?: "Your event is starting soon!"
        val notificationId = intent.getIntExtra("EVENT_ID", 0)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8+ requires a "Channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "EVENTIFY_CHANNEL", "Event Reminders", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notifications for upcoming events" }
            notificationManager.createNotificationChannel(channel)
        }

        // Build the actual push notification
        val builder = NotificationCompat.Builder(context, "EVENTIFY_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // Replace with your own app icon later!
            .setContentTitle(title)
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
    }
}

// 2. The Scheduler: This does the math and sets the exact Android Alarm
class EventAlarmScheduler(private val context: Context) {
    fun schedule(event: EventEntity) {
        if (event.remindBeforeMinutes == null) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, EventNotificationReceiver::class.java).apply {
            putExtra("EVENT_ID", event.id)
            putExtra("EVENT_TITLE", event.title)
            putExtra("EVENT_DESC", "Starting in ${event.remindBeforeMinutes} minutes!")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, event.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate the exact millisecond to fire the alarm
        val triggerTimeMs = event.timestamp - (event.remindBeforeMinutes * 60 * 1000L)

        // Only set alarms for the future
        if (triggerTimeMs > System.currentTimeMillis()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            } catch (e: SecurityException) {
                // Failsafe if exact alarms are disabled globally by the user
            }
        }
    }
}