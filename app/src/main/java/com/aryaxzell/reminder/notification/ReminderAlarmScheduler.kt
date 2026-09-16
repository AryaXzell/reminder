package com.aryaxzell.reminder.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.aryaxzell.reminder.data.Reminder

object ReminderAlarmScheduler {

    fun schedule(context: Context, reminder: Reminder) {
        val dueDate = reminder.dueDate ?: return
        if (reminder.isCompleted) {
            cancel(context, reminder.id)
            return
        }

        // Only schedule for future dates
        if (dueDate <= System.currentTimeMillis()) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
            putExtra(ReminderNotificationReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderNotificationReceiver.EXTRA_REMINDER_TITLE, reminder.title)
            putExtra(ReminderNotificationReceiver.EXTRA_REMINDER_NOTES, reminder.notes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueDate, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueDate, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueDate, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, dueDate, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.e("ReminderAlarmScheduler", "Exact alarm permission denied, fallback to setAndAllowWhileIdle", e)
            alarmManager.set(AlarmManager.RTC_WAKEUP, dueDate, pendingIntent)
        }
    }

    fun cancel(context: Context, reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
