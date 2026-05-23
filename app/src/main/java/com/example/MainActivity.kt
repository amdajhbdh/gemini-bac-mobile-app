package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.LessonSyncRepository
import com.example.ui.AppTab
import com.example.ui.LessonSyncViewModel
import com.example.ui.LessonSyncViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Establish the Room Database and Repository singleton interfaces
        val database = AppDatabase.getDatabase(this)
        val repository = LessonSyncRepository(database)

        // Instantiate state engine ViewModel through customized factory constructor
        val factory = LessonSyncViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[LessonSyncViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val currentTab by viewModel.currentTab.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val navigationItems = listOf(
                            NavigationItem(AppTab.DASHBOARD, "Chats", Icons.Default.Chat),
                            NavigationItem(AppTab.SCHEDULE, "Calendar", Icons.Default.CalendarMonth),
                            NavigationItem(AppTab.OCR_AGENT, "AI OCR", Icons.Default.AutoAwesome),
                            NavigationItem(AppTab.STUDY_ASSISTANT, "Studies", Icons.Default.School),
                            NavigationItem(AppTab.NOTES, "Notes", Icons.Default.Book),
                            NavigationItem(AppTab.ANALYTICS, "Metrics", Icons.Default.BarChart)
                        )

                        NavigationBar {
                            navigationItems.forEach { item ->
                                NavigationBarItem(
                                    selected = currentTab == item.tab,
                                    onClick = { viewModel.setTab(item.tab) },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label, fontSize = 9.sp) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            AppTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                            AppTab.SCHEDULE -> ScheduleScreen(viewModel = viewModel)
                            AppTab.OCR_AGENT -> OcrScreen(viewModel = viewModel)
                            AppTab.STUDY_ASSISTANT -> StudyAssistantScreen(viewModel = viewModel)
                            AppTab.NOTES -> NotesScreen(viewModel = viewModel)
                            AppTab.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val tab: AppTab,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
