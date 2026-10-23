package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM downloaded_media ORDER BY timestamp DESC")
    fun getAllMedia(): Flow<List<DownloadedMediaEntity>>

    @Query("SELECT COUNT(*) FROM downloaded_media")
    fun getMediaCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: DownloadedMediaEntity): Long

    @Delete
    suspend fun deleteMedia(media: DownloadedMediaEntity)

    @Query("DELETE FROM downloaded_media WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM downloaded_media")
    suspend fun clearAll()
}
