package com.aryaxzell.reminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aryaxzell.reminder.ReminderApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

            scope.launch {
                try {
                    val app = context.applicationContext as? ReminderApplication
                    val database = app?.database ?: return@launch
                    val dao = database.reminderDao()
                    val pendingReminders = dao.getPendingRemindersWithDueDate()
                    val now = System.currentTimeMillis()

                    for (reminder in pendingReminders) {
                        val dueDate = reminder.dueDate
                        if (dueDate != null && dueDate > now) {
                            ReminderAlarmScheduler.schedule(context, reminder)
                        }
                    }
                } catch (_: Exception) {
                    // Fail gracefully on background reschedule error
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
