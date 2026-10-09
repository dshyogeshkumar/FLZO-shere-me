package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.components.AppBottomNavigationBar
import com.example.ui.screens.AboutDeveloperScreen
import com.example.ui.screens.AppShareHubScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PhoneCloneScreen
import com.example.ui.screens.ReceiveWaitScreen
import com.example.ui.screens.SendFileSelectScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SpeedTestScreen
import com.example.ui.screens.TransferActiveScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Back handling to return to Home screen
    if (currentScreen != AppScreen.Home) {
        BackHandler {
            viewModel.navigateTo(AppScreen.Home)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090C10)),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            // Display Bottom Navigation Bar on top-level screens
            if (currentScreen in listOf(
                    AppScreen.Home,
                    AppScreen.SendSelect,
                    AppScreen.TransferActive,
                    AppScreen.History,
                    AppScreen.Settings
                )
            ) {
                AppBottomNavigationBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF090C10))
        ) {
            when (currentScreen) {
                AppScreen.Home -> HomeScreen(viewModel = viewModel)
                AppScreen.Files, AppScreen.SendSelect -> SendFileSelectScreen(viewModel = viewModel)
                AppScreen.ReceiveWait -> ReceiveWaitScreen(viewModel = viewModel)
                AppScreen.TransferActive -> TransferActiveScreen(viewModel = viewModel)
                AppScreen.History -> HistoryScreen(viewModel = viewModel)
                AppScreen.Settings -> SettingsScreen(viewModel = viewModel)
                AppScreen.AboutDeveloper -> AboutDeveloperScreen(viewModel = viewModel)
                AppScreen.PhoneClone -> PhoneCloneScreen(viewModel = viewModel)
                AppScreen.AppShareHub -> AppShareHubScreen(viewModel = viewModel)
                AppScreen.Diagnostics -> DiagnosticsScreen(viewModel = viewModel)
                AppScreen.SpeedTest -> SpeedTestScreen(viewModel = viewModel)
            }
        }
    }
}
