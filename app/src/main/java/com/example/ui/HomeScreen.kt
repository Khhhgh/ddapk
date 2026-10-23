package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.downloader.DownloadState
import com.example.ui.components.ExtractedResultCard
import com.example.ui.components.HeaderCard
import com.example.ui.components.InputCard
import com.example.ui.components.PlatformChipsRow
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.StatusGreen

@Composable
fun HomeScreen(
    viewModel: MediaViewModel
) {
    val context = LocalContext.current
    val isEnglish by viewModel.isEnglish.collectAsState()
    val serverOnline by viewModel.serverConnected.collectAsState()
    val platforms by viewModel.platforms.collectAsState()
    val inputUrl by viewModel.inputUrl.collectAsState()
    val isExtracting by viewModel.isExtracting.collectAsState()
    val extractedMedia by viewModel.extractedMedia.collectAsState()
    val extractMessage by viewModel.extractMessage.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.triggerOneClickDownload()
        } else {
            Toast.makeText(
                context,
                "يرجى منح إذن الوصول إلى مساحة التخزين لحفظ الوسائط في المعرض",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val requestDownloadAction = {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                viewModel.triggerOneClickDownload()
            } else {
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else {
            viewModel.triggerOneClickDownload()
        }
    }

    LaunchedEffect(downloadState) {
        when (val state = downloadState) {
            is DownloadState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetDownloadState()
            }
            is DownloadState.Error -> {
                Toast.makeText(context, state.errorMessage, Toast.LENGTH_LONG).show()
                viewModel.resetDownloadState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("home_screen_container")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header Card (Title, Logo, Lang switch, Server status)
            item {
                HeaderCard(
                    isEnglish = isEnglish,
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    serverOnline = serverOnline,
                    versionText = "v1.0.4"
                )
            }

            // 2. Active platforms chips row
            item {
                PlatformChipsRow(
                    platforms = platforms,
                    onPlatformClick = { platform ->
                        Toast.makeText(context, "المنصة نشطة: ${platform.name} - ضع الرابط في الخانة أدناه", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // 3. Input card with paste & start button
            item {
                InputCard(
                    url = inputUrl,
                    onUrlChange = { viewModel.updateInputUrl(it) },
                    onClear = { viewModel.clearInput() },
                    onStartClick = { viewModel.startExtraction() },
                    isExtracting = isExtracting
                )
            }

            // 4. Extracted Result Card (shows when extraction succeeded)
            if (extractedMedia != null) {
                item {
                    ExtractedResultCard(
                        media = extractedMedia!!,
                        downloadState = downloadState,
                        onClose = { viewModel.clearExtractedMedia() },
                        onDownloadClick = { requestDownloadAction() }
                    )
                }
            }
        }

        // Bottom Banner / Notification matching Screenshot 1
        AnimatedVisibility(
            visible = extractMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = extractMessage ?: "",
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
