package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DownloadedMediaEntity
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.PlatformFacebook
import com.example.ui.theme.PlatformInstagram
import com.example.ui.theme.PlatformPinterest
import com.example.ui.theme.PlatformTikTok
import com.example.ui.theme.PlatformTwitter
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File

@Composable
fun HistoryScreen(
    items: List<DownloadedMediaEntity>,
    onDeleteItem: (DownloadedMediaEntity) -> Unit,
    onClearAll: () -> Unit
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "مسح سجل التنزيلات",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في حذف جميع عناصر السجل؟",
                    color = TextSecondary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                ) {
                    Text("مسح الكل", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("إلغاء")
                }
            },
            containerColor = DarkSurfaceCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("history_screen_container")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Header matching Screenshot 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "مسح الكل" button on the left
            if (items.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x22EF4444))
                        .border(1.dp, DangerRed, RoundedCornerShape(12.dp))
                        .clickable { showClearDialog = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("clear_all_history_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear All",
                        tint = DangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "مسح الكل",
                        color = DangerRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(1.dp))
            }

            // Title and Subtitle on the right
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "سجل التنزيلات والميديا",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "العناصر المحفوظة في ألبوم الهاتف (${items.size})",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = "No media",
                        tint = TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "لا توجد تنزيلات سابقة بعد",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    HistoryItemCard(
                        item = item,
                        onDelete = { onDeleteItem(item) },
                        onShare = { shareMedia(context, item) },
                        onPlay = { openMedia(context, item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: DownloadedMediaEntity,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onPlay: () -> Unit
) {
    val platformColor = when (item.platform.lowercase()) {
        "instagram" -> Color(0xFFEAB308).copy(alpha = 0.25f)
        "tiktok" -> Color(0xFFEAB308).copy(alpha = 0.25f)
        "twitter" -> Color(0xFF0EA5E9).copy(alpha = 0.25f)
        "facebook" -> Color(0xFF1877F2).copy(alpha = 0.25f)
        "pinterest" -> Color(0xFFEF4444).copy(alpha = 0.25f)
        else -> Color(0x33FFFFFF)
    }

    val platformTextColor = when (item.platform.lowercase()) {
        "instagram", "tiktok" -> AccentYellow
        "twitter" -> Color(0xFF38BDF8)
        "facebook" -> Color(0xFF60A5FA)
        "pinterest" -> Color(0xFFF87171)
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("history_item_${item.id}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Top Row: File size on left, Platform badge on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.formattedSize,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(platformColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.platform.lowercase(),
                        color = platformTextColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }

            // Middle Row: Actions on left, Title in middle, Play button on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete & Share icons on the left
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_item_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = DangerRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("share_item_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = AccentYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Title
                Text(
                    text = item.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                // Play Preview button on the right matching Screenshot 2
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF14171C))
                        .border(1.dp, Color(0x33EAB308), RoundedCornerShape(12.dp))
                        .clickable { onPlay() }
                        .testTag("play_media_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Media",
                        tint = AccentYellow,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Bottom timestamp
            Text(
                text = item.formattedDate,
                color = TextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun openMedia(context: Context, item: DownloadedMediaEntity) {
    runCatching {
        val uri = Uri.parse(item.localUri)
        val mime = if (item.mediaType == "video") "video/*" else "image/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "عرض الوسائط"))
    }.onFailure {
        Toast.makeText(context, "الملف محفوظ في ألبوم المعرض", Toast.LENGTH_SHORT).show()
    }
}

private fun shareMedia(context: Context, item: DownloadedMediaEntity) {
    runCatching {
        val uri = Uri.parse(item.localUri)
        val mime = if (item.mediaType == "video") "video/*" else "image/*"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "${item.title}\nتم التحميل بواسطة تطبيق تحميل الميديا")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة الوسائط"))
    }.onFailure {
        Toast.makeText(context, "تعذر مشاركة الملف مباشرة", Toast.LENGTH_SHORT).show()
    }
}
