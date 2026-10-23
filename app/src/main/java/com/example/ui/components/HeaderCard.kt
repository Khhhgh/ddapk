package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HeaderCard(
    isEnglish: Boolean,
    onToggleLanguage: () -> Unit,
    serverOnline: Boolean = true,
    versionText: String = "v1.0.4"
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("app_header_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Top row: Language switch on left, Title & App Logo on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language pill switch
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x22FFFFFF))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                        .clickable { onToggleLanguage() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("language_toggle_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isEnglish) "عربي" else "EN",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Language",
                        tint = AccentYellow,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Title and Logo on the right matching Screenshot 1
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isEnglish) "Media Downloader" else "تحميل الميديا",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isEnglish) "Fast cloud client for all platforms" else "عميل سحابي فائق السرعة لجميع المنصات",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // App icon image with rounded borders
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x44EAB308), RoundedCornerShape(12.dp))
                    )
                }
            }

            // Bottom row: Version on left, Server status on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = versionText,
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isEnglish) "Cloud Server: Connected & Stable" else "الخادم السحابي: متصل ومستقر",
                        color = StatusGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (serverOnline) StatusGreen else Color.Red)
                    )
                }
            }
        }
    }
}
