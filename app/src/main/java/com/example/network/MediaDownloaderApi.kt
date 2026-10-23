package com.example.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MediaDownloaderApi {

    @GET("api/platforms")
    suspend fun getPlatforms(): Response<PlatformsResponse>

    @GET("api/status")
    suspend fun getStatus(): Response<StatusResponse>

    @POST("api/extract")
    suspend fun extractPost(@Body request: ExtractRequest): Response<ExtractResponse>

    @GET("api/extract")
    suspend fun extractGet(
        @Query("url") url: String,
        @Query("media_type") mediaType: String = "video"
    ): Response<ExtractResponse>

    @POST("api/saved")
    suspend fun reportSaved(@Body request: SaveReportRequest): Response<Map<String, Any>>
}
