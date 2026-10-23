package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun BottomNavBar(
    selectedTab: Int, // 0 = Download, 1 = History
    historyCount: Int,
    onTabSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .border(width = 1.dp, color = DarkCardBorder)
            .navigationBarsPadding()
            .height(72.dp)
            .testTag("bottom_nav_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 0: "التحميل" (Downloads / Home)
            val isDownloadActive = selectedTab == 0
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(0) }
                    .padding(vertical = 8.dp)
                    .testTag("nav_download_tab"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isDownloadActive) {
                    Box(
                        modifier = Modifier
                            .size(width = 56.dp, height = 30.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(AccentYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Download Tab Active",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Download Tab Inactive",
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "التحميل",
                    color = if (isDownloadActive) AccentYellow else TextMuted,
                    fontSize = 12.sp,
                    fontWeight = if (isDownloadActive) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Tab 1: "السجل" (History with badge)
            val isHistoryActive = selectedTab == 1
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(1) }
                    .padding(vertical = 8.dp)
                    .testTag("nav_history_tab"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isHistoryActive) {
                    Box(
                        modifier = Modifier
                            .size(width = 56.dp, height = 30.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(AccentYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (historyCount > 0) {
                                Text(
                                    text = "$historyCount",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = "History Tab Active",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "History Tab Inactive",
                            tint = TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        if (historyCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(AccentYellow),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$historyCount",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "السجل",
                    color = if (isHistoryActive) AccentYellow else TextMuted,
                    fontSize = 12.sp,
                    fontWeight = if (isHistoryActive) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
