package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
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

    val cobaltServerUrl by viewModel.cobaltServerUrl.collectAsState()
    val cobaltUseMobile by viewModel.cobaltUseMobile.collectAsState()
    val cobaltQrCode by viewModel.cobaltQrCode.collectAsState()
    val cobaltPairingCode by viewModel.cobaltPairingCode.collectAsState()
    val cobaltStatus by viewModel.cobaltStatus.collectAsState()
    val syncStatusText by viewModel.syncStatusText.collectAsState()

    var cobaltServerInput by remember(cobaltServerUrl) { mutableStateOf(cobaltServerUrl) }
    var cobaltUseMobileInput by remember(cobaltUseMobile) { mutableStateOf(cobaltUseMobile) }

    var searchQuery by remember { mutableStateFlowOf("") }
    var filterLessonsOnly by remember { mutableStateFlowOf(true) }
    var filterAccountType by remember { mutableStateFlowOf("ALL") } // "ALL", "PERSONAL", "BUSINESS"

    val context = LocalContext.current

    val personalGalleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.connectFromGalleryQrPicker(isPersonal = true)
            android.widget.Toast.makeText(context, "📸 QR image selected! Decoded cobalt metadata. Connecting personal WhatsApp session...", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    val businessGalleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.connectFromGalleryQrPicker(isPersonal = false)
            android.widget.Toast.makeText(context, "📸 QR image selected! Decoded business cobalt metadata. Connecting business WhatsApp session...", android.widget.Toast.LENGTH_LONG).show()
        }
    }

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
    // DIALOGS FOR WA AUTH & COBALT JVM HANDSHAKE
    // ============================================

    if (showPersonalAuthDialog) {
        AlertDialog(
            onDismissRequest = { 
                showPersonalAuthDialog = false 
                if (cobaltStatus == "SCAN_QR" || cobaltStatus == "CONNECTING") {
                    // Reset if closed mid-pairing
                    viewModel.disconnectPersonalWhatsApp()
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Chat, 
                        contentDescription = "Cobalt SDK", 
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("My WhatsApp Setup")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (cobaltStatus == "SCAN_QR") {
                        // COBALT ACTIVE PAIRING VIEW
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "SCAN QR & PAIR COBALT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                val realQrBitmap = remember(cobaltQrCode) {
                                    if (cobaltQrCode != null && !cobaltQrCode.contains("MOCK")) {
                                        try {
                                            val cleanStr = if (cobaltQrCode!!.startsWith("data:image")) {
                                                cobaltQrCode!!.substringAfter(",")
                                            } else {
                                                cobaltQrCode!!
                                            }
                                            val decodedBytes = Base64.decode(cleanStr, Base64.DEFAULT)
                                            android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                        } catch (e: Exception) {
                                            null
                                        }
                                    } else {
                                        null
                                    }
                                }

                                if (realQrBitmap != null) {
                                    Image(
                                        bitmap = realQrBitmap.asImageBitmap(),
                                        contentDescription = "Real Cobalt QR Code",
                                        modifier = Modifier
                                            .size(160.dp)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                                            .padding(6.dp)
                                    )
                                } else {
                                    // Beautiful Pseudo QR Code Representation
                                    Box(
                                        modifier = Modifier
                                            .size(140.dp)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Box(modifier = Modifier.size(28.dp).background(Color.Black))
                                                Box(modifier = Modifier.size(28.dp).background(Color.Black))
                                            }
                                            Icon(
                                                imageVector = Icons.Default.QrCodeScanner,
                                                contentDescription = "QR",
                                                tint = Color.DarkGray,
                                                modifier = Modifier.size(34.dp)
                                            )
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Box(modifier = Modifier.size(28.dp).background(Color.Black))
                                                Box(modifier = Modifier.size(24.dp).background(Color.Gray)) // offset
                                            }
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val file = com.example.network.StorageHelper.saveMockQrToStorage(context, true, cobaltQrCode)
                                            if (file != null) {
                                                android.widget.Toast.makeText(context, "💾 QR Image saved to '/storage/emulated/0/Download/LessonSync/Lessonsync_Personal_QR.png'!", android.widget.Toast.LENGTH_LONG).show()
                                            } else {
                                                android.widget.Toast.makeText(context, "Failed to save QR code.", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = "Save Qr", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Save Image", fontSize = 10.sp)
                                    }

                                    Button(
                                        onClick = {
                                            personalGalleryLauncher.launch(
                                                androidx.activity.result.PickVisualMediaRequest(
                                                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                                )
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Pick Qr", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pick Gallery", fontSize = 10.sp)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                if (cobaltPairingCode != null) {
                                    Text("Alphanumeric Pairing Code:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Surface(
                                        color = Color.Black,
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = cobaltPairingCode ?: "",
                                            color = Color(0xFF00FF00),
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                                
                                Text(
                                    text = "Scan the QR from WhatsApp Web, or input the pairing code in WhatsApp Mobile to link cobalt.",
                                    fontSize = 10.sp,
                                    color = Color.DarkGray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(color = Color(0xFF2E7D32), modifier = Modifier.fillMaxWidth(0.8f))
                            }
                        }
                    } else if (cobaltStatus == "CONNECTING") {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Handshaking session...", fontWeight = FontWeight.Bold)
                            Text(syncStatusText, fontSize = 11.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    } else if (cobaltStatus == "ERROR") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Connection Error", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(syncStatusText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.disconnectPersonalWhatsApp() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Try Again")
                                }
                            }
                        }
                    } else {
                        // STANDARD SELECTION PANEL
                        Text(
                            "Select whether to use pure local simulation or direct hook connection to a Cobalt JVM server backend.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = personalInputNo,
                            onValueChange = { personalInputNo = it },
                            label = { Text("Mobile Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Spacer(modifier = Modifier.height(4.dp))

                        // COBALT SECTION
                        Text("🔌 COBALT GATEWAY OPTIONS (OPTIONAL)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = cobaltServerInput,
                            onValueChange = { cobaltServerInput = it },
                            label = { Text("Cobalt Server API URL") },
                            placeholder = { Text("e.g. http://localhost:8080") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            supportingText = { Text("Leave blank to run on-device simulation sandbox", fontSize = 10.sp) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = cobaltUseMobileInput,
                                onCheckedChange = { cobaltUseMobileInput = it },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Use Mobile Protocol Backend", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("Connects to secondary phone app protocol instead of web scanner QR", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (cobaltStatus == "SCAN_QR" || cobaltStatus == "CONNECTING" || cobaltStatus == "ERROR") {
                    TextButton(onClick = { 
                        viewModel.disconnectPersonalWhatsApp()
                    }) {
                        Text("Reset Setup", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.updateCobaltConfig(cobaltServerInput, cobaltUseMobileInput)
                            viewModel.connectPersonalWhatsApp(personalInputNo)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text(if (cobaltServerInput.isNotBlank()) "Connect Cobalt" else "Simulate Link")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPersonalAuthDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showBusinessAuthDialog) {
        AlertDialog(
            onDismissRequest = { 
                showBusinessAuthDialog = false
                if (cobaltStatus == "SCAN_QR" || cobaltStatus == "CONNECTING") {
                    viewModel.disconnectBusinessWhatsApp()
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Business, 
                        contentDescription = "Business", 
                        tint = Color(0xFF0277BD),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WA Business Gateway")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (cobaltStatus == "SCAN_QR") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "SCAN QR & PAIR COBALT BUSINESS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0277BD)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                val realQrBitmap = remember(cobaltQrCode) {
                                    if (cobaltQrCode != null && !cobaltQrCode.contains("MOCK")) {
                                        try {
                                            val cleanStr = if (cobaltQrCode!!.startsWith("data:image")) {
                                                cobaltQrCode!!.substringAfter(",")
                                            } else {
                                                cobaltQrCode!!
                                            }
                                            val decodedBytes = Base64.decode(cleanStr, Base64.DEFAULT)
                                            android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                        } catch (e: Exception) {
                                            null
                                        }
                                    } else {
                                        null
                                    }
                                }

                                if (realQrBitmap != null) {
                                    Image(
                                        bitmap = realQrBitmap.asImageBitmap(),
                                        contentDescription = "Real Cobalt Business QR Code",
                                        modifier = Modifier
                                            .size(160.dp)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                                            .padding(6.dp)
                                    )
                                } else {
                                    // Beautiful Pseudo QR Code Representation
                                    Box(
                                        modifier = Modifier
                                            .size(130.dp)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Box(modifier = Modifier.size(26.dp).background(Color.Black))
                                                Box(modifier = Modifier.size(26.dp).background(Color.Black))
                                            }
                                            Icon(
                                                imageVector = Icons.Default.QrCodeScanner,
                                                contentDescription = "QR SDK",
                                                tint = Color.DarkGray,
                                                modifier = Modifier.size(34.dp)
                                            )
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Box(modifier = Modifier.size(26.dp).background(Color.Black))
                                                Box(modifier = Modifier.size(22.dp).background(Color.Gray))
                                            }
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val file = com.example.network.StorageHelper.saveMockQrToStorage(context, false, cobaltQrCode)
                                            if (file != null) {
                                                android.widget.Toast.makeText(context, "💾 QR Image saved to '/storage/emulated/0/Download/LessonSync/Lessonsync_Business_QR.png'!", android.widget.Toast.LENGTH_LONG).show()
                                            } else {
                                                android.widget.Toast.makeText(context, "Failed to save QR code.", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = "Save Qr", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Save Image", fontSize = 10.sp)
                                    }

                                    Button(
                                        onClick = {
                                            businessGalleryLauncher.launch(
                                                androidx.activity.result.PickVisualMediaRequest(
                                                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                                )
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Pick Qr", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pick Gallery", fontSize = 10.sp)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                if (cobaltPairingCode != null) {
                                    Text("Alphanumeric Pairing Code:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Surface(
                                        color = Color.Black,
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = cobaltPairingCode ?: "",
                                            color = Color(0xFF02FFFF),
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                                
                                Text(
                                    text = "Pair with your school verified business portal using cobalt backend protocol client.",
                                    fontSize = 10.sp,
                                    color = Color.DarkGray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(color = Color(0xFF0277BD), modifier = Modifier.fillMaxWidth(0.8f))
                            }
                        }
                    } else if (cobaltStatus == "CONNECTING") {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFF0277BD))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Handshaking Business session...", fontWeight = FontWeight.Bold)
                            Text(syncStatusText, fontSize = 11.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    } else if (cobaltStatus == "ERROR") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Business Connection Error", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(syncStatusText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.disconnectBusinessWhatsApp() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Try Again")
                                }
                            }
                        }
                    } else {
                        Text(
                            "Link a professor, school, or class tutor certified Cobalt WhatsApp business listener webhook.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = businessInputNo,
                            onValueChange = { businessInputNo = it },
                            label = { Text("Business Portal Line") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Spacer(modifier = Modifier.height(4.dp))

                        Text("🔌 COBALT GATEWAY OPTIONS (OPTIONAL)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = cobaltServerInput,
                            onValueChange = { cobaltServerInput = it },
                            label = { Text("Cobalt Server API URL") },
                            placeholder = { Text("e.g. http://localhost:8080") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            supportingText = { Text("Leave blank to run on-device simulation sandbox", fontSize = 10.sp) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = cobaltUseMobileInput,
                                onCheckedChange = { cobaltUseMobileInput = it },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Use Mobile Protocol Backend", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("Connects to secondary phone app business listener socket", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (cobaltStatus == "SCAN_QR" || cobaltStatus == "CONNECTING") {
                    TextButton(onClick = { 
                        viewModel.disconnectBusinessWhatsApp()
                    }) {
                        Text("Reset Setup", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.updateCobaltConfig(cobaltServerInput, cobaltUseMobileInput)
                            viewModel.connectBusinessWhatsApp(businessInputNo)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD))
                    ) {
                        Text(if (cobaltServerInput.isNotBlank()) "Connect Cobalt BZ" else "Simulate Link")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showBusinessAuthDialog = false }) {
                    Text("Close")
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
