package com.aryaxzell.reminder

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aryaxzell.reminder.data.Reminder
import com.aryaxzell.reminder.data.ReminderDao
import com.aryaxzell.reminder.data.ReminderDatabase
import com.aryaxzell.reminder.data.ReminderList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReminderDaoTest {

    private lateinit var database: ReminderDatabase
    private lateinit var dao: ReminderDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ReminderDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.reminderDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveList() = runBlocking {
        val list = ReminderList(name = "Work", colorHex = "#007AFF", iconName = "briefcase")
        val listId = dao.insertList(list)

        val allLists = dao.getAllLists().first()
        assertTrue(allLists.any { it.name == "Work" && it.id == listId.toInt() })
    }

    @Test
    fun insertAndRetrieveReminder() = runBlocking {
        val listId = dao.insertList(ReminderList(name = "Personal", colorHex = "#FF9500", iconName = "list")).toInt()
        val reminder = Reminder(
            listId = listId,
            title = "Buy Groceries",
            notes = "Milk, Eggs, Bread",
            dueDate = 1700000000000L,
            timeOfDay = "Morning",
            isFlagged = true
        )
        val reminderId = dao.insertReminder(reminder)

        val retrieved = dao.getReminderById(reminderId.toInt())
        assertNotNull(retrieved)
        assertEquals("Buy Groceries", retrieved?.title)
        assertEquals("Milk, Eggs, Bread", retrieved?.notes)
        assertEquals(true, retrieved?.isFlagged)
    }

    @Test
    fun updateReminderCompletion() = runBlocking {
        val reminder = Reminder(listId = 1, title = "Task to complete", isCompleted = false)
        val id = dao.insertReminder(reminder).toInt()

        dao.updateReminder(reminder.copy(id = id, isCompleted = true))
        val updated = dao.getReminderById(id)
        assertEquals(true, updated?.isCompleted)
    }

    @Test
    fun deleteReminder() = runBlocking {
        val reminder = Reminder(listId = 1, title = "Temporary Task")
        val id = dao.insertReminder(reminder).toInt()

        val item = dao.getReminderById(id)
        assertNotNull(item)
        item?.let { dao.deleteReminder(it) }

        val afterDelete = dao.getReminderById(id)
        assertNull(afterDelete)
    }

    @Test
    fun getPendingRemindersWithDueDate() = runBlocking {
        val now = System.currentTimeMillis()
        val pending1 = Reminder(listId = 1, title = "Future 1", dueDate = now + 10000, isCompleted = false)
        val pending2 = Reminder(listId = 1, title = "Future 2", dueDate = now + 20000, isCompleted = false)
        val completed = Reminder(listId = 1, title = "Completed Future", dueDate = now + 30000, isCompleted = true)
        val noDueDate = Reminder(listId = 1, title = "No Due Date", dueDate = null, isCompleted = false)

        dao.insertReminder(pending1)
        dao.insertReminder(pending2)
        dao.insertReminder(completed)
        dao.insertReminder(noDueDate)

        val results = dao.getPendingRemindersWithDueDate()
        assertEquals(2, results.size)
        assertTrue(results.any { it.title == "Future 1" })
        assertTrue(results.any { it.title == "Future 2" })
    }
}
