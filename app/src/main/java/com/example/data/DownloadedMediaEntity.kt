package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_media")
data class DownloadedMediaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String = "",
    val platform: String, // "tiktok", "instagram", "facebook", "pinterest", "twitter", etc.
    val mediaType: String, // "video" or "image"
    val fileFormat: String, // "MP4", "JPG", "PNG", etc.
    val fileSizeBytes: Long = 0,
    val formattedSize: String = "",
    val localUri: String,
    val sourceUrl: String = "",
    val thumbnailUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDate: String = ""
)
