package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    // Lists
    @Query("SELECT * FROM reminder_lists ORDER BY sortOrder ASC, id ASC")
    fun getAllLists(): Flow<List<ReminderList>>

    @Query("SELECT * FROM reminder_lists WHERE id = :id LIMIT 1")
    suspend fun getListById(id: Int): ReminderList?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ReminderList): Long

    @Update
    suspend fun updateList(list: ReminderList)

    @Delete
    suspend fun deleteList(list: ReminderList)

    @Query("DELETE FROM reminder_lists WHERE id = :id")
    suspend fun deleteListById(id: Int)

    // Reminders
    @Query("SELECT * FROM reminders ORDER BY creationTime DESC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE listId = :listId ORDER BY creationTime DESC")
    fun getRemindersForList(listId: Int): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Int): Reminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Int)

    // Smart lists custom helper queries or simple filtering in flow / code
    @Query("SELECT * FROM reminders WHERE isCompleted = 1 ORDER BY creationTime DESC")
    fun getCompletedReminders(): Flow<List<Reminder>>
}
