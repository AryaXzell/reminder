package com.example.data

import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {
    val allLists: Flow<List<ReminderList>> = reminderDao.getAllLists()
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()
    val completedReminders: Flow<List<Reminder>> = reminderDao.getCompletedReminders()

    fun getRemindersForList(listId: Int): Flow<List<Reminder>> {
        return reminderDao.getRemindersForList(listId)
    }

    suspend fun getListById(id: Int): ReminderList? {
        return reminderDao.getListById(id)
    }

    suspend fun getReminderById(id: Int): Reminder? {
        return reminderDao.getReminderById(id)
    }

    suspend fun insertList(list: ReminderList): Long {
        return reminderDao.insertList(list)
    }

    suspend fun updateList(list: ReminderList) {
        reminderDao.updateList(list)
    }

    suspend fun deleteList(list: ReminderList) {
        reminderDao.deleteList(list)
    }

    suspend fun deleteListById(id: Int) {
        reminderDao.deleteListById(id)
    }

    suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun deleteReminderById(id: Int) {
        reminderDao.deleteReminderById(id)
    }
}
