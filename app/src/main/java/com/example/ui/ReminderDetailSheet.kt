package com.example.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Reminder
import com.example.data.ReminderList
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReminderDetailSheet(
    reminder: Reminder,
    lists: List<ReminderList>,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit,
    onDelete: (Int) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(reminder.title) }
    var notes by remember { mutableStateOf(reminder.notes ?: "") }
    var url by remember { mutableStateOf(reminder.url ?: "") }
    var selectedListId by remember { mutableStateOf(reminder.listId) }
    var isFlagged by remember { mutableStateOf(reminder.isFlagged) }
    var priority by remember { mutableStateOf(reminder.priority) }

    // Date & Time states
    var hasDate by remember { mutableStateOf(reminder.dueDate != null) }
    var dueDateTimestamp by remember { mutableStateOf(reminder.dueDate ?: System.currentTimeMillis()) }
    var hasTime by remember { mutableStateOf(reminder.timeOfDay != null) }
    var timeOfDayString by remember { mutableStateOf(reminder.timeOfDay ?: "Morning") }

    val calendar = remember(dueDateTimestamp) {
        Calendar.getInstance().apply { timeInMillis = dueDateTimestamp }
    }

    val dateFormat = remember { SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            containerColor = iOSLightBackground,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(iOSLightBackground)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cancel",
                        color = iOSBlue,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.clickable { onDismiss() }
                    )

                    Text(
                        text = "Details",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Text(
                        text = "Done",
                        color = if (title.trim().isNotEmpty()) iOSBlue else iOSSilver,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(enabled = title.trim().isNotEmpty()) {
                            val updated = reminder.copy(
                                title = title.trim(),
                                notes = notes.trim().ifEmpty { null },
                                url = url.trim().ifEmpty { null },
                                listId = selectedListId,
                                isFlagged = isFlagged,
                                priority = priority,
                                dueDate = if (hasDate) dueDateTimestamp else null,
                                timeOfDay = if (hasDate && hasTime) timeOfDayString else null
                            )
                            onSave(updated)
                            onDismiss()
                        }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title, Notes & URL Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = iOSCardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            ),
                            decorationBox = { inner ->
                                if (title.isEmpty()) Text("Title", color = iOSSilver, fontSize = 17.sp)
                                inner()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Divider(color = iOSSeparator, thickness = 0.5.dp)

                        BasicTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 15.sp,
                                color = Color.Black
                            ),
                            decorationBox = { inner ->
                                if (notes.isEmpty()) Text("Notes", color = iOSSilver, fontSize = 15.sp)
                                inner()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 40.dp)
                        )

                        Divider(color = iOSSeparator, thickness = 0.5.dp)

                        BasicTextField(
                            value = url,
                            onValueChange = { url = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 15.sp,
                                color = iOSBlue
                            ),
                            decorationBox = { inner ->
                                if (url.isEmpty()) Text("URL", color = iOSSilver, fontSize = 15.sp)
                                inner()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Date & Time Controls Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = iOSCardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Date Toggle Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(iOSRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text("Date", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                                    if (hasDate) {
                                        Text(
                                            text = dateFormat.format(Date(dueDateTimestamp)),
                                            fontSize = 13.sp,
                                            color = iOSBlue,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Switch(
                                checked = hasDate,
                                onCheckedChange = { hasDate = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = iOSGreen
                                )
                            )
                        }

                        if (hasDate) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val now = Calendar.getInstance()
                                        DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                val c = Calendar.getInstance().apply {
                                                    timeInMillis = dueDateTimestamp
                                                    set(Calendar.YEAR, year)
                                                    set(Calendar.MONTH, month)
                                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                                }
                                                dueDateTimestamp = c.timeInMillis
                                            },
                                            now.get(Calendar.YEAR),
                                            now.get(Calendar.MONTH),
                                            now.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E5EA), contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Select Date...", fontSize = 13.sp)
                                }
                            }

                            Divider(color = iOSSeparator, thickness = 0.5.dp)

                            // Time Toggle Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(iOSBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Schedule, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                    Column {
                                        Text("Time", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                                        if (hasTime) {
                                            Text(
                                                text = timeOfDayString,
                                                fontSize = 13.sp,
                                                color = iOSBlue,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Switch(
                                    checked = hasTime,
                                    onCheckedChange = { hasTime = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = iOSGreen
                                    )
                                )
                            }

                            if (hasTime) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Morning (09:00)", "Afternoon (13:00)", "Tonight (20:00)").forEach { preset ->
                                        val label = preset.substringBefore(" ")
                                        val isSelected = timeOfDayString.startsWith(label)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) iOSBlue else Color(0xFFE5E5EA))
                                                .clickable { timeOfDayString = preset }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = label,
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

                // Flag & Priority Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = iOSCardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Flag Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(iOSOrange),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Flag, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Text("Flag", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                            }

                            Switch(
                                checked = isFlagged,
                                onCheckedChange = { isFlagged = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = iOSGreen
                                )
                            )
                        }

                        Divider(color = iOSSeparator, thickness = 0.5.dp)

                        // Priority Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(iOSRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.PriorityHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Text("Priority", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(0 to "None", 1 to "!", 2 to "!!", 3 to "!!!").forEach { (valPrio, label) ->
                                    val isSelected = priority == valPrio
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) iOSBlue else Color(0xFFE5E5EA))
                                            .clickable { priority = valPrio }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.White else Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // List Selector Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = iOSCardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("List", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)

                        lists.forEach { item ->
                            val isSelected = item.id == selectedListId
                            val listColor = Color(android.graphics.Color.parseColor(item.colorHex))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedListId = item.id }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(listColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = IconUtils.getIconByName(item.iconName),
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = item.name,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = Color.Black
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = iOSBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Delete Reminder Card Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDelete(reminder.id)
                            onDismiss()
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = iOSCardBackground)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Delete Reminder",
                            color = iOSRed,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
