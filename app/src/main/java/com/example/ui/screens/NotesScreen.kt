package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NoteItem
import com.example.ui.LessonSyncViewModel
import com.example.ui.components.CustomChip
import com.example.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: LessonSyncViewModel,
    modifier: Modifier = Modifier
) {
    val allNotes by viewModel.allNotes.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var selectedNote by remember { mutableStateOf<NoteItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Writing note states
    var isEditing by remember { mutableStateOf(false) }
    var noteTitleInput by remember { mutableStateOf("") }
    var noteContentInput by remember { mutableStateOf("") }

    var activeViewTab by remember { mutableStateOf("LOCAL") } // "LOCAL", "DOCS"

    val filteredNotes = remember(allNotes, searchQuery) {
        allNotes.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.content.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SectionHeader(
            title = "Notes & Sync Docs",
            action = {
                if (selectedNote == null && !isEditing) {
                    IconButton(
                        onClick = {
                            isEditing = true
                            selectedNote = null
                            noteTitleInput = ""
                            noteContentInput = ""
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Note")
                    }
                }
            }
        )

        // Local Notes vs Documentation Tabs
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CustomChip(text = "🗒️ Course Notes Manager", selected = activeViewTab == "LOCAL", onClick = { activeViewTab = "LOCAL"; selectedNote = null; isEditing = false })
            CustomChip(text = "📒 Google Notes & MD Docs", selected = activeViewTab == "DOCS", onClick = { activeViewTab = "DOCS"; selectedNote = null; isEditing = false })
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (activeViewTab == "LOCAL") {
            // NOTES CREATION AND EDITING ENGINE
            if (isEditing) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (selectedNote == null) "📝 CREATE COURSE NOTE" else "✏️ EDIT COURSE NOTE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = noteTitleInput,
                            onValueChange = { noteTitleInput = it },
                            label = { Text("Note Title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = noteContentInput,
                            onValueChange = { noteContentInput = it },
                            label = { Text("Content (Supports Markdown format)") },
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            maxLines = 15
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { isEditing = false; selectedNote = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    if (noteTitleInput.isNotBlank()) {
                                        if (selectedNote == null) {
                                            viewModel.createNote(noteTitleInput, noteContentInput)
                                        } else {
                                            viewModel.editNote(selectedNote?.id ?: 0, noteTitleInput, noteContentInput)
                                        }
                                        isEditing = false
                                        selectedNote = null
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Save Note")
                            }
                        }
                    }
                }
            } else if (selectedNote != null) {
                // READ INDIVIDUAL NOTE SCREEN
                val note = selectedNote ?: return
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "MEMBER NOTE DETAILS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { selectedNote = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(note.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        Text(note.content, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Markdown Export and Google Notes Copy CTAs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val mdDocument = """
                                    # ${note.title}
                                    Modified: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(note.updatedTimestamp))}
                                    
                                    ${note.content}
                                    """.trimIndent()
                                    clipboardManager.setText(AnnotatedString(mdDocument))
                                    Toast.makeText(context, "Exported as markdown (.md) to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Copy MD", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy as .md", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(note.content))
                                    Toast.makeText(context, "Copied content! Redirecting to Google Keep.", Toast.LENGTH_LONG).show()

                                    // Launch Google Keep intent
                                    try {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            `package` = "com.google.android.keep"
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TITLE, note.title)
                                            putExtra(Intent.EXTRA_TEXT, note.content)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Fallback open via web or app switcher if not installed
                                        val keepUrl = "https://keep.google.com/"
                                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(keepUrl))
                                        context.startActivity(webIntent)
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBC02D)) // Keep yellow
                            ) {
                                Icon(Icons.Default.Launch, contentDescription = "Keep", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync Google Notes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    isEditing = true
                                    noteTitleInput = note.title
                                    noteContentInput = note.content
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit content")
                            }

                            Spacer(modifier = Modifier.weight(1.0f))

                            TextButton(
                                onClick = {
                                    viewModel.deleteNote(note)
                                    selectedNote = null
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete Note")
                            }
                        }
                    }
                }
            } else {
                // BROWSE LOCAL NOTES LIST
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search written notes...", fontSize = 14.sp) },
                    prefix = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                if (filteredNotes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No notes found. Tap '+' to compose an entry!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    filteredNotes.forEach { nt ->
                        Card(
                            onClick = { selectedNote = nt },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(nt.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Read", modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = nt.content,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // DOCUMENTATION FOR NOTES, MARKDOWN, AND GOOGLE KEEP
            DocumentationPanel(viewModel = viewModel, clipboardManager = clipboardManager, context = context)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DocumentationPanel(
    viewModel: LessonSyncViewModel,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    context: android.content.Context
) {
    var serverRunning by remember { mutableStateOf(com.example.network.LocalApiServer.isRunning) }
    var currentPort by remember { mutableStateOf(com.example.network.LocalApiServer.activePort) }

    // 1. Storage Downloads Card
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Download, contentDescription = "Download Center", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "📥 PHYSICAL STORAGE DOWNLOADS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Export all your written course notes, study guides, vocabulary lists, and indexed exam papers directly to your phone's external shared storage at '/storage/emulated/0/Download/LessonSync/'. This allows immediate retrieval and programmatic parsing from Termux, file explorers, or markdown viewers (like Obsidian).",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    viewModel.exportAllDocsToStorage { count ->
                        Toast.makeText(
                            context,
                            "Success! Downloaded $count academic documents and guides to '/storage/emulated/0/Download/LessonSync/'!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Academic Docs to Storage", fontWeight = FontWeight.Bold)
            }
        }
    }

    // 2. Termux Programmatic Console Card
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = "Termux",
                        tint = if (serverRunning) Color(0xFF2E7D32) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "🔌 TERMUX PROGRAMMATIC CONSOLE API",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (serverRunning) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                    )
                }

                Switch(
                    checked = serverRunning,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            com.example.network.LocalApiServer.start(context, viewModel.repository, 9090)
                        } else {
                            com.example.network.LocalApiServer.stop()
                        }
                        serverRunning = com.example.network.LocalApiServer.isRunning
                        currentPort = com.example.network.LocalApiServer.activePort
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2E7D32), checkedTrackColor = Color(0xFFC8E6C9))
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Launches a local high-performance background loop inside LessonSync at localhost port $currentPort. Termux scripts running on this device or ADB shells can invoke standard endpoints to programmatically read/write data.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (serverRunning) Color(0xFF2E7D32) else Color.Red, RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (serverRunning) "Server running at: http://127.0.0.1:$currentPort/" else "Console API server is OFFLINE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (serverRunning) Color(0xFF2E7D32) else Color.Red
                        )
                    }
                    if (serverRunning) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Access via Termux or Localhost clients is fully enabled.",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("📟 QUICK REUSABLE TERMUX CURL COMMANDS", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))

            val sampleCommands = listOf(
                "Handshake" to "curl http://localhost:$currentPort/",
                "Fetch Notes JSON" to "curl http://localhost:$currentPort/notes",
                "Trigger Markdown Downloads" to "curl http://localhost:$currentPort/export_docs",
                "Inject Note" to "curl -X POST -d \"title=TermuxHW&content=FinishQuadraticFormulas\" http://localhost:$currentPort/add_note",
                "Inject Message" to "curl -X POST -d \"messageText=ReviewChapter3Page12\" http://localhost:$currentPort/add_message"
            )

            sampleCommands.forEach { (label, cmd) ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        TextButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(cmd))
                                Toast.makeText(context, "$label command copied!", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontSize = 10.sp)
                        }
                    }
                    Surface(
                        color = Color.Black,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = cmd,
                            color = Color(0xFF00FF00),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "💻 COMPLETE TERMUX INTEGRATION SCRIPT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                val scriptText = """
                # Paste directly into your Termux terminal on Android:
                cat << 'EOF' > lessonsync.sh
                #!/bin/bash
                PORT=$currentPort
                DIR="/storage/emulated/0/Download/LessonSync"
                
                echo "=============================="
                echo " === LESSONSYNC TERMUX SCRIPT ==="
                echo "=============================="
                echo "Connecting local API at port: ${'$'}PORT"
                echo "Mounted document directory: ${'$'}DIR"
                
                # Check server connectivity
                if curl -s -f http://localhost:${'$'}PORT/ >/dev/null; then
                    echo "✅ Local API Server is responsive"
                else
                    echo "❌ Local API Offline. Start Server in Notes tab."
                fi
                
                # List local Markdown resources
                echo -e "\n📁 EXPORTED DOCUMENT FILES:"
                if [ -d "${'$'}DIR" ]; then
                    find "${'$'}DIR" -name "*.md" | sed 's|^/storage/emulated/0/|  |'
                else
                    echo "  No files found. Tap 'Export Academic Docs' in App."
                fi
                
                # Fetch recent chats programmatically
                echo -e "\n💬 RECENT MESSAGES RECEIVED:"
                curl -s http://localhost:${'$'}PORT/messages | cut -c1-150
                
                # Send terminal message back to the app
                echo -e "\n📨 PUSH NOTES BACK TO THE SCREEN?"
                echo "Press enter to exit, or type a note to send:"
                read txt
                if [ ! -z "${'$'}txt" ]; then
                    curl -s -X POST -d "messageText=${'$'}txt" http://localhost:${'$'}PORT/add_message
                    echo "✅ Injected commandline feed note successfully!"
                fi
                EOF
                chmod +x lessonsync.sh
                ./lessonsync.sh
                """.trimIndent()

                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(scriptText))
                        Toast.makeText(context, "Full script copied to clipboard! Paste into Termux.", Toast.LENGTH_LONG).show()
                    },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy script", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Script", fontSize = 10.sp)
                }
            }
            Text(
                "Copy this entire unified script, launch Termux, and paste it. It automatically interfaces with the offline markdown directory and POST/GET APIs.",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Surface(
                color = Color.Black,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = """
                    # In Termux: paste and press enter!
                    cat << 'EOF' > lessonsync.sh
                    #!/bin/bash
                    PORT=$currentPort
                    DIR="/storage/emulated/0/Download/LessonSync"
                    # Checks server & reads notes:
                    ./lessonsync.sh
                    """.trimIndent(),
                    color = Color(0xFF00FF00),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "NOTES INTEGRATION & EXPORTS GUIDE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Section 1: Local Notes in-app
            DocSection(
                title = "1. Managing Notes in LessonSync 📱",
                desc = "Write, edit, and keep track of lesson notes directly in the 'Course Notes Manager' sub-tab. All entries support structured rich markdown styling so you can outline core mathematical formulas and definitions easily."
            )

            // Section 2: Markdown exports
            DocSection(
                title = "2. Working with Markdown (.md) 📝",
                desc = "Need your notes outside the app? We provide one-click exports! Tap 'Copy as .md' on any note card to automatically structure headings (#), timestamp labels, and code structures copyable to Obsidian, Notion, or text editors directly."
            )

            // Section 3: Google notes / Google keep
            DocSection(
                title = "3. Syncing with Google Keep / Google Notes 📒",
                desc = "To sync with your Google account seamlessly: Tap 'Sync Google Notes' inside any note. Under the hood, this packs the note text, saves it, and invokes an Android action Intent launching the official Google Keep, ready for you to paste or share directly with standard checklists!"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action: open Google notes Keep directly
            Button(
                onClick = {
                    try {
                        val launchKeep = context.packageManager.getLaunchIntentForPackage("com.google.android.keep")
                        if (launchKeep != null) {
                            context.startActivity(launchKeep)
                        } else {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://keep.google.com/"))
                            context.startActivity(webIntent)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Redirecting to Google Keep web portal.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBC02D)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = "Keep", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Google Keep / Google Notes", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
fun DocSection(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
    }
}
