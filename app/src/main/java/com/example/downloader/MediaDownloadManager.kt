package com.example.downloader

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.DownloadedMediaEntity
import com.example.network.ApiClient
import com.example.network.SaveReportRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Int, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState()
    data class Success(val media: DownloadedMediaEntity, val message: String) : DownloadState()
    data class Error(val errorMessage: String) : DownloadState()
}

data class DownloadTaskItem(
    val downloadUrl: String,
    val mediaType: String, // "video" or "image"
    val format: String,    // "mp4", "jpg", etc.
    val title: String = ""
)

class MediaDownloadManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val mediaDao = db.mediaDao()
    private val okHttpClient = ApiClient.okHttpClient

    fun downloadAndSave(
        items: List<DownloadTaskItem>,
        mainTitle: String,
        author: String,
        platform: String,
        thumbnailUrl: String,
        sourceUrl: String
    ): Flow<DownloadState> = flow {
        if (items.isEmpty()) {
            emit(DownloadState.Error("لا توجد روابط وسائط صالحة للتنزيل"))
            return@flow
        }

        emit(DownloadState.Downloading(progress = 0, bytesDownloaded = 0, totalBytes = 0))

        val totalItems = items.size
        val completedItemsCount = AtomicInteger(0)
        val totalBytesReadGlobal = AtomicLong(0)
        val successfulEntities = mutableListOf<DownloadedMediaEntity>()

        try {
            // Concurrent parallel downloading for ultra-fast performance
            val results = coroutineScope {
                items.mapIndexed { index, item ->
                    async(Dispatchers.IO) {
                        downloadSingleItem(
                            item = item,
                            index = index,
                            totalCount = totalItems,
                            mainTitle = mainTitle,
                            author = author,
                            platform = platform,
                            thumbnailUrl = thumbnailUrl,
                            sourceUrl = sourceUrl,
                            onProgressUpdate = { bytesReadDelta, progressPercent ->
                                totalBytesReadGlobal.addAndGet(bytesReadDelta)
                            }
                        )
                    }
                }.awaitAll()
            }

            val savedEntities = results.filterNotNull()
            if (savedEntities.isEmpty()) {
                emit(DownloadState.Error("تعذر حفظ الوسائط في ألبوم الهاتف"))
                reportToServer(sourceUrl, platform, false, "Failed to save any media to MediaStore")
                return@flow
            }

            successfulEntities.addAll(savedEntities)

            // Final 100% emission
            emit(
                DownloadState.Downloading(
                    progress = 100,
                    bytesDownloaded = totalBytesReadGlobal.get(),
                    totalBytes = totalBytesReadGlobal.get()
                )
            )

            // Insert records into Room database
            for (entity in successfulEntities) {
                mediaDao.insertMedia(entity)
            }

            // Report telemetry to Ubuntu backend
            reportToServer(sourceUrl, platform, true, "")

            val hasVideo = successfulEntities.any { it.mediaType.equals("video", ignoreCase = true) }
            val hasImage = successfulEntities.any { it.mediaType.equals("image", ignoreCase = true) }

            val successMsg = when {
                totalItems > 1 && hasVideo && hasImage -> "تم حفظ $totalItems عناصر بنجاح في ألبومات الصور والفيديو!"
                totalItems > 1 && hasImage -> "تم حفظ $totalItems صور في ألبوم Pictures بنجاح!"
                totalItems > 1 -> "تم حفظ $totalItems مقاطع في ألبوم Movies بنجاح!"
                hasVideo -> "تم حفظ الفيديو في ألبوم Movies بنجاح!"
                else -> "تم حفظ الصورة في ألبوم Pictures بنجاح!"
            }

            emit(DownloadState.Success(successfulEntities.first(), successMsg))

        } catch (e: Exception) {
            val errorMsg = when {
                e is java.net.SocketTimeoutException -> "انتهت مهلة الاتصال بالخادم (30 ثانية). تحقق من اتصال الإنترنت"
                e is java.net.UnknownHostException -> "تعذر الوصول إلى الخادم. تحقق من اتصال الإنترنت"
                else -> e.localizedMessage ?: "حدث خطأ أثناء التنزيل وحفظ الوسائط"
            }
            emit(DownloadState.Error(errorMsg))
            reportToServer(sourceUrl, platform, false, e.message ?: "Exception")
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun downloadSingleItem(
        item: DownloadTaskItem,
        index: Int,
        totalCount: Int,
        mainTitle: String,
        author: String,
        platform: String,
        thumbnailUrl: String,
        sourceUrl: String,
        onProgressUpdate: (bytesDelta: Long, progressPercent: Int) -> Unit
    ): DownloadedMediaEntity? {
        val isVideo = item.mediaType.equals("video", ignoreCase = true)
        val defaultFormat = if (isVideo) "mp4" else "jpg"
        val activeFormat = if (item.format.isNotBlank()) item.format.lowercase() else defaultFormat
        val extension = activeFormat.removePrefix(".")

        var tempFile: File? = null

        return try {
            tempFile = File.createTempFile("temp_${System.currentTimeMillis()}_${index}_", ".$extension", context.cacheDir)

            val request = Request.Builder()
                .url(item.downloadUrl)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36")
                .header("Accept", "*/*")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful || response.body == null) {
                return null
            }

            val body = response.body!!
            val contentLength = body.contentLength()
            val inputStream = body.byteStream()

            var bytesTotalRead: Long = 0

            FileOutputStream(tempFile).use { fileOutputStream ->
                val buffer = ByteArray(16384) // 16KB buffer
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead)
                    bytesTotalRead += bytesRead
                    val percent = if (contentLength > 0) ((bytesTotalRead * 100) / contentLength).toInt() else 50
                    onProgressUpdate(bytesRead.toLong(), percent)
                }
                fileOutputStream.flush()
            }

            val itemSuffix = if (totalCount > 1) "_${index + 1}" else ""
            val rawTitle = if (item.title.isNotBlank()) item.title else mainTitle
            val safeTitle = rawTitle.ifBlank { "media_${System.currentTimeMillis()}" }
                .replace(Regex("[^a-zA-Z0-9ء-ي_\\-]"), "_")
                .take(30) + itemSuffix

            val saveResult = MediaSaver.saveFromFile(
                context = context,
                tempFile = tempFile,
                suggestedFileName = safeTitle,
                isVideo = isVideo,
                format = extension
            ) ?: return null

            val formattedSize = formatFileSize(saveResult.sizeBytes)
            val formattedDate = formatCurrentDate()

            DownloadedMediaEntity(
                title = if (totalCount > 1) "$mainTitle ($safeTitle)" else mainTitle.ifBlank { "تحميل من $platform" },
                author = author,
                platform = platform.lowercase(),
                mediaType = if (isVideo) "video" else "image",
                fileFormat = extension.uppercase(),
                fileSizeBytes = saveResult.sizeBytes,
                formattedSize = formattedSize,
                localUri = saveResult.uri.toString(),
                sourceUrl = sourceUrl,
                thumbnailUrl = thumbnailUrl,
                timestamp = System.currentTimeMillis(),
                formattedDate = formattedDate
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            runCatching {
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete()
                }
            }
        }
    }

    private suspend fun reportToServer(url: String, platform: String, success: Boolean, reason: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                ApiClient.api.reportSaved(
                    SaveReportRequest(
                        url = url,
                        platform = platform.lowercase(),
                        success = success,
                        reason = reason
                    )
                )
            }
        }
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "1.2 MB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
            else -> "$bytes B"
        }
    }

    private fun formatCurrentDate(): String {
        val arabicMonths = arrayOf(
            "كانون الثاني", "شباط", "آذار", "نيسان", "أيار", "حزيران",
            "تموز", "آب", "أيلول", "تشرين الأول", "تشرين الثاني", "كانون الأول"
        )
        val cal = java.util.Calendar.getInstance()
        val monthName = arabicMonths[cal.get(java.util.Calendar.MONTH)]
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val year = cal.get(java.util.Calendar.YEAR)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
        val timeStr = timeFormat.format(Date())
        return "$monthName $day, $year • $timeStr"
    }
}
