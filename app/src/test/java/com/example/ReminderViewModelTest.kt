package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.Reminder
import com.example.data.ReminderDao
import com.example.data.ReminderList
import com.example.data.ReminderRepository
import com.example.data.SmartListType
import com.example.ui.ReminderViewModel
import com.example.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReminderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var app: Application

    // Fake Dao for predictable ViewModel unit tests
    private class FakeReminderDao : ReminderDao {
        val listsFlow = MutableStateFlow<List<ReminderList>>(emptyList())
        val remindersFlow = MutableStateFlow<List<Reminder>>(emptyList())

        override fun getAllLists(): Flow<List<ReminderList>> = listsFlow
        override suspend fun getListById(id: Int): ReminderList? = listsFlow.value.find { it.id == id }
        override suspend fun insertList(list: ReminderList): Long {
            val nextId = (listsFlow.value.maxOfOrNull { it.id } ?: 0) + 1
            val item = list.copy(id = nextId)
            listsFlow.value = listsFlow.value + item
            return nextId.toLong()
        }
        override suspend fun updateList(list: ReminderList) {
            listsFlow.value = listsFlow.value.map { if (it.id == list.id) list else it }
        }
        override suspend fun deleteList(list: ReminderList) {
            listsFlow.value = listsFlow.value.filter { it.id != list.id }
        }
        override fun getAllReminders(): Flow<List<Reminder>> = remindersFlow
        override fun getRemindersByListId(listId: Int): Flow<List<Reminder>> = MutableStateFlow(remindersFlow.value.filter { it.listId == listId })
        override suspend fun getReminderById(id: Int): Reminder? = remindersFlow.value.find { it.id == id }
        override suspend fun insertReminder(reminder: Reminder): Long {
            val nextId = (remindersFlow.value.maxOfOrNull { it.id } ?: 0) + 1
            val item = reminder.copy(id = nextId)
            remindersFlow.value = remindersFlow.value + item
            return nextId.toLong()
        }
        override suspend fun updateReminder(reminder: Reminder) {
            remindersFlow.value = remindersFlow.value.map { if (it.id == reminder.id) reminder else it }
        }
        override suspend fun deleteReminder(reminder: Reminder) {
            remindersFlow.value = remindersFlow.value.filter { it.id != reminder.id }
        }
        override suspend fun getPendingRemindersWithDueDate(now: Long): List<Reminder> {
            return remindersFlow.value.filter { !it.isCompleted && it.dueDate != null && it.dueDate > now }
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        app = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSmartListDerivedCountsAndFiltering() = runTest(testDispatcher) {
        val fakeDao = FakeReminderDao()
        val repo = ReminderRepository(fakeDao)

        val cal = Calendar.getInstance()
        val todayMillis = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 2)
        val futureMillis = cal.timeInMillis

        val r1 = Reminder(id = 1, title = "Today Task", dueDate = todayMillis, isCompleted = false, isFlagged = true)
        val r2 = Reminder(id = 2, title = "Scheduled Future Task", dueDate = futureMillis, isCompleted = false, isFlagged = false)
        val r3 = Reminder(id = 3, title = "Completed Task", dueDate = todayMillis, isCompleted = true, isFlagged = false)
        val r4 = Reminder(id = 4, title = "No Date Flagged Task", isCompleted = false, isFlagged = true)

        fakeDao.remindersFlow.value = listOf(r1, r2, r3, r4)

        val viewModel = ReminderViewModel(app, repo)
        advanceUntilIdle()

        val counts = viewModel.smartListCounts.first()
        assertEquals(1, counts[SmartListType.TODAY]) // r1
        assertEquals(2, counts[SmartListType.SCHEDULED]) // r1 and r2
        assertEquals(3, counts[SmartListType.ALL]) // r1, r2, r4
        assertEquals(2, counts[SmartListType.FLAGGED]) // r1, r4
        assertEquals(1, counts[SmartListType.COMPLETED]) // r3

        val todayReminders = viewModel.getSmartListReminders(SmartListType.TODAY).first()
        assertEquals(1, todayReminders.size)
        assertEquals("Today Task", todayReminders[0].title)

        val completedReminders = viewModel.getSmartListReminders(SmartListType.COMPLETED).first()
        assertEquals(1, completedReminders.size)
        assertEquals("Completed Task", completedReminders[0].title)
    }
}
