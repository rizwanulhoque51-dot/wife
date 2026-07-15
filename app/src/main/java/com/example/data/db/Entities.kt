package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "Husband" or "Wife"
    val content: String,
    val mediaUri: String? = null,
    val mediaType: String = "text", // "text", "image", "video", "voice", "file"
    val timestamp: Long = System.currentTimeMillis(),
    val isSeen: Boolean = false,
    val replyToId: Int? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val reactions: String = "", // e.g. "❤️,😆"
    val isDeletedForEveryone: Boolean = false,
    val isDeletedForMe: String = "" // "Husband" or "Wife" or ""
)

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val caption: String,
    val imageRes: Int, // drawable resource ID
    val imageUri: String? = null, // user selected file
    val uploadedBy: String, // "Husband" or "Wife"
    val timestamp: Long = System.currentTimeMillis(),
    val isFavourite: Boolean = false,
    val isHD: Boolean = false
)

@Entity(tableName = "gallery")
data class GalleryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val mediaRes: Int,
    val mediaUri: String? = null,
    val mediaType: String, // "photo" or "video"
    val album: String = "General", // Album name
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "diary")
data class DiaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val writtenBy: String, // "Husband" or "Wife"
    val timestamp: Long = System.currentTimeMillis(),
    val mood: String = "❤️"
)
