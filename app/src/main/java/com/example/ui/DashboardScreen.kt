package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Reminder
import com.example.data.ReminderList
import com.example.ui.theme.*
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: ReminderViewModel,
    onNavigateToList: (Int) -> Unit,
    onNavigateToSmartList: (SmartListType) -> Unit,
    onNavigateToNewList: (Int?) -> Unit
) {
    val lists by viewModel.allLists.collectAsState()
    val allReminders by viewModel.allReminders.collectAsState()
    val isEditing by viewModel.isDashboardEditing.collectAsState()
    val isICloudDismissed by viewModel.isICloudDismissed.collectAsState()
    val smartListsVis by viewModel.smartListsVisibility.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // State for Quick New Reminder modal
    var showQuickAddReminder by remember { mutableStateOf(false) }
    var selectedReminderForDetail by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        containerColor = iOSLightBackground,
        bottomBar = {
            if (!isEditing) {
                // Native iOS Reminders persistent bottom bar
                Surface(
                    color = iOSLightBackground,
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "+ New Reminder" button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showQuickAddReminder = true }
                                .padding(vertical = 6.dp, horizontal = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(iOSBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "New Reminder",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "New Reminder",
                                color = iOSBlue,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // "Add List" button
                        Text(
                            text = "Add List",
                            color = iOSBlue,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onNavigateToNewList(null) }
                                .padding(vertical = 6.dp, horizontal = 4.dp)
                        )
                    }
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

            // Search and Edit Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search Box
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE3E3E9))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = iOSSilver,
                        modifier = Modifier.size(20.dp)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(text = "Search", color = iOSSilver, fontSize = 17.sp)
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 17.sp,
                                color = Color.Black
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear Search",
                            tint = iOSSilver,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }

                // Edit/Done Button
                if (isEditing) {
                    IconButton(
                        onClick = { viewModel.setDashboardEditing(false) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(iOSBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Done",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Edit",
                        color = iOSBlue,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .clickable { viewModel.setDashboardEditing(true) }
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Check if Global Search is active
            if (searchQuery.trim().isNotEmpty()) {
                // Global Search Results View
                val query = searchQuery.trim()
                val matchingReminders = allReminders.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            (it.notes?.contains(query, ignoreCase = true) == true)
                }

                Text(
                    text = "SEARCH RESULTS (${matchingReminders.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = iOSSilver,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                if (matchingReminders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Reminders Found",
                            color = iOSSilver,
                            fontSize = 17.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(matchingReminders) { reminder ->
                            val list = lists.find { it.id == reminder.listId }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedReminderForDetail = reminder },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = iOSCardBackground)
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
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (reminder.isCompleted) iOSBlue else Color.Transparent
                                                )
                                                .clickable { viewModel.toggleReminderCompletion(reminder) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (reminder.isCompleted) {
                                                Icon(
                                                    Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFE5E5EA))
                                                )
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = reminder.title,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (reminder.isCompleted) iOSSilver else Color.Black,
                                                textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                            )
                                            if (!reminder.notes.isNullOrEmpty()) {
                                                Text(
                                                    text = reminder.notes,
                                                    fontSize = 13.sp,
                                                    color = iOSSilver,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    if (list != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(android.graphics.Color.parseColor(list.colorHex)).copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = list.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(android.graphics.Color.parseColor(list.colorHex))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            } else {
                // Main Dashboard Content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Smart List Section
                    if (isEditing) {
                        item {
                            Text(
                                text = "Smart Lists",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = iOSSilver,
                                modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(iOSCardBackground)
                            ) {
                                SmartListEditRow(
                                    type = SmartListType.TODAY,
                                    title = "Today",
                                    icon = Icons.Filled.Today,
                                    color = iOSBlue,
                                    isChecked = smartListsVis[SmartListType.TODAY] ?: true,
                                    onCheckedChange = { viewModel.toggleSmartListVisibility(SmartListType.TODAY) }
                                )
                                Divider(color = iOSSeparator, thickness = 0.5.dp, modifier = Modifier.padding(start = 54.dp))
                                SmartListEditRow(
                                    type = SmartListType.SCHEDULED,
                                    title = "Scheduled",
                                    icon = Icons.Filled.CalendarMonth,
                                    color = iOSRed,
                                    isChecked = smartListsVis[SmartListType.SCHEDULED] ?: true,
                                    onCheckedChange = { viewModel.toggleSmartListVisibility(SmartListType.SCHEDULED) }
                                )
                                Divider(color = iOSSeparator, thickness = 0.5.dp, modifier = Modifier.padding(start = 54.dp))
                                SmartListEditRow(
                                    type = SmartListType.ALL,
                                    title = "All",
                                    icon = Icons.Filled.Inbox,
                                    color = iOSDarkGrey,
                                    isChecked = smartListsVis[SmartListType.ALL] ?: true,
                                    onCheckedChange = { viewModel.toggleSmartListVisibility(SmartListType.ALL) }
                                )
                                Divider(color = iOSSeparator, thickness = 0.5.dp, modifier = Modifier.padding(start = 54.dp))
                                SmartListEditRow(
                                    type = SmartListType.FLAGGED,
                                    title = "Flagged",
                                    icon = Icons.Filled.Flag,
                                    color = iOSOrange,
                                    isChecked = smartListsVis[SmartListType.FLAGGED] ?: true,
                                    onCheckedChange = { viewModel.toggleSmartListVisibility(SmartListType.FLAGGED) }
                                )
                                Divider(color = iOSSeparator, thickness = 0.5.dp, modifier = Modifier.padding(start = 54.dp))
                                SmartListEditRow(
                                    type = SmartListType.COMPLETED,
                                    title = "Completed",
                                    icon = Icons.Filled.CheckCircle,
                                    color = iOSSilver,
                                    isChecked = smartListsVis[SmartListType.COMPLETED] ?: true,
                                    onCheckedChange = { viewModel.toggleSmartListVisibility(SmartListType.COMPLETED) }
                                )
                            }
                        }
                    } else {
                        // Smart List Grid
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val activeSmartLists = SmartListType.values().filter { smartListsVis[it] ?: true }
                                for (i in activeSmartLists.indices step 2) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val item1 = activeSmartLists[i]
                                        SmartListCard(
                                            type = item1,
                                            count = viewModel.getCountForSmartList(item1),
                                            modifier = Modifier.weight(1f),
                                            onClick = { onNavigateToSmartList(item1) }
                                        )

                                        if (i + 1 < activeSmartLists.size) {
                                            val item2 = activeSmartLists[i + 1]
                                            SmartListCard(
                                                type = item2,
                                                count = viewModel.getCountForSmartList(item2),
                                                modifier = Modifier.weight(1f),
                                                onClick = { onNavigateToSmartList(item2) }
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        // iCloud Sync Card
                        if (!isICloudDismissed) {
                            item {
                                ICloudSyncCard(
                                    onDismiss = { viewModel.dismissICloud() },
                                    onSettingsClick = { viewModel.showDictationDialog() }
                                )
                            }
                        }
                    }

                    // My Lists header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "My Lists",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                letterSpacing = (-0.4).sp
                            )
                        }
                    }

                    // Lists Container
                    if (lists.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = iOSCardBackground)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No Custom Lists",
                                        color = iOSSilver,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(iOSCardBackground)
                            ) {
                                lists.forEachIndexed { index, list ->
                                    val listIcon = IconUtils.getIconByName(list.iconName)
                                    val listColor = Color(android.graphics.Color.parseColor(list.colorHex))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (isEditing) {
                                                    onNavigateToNewList(list.id)
                                                } else {
                                                    onNavigateToList(list.id)
                                                }
                                            }
                                            .padding(vertical = 12.dp, horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (isEditing) {
                                                IconButton(
                                                    onClick = { viewModel.deleteList(list.id) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.RemoveCircle,
                                                        contentDescription = "Delete",
                                                        tint = iOSRed,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(listColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = listIcon,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = list.name,
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.Black,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (list.listType == "Grocery") {
                                                    Text(
                                                        text = "Grocery List",
                                                        fontSize = 12.sp,
                                                        color = iOSSilver
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (isEditing) {
                                                Icon(
                                                    imageVector = Icons.Filled.Info,
                                                    contentDescription = "Edit List",
                                                    tint = iOSBlue,
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clickable { onNavigateToNewList(list.id) }
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Filled.Menu,
                                                    contentDescription = "Reorder",
                                                    tint = iOSSilver,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else {
                                                val listCount = allReminders.count {
                                                    it.listId == list.id && !it.isCompleted
                                                }
                                                Text(
                                                    text = listCount.toString(),
                                                    color = iOSSilver,
                                                    fontSize = 17.sp
                                                )
                                                Icon(
                                                    imageVector = Icons.Filled.ChevronRight,
                                                    contentDescription = null,
                                                    tint = iOSSilver,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (index < lists.size - 1) {
                                        val startPadding = if (isEditing) 96.dp else 60.dp
                                        Divider(
                                            color = iOSSeparator,
                                            thickness = 0.5.dp,
                                            modifier = Modifier.padding(start = startPadding)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Quick Add Reminder Dialog
    if (showQuickAddReminder) {
        val targetList = lists.firstOrNull()
        if (targetList != null) {
            val emptyReminder = Reminder(
                title = "",
                listId = targetList.id
            )
            ReminderDetailSheet(
                reminder = emptyReminder,
                lists = lists,
                onDismiss = { showQuickAddReminder = false },
                onSave = { newReminder ->
                    viewModel.addReminder(
                        title = newReminder.title,
                        notes = newReminder.notes,
                        url = newReminder.url,
                        listId = newReminder.listId,
                        dueDate = newReminder.dueDate,
                        timeOfDay = newReminder.timeOfDay,
                        priority = newReminder.priority,
                        isFlagged = newReminder.isFlagged
                    )
                },
                onDelete = {}
            )
        }
    }

    // Detail Inspector Dialog for selected reminder
    selectedReminderForDetail?.let { rem ->
        ReminderDetailSheet(
            reminder = rem,
            lists = lists,
            onDismiss = { selectedReminderForDetail = null },
            onSave = { updated ->
                viewModel.updateReminder(updated)
                selectedReminderForDetail = null
            },
            onDelete = { id ->
                viewModel.deleteReminder(id)
                selectedReminderForDetail = null
            }
        )
    }
}

@Composable
fun SmartListCard(
    type: SmartListType,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val (title, icon, colorHex) = when (type) {
        SmartListType.TODAY -> Triple("Today", Icons.Filled.Today, "#007AFF")
        SmartListType.SCHEDULED -> Triple("Scheduled", Icons.Filled.CalendarMonth, "#FF3B30")
        SmartListType.ALL -> Triple("All", Icons.Filled.Inbox, "#2C2C2E")
        SmartListType.FLAGGED -> Triple("Flagged", Icons.Filled.Flag, "#FF9500")
        SmartListType.COMPLETED -> Triple("Completed", Icons.Filled.CheckCircle, "#8E8E93")
    }

    val color = Color(android.graphics.Color.parseColor(colorHex))
    val todayDayNumber = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString() }

    Card(
        modifier = modifier
            .height(86.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Smart List Icon Circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (type == SmartListType.TODAY) {
                        // Dynamic day badge for Today smart list tile
                        Text(
                            text = todayDayNumber,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Count
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }

            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

@Composable
fun SmartListEditRow(
    type: SmartListType,
    title: String,
    icon: ImageVector,
    color: Color,
    isChecked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isChecked) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                contentDescription = if (isChecked) "Checked" else "Unchecked",
                tint = if (isChecked) iOSBlue else iOSSilver,
                modifier = Modifier.size(24.dp)
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }

        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = "Reorder",
            tint = iOSSilver,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ICloudSyncCard(
    onDismiss: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = iOSCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E5EA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Cloud,
                        contentDescription = "iCloud",
                        tint = iOSSilver,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sync Reminders with iCloud",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Dismiss",
                            tint = iOSSilver,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onDismiss() }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "To access your reminders on all of your devices, sign into iCloud and enable Reminders.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray,
                        lineHeight = 17.sp
                    )
                }
            }

            Divider(color = iOSSeparator, thickness = 0.5.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSettingsClick() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Go to Settings",
                    color = iOSBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
