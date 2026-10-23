package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlatformsResponse(
    val status: String? = null,
    val platforms: List<PlatformModel>? = null
)

@JsonClass(generateAdapter = true)
data class PlatformModel(
    val id: String,
    val name: String,
    val active: Boolean = true
)

@JsonClass(generateAdapter = true)
data class StatusResponse(
    val status: String? = null,
    val version: String? = null,
    val mode: String? = null
)

@JsonClass(generateAdapter = true)
data class ExtractRequest(
    val url: String,
    @Json(name = "media_type") val mediaType: String = "video"
)

@JsonClass(generateAdapter = true)
data class MediaItem(
    val url: String? = null,
    val type: String? = null, // "video" or "image"
    val ext: String? = null,
    val format: String? = null,
    val quality: String? = null,
    val size: String? = null
)

@JsonClass(generateAdapter = true)
data class ExtractResponse(
    val status: String? = null,
    val title: String? = null,
    val author: String? = null,
    val platform: String? = null,
    val thumbnail: String? = null,
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @Json(name = "media_url") val mediaUrl: String? = null,
    @Json(name = "download_url") val downloadUrl: String? = null,
    val url: String? = null,
    @Json(name = "media_type") val mediaType: String? = null,
    val format: String? = null,
    val ext: String? = null,
    val media: List<MediaItem>? = null,
    val medias: List<MediaItem>? = null
)

@JsonClass(generateAdapter = true)
data class SaveReportRequest(
    val url: String,
    val platform: String,
    val success: Boolean = true,
    val reason: String = ""
)
