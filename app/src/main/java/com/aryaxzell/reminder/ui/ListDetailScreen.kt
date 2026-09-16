package com.aryaxzell.reminder.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaxzell.reminder.data.Reminder
import com.aryaxzell.reminder.data.ReminderList
import com.aryaxzell.reminder.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ListDetailScreen(
    viewModel: ReminderViewModel,
    listId: Int? = null,
    smartType: SmartListType? = null,
    onBack: () -> Unit
) {
    val lists by viewModel.allLists.collectAsState()
    val remindersState by viewModel.allReminders.collectAsState()
    val isGroupedByTime by viewModel.isGroupedByTime.collectAsState()

    // Determine current list context
    val currentList = remember(listId, lists) {
        if (listId != null) lists.find { it.id == listId } else null
    }

    // Determine title, color, and items
    val titleText = when {
        smartType != null -> when (smartType) {
            SmartListType.TODAY -> "Today"
            SmartListType.SCHEDULED -> "Scheduled"
            SmartListType.ALL -> "All"
            SmartListType.FLAGGED -> "Flagged"
            SmartListType.COMPLETED -> "Completed"
        }
        currentList != null -> currentList.name
        else -> "Reminders"
    }

    val titleColorHex = when {
        smartType != null -> when (smartType) {
            SmartListType.TODAY -> "#007AFF"
            SmartListType.SCHEDULED -> "#FF3B30"
            SmartListType.ALL -> "#2C2C2E"
            SmartListType.FLAGGED -> "#FF9500"
            SmartListType.COMPLETED -> "#8E8E93"
        }
        currentList != null -> currentList.colorHex
        else -> "#007AFF"
    }
    val titleColor = Color(android.graphics.Color.parseColor(titleColorHex))

    // Active reminders inside this list context
    val listColorMap = remember(lists) {
        lists.associate { it.id to it.colorHex }
    }

    val filteredReminders = remember(listId, smartType, remindersState) {
        when {
            smartType != null -> viewModel.getRemindersForSmartList(smartType)
            listId != null -> remindersState.filter { it.listId == listId && !it.isCompleted }
            else -> emptyList()
        }
    }

    // Completed reminders in this context (for normal lists or ALL)
    val completedInList = remember(listId, remindersState) {
        if (listId != null) remindersState.filter { it.listId == listId && it.isCompleted }
        else emptyList()
    }

    // Inline add state
    var isAddingInline by remember { mutableStateOf(false) }
    var inlineTitle by remember { mutableStateOf("") }
    var inlineNote by remember { mutableStateOf("") }
    var inlineTimeOfDay by remember { mutableStateOf("Morning") }
    var inlinePriority by remember { mutableStateOf(0) }
    var inlineFlagged by remember { mutableStateOf(smartType == SmartListType.FLAGGED) }

    // Menu state
    var showMenu by remember { mutableStateOf(false) }
    var showCompletedSection by remember { mutableStateOf(true) }
    var showClearCompletedConfirmation by remember { mutableStateOf(false) }

    // Selected Reminder for Detailed Inspector Sheet
    var selectedReminderForSheet by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        containerColor = iOSLightBackground,
        floatingActionButton = {
            if (!isAddingInline && smartType != SmartListType.COMPLETED) {
                FloatingActionButton(
                    onClick = {
                        isAddingInline = true
                    },
                    containerColor = iOSBlue,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Reminder", modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = "Back",
                        tint = iOSBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Inline Done Checkmark button when adding a reminder
                if (isAddingInline) {
                    IconButton(
                        onClick = {
                            if (inlineTitle.trim().isNotEmpty()) {
                                val targetListId = currentList?.id ?: lists.firstOrNull()?.id ?: 1
                                val computedDueDate = if (smartType == SmartListType.TODAY || isGroupedByTime) {
                                    DateTimeUtils.getTodayWithTime(inlineTimeOfDay)
                                } else null
                                val computedTimeOfDay = if (smartType == SmartListType.TODAY || isGroupedByTime) {
                                    inlineTimeOfDay
                                } else null

                                viewModel.addReminder(
                                    title = inlineTitle.trim(),
                                    notes = inlineNote.trim().ifEmpty { null },
                                    listId = targetListId,
                                    dueDate = computedDueDate,
                                    timeOfDay = computedTimeOfDay,
                                    priority = inlinePriority,
                                    isFlagged = inlineFlagged || (smartType == SmartListType.FLAGGED)
                                )
                                inlineTitle = ""
                                inlineNote = ""
                                inlinePriority = 0
                                inlineFlagged = (smartType == SmartListType.FLAGGED)
                                isAddingInline = false
                            } else {
                                isAddingInline = false
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(iOSBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Save",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Menu dots button
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreHoriz,
                                contentDescription = "More Options",
                                tint = iOSBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // iOS-style Popup Action Menu
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .background(Color.White)
                                .width(230.dp)
                        ) {
                            if (listId != null) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (showCompletedSection) "Hide Completed" else "Show Completed",
                                            color = Color.Black
                                        )
                                    },
                                    onClick = {
                                        showCompletedSection = !showCompletedSection
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (showCompletedSection) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = null,
                                            tint = Color.Black
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear Completed", color = iOSRed) },
                                    onClick = {
                                        showMenu = false
                                        showClearCompletedConfirmation = true
                                    },
                                    leadingIcon = { Icon(Icons.Filled.DeleteSweep, contentDescription = null, tint = iOSRed) }
                                )
                                Divider(color = iOSSeparator, thickness = 0.5.dp)
                            }

                            if (smartType == SmartListType.TODAY || smartType == SmartListType.SCHEDULED) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Group by Time", color = Color.Black)
                                            if (isGroupedByTime) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = iOSBlue,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.toggleGroupByTime()
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Filled.ViewAgenda, contentDescription = null, tint = Color.Black) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title Header
            Text(
                text = titleText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Inline Add row at the top if isAddingInline is true
            AnimatedVisibility(visible = isAddingInline) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = iOSCardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(1.5.dp, iOSSilver, CircleShape)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                BasicTextField(
                                    value = inlineTitle,
                                    onValueChange = { inlineTitle = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    ),
                                    decorationBox = { innerTextField ->
                                        if (inlineTitle.isEmpty()) {
                                            Text("New Reminder", color = iOSSilver, fontSize = 17.sp)
                                        }
                                        innerTextField()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics {
                                            contentDescription = "New Reminder Title"
                                        }
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                BasicTextField(
                                    value = inlineNote,
                                    onValueChange = { inlineNote = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        color = iOSSilver
                                    ),
                                    decorationBox = { innerTextField ->
                                        if (inlineNote.isEmpty()) {
                                             Text("Add Note", color = iOSSilver, fontSize = 14.sp)
                                        }
                                        innerTextField()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics {
                                            contentDescription = "New Reminder Notes"
                                        }
                                )
                            }

                            IconButton(
                                onClick = {
                                    val temp = Reminder(
                                        title = inlineTitle,
                                        notes = inlineNote.ifEmpty { null },
                                        listId = currentList?.id ?: lists.firstOrNull()?.id ?: 1,
                                        isFlagged = inlineFlagged,
                                        priority = inlinePriority
                                    )
                                    selectedReminderForSheet = temp
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = "Details",
                                    tint = iOSBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Priority & Flag quick row
                        Divider(color = iOSSeparator, thickness = 0.5.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Priority:", fontSize = 12.sp, color = iOSSilver, fontWeight = FontWeight.Bold)
                                listOf(0 to "None", 1 to "!", 2 to "!!", 3 to "!!!").forEach { (p, label) ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (inlinePriority == p) iOSBlue else Color(0xFFE5E5EA))
                                            .clickable { inlinePriority = p }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            color = if (inlinePriority == p) Color.White else Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (inlineFlagged) iOSOrange.copy(alpha = 0.15f) else Color(0xFFE5E5EA))
                                    .clickable { inlineFlagged = !inlineFlagged }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Flag,
                                    contentDescription = "Flag",
                                    tint = if (inlineFlagged) iOSOrange else iOSSilver,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Flag",
                                    fontSize = 12.sp,
                                    color = if (inlineFlagged) iOSOrange else Color.Black,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // TimeOfDay group selectors if smartType is TODAY or isGroupedByTime is active
                        if (smartType == SmartListType.TODAY || isGroupedByTime) {
                            Divider(color = iOSSeparator, thickness = 0.5.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Time of Day: ", fontSize = 12.sp, color = iOSSilver, fontWeight = FontWeight.Bold)
                                listOf("Morning", "Afternoon", "Tonight").forEach { timeOption ->
                                    val isSelected = inlineTimeOfDay == timeOption
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) iOSBlue else Color(0xFFE5E5EA))
                                            .clickable { inlineTimeOfDay = timeOption }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = timeOption,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.White else Color.Black,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Reminders List with possible Grouping
            if (filteredReminders.isEmpty() && completedInList.isEmpty() && !isAddingInline) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Reminders",
                        color = iOSSilver,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isGroupedByTime && (smartType == SmartListType.TODAY || smartType == SmartListType.SCHEDULED)) {
                        val grouped = filteredReminders.groupBy { DateTimeUtils.normalizeToSection(it.timeOfDay) }

                        listOf(
                            DateTimeUtils.TIME_PRESET_MORNING,
                            DateTimeUtils.TIME_PRESET_AFTERNOON,
                            DateTimeUtils.TIME_PRESET_TONIGHT
                        ).forEach { section ->
                            val itemsInSection = grouped[section] ?: emptyList()

                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = section,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = iOSSilver,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    Divider(color = iOSSeparator, thickness = 0.5.dp)
                                }
                            }

                            if (itemsInSection.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "No Reminders in $section", fontSize = 13.sp, color = iOSSilver)
                                    }
                                }
                            } else {
                                items(itemsInSection, key = { it.id }) { reminder ->
                                    val listColor = listColorMap[reminder.listId] ?: "#007AFF"
                                    ReminderRowItem(
                                        reminder = reminder,
                                        listColorHex = listColor,
                                        onToggleCompletion = { viewModel.toggleReminderCompletion(reminder) },
                                        onOpenDetail = { selectedReminderForSheet = reminder },
                                        onDelete = { viewModel.deleteReminder(reminder.id) }
                                    )
                                }
                            }
                        }
                    } else {
                        // Regular list detail flat view
                        items(filteredReminders, key = { it.id }) { reminder ->
                            val listColor = listColorMap[reminder.listId] ?: "#007AFF"
                            ReminderRowItem(
                                reminder = reminder,
                                listColorHex = listColor,
                                onToggleCompletion = { viewModel.toggleReminderCompletion(reminder) },
                                onOpenDetail = { selectedReminderForSheet = reminder },
                                onDelete = { viewModel.deleteReminder(reminder.id) }
                            )
                        }
                    }

                    // Completed Section for List Detail
                    if (completedInList.isNotEmpty() && showCompletedSection) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${completedInList.size} Completed",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = iOSSilver
                                )

                                Text(
                                    text = "Clear",
                                    color = iOSBlue,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.clickable {
                                        viewModel.clearCompletedReminders(listId)
                                    }
                                )
                            }
                            Divider(color = iOSSeparator, thickness = 0.5.dp)
                        }

                        items(completedInList, key = { it.id }) { reminder ->
                            val listColor = listColorMap[reminder.listId] ?: "#007AFF"
                            ReminderRowItem(
                                reminder = reminder,
                                listColorHex = listColor,
                                onToggleCompletion = { viewModel.toggleReminderCompletion(reminder) },
                                onOpenDetail = { selectedReminderForSheet = reminder },
                                onDelete = { viewModel.deleteReminder(reminder.id) }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }

    if (showClearCompletedConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearCompletedConfirmation = false },
            title = { Text("Clear Completed Reminders?") },
            text = { Text("Are you sure you want to clear all completed reminders in this list? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearCompletedConfirmation = false
                        if (listId != null) {
                            viewModel.clearCompletedReminders(listId)
                        }
                    }
                ) {
                    Text("Clear", color = iOSRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCompletedConfirmation = false }) {
                    Text("Cancel", color = iOSBlue)
                }
            }
        )
    }

    // Detail Inspector Dialog for selected reminder
    selectedReminderForSheet?.let { rem ->
        ReminderDetailSheet(
            reminder = rem,
            lists = lists,
            onDismiss = { selectedReminderForSheet = null },
            onSave = { updated ->
                if (updated.id == 0) {
                    viewModel.addReminder(
                        title = updated.title,
                        notes = updated.notes,
                        url = updated.url,
                        listId = updated.listId,
                        dueDate = updated.dueDate,
                        timeOfDay = updated.timeOfDay,
                        priority = updated.priority,
                        isFlagged = updated.isFlagged
                    )
                } else {
                    viewModel.updateReminder(updated)
                }
                selectedReminderForSheet = null
                isAddingInline = false
                inlineTitle = ""
                inlineNote = ""
            },
            onDelete = { id ->
                if (id != 0) {
                    viewModel.deleteReminder(id)
                }
                selectedReminderForSheet = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderRowItem(
    reminder: Reminder,
    listColorHex: String,
    onToggleCompletion: () -> Unit,
    onOpenDetail: () -> Unit,
    onDelete: () -> Unit
) {
    val listColor = Color(android.graphics.Color.parseColor(listColorHex))
    val dateFormat = remember { SimpleDateFormat("EEE, dd MMM", Locale.getDefault()) }
    val context = LocalContext.current

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) iOSRed else Color.Transparent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(color)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Reminder",
                    tint = Color.White
                )
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenDetail() },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = iOSCardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Circle checkbox matching list color
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, if (reminder.isCompleted) listColor else iOSSilver, CircleShape)
                            .background(if (reminder.isCompleted) listColor else Color.Transparent)
                            .clickable { onToggleCompletion() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (reminder.isCompleted) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Text titles and metadata
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Priority indicator (!, !!, !!!)
                            if (reminder.priority > 0) {
                                val prioExclamations = "!".repeat(reminder.priority)
                                Text(
                                    text = prioExclamations,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = iOSRed
                                )
                            }

                            Text(
                                text = reminder.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (reminder.isCompleted) iOSSilver else Color.Black,
                                textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )

                            if (reminder.isFlagged) {
                                Icon(
                                    imageVector = Icons.Filled.Flag,
                                    contentDescription = "Flagged",
                                    tint = iOSOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        if (!reminder.notes.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = reminder.notes,
                                fontSize = 14.sp,
                                color = iOSSilver,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (!reminder.url.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            val rawUrl = reminder.url.trim()
                            val formattedUrl = if (rawUrl.startsWith("http://", ignoreCase = true) || rawUrl.startsWith("https://", ignoreCase = true)) {
                                rawUrl
                            } else {
                                "https://$rawUrl"
                            }
                            Text(
                                text = reminder.url,
                                fontSize = 12.sp,
                                color = iOSBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore malformed URL or no activity to handle intent
                                    }
                                }
                            )
                        }

                        if (reminder.dueDate != null || reminder.timeOfDay != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (reminder.dueDate != null) {
                                    Text(
                                        text = dateFormat.format(Date(reminder.dueDate)),
                                        fontSize = 12.sp,
                                        color = if (reminder.dueDate < System.currentTimeMillis() && !reminder.isCompleted) iOSRed else iOSSilver
                                    )
                                }
                                if (reminder.timeOfDay != null) {
                                    Text(
                                        text = "• ${DateTimeUtils.formatTimeDisplay(reminder.timeOfDay)}",
                                        fontSize = 12.sp,
                                        color = iOSSilver
                                    )
                                }
                            }
                        }
                    }
                }

                // Right side info (i) button
                IconButton(
                    onClick = onOpenDetail,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Details",
                        tint = iOSBlue.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
