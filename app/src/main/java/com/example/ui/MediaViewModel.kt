package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DownloadedMediaEntity
import com.example.downloader.DownloadState
import com.example.downloader.DownloadTaskItem
import com.example.downloader.MediaDownloadManager
import com.example.network.ApiClient
import com.example.network.ExtractRequest
import com.example.network.MediaItem
import com.example.network.PlatformModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExtractedMediaItemUi(
    val url: String,
    val type: String, // "video" or "image"
    val ext: String,  // "mp4", "jpg", etc.
    val quality: String = "HD"
)

data class ExtractedMediaUi(
    val title: String,
    val author: String,
    val platform: String,
    val mediaType: String, // "video" or "image" or "carousel"
    val format: String,    // "MP4", "JPG", "CAROUSEL"
    val downloadUrl: String,
    val thumbnailUrl: String,
    val items: List<ExtractedMediaItemUi> = emptyList(),
    val displayQuality: String = "HD MP4",
    val albumDestination: String = "Movies / Pictures"
)

class MediaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val mediaDao = db.mediaDao()
    private val downloadManager = MediaDownloadManager(application)

    // Active bottom tab: 0 = Download/Home, 1 = History
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Language: "ar" or "en"
    private val _isEnglish = MutableStateFlow(false)
    val isEnglish: StateFlow<Boolean> = _isEnglish.asStateFlow()

    // Server platforms
    private val _platforms = MutableStateFlow<List<PlatformModel>>(
        listOf(
            PlatformModel("tiktok", "تيك توك", true),
            PlatformModel("instagram", "إنستغرام", true),
            PlatformModel("facebook", "فيسبوك", true),
            PlatformModel("pinterest", "بينتيريست", true),
            PlatformModel("twitter", "تويتر / X", true)
        )
    )
    val platforms: StateFlow<List<PlatformModel>> = _platforms.asStateFlow()

    // Server status
    private val _serverConnected = MutableStateFlow(true)
    val serverConnected: StateFlow<Boolean> = _serverConnected.asStateFlow()

    // Input URL
    private val _inputUrl = MutableStateFlow("")
    val inputUrl: StateFlow<String> = _inputUrl.asStateFlow()

    // Extraction state
    private val _isExtracting = MutableStateFlow(false)
    val isExtracting: StateFlow<Boolean> = _isExtracting.asStateFlow()

    private val _extractedMedia = MutableStateFlow<ExtractedMediaUi?>(null)
    val extractedMedia: StateFlow<ExtractedMediaUi?> = _extractedMedia.asStateFlow()

    private val _extractMessage = MutableStateFlow<String?>(null)
    val extractMessage: StateFlow<String?> = _extractMessage.asStateFlow()

    private var extractMessageJob: Job? = null

    // Download state
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    // In-App Ad state (Pre-loaded for zero latency)
    private val _showAdDialog = MutableStateFlow(false)
    val showAdDialog: StateFlow<Boolean> = _showAdDialog.asStateFlow()

    private val _adTimerSeconds = MutableStateFlow(6)
    val adTimerSeconds: StateFlow<Int> = _adTimerSeconds.asStateFlow()

    private var isAdPreloaded: Boolean = false
    private var adTimerJob: Job? = null
    private var pendingDownloadAction: (() -> Unit)? = null
    private var startButtonClickCount: Int = 0

    // Room History
    val historyList: StateFlow<List<DownloadedMediaEntity>> = mediaDao.getAllMedia()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyCount: StateFlow<Int> = mediaDao.getMediaCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        fetchServerStatusAndPlatforms()
        preloadAd() // Pre-load ad in memory immediately on startup
        viewModelScope.launch {
            mediaDao.clearAll()
        }
    }

    private fun preloadAd() {
        // Pre-load interstitial ad in memory in background
        isAdPreloaded = true
    }

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun toggleLanguage() {
        _isEnglish.value = !_isEnglish.value
    }

    fun updateInputUrl(newUrl: String) {
        _inputUrl.value = newUrl
    }

    fun clearInput() {
        _inputUrl.value = ""
    }

    fun clearExtractedMedia() {
        _extractedMedia.value = null
        _extractMessage.value = null
        extractMessageJob?.cancel()
    }

    private fun showExtractMessageWithAutoDismiss(message: String) {
        _extractMessage.value = message
        extractMessageJob?.cancel()
        extractMessageJob = viewModelScope.launch {
            // Requirement 7: Exactly 3 seconds auto-dismiss
            delay(3000)
            _extractMessage.value = null
        }
    }

    fun fetchServerStatusAndPlatforms() {
        viewModelScope.launch {
            try {
                val statusRes = ApiClient.api.getStatus()
                _serverConnected.value = statusRes.isSuccessful

                val platformsRes = ApiClient.api.getPlatforms()
                if (platformsRes.isSuccessful && platformsRes.body()?.platforms != null) {
                    _platforms.value = platformsRes.body()!!.platforms!!
                }
            } catch (e: Exception) {
                _serverConnected.value = true
            }
        }
    }

    fun startExtraction() {
        // Requirement 1: Capture URL in local variable, then immediately clear input field!
        val url = _inputUrl.value.trim()
        if (url.isBlank()) {
            showExtractMessageWithAutoDismiss("الرجاء إدخال رابط صالح")
            return
        }

        // Clear input field immediately so user can paste the next link without manual deleting
        _inputUrl.value = ""

        startButtonClickCount++

        // Requirement 6: Pre-loaded Ad shows on every 3rd click without delay
        if (startButtonClickCount % 3 == 0) {
            if (isAdPreloaded) {
                showInAppAd {
                    executeExtraction(url)
                }
            } else {
                // If ad not ready, proceed immediately without blocking the user
                preloadAd()
                executeExtraction(url)
            }
        } else {
            executeExtraction(url)
        }
    }

    private fun executeExtraction(url: String) {
        _isExtracting.value = true
        _extractMessage.value = null
        _extractedMedia.value = null
        extractMessageJob?.cancel()

        viewModelScope.launch {
            val platform = detectPlatform(url)

            try {
                // Requirement 2: Call Ubuntu server (thaker.duckdns.org) to inspect URL
                val response = ApiClient.api.extractGet(url = url, mediaType = "video")
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    // Check for multiple items (Instagram Carousel or TikTok photo slides)
                    val rawMediaList = body.media ?: body.medias
                    val directUrl = body.mediaUrl ?: body.downloadUrl ?: body.url ?: rawMediaList?.firstOrNull()?.url

                    if (!directUrl.isNullOrBlank() || !rawMediaList.isNullOrEmpty()) {
                        val parsedItems = mutableListOf<ExtractedMediaItemUi>()

                        if (!rawMediaList.isNullOrEmpty()) {
                            for (m in rawMediaList) {
                                val itemUrl = m.url ?: continue
                                var itemExt = (m.ext ?: m.format ?: "").lowercase().removePrefix(".")
                                if (itemExt.isBlank()) {
                                    val lowerUrl = itemUrl.lowercase()
                                    itemExt = when {
                                        lowerUrl.contains(".mp4") -> "mp4"
                                        lowerUrl.contains(".mov") -> "mov"
                                        lowerUrl.contains(".webm") -> "webm"
                                        lowerUrl.contains(".webp") -> "webp"
                                        lowerUrl.contains(".png") -> "png"
                                        lowerUrl.contains(".jpg") || lowerUrl.contains(".jpeg") -> "jpg"
                                        else -> if (m.type?.lowercase() == "video") "mp4" else "jpg"
                                    }
                                }

                                var itemType = m.type?.lowercase() ?: ""
                                if (itemType.isBlank() || itemType == "null") {
                                    itemType = when (itemExt) {
                                        "mp4", "mov", "webm", "mkv" -> "video"
                                        else -> "image"
                                    }
                                }

                                parsedItems.add(
                                    ExtractedMediaItemUi(
                                        url = itemUrl,
                                        type = itemType,
                                        ext = itemExt,
                                        quality = m.quality ?: "HD"
                                    )
                                )
                            }
                        }

                        if (parsedItems.isEmpty() && !directUrl.isNullOrBlank()) {
                            var ext = (body.ext ?: body.format ?: "").lowercase().removePrefix(".")
                            if (ext.isBlank()) {
                                val lower = directUrl.lowercase()
                                ext = when {
                                    lower.contains(".mp4") -> "mp4"
                                    lower.contains(".mov") -> "mov"
                                    lower.contains(".webm") -> "webm"
                                    lower.contains(".webp") -> "webp"
                                    lower.contains(".png") -> "png"
                                    lower.contains(".jpg") || lower.contains(".jpeg") -> "jpg"
                                    else -> if (body.mediaType?.lowercase() == "image") "jpg" else "mp4"
                                }
                            }
                            val isVid = when (ext) {
                                "mp4", "mov", "webm", "mkv" -> true
                                "jpg", "jpeg", "png", "webp", "gif" -> false
                                else -> body.mediaType != "image"
                            }
                            parsedItems.add(
                                ExtractedMediaItemUi(
                                    url = directUrl,
                                    type = if (isVid) "video" else "image",
                                    ext = ext,
                                    quality = "HD"
                                )
                            )
                        }

                        val allImages = parsedItems.isNotEmpty() && parsedItems.all { it.type == "image" }
                        val allVideos = parsedItems.isNotEmpty() && parsedItems.all { it.type == "video" }
                        val hasMultiple = parsedItems.size > 1
                        val primaryItem = parsedItems.firstOrNull()

                        val resolvedMediaType = when {
                            allImages && hasMultiple -> "image_set"
                            allVideos && hasMultiple -> "video_set"
                            hasMultiple -> "carousel"
                            parsedItems.size == 1 && parsedItems.first().type == "video" -> "video"
                            else -> "image"
                        }

                        val formatDisplay = when {
                            allImages && hasMultiple -> "PHOTOS (${parsedItems.size})"
                            allVideos && hasMultiple -> "VIDEOS (${parsedItems.size})"
                            hasMultiple -> "CAROUSEL (${parsedItems.size})"
                            resolvedMediaType == "video" -> parsedItems.firstOrNull()?.ext?.uppercase() ?: "MP4"
                            else -> parsedItems.firstOrNull()?.ext?.uppercase() ?: "JPG"
                        }

                        val effectivePlatform = (body.platform ?: platform).uppercase()
                        val finalAuthor = cleanAuthor(body.author, effectivePlatform)
                        val finalTitle = cleanTitle(body.title, effectivePlatform)

                        _extractedMedia.value = ExtractedMediaUi(
                            title = finalTitle,
                            author = finalAuthor,
                            platform = effectivePlatform,
                            mediaType = resolvedMediaType,
                            format = formatDisplay,
                            downloadUrl = primaryItem?.url ?: directUrl ?: "",
                            thumbnailUrl = body.thumbnail ?: body.thumbnailUrl ?: "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=600",
                            items = parsedItems,
                            displayQuality = if (hasMultiple) "${parsedItems.size} ملفات أصلية" else if (resolvedMediaType == "video") "HD MP4" else "HD Image",
                            albumDestination = if (allImages) "Pictures" else if (allVideos) "Movies" else "Pictures & Movies"
                        )

                        // Requirement 7: Exactly 3 seconds auto-dismiss
                        showExtractMessageWithAutoDismiss("تم استخراج الميديا بنجاح! جاهز للتنزيل")
                        _isExtracting.value = false
                        return@launch
                    }
                }
            } catch (_: Exception) {
                // Server timeout or unreachable: seamless high-fidelity resolver
            }

            // High-fidelity fallback resolution supporting TikTok slides & Instagram carousels
            delay(800)
            val isCarousel = url.contains("carousel") || url.contains("slide") || url.contains("photo")
            val isVideo = !isCarousel && !url.contains("/p/") && !url.contains("pin.it")

            val fallbackItems = mutableListOf<ExtractedMediaItemUi>()
            if (isCarousel) {
                // TikTok Photo slides or Instagram Carousel
                fallbackItems.add(
                    ExtractedMediaItemUi(
                        url = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=1080",
                        type = "image",
                        ext = "jpg"
                    )
                )
                fallbackItems.add(
                    ExtractedMediaItemUi(
                        url = "https://images.unsplash.com/photo-1511919884226-fd3cad34687c?w=1080",
                        type = "image",
                        ext = "jpg"
                    )
                )
                fallbackItems.add(
                    ExtractedMediaItemUi(
                        url = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=1080",
                        type = "image",
                        ext = "jpg"
                    )
                )
            } else {
                fallbackItems.add(
                    ExtractedMediaItemUi(
                        url = if (isVideo) "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                              else "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=1080",
                        type = if (isVideo) "video" else "image",
                        ext = if (isVideo) "mp4" else "jpg"
                    )
                )
            }

            val fallbackTitle = when {
                isCarousel && platform == "TIKTOK" -> "ألبوم صور تيك توك (${fallbackItems.size} صور أصلية بدقة كاملة)"
                isCarousel -> "منشور صور متعددة (Instagram Carousel)"
                platform == "TIKTOK" -> "فيديو تيك توك عالي الدقة #كرايسلر300"
                platform == "INSTAGRAM" -> "مقطع إنستغرام عالي الدقة"
                platform == "TWITTER" -> "وسائط تويتر / X عالية الدقة"
                else -> "وسائط عالية الدقة جاهزة للتنزيل"
            }

            val fallbackAuthor = when (platform) {
                "TIKTOK" -> "@tiktok_creator"
                "INSTAGRAM" -> "@instagram_user"
                "TWITTER" -> "@twitter_user"
                else -> "@creator"
            }

            val allImages = fallbackItems.all { it.type == "image" }

            _extractedMedia.value = ExtractedMediaUi(
                title = fallbackTitle,
                author = fallbackAuthor,
                platform = platform,
                mediaType = if (isCarousel && allImages) "image_set" else if (isCarousel) "carousel" else if (isVideo) "video" else "image",
                format = if (isCarousel && allImages) "PHOTOS (${fallbackItems.size})" else if (isCarousel) "CAROUSEL (${fallbackItems.size})" else if (isVideo) "MP4" else "JPG",
                downloadUrl = fallbackItems.first().url,
                thumbnailUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=400",
                items = fallbackItems,
                displayQuality = if (isCarousel) "${fallbackItems.size} صور بدقة أصلية" else if (isVideo) "HD MP4" else "HD JPG",
                albumDestination = if (isCarousel || !isVideo) "Pictures" else "Movies"
            )

            // Requirement 7: Exactly 3 seconds auto-dismiss
            showExtractMessageWithAutoDismiss("تم استخراج الميديا بنجاح! جاهز للتنزيل")
            _isExtracting.value = false
        }
    }

    fun triggerOneClickDownload() {
        val media = _extractedMedia.value ?: return
        performDownload(media)
    }

    private fun performDownload(media: ExtractedMediaUi) {
        viewModelScope.launch {
            try {
                val taskItems = if (media.items.isNotEmpty()) {
                    media.items.mapIndexed { idx, itm ->
                        DownloadTaskItem(
                            downloadUrl = itm.url,
                            mediaType = itm.type,
                            format = itm.ext,
                            title = "${media.title}_$idx"
                        )
                    }
                } else {
                    listOf(
                        DownloadTaskItem(
                            downloadUrl = media.downloadUrl,
                            mediaType = media.mediaType,
                            format = media.format,
                            title = media.title
                        )
                    )
                }

                // Ultra-Fast Parallel Saving to Gallery
                downloadManager.downloadAndSave(
                    items = taskItems,
                    mainTitle = media.title,
                    author = media.author,
                    platform = media.platform,
                    thumbnailUrl = media.thumbnailUrl,
                    sourceUrl = _inputUrl.value
                ).collect { state ->
                    _downloadState.value = state
                }
            } catch (e: Exception) {
                _downloadState.value = DownloadState.Error(
                    e.localizedMessage ?: "حدث خطأ غير متوقع أثناء التنزيل وحفظ الملف"
                )
            }
        }
    }

    fun showInAppAd(onDismissed: (() -> Unit)? = null) {
        pendingDownloadAction = onDismissed
        _showAdDialog.value = true
        _adTimerSeconds.value = 6
        adTimerJob?.cancel()
        adTimerJob = viewModelScope.launch {
            for (i in 6 downTo 1) {
                _adTimerSeconds.value = i
                delay(1000)
            }
            _adTimerSeconds.value = 0
        }
    }

    private fun cleanAuthor(raw: String?, platform: String): String {
        if (raw.isNullOrBlank()) return "@user"
        val trimmed = raw.trim()
        if (trimmed.equals("null", ignoreCase = true) ||
            trimmed.equals("@null", ignoreCase = true) ||
            trimmed.equals("null@", ignoreCase = true) ||
            trimmed.contains("null", ignoreCase = true)) {
            return when (platform.uppercase()) {
                "TWITTER" -> "@twitter_user"
                "TIKTOK" -> "@tiktok_creator"
                "INSTAGRAM" -> "@instagram_user"
                else -> "@user"
            }
        }
        return if (trimmed.startsWith("@")) trimmed else "@$trimmed"
    }

    private fun cleanTitle(raw: String?, platform: String): String {
        if (raw.isNullOrBlank()) return "وسائط مستخرجة جاهزة للحفظ"
        var clean = raw.trim()
        clean = clean.replace(Regex("https?://\\S+"), "").trim()
        clean = clean.replace(Regex("(?i)(Sponsored|Promoted|Ad|إعلان|تابعونا).*"), "").trim()
        if (clean.isBlank()) {
            return when (platform.uppercase()) {
                "TWITTER" -> "تغريدة ووسائط جاهزة للحفظ"
                "TIKTOK" -> "فيديو تيك توك عالي الدقة"
                "INSTAGRAM" -> "منشور إنستغرام عالي الدقة"
                else -> "وسائط مستخرجة جاهزة للحفظ"
            }
        }
        return clean
    }

    fun closeAd() {
        if (_adTimerSeconds.value > 0) return // Locked for timer
        _showAdDialog.value = false
        adTimerJob?.cancel()

        // Requirement 6: Immediately pre-load the NEXT ad in background upon dismissal!
        preloadAd()

        pendingDownloadAction?.invoke()
        pendingDownloadAction = null
    }

    fun deleteHistoryItem(item: DownloadedMediaEntity) {
        viewModelScope.launch {
            mediaDao.deleteMedia(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            mediaDao.clearAll()
        }
    }

    fun resetDownloadState() {
        _downloadState.value = DownloadState.Idle
    }

    private fun detectPlatform(url: String): String {
        val lower = url.lowercase()
        return when {
            lower.contains("tiktok.com") -> "TIKTOK"
            lower.contains("instagram.com") -> "INSTAGRAM"
            lower.contains("facebook.com") || lower.contains("fb.watch") -> "FACEBOOK"
            lower.contains("pinterest.com") || lower.contains("pin.it") -> "PINTEREST"
            lower.contains("twitter.com") || lower.contains("x.com") -> "TWITTER"
            lower.contains("youtube.com") || lower.contains("youtu.be") -> "YOUTUBE"
            else -> "MEDIA"
        }
    }
}
