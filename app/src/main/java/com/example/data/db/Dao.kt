package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Settings
    @Query("SELECT * FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): SettingEntity?

    @Query("SELECT * FROM settings")
    fun getAllSettingsFlow(): Flow<List<SettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SettingEntity)

    // Messages
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: Int)

    // Photos
    @Query("SELECT * FROM photos ORDER BY timestamp DESC")
    fun getAllPhotosFlow(): Flow<List<PhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Update
    suspend fun updatePhoto(photo: PhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deletePhotoById(id: Int)

    // Gallery
    @Query("SELECT * FROM gallery ORDER BY timestamp DESC")
    fun getAllGalleryFlow(): Flow<List<GalleryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGalleryItem(item: GalleryEntity)

    @Delete
    suspend fun deleteGalleryItem(item: GalleryEntity)

    @Query("DELETE FROM gallery WHERE id = :id")
    suspend fun deleteGalleryById(id: Int)

    // Diary
    @Query("SELECT * FROM diary ORDER BY timestamp DESC")
    fun getAllDiaryFlow(): Flow<List<DiaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryEntry(entry: DiaryEntity)

    @Delete
    suspend fun deleteDiaryEntry(entry: DiaryEntity)
}
