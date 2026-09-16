package com.aryaxzell.reminder.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aryaxzell.reminder.data.Reminder
import com.aryaxzell.reminder.data.ReminderList
import com.aryaxzell.reminder.data.ReminderRepository
import com.aryaxzell.reminder.notification.ReminderAlarmScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

sealed interface Screen {
    object Onboarding : Screen
    object Dashboard : Screen
    data class ListDetail(val listId: Int) : Screen
    data class SmartListDetail(val type: SmartListType) : Screen
    data class NewList(val listId: Int? = null) : Screen
}

enum class SmartListType {
    TODAY, SCHEDULED, ALL, FLAGGED, COMPLETED
}

class ReminderViewModel(
    application: Application,
    private val repository: ReminderRepository
) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)

    // Navigation state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Onboarding completion state
    private val _isOnboardingCompleted = MutableStateFlow(sharedPrefs.getBoolean("onboarding_completed", false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    // Dashboard Editing State
    private val _isDashboardEditing = MutableStateFlow(false)
    val isDashboardEditing: StateFlow<Boolean> = _isDashboardEditing.asStateFlow()

    // Group by Time setting for Today list
    private val _isGroupedByTime = MutableStateFlow(sharedPrefs.getBoolean("grouped_by_time", true))
    val isGroupedByTime: StateFlow<Boolean> = _isGroupedByTime.asStateFlow()

    // Lists state
    val allLists: StateFlow<List<ReminderList>> = repository.allLists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reminders state
    val allReminders: StateFlow<List<Reminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Derived smart list mapping
    val smartListReminders: StateFlow<Map<SmartListType, List<Reminder>>> = allReminders
        .map { reminders ->
            val active = reminders.filter { !it.isCompleted }
            mapOf(
                SmartListType.TODAY to active.filter { DateTimeUtils.isDueToday(it.dueDate) },
                SmartListType.SCHEDULED to active.filter { it.dueDate != null }.sortedBy { it.dueDate },
                SmartListType.ALL to active,
                SmartListType.FLAGGED to active.filter { it.isFlagged },
                SmartListType.COMPLETED to reminders.filter { it.isCompleted }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val smartListCounts: StateFlow<Map<SmartListType, Int>> = smartListReminders
        .map { map -> map.mapValues { it.value.size } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Selected Smart lists visibility (Today, Scheduled, All, Flagged, Completed)
    private val _smartListsVisibility = MutableStateFlow(
        mapOf(
            SmartListType.TODAY to sharedPrefs.getBoolean("show_smart_today", true),
            SmartListType.SCHEDULED to sharedPrefs.getBoolean("show_smart_scheduled", true),
            SmartListType.ALL to sharedPrefs.getBoolean("show_smart_all", true),
            SmartListType.FLAGGED to sharedPrefs.getBoolean("show_smart_flagged", true),
            SmartListType.COMPLETED to sharedPrefs.getBoolean("show_smart_completed", true)
        )
    )
    val smartListsVisibility: StateFlow<Map<SmartListType, Boolean>> = _smartListsVisibility.asStateFlow()

    init {
        // If onboarding is not completed, set current screen to Onboarding
        if (!_isOnboardingCompleted.value) {
            _currentScreen.value = Screen.Onboarding
        }
    }

    // Navigation functions
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun completeOnboarding() {
        sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
        _isOnboardingCompleted.value = true
        _currentScreen.value = Screen.Dashboard
    }

    fun toggleDashboardEditing() {
        _isDashboardEditing.value = !_isDashboardEditing.value
    }

    fun setDashboardEditing(editing: Boolean) {
        _isDashboardEditing.value = editing
    }

    fun toggleSmartListVisibility(type: SmartListType) {
        val currentMap = _smartListsVisibility.value.toMutableMap()
        val currentVal = currentMap[type] ?: true
        currentMap[type] = !currentVal
        _smartListsVisibility.value = currentMap

        val prefKey = when (type) {
            SmartListType.TODAY -> "show_smart_today"
            SmartListType.SCHEDULED -> "show_smart_scheduled"
            SmartListType.ALL -> "show_smart_all"
            SmartListType.FLAGGED -> "show_smart_flagged"
            SmartListType.COMPLETED -> "show_smart_completed"
        }
        sharedPrefs.edit().putBoolean(prefKey, !currentVal).apply()
    }

    fun toggleGroupByTime() {
        val current = _isGroupedByTime.value
        sharedPrefs.edit().putBoolean("grouped_by_time", !current).apply()
        _isGroupedByTime.value = !current
    }

    // DB Operations
    fun addList(name: String, colorHex: String, iconName: String = "list", listType: String = "Standard") {
        viewModelScope.launch {
            repository.insertList(
                ReminderList(
                    name = name,
                    colorHex = colorHex,
                    iconName = iconName,
                    listType = listType,
                    sortOrder = (allLists.value.maxOfOrNull { it.sortOrder } ?: 0) + 1
                )
            )
        }
    }

    fun updateList(list: ReminderList) {
        viewModelScope.launch {
            repository.updateList(list)
        }
    }

    fun deleteList(listId: Int) {
        viewModelScope.launch {
            repository.deleteListById(listId)
        }
    }

    fun addReminder(
        title: String,
        notes: String? = null,
        url: String? = null,
        listId: Int,
        dueDate: Long? = null,
        timeOfDay: String? = null,
        priority: Int = 0,
        isFlagged: Boolean = false
    ) {
        viewModelScope.launch {
            val newReminder = Reminder(
                title = title,
                notes = notes,
                url = url,
                listId = listId,
                dueDate = dueDate,
                timeOfDay = timeOfDay,
                priority = priority,
                isFlagged = isFlagged
            )
            val insertedId = repository.insertReminder(newReminder)
            if (dueDate != null) {
                ReminderAlarmScheduler.schedule(getApplication(), newReminder.copy(id = insertedId.toInt()))
            }
        }
    }

    fun toggleReminderCompletion(reminder: Reminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isCompleted = !reminder.isCompleted)
            repository.updateReminder(updated)
            if (updated.isCompleted) {
                ReminderAlarmScheduler.cancel(getApplication(), reminder.id)
            } else if (updated.dueDate != null) {
                ReminderAlarmScheduler.schedule(getApplication(), updated)
            }
        }
    }

    fun toggleReminderFlag(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(isFlagged = !reminder.isFlagged))
        }
    }

    fun deleteReminder(reminderId: Int) {
        viewModelScope.launch {
            ReminderAlarmScheduler.cancel(getApplication(), reminderId)
            repository.deleteReminderById(reminderId)
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
            if (reminder.dueDate != null && !reminder.isCompleted) {
                ReminderAlarmScheduler.schedule(getApplication(), reminder)
            } else {
                ReminderAlarmScheduler.cancel(getApplication(), reminder.id)
            }
        }
    }

    fun clearCompletedReminders(listId: Int? = null) {
        viewModelScope.launch {
            val completed = allReminders.value.filter {
                it.isCompleted && (listId == null || it.listId == listId)
            }
            completed.forEach { reminder ->
                repository.deleteReminderById(reminder.id)
            }
        }
    }

    // Helper functions to count reminders
    fun getCountForSmartList(type: SmartListType): Int {
        return smartListCounts.value[type] ?: 0
    }

    fun getRemindersForSmartList(type: SmartListType): List<Reminder> {
        return smartListReminders.value[type] ?: emptyList()
    }

    fun getSmartListReminders(type: SmartListType): Flow<List<Reminder>> {
        return smartListReminders.map { it[type] ?: emptyList() }
    }

    fun isDueToday(timestamp: Long?): Boolean {
        return DateTimeUtils.isDueToday(timestamp)
    }
}

class ReminderViewModelFactory(
    private val application: Application,
    private val repository: ReminderRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReminderViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

