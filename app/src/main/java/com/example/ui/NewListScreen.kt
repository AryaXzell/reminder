package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun NewListScreen(
    viewModel: ReminderViewModel,
    listId: Int? = null,
    onDismiss: () -> Unit
) {
    val lists by viewModel.allLists.collectAsState()
    val existingList = remember(listId, lists) {
        if (listId != null) lists.find { it.id == listId } else null
    }

    var listName by remember { mutableStateOf(existingList?.name ?: "") }
    var selectedColorHex by remember { mutableStateOf(existingList?.colorHex ?: "#007AFF") }
    var selectedIconName by remember { mutableStateOf(existingList?.iconName ?: "list") }
    var selectedListType by remember { mutableStateOf(existingList?.listType ?: "Standard") }

    // 12 official iOS colors
    val colors = listOf(
        "#FF3B30", // Red
        "#FF9500", // Orange
        "#FFCC00", // Yellow
        "#34C759", // Green
        "#00C7BE", // Mint
        "#30B0C7", // Teal
        "#32ADE6", // Cyan
        "#007AFF", // Blue
        "#5856D6", // Indigo
        "#AF52DE", // Purple
        "#FF2D55", // Pink
        "#A2845E"  // Brown
    )

    val canSave = listName.trim().isNotEmpty()

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
                    text = if (existingList != null) "List Info" else "New List",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "Done",
                    color = if (canSave) iOSBlue else iOSSilver,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(enabled = canSave) {
                        if (existingList != null) {
                            viewModel.updateList(
                                existingList.copy(
                                    name = listName.trim(),
                                    colorHex = selectedColorHex,
                                    iconName = selectedIconName,
                                    listType = selectedListType
                                )
                            )
                        } else {
                            viewModel.addList(
                                name = listName.trim(),
                                colorHex = selectedColorHex,
                                iconName = selectedIconName,
                                listType = selectedListType
                            )
                        }
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Central large List Card Container with Icon and Text Field
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = iOSCardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Large Circular icon preview (matches selected color and selected icon)
                    val parsedColor = Color(android.graphics.Color.parseColor(selectedColorHex))
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .shadow(4.dp, CircleShape)
                            .background(parsedColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconUtils.getIconByName(selectedIconName),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    // Rounded Name input text field
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE5E5EA))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (listName.isEmpty()) {
                                Text(
                                    text = "List Name",
                                    color = iOSSilver,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            BasicTextField(
                                value = listName,
                                onValueChange = { listName = it },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }

                    // List Type Selector Segment (Standard vs Grocery)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE5E5EA))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Standard", "Grocery").forEach { type ->
                            val isSelected = selectedListType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable { selectedListType = type }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            // Colors Palette Selector Card (6x2 grid)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = iOSCardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "COLORS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = iOSSilver
                    )

                    // 2 rows of 6 colors
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            colors.take(6).forEach { colorHex ->
                                ColorCircle(
                                    colorHex = colorHex,
                                    isSelected = selectedColorHex == colorHex,
                                    onClick = { selectedColorHex = colorHex }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            colors.drop(6).take(6).forEach { colorHex ->
                                ColorCircle(
                                    colorHex = colorHex,
                                    isSelected = selectedColorHex == colorHex,
                                    onClick = { selectedColorHex = colorHex }
                                )
                            }
                        }
                    }
                }
            }

            // Symbols / Icons Grid Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = iOSCardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SYMBOLS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = iOSSilver
                    )

                    // Grid of available symbols
                    val icons = IconUtils.availableIcons
                    val chunkedIcons = icons.chunked(6)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        chunkedIcons.forEach { rowIcons ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                rowIcons.forEach { (name, vector) ->
                                    val isSelected = selectedIconName.equals(name, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFFE5E5EA) else Color.Transparent)
                                            .border(
                                                width = if (isSelected) 2.dp else 0.dp,
                                                color = if (isSelected) iOSBlue else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedIconName = name },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = vector,
                                            contentDescription = name,
                                            tint = if (isSelected) iOSBlue else Color(0xFF3C3C43),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                // Pad empty slots in row if less than 6
                                repeat(6 - rowIcons.size) {
                                    Spacer(modifier = Modifier.size(44.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ColorCircle(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = Color(android.graphics.Color.parseColor(colorHex))

    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) iOSBlue else Color.Transparent,
                shape = CircleShape
            )
            .padding(if (isSelected) 3.dp else 0.dp)
            .background(if (isSelected) Color.White else Color.Transparent, CircleShape)
            .padding(if (isSelected) 3.dp else 0.dp)
            .background(color, CircleShape)
    )
}
