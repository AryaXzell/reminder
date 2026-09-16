package com.aryaxzell.reminder.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_lists")
data class ReminderList(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String, // e.g. "#007AFF" for iOS Blue, etc.
    val iconName: String = "list", // Icon key name
    val listType: String = "Standard", // "Standard" or "Grocery"
    val isVisible: Boolean = true, // To support the "Edit" dashboard checklist toggle
    val sortOrder: Int = 0
)

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = ReminderList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["listId"])
    ]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val notes: String? = null,
    val url: String? = null,
    val dueDate: Long? = null, // timestamp in millis
    val timeOfDay: String? = null, // "Morning", "Afternoon", "Tonight", or formatted time "09:00"
    val priority: Int = 0, // 0 = None, 1 = Low (!), 2 = Medium (!!), 3 = High (!!!)
    val isCompleted: Boolean = false,
    val listId: Int, // foreign key to reminder_lists
    val isFlagged: Boolean = false,
    val creationTime: Long = System.currentTimeMillis()
)

