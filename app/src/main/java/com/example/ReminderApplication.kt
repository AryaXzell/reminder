package com.example

import android.app.Application
import com.example.data.ReminderDatabase
import com.example.data.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ReminderApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { ReminderDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { ReminderRepository(database.reminderDao()) }
}
