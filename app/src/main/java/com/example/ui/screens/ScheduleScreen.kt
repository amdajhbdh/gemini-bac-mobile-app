package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScheduleItem
import com.example.data.TaskReminder
import com.example.ui.LessonSyncViewModel
import com.example.ui.components.CustomChip
import com.example.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: LessonSyncViewModel,
    modifier: Modifier = Modifier
) {
    val weeklySchedule by viewModel.weeklySchedule.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()

    var selectedDay by remember { mutableStateOf("Monday") }
    var showAddSlotDialog by remember { mutableStateOf(false) }

    // Dialog input fields
    var subjectInput by remember { mutableStateOf("") }
    var startInput by remember { mutableStateOf("09:00") }
    var endInput by remember { mutableStateOf("10:30") }
    var classroomInput by remember { mutableStateOf("Room 201") }

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

    // Filter schedule items for selected day
    val filteredSchedule = remember(weeklySchedule, selectedDay) {
        weeklySchedule.filter { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        SectionHeader(
            title = "School Weekly Schedule",
            action = {
                IconButton(
                    onClick = { showAddSlotDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Class")
                }
            }
        )

        // Day of Week horizontal selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            daysOfWeek.forEach { day ->
                CustomChip(
                    text = day,
                    selected = selectedDay == day,
                    onClick = { selectedDay = day }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Time slot listings
        if (filteredSchedule.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EventNote,
                        contentDescription = "No classes",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No classes scheduled for $selectedDay.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showAddSlotDialog = true }) {
                        Text("Add Lesson Slot")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredSchedule) { slot ->
                    // Find tasks matching this class subject
                    val relatedTasks = remember(allTasks, slot.subject) {
                        allTasks.filter { task ->
                            task.title.contains(slot.subject, ignoreCase = true) ||
                            task.description.contains(slot.subject, ignoreCase = true)
                        }
                    }

                    ScheduleSlotCard(
                        slot = slot,
                        syncedTasks = relatedTasks,
                        onDeleteSlot = { viewModel.removeScheduleItem(slot) },
                        onToggleTask = { task -> viewModel.toggleTask(task) }
                    )
                }
            }
        }
    }

    // Add slot dialog
    if (showAddSlotDialog) {
        AlertDialog(
            onDismissRequest = { showAddSlotDialog = false },
            title = { Text("Add Class Schedule Slot") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = subjectInput,
                        onValueChange = { subjectInput = it },
                        label = { Text("Subject (e.g. Mathematics, Physics)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startInput,
                            onValueChange = { startInput = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("09:00") }
                        )
                        OutlinedTextField(
                            value = endInput,
                            onValueChange = { endInput = it },
                            label = { Text("End Time") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("10:30") }
                        )
                    }

                    OutlinedTextField(
                        value = classroomInput,
                        onValueChange = { classroomInput = it },
                        label = { Text("Classroom/Lab Room") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subjectInput.isNotBlank()) {
                            viewModel.createScheduleItem(
                                day = selectedDay,
                                subject = subjectInput,
                                start = startInput,
                                end = endInput,
                                room = classroomInput
                            )
                            showAddSlotDialog = false
                            subjectInput = ""
                        }
                    }
                ) {
                    Text("Add Slot")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSlotDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ScheduleSlotCard(
    slot: ScheduleItem,
    syncedTasks: List<TaskReminder>,
    onDeleteSlot: () -> Unit,
    onToggleTask: (TaskReminder) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Main slot content
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${slot.startTime} - ${slot.endTime}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = slot.classroom,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = slot.subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row {
                    if (syncedTasks.isNotEmpty()) {
                        // Status badge showing connected tasks
                        val completedCount = syncedTasks.count { it.isCompleted }
                        val allCompleted = completedCount == syncedTasks.size

                        Surface(
                            color = if (allCompleted) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                            contentColor = if (allCompleted) Color(0xFF2E7D32) else Color(0xFFE65100),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (allCompleted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = "Status",
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "$completedCount/${syncedTasks.size} Tasks Done",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    IconButton(onClick = onDeleteSlot) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Sync alert if incomplete tasks match
            val incompleteTasks = syncedTasks.filter { !it.isCompleted }
            if (incompleteTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                _syncBanner(count = incompleteTasks.size, onExpand = { expanded = !expanded }, isExpanded = expanded)

                // Expanded synced homework lists
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Incomplete tasks synced with this lesson window:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        incompleteTasks.forEach { task ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = task.isCompleted,
                                        onCheckedChange = { onToggleTask(task) }
                                    )
                                    Column {
                                        Text(
                                            text = task.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Due ${task.dueDate} at ${task.dueTime}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun _syncBanner(
    count: Int,
    onExpand: () -> Unit,
    isExpanded: Boolean
) {
    Surface(
        onClick = onExpand,
        color = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync Alert",
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Intelligent Sync: $count tasks due around lesson",
                    fontSize = 11.sp,
                    color = Color(0xFFE65100),
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Expand",
                tint = Color(0xFFE65100),
                modifier = Modifier.size(16.dp)
              )
        }
    }
}
