package com.aryaxzell.reminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.aryaxzell.reminder.ui.*
import com.aryaxzell.reminder.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    // Retrieve database and repository from Application class
    private val appRepository by lazy { (application as ReminderApplication).repository }

    // Instantiate ViewModel with Custom Factory
    private val viewModel: ReminderViewModel by viewModels {
        ReminderViewModelFactory(application, appRepository)
    }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Notification permission result handled
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MyApplicationTheme {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainContent(viewModel: ReminderViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val screen = currentScreen) {
                is Screen.Onboarding -> {
                    OnboardingScreen(
                        onContinueClick = { viewModel.completeOnboarding() }
                    )
                }
                is Screen.Dashboard -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToList = { listId -> viewModel.navigateTo(Screen.ListDetail(listId)) },
                        onNavigateToSmartList = { type -> viewModel.navigateTo(Screen.SmartListDetail(type)) },
                        onNavigateToNewList = { listId -> viewModel.navigateTo(Screen.NewList(listId)) }
                    )
                }
                is Screen.ListDetail -> {
                    ListDetailScreen(
                        viewModel = viewModel,
                        listId = screen.listId,
                        onBack = { viewModel.navigateTo(Screen.Dashboard) }
                    )
                }
                is Screen.SmartListDetail -> {
                    ListDetailScreen(
                        viewModel = viewModel,
                        smartType = screen.type,
                        onBack = { viewModel.navigateTo(Screen.Dashboard) }
                    )
                }
                is Screen.NewList -> {
                    NewListScreen(
                        viewModel = viewModel,
                        listId = screen.listId,
                        onDismiss = { viewModel.navigateTo(Screen.Dashboard) }
                    )
                }
            }
        }
    }
}
