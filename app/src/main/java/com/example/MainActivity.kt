package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.HistoryScreen
import com.example.ui.HomeScreen
import com.example.ui.MediaViewModel
import com.example.ui.components.AdOverlay
import com.example.ui.components.BottomNavBar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private var sharedUrlFromIntent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        extractSharedUrl(intent)

        setContent {
            MyApplicationTheme {
                val viewModel: MediaViewModel = viewModel()

                // Check for incoming shared URL from other apps
                LaunchedEffect(sharedUrlFromIntent) {
                    sharedUrlFromIntent?.let {
                        viewModel.updateInputUrl(it)
                        viewModel.startExtraction()
                    }
                }

                // Storage permissions launcher
                val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                    )
                } else {
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { /* Permissions handled gracefully */ }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(permissionsToRequest)
                }

                val isEnglish by viewModel.isEnglish.collectAsState()
                val currentTab by viewModel.currentTab.collectAsState()
                val historyList by viewModel.historyList.collectAsState()
                val historyCount by viewModel.historyCount.collectAsState()
                val showAdDialog by viewModel.showAdDialog.collectAsState()
                val adTimerSeconds by viewModel.adTimerSeconds.collectAsState()

                // BackHandler: Return to Home if on History tab
                BackHandler(enabled = currentTab != 0 && !showAdDialog) {
                    viewModel.setTab(0)
                }

                CompositionLocalProvider(
                    LocalLayoutDirection provides if (isEnglish) LayoutDirection.Ltr else LayoutDirection.Rtl
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBackground)
                    ) {
                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .testTag("main_scaffold"),
                            bottomBar = {
                                BottomNavBar(
                                    selectedTab = currentTab,
                                    historyCount = historyCount,
                                    onTabSelected = { viewModel.setTab(it) }
                                )
                            },
                            containerColor = DarkBackground
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentTab) {
                                    0 -> HomeScreen(viewModel = viewModel)
                                    1 -> HistoryScreen(
                                        items = historyList,
                                        onDeleteItem = { viewModel.deleteHistoryItem(it) },
                                        onClearAll = { viewModel.clearAllHistory() }
                                    )
                                }
                            }
                        }

                        // Fullscreen In-App Ad Overlay with 8-second countdown lock
                        AnimatedVisibility(
                            visible = showAdDialog,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            AdOverlay(
                                secondsLeft = adTimerSeconds,
                                onClose = { viewModel.closeAd() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        extractSharedUrl(intent)
    }

    private fun extractSharedUrl(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!text.isNullOrBlank()) {
                val extracted = extractUrlFromText(text)
                sharedUrlFromIntent = extracted ?: text
            }
        }
    }

    private fun extractUrlFromText(text: String): String? {
        val regex = Regex("https?://[^\\s]+")
        return regex.find(text)?.value
    }
}
