# Add project specific ProGuard rules here.
# Retain models and network entities for serialization
-keep class com.example.network.** { *; }
-keep class com.example.data.** { *; }
-keep class com.example.downloader.** { *; }
-keep class com.example.ui.** { *; }

# Moshi rules
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}

# Retrofit & OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Coroutines
-keep class kotlinx.coroutines.** { *; }

# WebView & JS Interface
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, java.lang.String);
}
