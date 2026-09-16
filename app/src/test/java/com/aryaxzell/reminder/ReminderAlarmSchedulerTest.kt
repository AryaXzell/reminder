package com.aryaxzell.reminder

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.aryaxzell.reminder.data.Reminder
import com.aryaxzell.reminder.notification.ReminderAlarmScheduler
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReminderAlarmSchedulerTest {

    @Test
    fun testScheduleAndCancelDoesNotThrow() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val futureTime = System.currentTimeMillis() + 60000

        val reminder = Reminder(
            id = 42,
            listId = 1,
            title = "Alarm Test Reminder",
            notes = "Test notes",
            dueDate = futureTime,
            isCompleted = false
        )

        // Schedule should succeed without exception
        ReminderAlarmScheduler.schedule(context, reminder)

        // Cancel should succeed without exception
        ReminderAlarmScheduler.cancel(context, reminder.id)

        assertNotNull(context.getSystemService(Context.ALARM_SERVICE))
    }
}
