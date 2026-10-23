package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.downloader.DownloadState
import com.example.ui.ExtractedMediaUi
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ExtractedResultCard(
    media: ExtractedMediaUi,
    downloadState: DownloadState,
    onClose: () -> Unit,
    onDownloadClick: () -> Unit
) {
    val isVideo = media.mediaType.equals("video", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("extracted_media_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Top Row: Dismiss 'X' on left, Platform pill on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("dismiss_result_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Platform badge (e.g. TIKTOK)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentYellow)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = media.platform.uppercase(),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Middle info row matching Screenshot 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Texts on the left/center
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = media.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = media.author,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "جاهز للحفظ في ألبوم الهاتف",
                        color = StatusGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End
                    )
                }

                // Thumbnail image on the right
                AsyncImage(
                    model = media.thumbnailUrl,
                    contentDescription = "Media Preview",
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                        .background(DarkBackground),
                    contentScale = ContentScale.Crop
                )
            }

            // Download Progress Bar if downloading
            if (downloadState is DownloadState.Downloading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${downloadState.progress}%",
                            color = AccentYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "جاري التنزيل وحفظ الوسائط في المعرض...",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    LinearProgressIndicator(
                        progress = { downloadState.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AccentYellow,
                        trackColor = Color(0x33FFFFFF)
                    )
                }
            }

            // Gradient Download Button / Card matching Screenshot 1
            val isDownloading = downloadState is DownloadState.Downloading
            val gradientBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF84CC16), // Lime green
                    Color(0xFFEAB308), // Amber gold
                    Color(0xFFFBBF24)  // Bright yellow
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(gradientBrush)
                    .clickable(enabled = !isDownloading) { onDownloadClick() }
                    .padding(horizontal = 14.dp)
                    .testTag("one_click_download_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left circle with download arrow
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2127)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                color = AccentYellow,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Download",
                                tint = AccentYellow,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Center text labels
                    val allImages = media.items.isNotEmpty() && media.items.all { it.type == "image" }
                    val allVideos = media.items.isNotEmpty() && media.items.all { it.type == "video" }
                    val isMulti = media.items.size > 1

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val mainLabel = when {
                            isMulti && allImages -> "تحميل الصور (${media.items.size} صور)"
                            isMulti && allVideos -> "تحميل الفيديوهات (${media.items.size} مقاطع)"
                            isMulti -> "تحميل جميع الوسائط (${media.items.size})"
                            isVideo -> "تحميل الفيديو (${media.format})"
                            else -> "تحميل الصورة (${media.format})"
                        }
                        val subLabel = when {
                            isMulti && allImages -> "حفظ متوازي فائق السرعة في ألبوم الصور"
                            isMulti && allVideos -> "حفظ متوازي فائق السرعة في ألبوم Movies"
                            isMulti -> "حفظ متوازي فائق السرعة في ألبوم الصور والفيديو"
                            isVideo -> "حفظ كفيديو عالي الدقة في ألبوم Movies"
                            else -> "حفظ كصورة عالية الدقة في ألبوم Pictures"
                        }
                        Text(
                            text = mainLabel,
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = subLabel,
                            color = Color(0xFF374151),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Right circle with camera / video icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x33000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        val iconVector = when {
                            isMulti && allImages -> Icons.Default.Image
                            isMulti && allVideos -> Icons.Default.Videocam
                            isMulti -> Icons.Default.FileDownload
                            isVideo -> Icons.Default.Videocam
                            else -> Icons.Default.Image
                        }
                        Icon(
                            imageVector = iconVector,
                            contentDescription = "Media Type",
                            tint = Color(0xFF111827),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
