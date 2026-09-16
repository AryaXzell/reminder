package com.aryaxzell.reminder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ReminderList::class, Reminder::class], version = 2, exportSchema = false)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: ReminderDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN url TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE reminders ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE reminder_lists ADD COLUMN listType TEXT NOT NULL DEFAULT 'Standard'")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "reminder_database"
                )
                .addMigrations(MIGRATION_1_2)
                .addCallback(ReminderDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class ReminderDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.reminderDao())
                }
            }
        }

        suspend fun populateDatabase(reminderDao: ReminderDao) {
            // Default "Reminders" list (iOS Blue #007AFF)
            reminderDao.insertList(
                ReminderList(
                    name = "Reminders",
                    colorHex = "#007AFF",
                    iconName = "list",
                    isVisible = true,
                    sortOrder = 0
                )
            )
        }
    }
}
