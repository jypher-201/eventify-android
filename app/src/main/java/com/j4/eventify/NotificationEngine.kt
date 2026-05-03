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
        if (event.remindBeforeMinutes.isEmpty()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // ── Loop through every reminder the user selected ──
        event.remindBeforeMinutes.forEach { minutes ->

            // Build a human-readable string for the notification text
            val timeText = when {
                minutes >= 60 * 24 -> "${minutes / (60 * 24)} days"
                minutes >= 60 -> "${minutes / 60} hours"
                else -> "$minutes minutes"
            }

            val intent = Intent(context, EventNotificationReceiver::class.java).apply {
                putExtra("EVENT_ID", event.id)
                putExtra("EVENT_TITLE", event.title)
                putExtra("EVENT_DESC", "Starting in $timeText!")
            }

            // ── THE FIX: Create a totally unique ID for this specific alarm! ──
            // If we just use event.id, they overwrite each other.
            // Multiplying by 10000 and adding the minutes creates a unique fingerprint.
            val uniqueAlarmCode = (event.id * 10000) + minutes

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                uniqueAlarmCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Calculate the exact millisecond to fire this specific alarm
            val triggerTimeMs = event.timestamp - (minutes * 60 * 1000L)

            // Only set alarms for the future
            if (triggerTimeMs > System.currentTimeMillis()) {
                try {
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTimeMs, pendingIntent)
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                } catch (e: SecurityException) {
                    // Failsafe
                }
            }
        }
    }
}