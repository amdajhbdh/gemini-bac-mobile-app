package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Flashcard
import com.example.data.StudyGuide
import com.example.data.IndexedExam
import com.example.data.WordItem
import com.example.data.LanguageQa
import com.example.ui.LessonSyncViewModel
import com.example.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyAssistantScreen(
    viewModel: LessonSyncViewModel,
    modifier: Modifier = Modifier
) {
    val allGuides by viewModel.allGuides.collectAsState()
    val researchLoading by viewModel.researchLoading.collectAsState()
    val selectedGuideId by viewModel.selectedGuideId.collectAsState()
    val currentFlashcards by viewModel.currentFlashcards.collectAsState()

    // Language additions
    val allExams by viewModel.allExams.collectAsState()
    val allWords by viewModel.allWords.collectAsState()
    val allQa by viewModel.allQa.collectAsState()
    val qaLoading by viewModel.qaLoading.collectAsState()
    val examParsingLoading by viewModel.examParsingLoading.collectAsState()

    var activeMainTab by remember { mutableStateOf("GUIDES") } // "GUIDES", "EXAMS", "WORDS", "QA"

    var subjectInput by remember { mutableStateOf("Language Studies") }
    var topicInput by remember { mutableStateOf("Spanish Verbs & Vocabs") }
    var expandedForm by remember { mutableStateOf(false) }

    val activeGuide = remember(allGuides, selectedGuideId) {
        allGuides.find { it.id == selectedGuideId }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SectionHeader(
            title = "AI Study & Languages Hub",
            action = {
                if (activeMainTab == "GUIDES") {
                    TextButton(onClick = { expandedForm = !expandedForm }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (expandedForm) Icons.Default.ExpandLess else Icons.Default.Search,
                                contentDescription = "Search"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (expandedForm) "Collapse Form" else "Research Tool")
                        }
                    }
                }
            }
        )

        // ============================================
        // MODERN TOP HORIZONTAL TAB SELECTOR
        // ============================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                Pair("GUIDES", "Study Guides"),
                Pair("EXAMS", "Exams Indexer"),
                Pair("WORDS", "Word Lists"),
                Pair("QA", "Coach Q&A"),
                Pair("CLOUDSYNC", "Cloud Database Sync")
            )
            
            tabs.forEach { (key, label) ->
                val selected = activeMainTab == key
                val containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                
                Surface(
                    onClick = { activeMainTab = key },
                    color = containerColor,
                    contentColor = contentColor,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(2.dp),
                    tonalElevation = if (selected) 4.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val icon = when (key) {
                            "GUIDES" -> Icons.Default.MenuBook
                            "EXAMS" -> Icons.Default.LibraryBooks
                            "WORDS" -> Icons.Default.Translate
                            "QA" -> Icons.Default.Chat
                            else -> Icons.Default.Cloud
                        }
                        Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ============================================
        // TAB 1 DETAIL RENDERING: study guides
        // ============================================
        if (activeMainTab == "GUIDES") {
            // Topic Research Generator Form (collapsible)
            AnimatedVisibility(
                visible = expandedForm || allGuides.isEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "RESEARCH DISCOVERY ENGINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = subjectInput,
                            onValueChange = { subjectInput = it },
                            label = { Text("School Course Subject") },
                            placeholder = { Text("e.g. French, Chemistry, Computer Science") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Class, contentDescription = "Course") }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = topicInput,
                            onValueChange = { topicInput = it },
                            label = { Text("Research Topic / Sub-subject") },
                            placeholder = { Text("e.g. Subjunctive Mood, Molecular Bonding") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = "Topic") }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (researchLoading) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Automating academic topic research via Gemini...", fontSize = 11.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (subjectInput.isNotBlank() && topicInput.isNotBlank()) {
                                        viewModel.searchSubjectAndGenerate(subjectInput, topicInput)
                                        expandedForm = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Go")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Research Subject and Compile Guide", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Main Guide Panel View (If a guide has been selected)
            if (activeGuide != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                activeGuide.subject.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                activeGuide.topic,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        IconButton(
                            onClick = { viewModel.selectGuide(null) },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sub tabs for Guide details vs flashcards quiz
                var activeSubTab by remember { mutableStateOf("GUIDE") } // "GUIDE", "FLASHCARDS"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { activeSubTab = "GUIDE" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeSubTab == "GUIDE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (activeSubTab == "GUIDE") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Read", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Read Guide")
                    }

                    Button(
                        onClick = { activeSubTab = "FLASHCARDS" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeSubTab == "FLASHCARDS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (activeSubTab == "FLASHCARDS") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = "Cards", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Flashcards (${currentFlashcards.size})")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (activeSubTab == "GUIDE") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = activeGuide.content,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    if (currentFlashcards.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No flashcards found for this topic.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        FlashcardQuizEngine(
                            flashcards = currentFlashcards,
                            onRatePerformance = { card, score ->
                                viewModel.updateFlashcardScore(card, score)
                            }
                        )
                    }
                }
            } else {
                // Show library of researched guides
                Text(
                    "My Study Guides Library",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                if (allGuides.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Empty",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Your library is empty. Generate study reviews above!",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    allGuides.forEach { gd ->
                        Card(
                            onClick = { viewModel.selectGuide(gd.id) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = gd.subject,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(gd.topic, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(gd.summary, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = "View", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // ============================================
        // TAB 2 DETAIL RENDERING: exams indexer
        // ============================================
        else if (activeMainTab == "EXAMS") {
            var showAddExamForm by remember { mutableStateOf(false) }
            var examTitleInput by remember { mutableStateOf("") }
            var examSubjectInput by remember { mutableStateOf("French") }
            var examContentInput by remember { mutableStateOf("") }
            var activeExamDetail by remember { mutableStateOf<IndexedExam?>(null) }

            if (activeExamDetail != null) {
                // EXAM DETAIL VIEW OVERLAY
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { activeExamDetail = null }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Exam Paper Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            
                            IconButton(onClick = {
                                viewModel.deleteIndexedExam(activeExamDetail!!.id)
                                activeExamDetail = null
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Exam", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Text(activeExamDetail!!.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Text(activeExamDetail!!.subject, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = activeExamDetail!!.content,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )
                    }
                }
            } else {
                // GENERAL CORES FOR EXAMS TAB
                Button(
                    onClick = { showAddExamForm = !showAddExamForm },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(if (showAddExamForm) Icons.Default.Close else Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (showAddExamForm) "Cancel Index Form" else "Index & Upload New Past Exam", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                AnimatedVisibility(
                    visible = showAddExamForm,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("INDEX NEW EXAM DOCUMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = examTitleInput,
                                onValueChange = { examTitleInput = it },
                                label = { Text("Exam Title / Paper Year") },
                                placeholder = { Text("e.g. French B1 June 2025 Mock") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = examSubjectInput,
                                onValueChange = { examSubjectInput = it },
                                label = { Text("Target Language (Subject)") },
                                placeholder = { Text("French, German, English, Spanish") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = examContentInput,
                                onValueChange = { examContentInput = it },
                                label = { Text("Exam Text / Content / Prompts") },
                                placeholder = { Text("Paste entire paper transcription here...") },
                                minLines = 4,
                                maxLines = 8,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (examTitleInput.isNotBlank() && examContentInput.isNotBlank()) {
                                        viewModel.indexPastExam(examTitleInput, examSubjectInput, examContentInput)
                                        examTitleInput = ""
                                        examContentInput = ""
                                        showAddExamForm = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Index Exam paper & Save to Database", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "Indexed Language Past Exams",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Select an exam to read it, or use the AI Extract button to populate your Flashcards and word collections from its resources.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (allExams.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        Text("No past exams indexed yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    if (examParsingLoading) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("AI Parsing Exam via Gemini...", fontWeight = FontWeight.Bold)
                                Text("Creating custom vocabulary and flashcards...", fontSize = 11.sp)
                            }
                        }
                    }

                    allExams.forEach { exam ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = exam.subject,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(exam.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Row {
                                        IconButton(onClick = { activeExamDetail = exam }) {
                                            Icon(Icons.Default.MenuBook, contentDescription = "Read", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { viewModel.runExamAiExtraction(exam) }) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = "Parse AI", tint = Color(0xFFF7941D))
                                        }
                                    }
                                }

                                Text(
                                    text = exam.summary,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                                
                                Button(
                                    onClick = { viewModel.runExamAiExtraction(exam) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                                    modifier = Modifier.fillMaxWidth().height(32.dp).padding(top = 8.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI", modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI Extract Vocabularies & Flashcards", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ============================================
        // TAB 3 DETAIL RENDERING: Word Glossaries
        // ============================================
        else if (activeMainTab == "WORDS") {
            var showAddWordForm by remember { mutableStateOf(false) }
            var wordInput by remember { mutableStateOf("") }
            var wordMeaningInput by remember { mutableStateOf("") }
            var wordExampleInput by remember { mutableStateOf("") }
            var wordLanguageInput by remember { mutableStateOf("French") }
            var wordDifficultyInput by remember { mutableStateOf("Medium") }

            var selectFilterLang by remember { mutableStateOf("All") }
            var searchQuery by remember { mutableStateOf("") }

            Button(
                onClick = { showAddWordForm = !showAddWordForm },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(if (showAddWordForm) Icons.Default.Close else Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (showAddWordForm) "Cancel Word Form" else "Add Custom Vocabulary Word", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedVisibility(
                visible = showAddWordForm,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("ADD WORD TO COLLECTION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = wordInput,
                            onValueChange = { wordInput = it },
                            label = { Text("Word / Idiom (in target language)") },
                            placeholder = { Text("e.g. S'entraîner") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = wordMeaningInput,
                            onValueChange = { wordMeaningInput = it },
                            label = { Text("Translation / Definition") },
                            placeholder = { Text("e.g. To practice / train") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = wordExampleInput,
                            onValueChange = { wordExampleInput = it },
                            label = { Text("Example Sentence (Contextualized)") },
                            placeholder = { Text("elle s'entraîne tous les jours") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = wordLanguageInput,
                                onValueChange = { wordLanguageInput = it },
                                label = { Text("Language") },
                                placeholder = { Text("French, Spanish") },
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = wordDifficultyInput,
                                onValueChange = { wordDifficultyInput = it },
                                label = { Text("Difficulty") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (wordInput.isNotBlank() && wordMeaningInput.isNotBlank()) {
                                    viewModel.addVocabularyWord(wordInput, wordMeaningInput, wordExampleInput, wordLanguageInput, wordDifficultyInput)
                                    wordInput = ""
                                    wordMeaningInput = ""
                                    wordExampleInput = ""
                                    showAddWordForm = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Insert Word into Glossary", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter row for database query
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Word / Meaning") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick language selector chips
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "French", "Spanish").forEach { lang ->
                    val isSelected = selectFilterLang == lang
                    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    val textClr = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    Surface(
                        onClick = { selectFilterLang = lang },
                        color = color,
                        contentColor = textClr,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(lang, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("My Word Lists Glossary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            val filteredWords = allWords.filter {
                val matchesLang = selectFilterLang == "All" || it.language.equals(selectFilterLang, ignoreCase = true)
                val matchesQuery = it.word.contains(searchQuery, ignoreCase = true) || it.definition.contains(searchQuery, ignoreCase = true)
                matchesLang && matchesQuery
            }

            if (filteredWords.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Text("No words match criteria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                filteredWords.forEach { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.word,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Definition: ${item.definition}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = item.language,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteVocabularyWord(item.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            if (item.example.isNotBlank()) {
                                Text(
                                    text = "Example context: \"${item.example}\"",
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ============================================
        // TAB 4 DETAIL RENDERING: AI Coach Chat Q&A
        // ============================================
        else if (activeMainTab == "QA") {
            var qaQuestionInput by remember { mutableStateOf("") }

            Text(
                "Ask your Language Coach",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "The Coach parses your entire indexed exams database, custom vocabulary glossaries, and notes to provide rich answers to help you study.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Chat History Viewport
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    if (allQa.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.QuestionAnswer, contentDescription = "Empty", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Ask French or Spanish grammar queries to begin!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        allQa.forEach { qa ->
                            // User Message Bubble (Right)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
                                ) {
                                    Text(qa.question, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                                }
                            }

                            // AI Coach Message Bubble (Left)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp),
                                    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            qa.answer,
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (qaLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Coach is answering via Gemini...", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Prompt suggestions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val suggestions = listOf(
                    "Explain French Subjunctive Mood",
                    "German conjugation helper tips",
                    "Compare Imperfect vs Preterite",
                    "What words should I practice?"
                )
                
                suggestions.forEach { prompt ->
                    Surface(
                        onClick = { viewModel.queryLanguageCoach(prompt) },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(prompt, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Typing block
            OutlinedTextField(
                value = qaQuestionInput,
                onValueChange = { qaQuestionInput = it },
                label = { Text("Ask your Coach a language query") },
                placeholder = { Text("e.g. Translate 'Examen' or explain conjugation") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (qaQuestionInput.isNotBlank()) {
                                viewModel.queryLanguageCoach(qaQuestionInput)
                                qaQuestionInput = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { viewModel.clearLanguageQa() }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Chat History", fontSize = 11.sp)
                }
            }
        }
        
        // ============================================
        // TAB 5 DETAIL RENDERING: Turso and Neon Cloud Sync
        // ============================================
        else if (activeMainTab == "CLOUDSYNC") {
            val tursoUrlState by viewModel.tursoUrl.collectAsState()
            val tursoTokenState by viewModel.tursoToken.collectAsState()
            val neonUrlState by viewModel.neonUrl.collectAsState()
            val neonTokenState by viewModel.neonToken.collectAsState()
            val useSimulatedDbState by viewModel.useSimulatedDb.collectAsState()
            val syncLoadingState by viewModel.syncLoading.collectAsState()
            val syncStatusTextState by viewModel.syncStatusText.collectAsState()
            val lastSyncTimeState by viewModel.lastSyncTime.collectAsState()

            var inputTursoUrl by remember { mutableStateOf(tursoUrlState) }
            var inputTursoToken by remember { mutableStateOf(tursoTokenState) }
            var inputNeonUrl by remember { mutableStateOf(neonUrlState) }
            var inputNeonToken by remember { mutableStateOf(neonTokenState) }
            var inputUseSimulated by remember { mutableStateOf(useSimulatedDbState) }

            // Match states with user inputs dynamically
            LaunchedEffect(tursoUrlState, tursoTokenState, neonUrlState, neonTokenState, useSimulatedDbState) {
                inputTursoUrl = tursoUrlState
                inputTursoToken = tursoTokenState
                inputNeonUrl = neonUrlState
                inputNeonToken = neonTokenState
                inputUseSimulated = useSimulatedDbState
            }

            Text(
                "Multi-Cloud Database Sync",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Connect LessonSync notes and vocabulary glossaries to a custom Turso SQL database and Neon Serverless PostgreSQL database. Local SQLite (Room) acts as standard fallback callback whenever networks are offline.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Dynamic Connection Status Badge
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (inputUseSimulated) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    } else if (inputTursoUrl.isBlank() || inputNeonUrl.isBlank()) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                    } else {
                        Color(0xFF2E7D32).copy(alpha = 0.15f)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .border(
                        width = 1.dp,
                        color = if (inputUseSimulated) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        } else if (inputTursoUrl.isBlank() || inputNeonUrl.isBlank()) {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        } else {
                            Color(0xFF2E7D32).copy(alpha = 0.4f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (inputUseSimulated) Icons.Default.Info else if (inputTursoUrl.isBlank()) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = "Status ID",
                        tint = if (inputUseSimulated) {
                            MaterialTheme.colorScheme.primary
                        } else if (inputTursoUrl.isBlank() || inputNeonUrl.isBlank()) {
                            MaterialTheme.colorScheme.error
                        } else {
                            Color(0xFF2E7D32)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (inputUseSimulated) {
                                "Demo Mode: Simulated Cloud Sync Enabled"
                            } else if (inputTursoUrl.isBlank() || inputNeonUrl.isBlank()) {
                                "Local Callback Backup Mode Active"
                            } else {
                                "Multi-Cloud Direct Pipeline Connected"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (inputUseSimulated) {
                                "Verification engine running on simulated servers callback pipeline."
                            } else if (inputTursoUrl.isBlank() || inputNeonUrl.isBlank()) {
                                "All academic plans securely stored locally inside SQLite (Room Database) fallback."
                            } else {
                                "Active handshakes route uploads through Turso (libSQL) & Neon cloud systems."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Sync API Configuration Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "DATABASE ENDPOINT CREDENTIALS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Simulated database toggle first
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Dns, contentDescription = "Simulated", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Interactive Cloud Demo Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Let's testing of cloud flow without direct databases", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = inputUseSimulated,
                            onCheckedChange = { inputUseSimulated = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AnimatedVisibility(visible = !inputUseSimulated) {
                        Column {
                            // Turso Parameters
                            Text("1. TURSO (LIBSQL SQLITE EDGE)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            OutlinedTextField(
                                value = inputTursoUrl,
                                onValueChange = { inputTursoUrl = it },
                                label = { Text("Turso DB HTTP Endpoint URL") },
                                placeholder = { Text("e.g. https://your-db-name.turso.io") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = "Url", tint = MaterialTheme.colorScheme.secondary) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = inputTursoToken,
                                onValueChange = { inputTursoToken = it },
                                label = { Text("Turso Security Authorization Token") },
                                placeholder = { Text("Paste auth bearer token...") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = "Token", tint = MaterialTheme.colorScheme.secondary) }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Neon parameters
                            Text("2. NEON DB (SERVERLESS POSTGRESQL)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = inputNeonUrl,
                                onValueChange = { inputNeonUrl = it },
                                label = { Text("Neon HTTP SQL Endpoint URL") },
                                placeholder = { Text("e.g. https://<project-id>.aws.neon.tech/sql") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.CloudQueue, contentDescription = "Url", tint = Color(0xFF00E676)) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = inputNeonToken,
                                onValueChange = { inputNeonToken = it },
                                label = { Text("Neon Database API Auth Key / Token") },
                                placeholder = { Text("Paste Neon serverless auth token...") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = "Token", tint = Color(0xFF00E676)) }
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.updateCloudConfig(
                                tursoUrl = inputTursoUrl,
                                tursoToken = inputTursoToken,
                                neonUrl = inputNeonUrl,
                                neonToken = inputNeonToken,
                                useSimulated = inputUseSimulated
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Settings")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Database Configurations", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sync Control Panel Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "DATABASE SYNCHRONIZATION CONSOLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.testCloudConnections() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Speed, contentDescription = "Test", modifier = Modifier.size(18.dp))
                                Text("Check Link", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { viewModel.pushLocalDataToCloud() },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CloudUpload, contentDescription = "Push", modifier = Modifier.size(18.dp))
                                Text("Push to Clouds", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { viewModel.pullDataFromCloud() },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CloudDownload, contentDescription = "Pull", modifier = Modifier.size(18.dp))
                                Text("Pull from Clouds", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sync Terminal Logs Box
                    Text(
                        "TERMINAL STATUS LOGS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            if (syncLoadingState) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color(0xFF00E676))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Executing Cloud Transaction...", color = Color(0xFF00E676), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            Text(
                                text = syncStatusTextState,
                                color = Color(0xFFECEFF1),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    if (lastSyncTimeState > 0L) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val dateFormatted = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(lastSyncTimeState))
                        Text(
                            text = "Last Synced: $dateFormatted",
                            fontSize = 10.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Explanation Tip card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Offline Info", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("How does the callback fallback work?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "1. When Turso or Neon DB are unreachable or credentials are unconfigured, LessonSync safely defaults all edits, exams, and notes into your local Room database callback.\n" +
                        "2. This secures offline operation, ensuring that your study materials are never lost during exams or when studying in flights and subways.\n" +
                        "3. Once back online, simply click \"Push to Clouds\" to upload any offline modifications onto Turso/Neon in batch sequences.",
                        fontSize = 11.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun FlashcardQuizEngine(
    flashcards: List<Flashcard>,
    onRatePerformance: (Flashcard, Boolean) -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var flipped by remember { mutableStateOf(false) }

    val currentCard = flashcards.getOrNull(currentIndex) ?: return

    val rotationAngle by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress text indicator
        Text(
            text = "Flashcard Quiz ${currentIndex + 1} of ${flashcards.size}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 3D Rotation Card visualizer
        Card(
            onClick = { flipped = !flipped },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .graphicsLayer {
                    rotationY = rotationAngle
                    cameraDistance = 8 * density
                },
            colors = CardDefaults.cardColors(
                containerColor = if (flipped) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rotationAngle <= 90f) {
                    // Front of card : Question
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(currentCard.category.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = currentCard.question,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("💡 Tap Card to Flip", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                    }
                } else {
                    // Back of card : Answer
                    Column(
                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✅ SOLUTION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentCard.answer,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Success / Failure voting metrics (only visible when flipped review content)
        if (flipped) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onRatePerformance(currentCard, false)
                        flipped = false
                        if (currentIndex < flashcards.size - 1) currentIndex++ else currentIndex = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "No")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Need Work", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        onRatePerformance(currentCard, true)
                        flipped = false
                        if (currentIndex < flashcards.size - 1) currentIndex++ else currentIndex = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Yes")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Got It Right ✅", fontSize = 12.sp)
                }
            }
        } else {
            // General Next button representation if they just want to skip
            TextButton(
                onClick = {
                    if (currentIndex < flashcards.size - 1) currentIndex++ else currentIndex = 0
                }
            ) {
                Text("Skip Card Index ➡️")
            }
        }
    }
}
