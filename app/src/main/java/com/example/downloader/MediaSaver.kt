package com.example.downloader

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object MediaSaver {

    data class SaveResult(
        val uri: Uri,
        val filePath: String,
        val fileName: String,
        val mimeType: String,
        val sizeBytes: Long
    )

    fun saveFromFile(
        context: Context,
        tempFile: File,
        suggestedFileName: String,
        isVideo: Boolean,
        format: String
    ): SaveResult? {
        if (!tempFile.exists() || tempFile.length() == 0L) {
            return null
        }

        val extension = when {
            format.isNotBlank() -> format.lowercase().removePrefix(".")
            isVideo -> "mp4"
            else -> "jpg"
        }

        val cleanName = if (suggestedFileName.contains(".")) {
            suggestedFileName
        } else {
            "${suggestedFileName}_${System.currentTimeMillis()}.$extension"
        }

        val mimeType = when (extension) {
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "mov" -> "video/quicktime"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            else -> if (isVideo) "video/mp4" else "image/jpeg"
        }

        val resolver = context.contentResolver
        val fileSize = tempFile.length()

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val subFolder = if (isVideo) {
                    Environment.DIRECTORY_MOVIES + "/MediaDownloader"
                } else {
                    Environment.DIRECTORY_PICTURES + "/MediaDownloader"
                }

                val collection = if (isVideo) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                }

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, cleanName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, subFolder)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val uri = resolver.insert(collection, values) ?: return null

                resolver.openOutputStream(uri)?.use { outStream ->
                    FileInputStream(tempFile).use { inStream ->
                        inStream.copyTo(outStream)
                    }
                    outStream.flush()
                }

                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)

                val physicalPath = getPathFromUri(context, uri) ?: "/storage/emulated/0/$subFolder/$cleanName"

                // Immediate MediaScanner indexing for gallery appearance
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(physicalPath),
                    arrayOf(mimeType)
                ) { _, _ -> }

                val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                    data = uri
                }
                context.sendBroadcast(scanIntent)

                SaveResult(
                    uri = uri,
                    filePath = physicalPath,
                    fileName = cleanName,
                    mimeType = mimeType,
                    sizeBytes = fileSize
                )
            } else {
                // Legacy Android 9 and lower
                val targetDir = if (isVideo) {
                    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "MediaDownloader")
                } else {
                    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MediaDownloader")
                }
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }

                val destFile = File(targetDir, cleanName)
                FileInputStream(tempFile).use { inStream ->
                    FileOutputStream(destFile).use { outStream ->
                        inStream.copyTo(outStream)
                    }
                }

                val uri = Uri.fromFile(destFile)
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(destFile.absolutePath),
                    arrayOf(mimeType)
                ) { _, _ -> }

                SaveResult(
                    uri = uri,
                    filePath = destFile.absolutePath,
                    fileName = cleanName,
                    mimeType = mimeType,
                    sizeBytes = fileSize
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getPathFromUri(context: Context, uri: Uri): String? {
        val proj = arrayOf(MediaStore.MediaColumns.DATA)
        return runCatching {
            context.contentResolver.query(uri, proj, null, null, null)?.use { cursor ->
                val colIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                if (cursor.moveToFirst()) cursor.getString(colIndex) else null
            }
        }.getOrNull()
    }
}
