package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.WhatsappMessage
import com.example.ui.AppTab
import com.example.ui.LessonSyncViewModel
import com.example.ui.components.BiologyWhiteboardDrawing
import com.example.ui.components.CustomChip
import com.example.ui.components.MathWhiteboardDrawing
import com.example.ui.components.PhysicsWhiteboardDrawing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: LessonSyncViewModel,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    val allMessages by viewModel.allMessages.collectAsState()
    val lessonHistory by viewModel.lessonHistory.collectAsState()

    var searchQuery by remember { mutableStateFlowOf("") }
    var filterLessonsOnly by remember { mutableStateFlowOf(true) }
    var filterAccountType by remember { mutableStateFlowOf("ALL") } // "ALL", "PERSONAL", "BUSINESS"

    val context = LocalContext.current

    // Dialog state for authentications
    var showPersonalAuthDialog by remember { mutableStateOf(false) }
    var showBusinessAuthDialog by remember { mutableStateOf(false) }

    // Number input fields
    var personalInputNo by remember { mutableStateOf("+1 (555) 304-9281") }
    var businessInputNo by remember { mutableStateOf("+1 (555) 830-4921") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 1. WhatsApp Connection Status Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Personal account status card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (authState.isPersonalConnected) {
                        Color(0xFFE8F5E9)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                ),
                modifier = Modifier.weight(1f).border(
                    width = 1.dp,
                    color = if (authState.isPersonalConnected) Color(0xFF81C784) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Personal",
                            tint = if (authState.isPersonalConnected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "My WhatsApp",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (authState.isPersonalConnected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    if (authState.isPersonalConnected) {
                        Text(authState.personalPhone, fontSize = 11.sp, color = Color(0xFF388E3C))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.disconnectPersonalWhatsApp() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp).align(Alignment.End)
                        ) {
                            Text("Disconnect", fontSize = 10.sp)
                        }
                    } else {
                        Text("Disconnected", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { showPersonalAuthDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp).align(Alignment.End)
                        ) {
                            Text("Auth", fontSize = 10.sp)
                        }
                    }
                }
            }

            // WhatsApp Business card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (authState.isBusinessConnected) {
                        Color(0xFFE1F5FE)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                ),
                modifier = Modifier.weight(1f).border(
                    width = 1.dp,
                    color = if (authState.isBusinessConnected) Color(0xFF4FC3F7) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Business",
                            tint = if (authState.isBusinessConnected) Color(0xFF0277BD) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "WA Business",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (authState.isBusinessConnected) Color(0xFF0277BD) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    if (authState.isBusinessConnected) {
                        Text(authState.businessPhone, fontSize = 11.sp, color = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.disconnectBusinessWhatsApp() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp).align(Alignment.End)
                        ) {
                            Text("Disconnect", fontSize = 10.sp)
                        }
                    } else {
                        Text("Disconnected", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { showBusinessAuthDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp).align(Alignment.End)
                        ) {
                            Text("Auth", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Animated loader if authentication loading
        if (authState.authLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        }

        // 2. Search & Filter Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search lessons, keywords...", fontSize = 14.sp) },
            prefix = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            ),
            singleLine = true
        )

        // Filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomChip(
                text = "📚 All Lessons",
                selected = filterLessonsOnly,
                onClick = { filterLessonsOnly = true }
            )
            CustomChip(
                text = "💬 Full WhatsApp",
                selected = !filterLessonsOnly,
                onClick = { filterLessonsOnly = false }
            )

            VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))

            CustomChip(
                text = "All Profiles",
                selected = filterAccountType == "ALL",
                onClick = { filterAccountType = "ALL" }
            )
            CustomChip(
                text = "Personal Only",
                selected = filterAccountType == "PERSONAL",
                onClick = { filterAccountType = "PERSONAL" }
            )
            CustomChip(
                text = "Business Only",
                selected = filterAccountType == "BUSINESS",
                onClick = { filterAccountType = "BUSINESS" }
            )
        }

        // 3. Messages List
        val filteredList = remember(
            allMessages,
            lessonHistory,
            searchQuery,
            filterLessonsOnly,
            filterAccountType
        ) {
            val baseList = if (filterLessonsOnly) lessonHistory else allMessages
            baseList.filter { msg ->
                val matchesSearch = msg.messageText.contains(searchQuery, ignoreCase = true) ||
                        msg.chatName.contains(searchQuery, ignoreCase = true) ||
                        msg.sender.contains(searchQuery, ignoreCase = true) ||
                        msg.lessonSubject.contains(searchQuery, ignoreCase = true)

                val matchesAccount = when (filterAccountType) {
                    "PERSONAL" -> !msg.isBusiness
                    "BUSINESS" -> msg.isBusiness
                    else -> true
                }
                matchesSearch && matchesAccount
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No WhatsApp message logs match selection.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredList) { msg ->
                    MessageCard(
                        message = msg,
                        onAnalyzeImage = { imageUrl ->
                            // Here we construct a mock bitmap matching our Canvas image representations
                            val width = 450
                            val height = 280
                            val conf = Bitmap.Config.ARGB_8888
                            val bitmap = Bitmap.createBitmap(width, height, conf)
                            val canvas = Canvas(bitmap)

                            // Just draw a colored background for the bitmap so it is technically valid to process
                            val paint = android.graphics.Paint()
                            paint.color = android.graphics.Color.DKGRAY
                            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                            viewModel.setOcrImage(bitmap)
                            viewModel.runOcrAnalysis()
                            viewModel.setTab(AppTab.OCR_AGENT)
                        },
                        onGenerateGuide = { subject, topic ->
                            viewModel.searchSubjectAndGenerate(subject, topic)
                            viewModel.setTab(AppTab.STUDY_ASSISTANT)
                        }
                    )
                }
            }
        }
    }

    // ============================================
    // DIALOGS FOR WA AUTH
    // ============================================

    if (showPersonalAuthDialog) {
        AlertDialog(
            onDismissRequest = { showPersonalAuthDialog = false },
            title = { Text("Authenticate My WhatsApp") },
            text = {
                Column {
                    Text("Standard local personal account token simulator. Provide your verified phone number to connect session logs.", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = personalInputNo,
                        onValueChange = { personalInputNo = it },
                        label = { Text("Mobile Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.connectPersonalWhatsApp(personalInputNo)
                    showPersonalAuthDialog = false
                }) {
                    Text("Simulate Auth Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPersonalAuthDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBusinessAuthDialog) {
        AlertDialog(
            onDismissRequest = { showBusinessAuthDialog = false },
            title = { Text("Authenticate WA Business") },
            text = {
                Column {
                    Text("School tutor or Professor verified business account authentication adapter.", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = businessInputNo,
                        onValueChange = { businessInputNo = it },
                        label = { Text("Business Portal Line") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.connectBusinessWhatsApp(businessInputNo)
                    showBusinessAuthDialog = false
                }) {
                    Text("Authenticate Portal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBusinessAuthDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MessageCard(
    message: WhatsappMessage,
    onAnalyzeImage: (String) -> Unit,
    onGenerateGuide: (String, String) -> Unit
) {
    val date = Date(message.timestamp)
    val sdf = SimpleDateFormat("HH:mm | MMM d", Locale.getDefault())
    val formattedDate = sdf.format(date)

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
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (message.isBusiness) Color(0xFF0288D1).copy(alpha = 0.1f) else Color(0xFF2E7D32).copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (message.isBusiness) Icons.Default.Business else Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = if (message.isBusiness) Color(0xFF0288D1) else Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = message.sender,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = message.chatName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // Date stamp
                Text(
                    text = formattedDate,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subject label tag bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (message.isBusiness) {
                    Surface(
                        color = Color(0xFFE0F7FA),
                        contentColor = Color(0xFF006064),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Business Client",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        contentColor = Color(0xFF1B5E20),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "WhatsApp Personal",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (message.isLesson) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "📖 Lesson: ${message.lessonSubject}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body Message text
            Text(
                text = message.messageText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            // Vector drawing thumbnail if available
            if (message.imageUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    when (message.imageUrl) {
                        "math_limits_diagram" -> MathWhiteboardDrawing(modifier = Modifier.fillMaxSize())
                        "physics_kinematics_diagram" -> PhysicsWhiteboardDrawing(modifier = Modifier.fillMaxSize())
                        "biology_krebs_diagram" -> BiologyWhiteboardDrawing(modifier = Modifier.fillMaxSize())
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onAnalyzeImage(message.imageUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "OCR", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analyze Whiteboard OCR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (message.isLesson) {
                        FilledTonalButton(
                            onClick = { onGenerateGuide(message.lessonSubject, "Calculus & Limits Review") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.School, contentDescription = "Guides", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Study Guide", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Composable helper to bypass initial state flows for simple mutations
fun <T> mutableStateFlowOf(init: T): MutableState<T> = mutableStateOf(init)
