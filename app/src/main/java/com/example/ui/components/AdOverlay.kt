package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AdOverlay(
    adUrl: String = "https://asiafilm.org/4/f89ce863226eeb82a6e8daabbe6fd6ce",
    secondsLeft: Int,
    onClose: () -> Unit
) {
    // Prevent back press until 6 seconds finish
    BackHandler(enabled = true) {
        if (secondsLeft == 0) {
            onClose()
        }
    }

    var isLoading by remember { mutableStateOf(true) }
    var hasRenderCrashed by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewRef?.let { wv ->
                    wv.stopLoading()
                    (wv.parent as? ViewGroup)?.removeView(wv)
                    wv.destroy()
                }
            } catch (e: Exception) {
                Log.e("AdOverlay", "Error cleaning up WebView", e)
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("in_app_ad_container")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top header bar matching Screenshot 4
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close button with 8-second countdown lock
                    val canClose = secondsLeft == 0
                    Button(
                        onClick = {
                            if (canClose) onClose()
                        },
                        enabled = canClose,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canClose) AccentYellow else Color(0x33FFFFFF),
                            contentColor = if (canClose) Color.Black else Color(0x88FFFFFF),
                            disabledContainerColor = Color(0x22FFFFFF),
                            disabledContentColor = Color(0x66FFFFFF)
                        ),
                        modifier = Modifier
                            .testTag("ad_close_button")
                            .border(
                                width = 1.dp,
                                color = if (canClose) AccentYellow else Color(0x33FFFFFF),
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (canClose) "إلغاء ✕" else "إلغاء (${secondsLeft}s)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Sponsored Ad info on the right matching Screenshot 4
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "إعلان مدعوم",
                                color = AccentYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (canClose) "يمكنك الإلغاء الآن" else "يمكنك الإلغاء بعد $secondsLeft ثوانٍ",
                                color = if (canClose) Color(0xFF22C55E) else TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x33EAB308)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Sponsored Ad",
                                tint = AccentYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Main Content: WebView or graceful fallback if renderer crashes
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFF9FAFB))
                ) {
                    if (!hasRenderCrashed) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    webViewRef = this
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    // Use Software layer in virtualized cloud environments to prevent GPU OOM crashes
                                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)

                                    settings.apply {
                                        javaScriptEnabled = true
                                        domStorageEnabled = true
                                        databaseEnabled = true
                                        loadWithOverviewMode = true
                                        useWideViewPort = true
                                        cacheMode = WebSettings.LOAD_DEFAULT
                                        setSupportMultipleWindows(false)
                                        javaScriptCanOpenWindowsAutomatically = false
                                        mediaPlaybackRequiresUserGesture = true
                                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                        userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                                    }

                                    webChromeClient = WebChromeClient()

                                    webViewClient = object : WebViewClient() {
                                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                            super.onPageStarted(view, url, favicon)
                                            isLoading = true
                                        }

                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            isLoading = false
                                        }

                                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                            return false
                                        }

                                        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                                            handler?.proceed()
                                        }

                                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                            super.onReceivedError(view, request, error)
                                            isLoading = false
                                        }

                                        // CRITICAL: Overriding onRenderProcessGone prevents aw_browser_terminator crash!
                                        override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                            Log.w("AdOverlay", "Chromium render process terminated. Handled gracefully.")
                                            try {
                                                view?.let {
                                                    (it.parent as? ViewGroup)?.removeView(it)
                                                    it.destroy()
                                                }
                                            } catch (e: Exception) {
                                                Log.e("AdOverlay", "Error tearing down crashed webview", e)
                                            }
                                            hasRenderCrashed = true
                                            isLoading = false
                                            return true // Tells Android NOT to kill the host application
                                        }
                                    }

                                    loadUrl(adUrl)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Fallback UI matching Screenshot 4 (AliExpress banner aesthetic)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // AliExpress Header Banner matching Screenshot 4
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFE11D48), Color(0xFFF43F5E), Color(0xFFFB7185))
                                        )
                                    )
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "AliExpress",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp
                                    )
                                    Text(
                                        text = "Better choices, better prices",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }

                            // Sample product grid placeholders
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF3F4F6))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    Column {
                                        Text("Smart Watch Pro", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                        Text("$12.99", color = Color(0xFFE11D48), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF3F4F6))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    Column {
                                        Text("Wireless Earbuds", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                        Text("$8.50", color = Color(0xFFE11D48), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = "شكراً لدعمك التطبيق من خلال مشاهدة الإعلان",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (isLoading && !hasRenderCrashed) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DarkBackground.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = AccentYellow)
                                Text(
                                    text = "جاري تحميل الإعلان...",
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
