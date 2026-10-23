package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.network.PlatformModel
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.PlatformFacebook
import com.example.ui.theme.PlatformInstagram
import com.example.ui.theme.PlatformPinterest
import com.example.ui.theme.PlatformTikTok
import com.example.ui.theme.PlatformTwitter
import com.example.ui.theme.TextPrimary

@Composable
fun PlatformChipsRow(
    platforms: List<PlatformModel>,
    onPlatformClick: (PlatformModel) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section title with yellow indicator dot
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "المنصات النشطة على السيرفر",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Box(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(AccentYellow)
            )
        }

        // Horizontal scrolling platform chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            platforms.forEach { platform ->
                val dotColor = when (platform.id.lowercase()) {
                    "tiktok" -> PlatformTikTok
                    "instagram" -> PlatformInstagram
                    "facebook" -> PlatformFacebook
                    "pinterest" -> PlatformPinterest
                    "twitter" -> PlatformTwitter
                    else -> AccentYellow
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
                        .clickable { onPlatformClick(platform) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("platform_chip_${platform.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Text(
                        text = platform.name,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
