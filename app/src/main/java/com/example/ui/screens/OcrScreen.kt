package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LessonSyncViewModel
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(
    viewModel: LessonSyncViewModel,
    modifier: Modifier = Modifier
) {
    val ocrImage by viewModel.ocrImage.collectAsState()
    val ocrLoading by viewModel.ocrLoading.collectAsState()
    val ocrResult by viewModel.ocrResult.collectAsState()
    val ocrError by viewModel.ocrError.collectAsState()

    var activeResultTab by remember { mutableStateOf("STRUCTURED") } // "RAW", "STRUCTURED", "MAP", "SUMMARY"
    var selectedSketchName by remember { mutableStateOf("") }

    // Dialog state for camera simulator confirmation
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SectionHeader(
            title = "AI Multi-Way OCR Agent",
            action = {
                if (ocrImage != null) {
                    TextButton(onClick = { viewModel.setOcrImage(null); selectedSketchName = "" }) {
                        Text("Clear Image", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )

        // 1. Image source selector (if empty)
        if (ocrImage == null) {
            Text(
                "Select a classroom whiteboard sketch or lesson scan below to test the multi-way AI OCR analysis engine:",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Math Board Card
                SketchCard(
                    title = "Board 1: Limits & Continuum",
                    subject = "Mathematics",
                    modifier = Modifier.weight(1f),
                    onSelect = {
                        val bitmap = renderSketchToBitmap("math")
                        viewModel.setOcrImage(bitmap)
                        selectedSketchName = "Limits & Derivatives Whiteboard"
                    }
                )

                // Physics Card
                SketchCard(
                    title = "Board 2: Vectors & Projectile",
                    subject = "Physics",
                    modifier = Modifier.weight(1f),
                    onSelect = {
                        val bitmap = renderSketchToBitmap("physics")
                        viewModel.setOcrImage(bitmap)
                        selectedSketchName = "Kinematic Trajectory Whiteboard"
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bio Card
            SketchCard(
                title = "Board 3: Krebs Cycle Mitochondrial Path",
                subject = "Biology",
                modifier = Modifier.fillMaxWidth().height(100.dp),
                onSelect = {
                    val bitmap = renderSketchToBitmap("biology")
                    viewModel.setOcrImage(bitmap)
                    selectedSketchName = "Cellular Respiration Biology Chart"
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Or take simulated camera snap
            Card(
                onClick = {
                    val bitmap = renderSketchToBitmap("math")
                    viewModel.setOcrImage(bitmap)
                    selectedSketchName = "Live Snapshot: Calculus whiteboard"
                },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Take Simulated Device Whiteboard Snap", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }
            }
        } else {
            // Display loaded image and OCR controls
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (selectedSketchName.contains("Limit", ignoreCase = true) || selectedSketchName.contains("math", ignoreCase = true)) {
                        MathWhiteboardDrawing(modifier = Modifier.fillMaxSize())
                    } else if (selectedSketchName.contains("physics", ignoreCase = true) || selectedSketchName.contains("Trajectory", ignoreCase = true)) {
                        PhysicsWhiteboardDrawing(modifier = Modifier.fillMaxSize())
                    } else {
                        BiologyWhiteboardDrawing(modifier = Modifier.fillMaxSize())
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(topStart = 8.dp),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = selectedSketchName,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(6.dp),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // OCR Actions
            if (ocrResult == null && !ocrLoading) {
                Button(
                    onClick = { viewModel.runOcrAnalysis() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Run OCR")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run Multi-Way AI OCR Agent", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Loading state
        if (ocrLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Gemini is reading equations and organizing study tasks...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Deconstructing whiteboard structure in multiple ways", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // 3. Error state
        if (ocrError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = ocrError ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // 4. OCR Results View
        if (ocrResult != null) {
            Spacer(modifier = Modifier.height(14.dp))

            // Multi-tab selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CustomChip(
                    text = "Structured Markdown",
                    selected = activeResultTab == "STRUCTURED",
                    onClick = { activeResultTab = "STRUCTURED" }
                )
                CustomChip(
                    text = "Concept Map",
                    selected = activeResultTab == "MAP",
                    onClick = { activeResultTab = "MAP" }
                )
                CustomChip(
                    text = "Raw Text",
                    selected = activeResultTab == "RAW",
                    onClick = { activeResultTab = "RAW" }
                )
                CustomChip(
                    text = "Summary",
                    selected = activeResultTab == "SUMMARY",
                    onClick = { activeResultTab = "SUMMARY" }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Content Box
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    val contentToShow = when (activeResultTab) {
                        "RAW" -> ocrResult?.rawOcr
                        "STRUCTURED" -> ocrResult?.structuredMarkdown
                        "MAP" -> ocrResult?.conceptMap
                        else -> ocrResult?.actionableSummary
                    } ?: "Empty output"

                    Text(
                        text = when (activeResultTab) {
                            "RAW" -> "🔍 Raw Character Scan:"
                            "STRUCTURED" -> "📏 Formatted Structured Notes:"
                            "MAP" -> "🗺️ Key Relational Concept Connections:"
                            else -> "⚡ Actionable Summary & Learning Points:"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Text(
                        text = contentToShow,
                        style = if (activeResultTab == "RAW") MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace) else MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 5. Automated tasks block
            val tasks = ocrResult?.suggestedTasks ?: emptyList()
            if (tasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF81C784), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Task, contentDescription = "Tasks", tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Auto-Extracted Study Tasks (${tasks.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Render extracted items
                        tasks.forEach { t ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.ArrowRight,
                                    contentDescription = "*",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                )
                                Column {
                                    Text(t.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                    Text(t.description, fontSize = 10.sp, color = Color(0xFF1B5E20).copy(alpha = 0.8f))
                                    Text("Suggested schedule: Due in ${t.daysFromNow} days at ${t.dueTime}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.acceptOcrTasks()
                                showSuccessSnackbar = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Sync Tasks")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Organize & Sync Suggested Tasks", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showSuccessSnackbar) {
        AlertDialog(
            onDismissRequest = { showSuccessSnackbar = false },
            title = { Text("Tasks Organized!") },
            text = { Text("The suggested study activities have been synchronized inside your database and linked with your general study schedule calendar successfully.", fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { showSuccessSnackbar = false }) { Text("Awesome") }
            }
        )
    }
}

@Composable
fun SketchCard(
    title: String,
    subject: String,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp)
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                subject,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AttachFile, contentDescription = "Clip", modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(2.dp))
                Text("Select Vector", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// Bypasses JVM Bitmap rendering exceptions for mock testing
fun renderSketchToBitmap(type: String): Bitmap {
    val width = 450
    val height = 280
    val conf = Bitmap.Config.ARGB_8888
    val bitmap = Bitmap.createBitmap(width, height, conf)
    val canvas = Canvas(bitmap)

    val paint = android.graphics.Paint()
    paint.color = when (type) {
        "math" -> android.graphics.Color.rgb(30, 41, 59)
        "physics" -> android.graphics.Color.rgb(15, 23, 42)
        else -> android.graphics.Color.rgb(31, 41, 55)
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    return bitmap
}
