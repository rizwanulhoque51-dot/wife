package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.db.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    // DB flows
    val messages: StateFlow<List<MessageEntity>>
    val photos: StateFlow<List<PhotoEntity>>
    val galleryItems: StateFlow<List<GalleryEntity>>
    val diaryEntries: StateFlow<List<DiaryEntity>>

    // Local runtime states
    private val _activeUser = MutableStateFlow("Husband")
    val activeUser: StateFlow<String> = _activeUser.asStateFlow()

    private val _themeMode = MutableStateFlow("light") // "light" or "dark"
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true) // Start logged in for demo or lock
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Screen unlock statuses
    private val _isAppUnlocked = MutableStateFlow(false)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    private val _isMessagesUnlocked = MutableStateFlow(false)
    val isMessagesUnlocked: StateFlow<Boolean> = _isMessagesUnlocked.asStateFlow()

    private val _isPhotosUnlocked = MutableStateFlow(false)
    val isPhotosUnlocked: StateFlow<Boolean> = _isPhotosUnlocked.asStateFlow()

    private val _isGalleryUnlocked = MutableStateFlow(false)
    val isGalleryUnlocked: StateFlow<Boolean> = _isGalleryUnlocked.asStateFlow()

    // Config PINs cache
    private val _appPin = MutableStateFlow("123456")
    val appPin = _appPin.asStateFlow()

    private val _msgPin = MutableStateFlow("123456")
    val msgPin = _msgPin.asStateFlow()

    private val _photoPin = MutableStateFlow("123456")
    val photoPin = _photoPin.asStateFlow()

    private val _galleryPin = MutableStateFlow("123456")
    val galleryPin = _galleryPin.asStateFlow()

    // Active screen navigation
    private val _currentScreen = MutableStateFlow("splash")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Chat wallpaper
    private val _chatWallpaper = MutableStateFlow("default")
    val chatWallpaper = _chatWallpaper.asStateFlow()

    // Couple profile URI state
    private val _coupleProfileUri = MutableStateFlow<String?>(null)
    val coupleProfileUri: StateFlow<String?> = _coupleProfileUri.asStateFlow()

    // Gallery background music states
    private val _galleryMusicUri = MutableStateFlow<String?>(null)
    val galleryMusicUri: StateFlow<String?> = _galleryMusicUri.asStateFlow()

    private val _galleryMusicPosition = MutableStateFlow(0)
    val galleryMusicPosition: StateFlow<Int> = _galleryMusicPosition.asStateFlow()

    private val _galleryMusicVolume = MutableStateFlow(0.8f)
    val galleryMusicVolume: StateFlow<Float> = _galleryMusicVolume.asStateFlow()

    // Dynamic states
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    // Live sync and online states
    val isOnline = MutableStateFlow(true)

    // Call state variables
    private val _callState = MutableStateFlow("idle") // "idle", "ringing", "connected"
    val callState = _callState.asStateFlow()

    private val _callType = MutableStateFlow("voice") // "voice", "video"
    val callType = _callType.asStateFlow()

    private val _caller = MutableStateFlow("") // "Husband" or "Wife"
    val caller = _caller.asStateFlow()

    private val _callee = MutableStateFlow("") // "Husband" or "Wife"
    val callee = _callee.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted = _isMuted.asStateFlow()

    private val _isCameraEnabled = MutableStateFlow(true)
    val isCameraEnabled = _isCameraEnabled.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn = _isSpeakerOn.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(true)
    val isFrontCamera = _isFrontCamera.asStateFlow()

    private val _callDuration = MutableStateFlow(0)
    val callDuration = _callDuration.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao())

        // Collect flows
        messages = repository.allMessages.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        photos = repository.allPhotos.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        galleryItems = repository.allGalleryItems.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        diaryEntries = repository.allDiaryEntries.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        // Load settings and prepopulate
        viewModelScope.launch {
            repository.prepopulateDatabaseIfEmpty()
            _appPin.value = repository.getSettingValue(AppRepository.KEY_APP_PIN, "123456")
            _msgPin.value = repository.getSettingValue(AppRepository.KEY_MSG_PIN, "123456")
            _photoPin.value = repository.getSettingValue(AppRepository.KEY_PHOTO_PIN, "123456")
            _galleryPin.value = repository.getSettingValue(AppRepository.KEY_GALLERY_PIN, "123456")
            _activeUser.value = repository.getSettingValue(AppRepository.KEY_ACTIVE_USER, "Husband")
            _themeMode.value = repository.getSettingValue(AppRepository.KEY_THEME, "light")
            _chatWallpaper.value = repository.getSettingValue(AppRepository.KEY_CHAT_WALLPAPER, "default")
            val savedUri = repository.getSettingValue(AppRepository.KEY_COUPLE_PROFILE_URI, "")
            _coupleProfileUri.value = if (savedUri.isEmpty()) null else savedUri

            val savedMusicUri = repository.getSettingValue(AppRepository.KEY_GALLERY_MUSIC_URI, "")
            _galleryMusicUri.value = if (savedMusicUri.isEmpty()) null else savedMusicUri

            val savedPosition = repository.getSettingValue(AppRepository.KEY_GALLERY_MUSIC_POSITION, "0")
            _galleryMusicPosition.value = savedPosition.toIntOrNull() ?: 0

            val savedVolume = repository.getSettingValue(AppRepository.KEY_GALLERY_MUSIC_VOLUME, "0.8")
            _galleryMusicVolume.value = savedVolume.toFloatOrNull() ?: 0.8f
        }
    }

    fun saveCoupleProfile(uri: String) {
        viewModelScope.launch {
            _coupleProfileUri.value = uri
            repository.saveSetting(AppRepository.KEY_COUPLE_PROFILE_URI, uri)
        }
    }

    fun saveGalleryMusicUri(uri: String?) {
        viewModelScope.launch {
            _galleryMusicUri.value = uri
            repository.saveSetting(AppRepository.KEY_GALLERY_MUSIC_URI, uri ?: "")
        }
    }

    fun saveGalleryMusicPosition(position: Int) {
        viewModelScope.launch {
            _galleryMusicPosition.value = position
            repository.saveSetting(AppRepository.KEY_GALLERY_MUSIC_POSITION, position.toString())
        }
    }

    fun saveGalleryMusicVolume(volume: Float) {
        viewModelScope.launch {
            _galleryMusicVolume.value = volume
            repository.saveSetting(AppRepository.KEY_GALLERY_MUSIC_VOLUME, volume.toString())
        }
    }

    fun toggleActiveUser() {
        viewModelScope.launch {
            val newUser = if (_activeUser.value == "Husband") "Wife" else "Husband"
            _activeUser.value = newUser
            repository.saveSetting(AppRepository.KEY_ACTIVE_USER, newUser)
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val newTheme = if (_themeMode.value == "light") "dark" else "light"
            _themeMode.value = newTheme
            repository.saveSetting(AppRepository.KEY_THEME, newTheme)
        }
    }

    fun setWallpaper(wallpaper: String) {
        viewModelScope.launch {
            _chatWallpaper.value = wallpaper
            repository.saveSetting(AppRepository.KEY_CHAT_WALLPAPER, wallpaper)
        }
    }

    // Auth Simulation
    fun registerAccount(name: String, email: String, pin: String) {
        viewModelScope.launch {
            _isLoggedIn.value = true
            _appPin.value = pin
            _msgPin.value = pin
            _photoPin.value = pin
            _galleryPin.value = pin
            repository.saveSetting(AppRepository.KEY_APP_PIN, pin)
            repository.saveSetting(AppRepository.KEY_MSG_PIN, pin)
            repository.saveSetting(AppRepository.KEY_PHOTO_PIN, pin)
            repository.saveSetting(AppRepository.KEY_GALLERY_PIN, pin)
            if (activeUser.value == "Husband") {
                repository.saveSetting(AppRepository.KEY_COUPLE_NAME_HUSBAND, name)
            } else {
                repository.saveSetting(AppRepository.KEY_COUPLE_NAME_WIFE, name)
            }
        }
    }

    fun login(email: String) {
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
        _isAppUnlocked.value = false
        _isMessagesUnlocked.value = false
        _isPhotosUnlocked.value = false
        _isGalleryUnlocked.value = false
    }

    // Unlock logic
    fun unlockApp(pin: String): Boolean {
        return if (pin == _appPin.value) {
            _isAppUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun unlockMessages(pin: String): Boolean {
        return if (pin == _msgPin.value) {
            _isMessagesUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun unlockPhotos(pin: String): Boolean {
        return if (pin == _photoPin.value) {
            _isPhotosUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun unlockGallery(pin: String): Boolean {
        return if (pin == _galleryPin.value) {
            _isGalleryUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun lockAll() {
        _isAppUnlocked.value = false
        _isMessagesUnlocked.value = false
        _isPhotosUnlocked.value = false
        _isGalleryUnlocked.value = false
    }

    // Settings pin change
    fun changeAppPin(newPin: String) {
        viewModelScope.launch {
            _appPin.value = newPin
            repository.saveSetting(AppRepository.KEY_APP_PIN, newPin)
        }
    }

    fun changeMsgPin(newPin: String) {
        viewModelScope.launch {
            _msgPin.value = newPin
            repository.saveSetting(AppRepository.KEY_MSG_PIN, newPin)
        }
    }

    fun changePhotoPin(newPin: String) {
        viewModelScope.launch {
            _photoPin.value = newPin
            repository.saveSetting(AppRepository.KEY_PHOTO_PIN, newPin)
        }
    }

    fun changeGalleryPin(newPin: String) {
        viewModelScope.launch {
            _galleryPin.value = newPin
            repository.saveSetting(AppRepository.KEY_GALLERY_PIN, newPin)
        }
    }

    // Messages
    fun sendMessage(content: String, replyTo: MessageEntity? = null, mediaType: String = "text", mediaUri: String? = null) {
        viewModelScope.launch {
            val msg = MessageEntity(
                sender = _activeUser.value,
                content = content,
                mediaType = mediaType,
                mediaUri = mediaUri,
                replyToId = replyTo?.id,
                replyToText = replyTo?.content,
                replyToSender = replyTo?.sender,
                isSeen = false
            )
            repository.insertMessage(msg)
        }
    }

    fun deleteMessageForEveryone(message: MessageEntity) {
        viewModelScope.launch {
            val updated = message.copy(isDeletedForEveryone = true, content = "This message was deleted")
            repository.updateMessage(updated)
        }
    }

    fun deleteMessageForMe(message: MessageEntity) {
        viewModelScope.launch {
            val currentDeleted = message.isDeletedForMe
            val newlyDeleted = if (currentDeleted.isEmpty()) _activeUser.value else "Both"
            val updated = message.copy(isDeletedForMe = newlyDeleted)
            repository.updateMessage(updated)
        }
    }

    fun addReaction(message: MessageEntity, reaction: String) {
        viewModelScope.launch {
            val currentReactions = message.reactions
            val updatedReactions = if (currentReactions.contains(reaction)) {
                // Remove if already reacted
                currentReactions.replace(reaction, "").replace(",,", ",").trim(',')
            } else {
                if (currentReactions.isEmpty()) reaction else "$currentReactions,$reaction"
            }
            val updated = message.copy(reactions = updatedReactions)
            repository.updateMessage(updated)
        }
    }

    // Photos
    fun uploadPhoto(caption: String, imageUri: String? = null, imageRes: Int = R.drawable.img_couple_profile_1783693747710, isHD: Boolean = false) {
        viewModelScope.launch {
            val photo = PhotoEntity(
                caption = caption,
                imageRes = imageRes,
                imageUri = imageUri,
                uploadedBy = _activeUser.value,
                isHD = isHD
            )
            repository.insertPhoto(photo)

            // Also mirror in general gallery
            val galleryItem = GalleryEntity(
                title = if (caption.length > 15) caption.take(12) + "..." else caption.ifEmpty { "Couple Moment" },
                mediaRes = imageRes,
                mediaUri = imageUri,
                mediaType = "photo",
                album = "Photos"
            )
            repository.insertGalleryItem(galleryItem)
        }
    }

    fun togglePhotoFavourite(photo: PhotoEntity) {
        viewModelScope.launch {
            val updated = photo.copy(isFavourite = !photo.isFavourite)
            repository.updatePhoto(updated)
        }
    }

    fun deletePhoto(photo: PhotoEntity) {
        viewModelScope.launch {
            repository.deletePhoto(photo)
        }
    }

    // Gallery
    fun uploadGalleryItem(title: String, mediaRes: Int, mediaUri: String?, type: String, album: String) {
        viewModelScope.launch {
            val item = GalleryEntity(
                title = title,
                mediaRes = mediaRes,
                mediaUri = mediaUri,
                mediaType = type,
                album = album
            )
            repository.insertGalleryItem(item)
        }
    }

    fun deleteGalleryItem(item: GalleryEntity) {
        viewModelScope.launch {
            repository.deleteGalleryItem(item)
        }
    }

    // Diary
    fun addDiaryEntry(title: String, content: String, mood: String) {
        viewModelScope.launch {
            val entry = DiaryEntity(
                title = title,
                content = content,
                writtenBy = _activeUser.value,
                mood = mood
            )
            repository.insertDiaryEntry(entry)
        }
    }

    fun deleteDiaryEntry(entry: DiaryEntity) {
        viewModelScope.launch {
            repository.deleteDiaryEntry(entry)
        }
    }

    fun setTyping(typing: Boolean) {
        _isTyping.value = typing
    }

    // Call Actions
    fun startCall(type: String) {
        _callType.value = type
        _caller.value = _activeUser.value
        _callee.value = if (_activeUser.value == "Husband") "Wife" else "Husband"
        _callState.value = "ringing"
        _isMuted.value = false
        _isCameraEnabled.value = true
        _isSpeakerOn.value = false
        _isFrontCamera.value = true
        _callDuration.value = 0
    }

    fun acceptCall() {
        _callState.value = "connected"
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_callState.value == "connected") {
                delay(1000)
                _callDuration.value += 1
            }
        }
    }

    fun endCall(isDeclined: Boolean = false) {
        viewModelScope.launch {
            timerJob?.cancel()
            timerJob = null

            val durationSecs = _callDuration.value
            val typeName = if (_callType.value == "video") "Video Call" else "Voice Call"
            val callerUser = _caller.value
            val calleeUser = _callee.value
            val currentCallState = _callState.value

            if (currentCallState == "connected") {
                val minutes = durationSecs / 60
                val seconds = durationSecs % 60
                val durationStr = String.format("%02d:%02d", minutes, seconds)
                val content = "$typeName Ended ($durationStr)"
                val msg = MessageEntity(
                    sender = callerUser,
                    content = content,
                    mediaType = "call_ended",
                    isSeen = true
                )
                repository.insertMessage(msg)
            } else if (currentCallState == "ringing") {
                val content = if (isDeclined) "Declined $typeName" else "Missed $typeName"
                val msg = MessageEntity(
                    sender = callerUser,
                    content = content,
                    mediaType = "call_missed",
                    isSeen = false
                )
                repository.insertMessage(msg)
            }

            _callState.value = "idle"
            _callDuration.value = 0
        }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun toggleCamera() {
        _isCameraEnabled.value = !_isCameraEnabled.value
    }

    fun toggleSpeaker() {
        _isSpeakerOn.value = !_isSpeakerOn.value
    }

    fun toggleFrontCamera() {
        _isFrontCamera.value = !_isFrontCamera.value
    }
}
