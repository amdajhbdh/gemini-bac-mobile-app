package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LessonSyncViewModel
import com.example.ui.components.SectionHeader

@Composable
fun AnalyticsScreen(
    viewModel: LessonSyncViewModel,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val allGuides by viewModel.allGuides.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()

    // Retrieve global flashcards list to compute correctness rates
    val allFlashcards by viewModel.allFlashcards.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary

    // 1. Task calculations
    val totalTasks = allTasks.size
    val completedTasks = allTasks.count { it.isCompleted }
    val pendingTasks = totalTasks - completedTasks
    val taskCompetionPct = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks.toFloat()) else 0f

    // 2. Flashcards calculations
    val totalReviewed = allFlashcards.sumOf { it.checkedCount }
    val totalCorrect = allFlashcards.sumOf { it.correctCount }
    val generalMasteryPct = if (totalReviewed > 0) (totalCorrect.toFloat() / totalReviewed.toFloat()) else 0.75f // Default mock baseline

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SectionHeader(title = "Study Progress Analytics")

        // Streak Card Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Study Streak",
                value = "12 Days",
                icon = Icons.Default.Whatshot,
                color = Color(0xFFFF5722),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Active Guides",
                value = "${allGuides.size} Topics",
                icon = Icons.Default.MenuBook,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // First Visual representation chart: Task Completion donut
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "LESSON TASKS TERMINATED STATUS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Canvas scope donut drawing
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 14f
                            val sizeOffset = strokeWidth

                            // Background grey track
                            drawArc(
                                color = Color(0xFFE2E8F0),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )

                            // Active progress sector
                            val sweepAngleVal = if (totalTasks > 0) (taskCompetionPct * 360f) else 120f // Mock baseline if empty
                            drawArc(
                                color = Color(0xFF10B981), // Emerald green
                                startAngle = -90f,
                                sweepAngle = sweepAngleVal,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val percentageText = if (totalTasks > 0) "${(taskCompetionPct * 100).toInt()}%" else "60%"
                            Text(percentageText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Done", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Labels Column
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LegendRow(color = Color(0xFF10B981), text = "Completed Lesson Tasks: ${if (totalTasks > 0) completedTasks else 3}")
                        LegendRow(color = Color(0xFFE2E8F0), text = "Pending Reminders: ${if (totalTasks > 0) pendingTasks else 2}")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Syncs with lesson timetables automatically.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Second Visual representation chart: Weekly Active studies (Bar Chart)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "WEEKLY COMPLETED LESSON STUDY HOURS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Bar charts in Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    val studyMinutesList = listOf(180, 120, 240, 90, 150, 60, 45) // Mock tracked minutes spent

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val barSpacing = width / 8
                        val maxMinutes = 260f

                        // Draw background grid lines
                        for (i in 1..3) {
                            val yVal = height - (i * (height / 4))
                            drawLine(
                                color = Color(0xFFF1F5F9),
                                start = Offset(0f, yVal),
                                end = Offset(width, yVal),
                                strokeWidth = 1f
                            )
                        }

                        // Draw bars
                        studyMinutesList.forEachIndexed { index, mins ->
                            val barX = (index + 1) * barSpacing - 24f
                            val barHeight = (mins / maxMinutes) * (height - 30f)
                            val barY = height - 30f - barHeight

                            // Gradient simulated color
                            val color = when (index) {
                                1, 3 -> Color(0xFF818CF8) // Indigo
                                2 -> Color(0xFF38BDF8) // Sky blue
                                else -> primaryColor
                            }

                            // Render bar rectangle
                            drawRect(
                                color = color,
                                topLeft = Offset(barX, barY),
                                size = Size(30f, barHeight)
                            )
                        }
                    }

                    // Render footer text labels beneath bars
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        days.forEach { dayStr ->
                            Text(
                                text = dayStr,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Third Visual representation layout: Spaced Repetition Mastery Levels (Horizontal progress list)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "SUBJECT DISCOV ACCURACY RATE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Physics mastery progress
                SubjectProgressRow(
                    subject = "Physics Kinetics Map",
                    ratio = 0.85f,
                    color = Color(0xFF38BDF8),
                    totalCards = 5
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Math calculus progress
                SubjectProgressRow(
                    subject = "Calculus limits continuity",
                    ratio = generalMasteryPct,
                    color = Color(0xFF818CF8),
                    totalCards = if (allFlashcards.isNotEmpty()) allFlashcards.size else 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Biology metabolism progress
                SubjectProgressRow(
                    subject = "Biology Krebs cycle",
                    ratio = 0.70f,
                    color = Color(0xFF10B981),
                    totalCards = 4
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp)
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
                Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LegendRow(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SubjectProgressRow(
    subject: String,
    ratio: Float,
    color: Color,
    totalCards: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(subject, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("${(ratio * 100).toInt()}% (${totalCards} Cards Examined)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = ratio,
            color = color,
            trackColor = Color(0xFFF1F5F9),
            modifier = Modifier.fillMaxWidth().height(8.dp)
        )
    }
}
