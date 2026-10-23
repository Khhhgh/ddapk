package com.example.ui.components

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun InputCard(
    url: String,
    onUrlChange: (String) -> Unit,
    onClear: () -> Unit,
    onStartClick: () -> Unit,
    isExtracting: Boolean
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("input_card_container")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header: "أدخل رابط المنشور أو الفيديو"
            Text(
                text = "أدخل رابط المنشور أو الفيديو",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            // Rounded white input container matching Screenshot 1 & 3
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Link icon on left
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Link Icon",
                        tint = AccentYellow,
                        modifier = Modifier.size(22.dp)
                    )

                    // Text Field
                    Box(modifier = Modifier.weight(1f)) {
                        if (url.isEmpty()) {
                            Text(
                                text = "ضع رابط الفيديو هنا...",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }
                        BasicTextField(
                            value = url,
                            onValueChange = onUrlChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("url_text_field"),
                            textStyle = TextStyle(
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(AccentYellow),
                            singleLine = true
                        )
                    }

                    // Clear button if text is present
                    if (url.isNotEmpty()) {
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("clear_url_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Paste button on the right
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            if (clipboard != null && clipboard.hasPrimaryClip()) {
                                val item = clipboard.primaryClip?.getItemAt(0)
                                val text = item?.text?.toString()
                                if (!text.isNullOrBlank()) {
                                    onUrlChange(text)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("paste_url_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste from Clipboard",
                            tint = AccentYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Big Yellow Button: "⚡ ابدا"
            Button(
                onClick = onStartClick,
                enabled = !isExtracting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_extract_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentYellow,
                    contentColor = Color.Black,
                    disabledContainerColor = AccentYellow.copy(alpha = 0.6f),
                    disabledContentColor = Color.Black
                )
            ) {
                if (isExtracting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "جاري الفحص والاستخراج...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ابدا",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Start Icon",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
