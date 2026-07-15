package com.example.data.repository

import com.example.R
import com.example.data.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AppRepository(private val appDao: AppDao) {

    // Settings Keys
    companion object {
        const val KEY_APP_PIN = "app_pin"
        const val KEY_MSG_PIN = "msg_pin"
        const val KEY_PHOTO_PIN = "photo_pin"
        const val KEY_GALLERY_PIN = "gallery_pin"
        const val KEY_ACTIVE_USER = "active_user" // "Husband" or "Wife"
        const val KEY_THEME = "theme" // "light" or "dark"
        const val KEY_CHAT_WALLPAPER = "chat_wallpaper" // "default" or resource ID
        const val KEY_COUPLE_NAME_HUSBAND = "couple_name_husband"
        const val KEY_COUPLE_NAME_WIFE = "couple_name_wife"
        const val KEY_COUPLE_PROFILE_URI = "couple_profile_uri"
        const val KEY_GALLERY_MUSIC_URI = "gallery_music_uri"
        const val KEY_GALLERY_MUSIC_POSITION = "gallery_music_position"
        const val KEY_GALLERY_MUSIC_VOLUME = "gallery_music_volume"
    }

    // Exposed Flows
    val allMessages: Flow<List<MessageEntity>> = appDao.getAllMessagesFlow()
    val allPhotos: Flow<List<PhotoEntity>> = appDao.getAllPhotosFlow()
    val allGalleryItems: Flow<List<GalleryEntity>> = appDao.getAllGalleryFlow()
    val allDiaryEntries: Flow<List<DiaryEntity>> = appDao.getAllDiaryFlow()
    val allSettings: Flow<List<SettingEntity>> = appDao.getAllSettingsFlow()

    // Get specific settings
    suspend fun getSettingValue(key: String, defaultValue: String): String {
        return appDao.getSetting(key)?.value ?: defaultValue
    }

    suspend fun saveSetting(key: String, value: String) {
        appDao.insertSetting(SettingEntity(key, value))
    }

    // Messages operations
    suspend fun insertMessage(message: MessageEntity) = appDao.insertMessage(message)
    suspend fun updateMessage(message: MessageEntity) = appDao.updateMessage(message)
    suspend fun deleteMessage(message: MessageEntity) = appDao.deleteMessage(message)
    suspend fun deleteMessageById(id: Int) = appDao.deleteMessageById(id)

    // Photos operations
    suspend fun insertPhoto(photo: PhotoEntity) = appDao.insertPhoto(photo)
    suspend fun updatePhoto(photo: PhotoEntity) = appDao.updatePhoto(photo)
    suspend fun deletePhoto(photo: PhotoEntity) = appDao.deletePhoto(photo)
    suspend fun deletePhotoById(id: Int) = appDao.deletePhotoById(id)

    // Gallery operations
    suspend fun insertGalleryItem(item: GalleryEntity) = appDao.insertGalleryItem(item)
    suspend fun deleteGalleryItem(item: GalleryEntity) = appDao.deleteGalleryItem(item)
    suspend fun deleteGalleryById(id: Int) = appDao.deleteGalleryById(id)

    // Diary operations
    suspend fun insertDiaryEntry(entry: DiaryEntity) = appDao.insertDiaryEntry(entry)
    suspend fun deleteDiaryEntry(entry: DiaryEntity) = appDao.deleteDiaryEntry(entry)

    // Initialize the DB with beautiful romantic defaults if empty
    suspend fun prepopulateDatabaseIfEmpty() {
        // 1. PINs and active user
        if (appDao.getSetting(KEY_APP_PIN) == null) {
            appDao.insertSetting(SettingEntity(KEY_APP_PIN, "123456"))
        }
        if (appDao.getSetting(KEY_MSG_PIN) == null) {
            appDao.insertSetting(SettingEntity(KEY_MSG_PIN, "123456"))
        }
        if (appDao.getSetting(KEY_PHOTO_PIN) == null) {
            appDao.insertSetting(SettingEntity(KEY_PHOTO_PIN, "123456"))
        }
        if (appDao.getSetting(KEY_GALLERY_PIN) == null) {
            appDao.insertSetting(SettingEntity(KEY_GALLERY_PIN, "123456"))
        }
        if (appDao.getSetting(KEY_ACTIVE_USER) == null) {
            appDao.insertSetting(SettingEntity(KEY_ACTIVE_USER, "Husband"))
        }
        if (appDao.getSetting(KEY_COUPLE_NAME_HUSBAND) == null) {
            appDao.insertSetting(SettingEntity(KEY_COUPLE_NAME_HUSBAND, "Rizwan"))
        }
        if (appDao.getSetting(KEY_COUPLE_NAME_WIFE) == null) {
            appDao.insertSetting(SettingEntity(KEY_COUPLE_NAME_WIFE, "Ayesha"))
        }

        // 2. Prepopulate Chat Messages
        val messagesCount = allMessages.first().size
        if (messagesCount == 0) {
            val defaultMsgs = listOf(
                MessageEntity(sender = "Wife", content = "Hi love ❤️", timestamp = System.currentTimeMillis() - 600000),
                MessageEntity(sender = "Wife", content = "How are you?", timestamp = System.currentTimeMillis() - 590000),
                MessageEntity(sender = "Husband", content = "I'm good baby 😊\nAnd you?", timestamp = System.currentTimeMillis() - 500000, isSeen = true),
                MessageEntity(sender = "Wife", content = "I miss you so much 🥺", timestamp = System.currentTimeMillis() - 400000),
                MessageEntity(sender = "Husband", content = "I miss you more ❤️", timestamp = System.currentTimeMillis() - 300000, isSeen = true)
            )
            for (msg in defaultMsgs) {
                appDao.insertMessage(msg)
            }
        }

        // 3. Prepopulate Photos (Disabled to remove stock/demo couple photos)
        val photosCount = allPhotos.first().size
        if (photosCount == 0) {
            // Start empty as per user request to remove all stock/placeholder/AI-generated couple photos
        }

        // 4. Prepopulate Gallery (Disabled to remove stock/demo couple photos)
        val galleryCount = allGalleryItems.first().size
        if (galleryCount == 0) {
            // Start empty as per user request to remove all stock/placeholder/AI-generated couple photos
        }

        // 5. Prepopulate Diary
        val diaryCount = allDiaryEntries.first().size
        if (diaryCount == 0) {
            val defaultDiary = listOf(
                DiaryEntity(
                    title = "First Date Anniversary",
                    content = "Today we celebrated 3 years since our first coffee date! It feels like just yesterday. We walked in the park, held hands, and talked for hours. I love you more and more each day. ❤️",
                    writtenBy = "Wife",
                    timestamp = System.currentTimeMillis() - 86400000,
                    mood = "💖"
                ),
                DiaryEntity(
                    title = "Cozy Rainy Day",
                    content = "It was raining outside, so we stayed in, ordered some warm hot chocolate, and watched our favorite romantic movies together under a warm blanket. Best feeling ever.",
                    writtenBy = "Husband",
                    timestamp = System.currentTimeMillis() - 86400000 * 5,
                    mood = "🥰"
                )
            )
            for (d in defaultDiary) {
                appDao.insertDiaryEntry(d)
            }
        }
    }
}
