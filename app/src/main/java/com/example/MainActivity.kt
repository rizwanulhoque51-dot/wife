package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AppViewModel = viewModel()

            // Observe states from ViewModel
            val currentScreen by viewModel.currentScreen.collectAsState()
            val activeUser by viewModel.activeUser.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()

            // Passcode locks check
            val isAppUnlocked by viewModel.isAppUnlocked.collectAsState()
            val isMessagesUnlocked by viewModel.isMessagesUnlocked.collectAsState()
            val isPhotosUnlocked by viewModel.isPhotosUnlocked.collectAsState()
            val isGalleryUnlocked by viewModel.isGalleryUnlocked.collectAsState()

            // PIN keys cache
            val appPin by viewModel.appPin.collectAsState()
            val msgPin by viewModel.msgPin.collectAsState()
            val photoPin by viewModel.photoPin.collectAsState()
            val galleryPin by viewModel.galleryPin.collectAsState()

            // DB flows
            val messages by viewModel.messages.collectAsState()
            val photos by viewModel.photos.collectAsState()
            val galleryItems by viewModel.galleryItems.collectAsState()
            val diaryEntries by viewModel.diaryEntries.collectAsState()

            val isTyping by viewModel.isTyping.collectAsState()
            val chatWallpaper by viewModel.chatWallpaper.collectAsState()
            val coupleProfileUri by viewModel.coupleProfileUri.collectAsState()
            val galleryMusicUri by viewModel.galleryMusicUri.collectAsState()
            val galleryMusicPosition by viewModel.galleryMusicPosition.collectAsState()
            val galleryMusicVolume by viewModel.galleryMusicVolume.collectAsState()

            // Call States
            val callState by viewModel.callState.collectAsState()
            val callType by viewModel.callType.collectAsState()
            val caller by viewModel.caller.collectAsState()
            val callee by viewModel.callee.collectAsState()
            val isMuted by viewModel.isMuted.collectAsState()
            val isCameraEnabled by viewModel.isCameraEnabled.collectAsState()
            val isSpeakerOn by viewModel.isSpeakerOn.collectAsState()
            val isFrontCamera by viewModel.isFrontCamera.collectAsState()
            val callDuration by viewModel.callDuration.collectAsState()

            var destinationScreenState by remember { mutableStateOf("splash") }

            // Dynamic Navigation Helper
            fun navigateTo(screen: String) {
                destinationScreenState = screen
            }

            MyApplicationTheme(darkTheme = themeMode == "dark") {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (callState != "idle") {
                        CallScreen(
                            activeUser = activeUser,
                            callState = callState,
                            callType = callType,
                            caller = caller,
                            callee = callee,
                            isMuted = isMuted,
                            isCameraEnabled = isCameraEnabled,
                            isSpeakerOn = isSpeakerOn,
                            isFrontCamera = isFrontCamera,
                            callDuration = callDuration,
                            coupleProfileUri = coupleProfileUri,
                            onImageSelected = { viewModel.saveCoupleProfile(it) },
                            onAccept = { viewModel.acceptCall() },
                            onDecline = { viewModel.endCall(isDeclined = true) },
                            onEnd = { viewModel.endCall() },
                            onToggleMute = { viewModel.toggleMute() },
                            onToggleCamera = { viewModel.toggleCamera() },
                            onToggleSpeaker = { viewModel.toggleSpeaker() },
                            onToggleFrontCamera = { viewModel.toggleFrontCamera() }
                        )
                    } else {
                        Crossfade(
                            targetState = destinationScreenState,
                            animationSpec = androidx.compose.animation.core.tween(400),
                            label = "screen_navigation"
                        ) { screen ->
                        when (screen) {
                            "splash" -> {
                                SplashScreen(
                                    onTimeout = {
                                        if (isLoggedIn) {
                                            navigateTo("app_lock")
                                        } else {
                                            navigateTo("auth")
                                        }
                                    }
                                )
                            }
                            "auth" -> {
                                AuthScreen(
                                    isDark = themeMode == "dark",
                                    coupleProfileUri = coupleProfileUri,
                                    onImageSelected = { viewModel.saveCoupleProfile(it) },
                                    onAuthSuccess = {
                                        navigateTo("app_lock")
                                    },
                                    onRegister = { name, email, pin ->
                                        viewModel.registerAccount(name, email, pin)
                                    },
                                    onLoginSimulated = { email ->
                                        viewModel.login(email)
                                    }
                                )
                            }
                            "app_lock" -> {
                                LockScreen(
                                    title = "Enter App Password",
                                    correctPin = appPin,
                                    isDark = themeMode == "dark",
                                    coupleProfileUri = coupleProfileUri,
                                    onImageSelected = { viewModel.saveCoupleProfile(it) },
                                    onUnlockSuccess = {
                                        viewModel.unlockApp(appPin)
                                        navigateTo("dashboard")
                                    }
                                )
                            }
                            "dashboard" -> {
                                DashboardScreen(
                                    activeUser = activeUser,
                                    themeMode = themeMode,
                                    coupleProfileUri = coupleProfileUri,
                                    onImageSelected = { viewModel.saveCoupleProfile(it) },
                                    onNavigate = { dest ->
                                        if (dest == "messages_lock") {
                                            if (isMessagesUnlocked) navigateTo("chat") else navigateTo("messages_lock")
                                        } else if (dest == "photos_lock") {
                                            if (isPhotosUnlocked) navigateTo("photos") else navigateTo("photos_lock")
                                        } else if (dest == "gallery_lock") {
                                            if (isGalleryUnlocked) navigateTo("gallery") else navigateTo("gallery_lock")
                                        } else {
                                            navigateTo(dest)
                                        }
                                    },
                                    onToggleUser = {
                                        viewModel.toggleActiveUser()
                                    },
                                    onLogout = {
                                        viewModel.logout()
                                        navigateTo("auth")
                                    }
                                )
                            }
                            "messages_lock" -> {
                                LockScreen(
                                    title = "Enter Password to open Messages",
                                    correctPin = msgPin,
                                    isDark = themeMode == "dark",
                                    coupleProfileUri = coupleProfileUri,
                                    onImageSelected = { viewModel.saveCoupleProfile(it) },
                                    onUnlockSuccess = {
                                        viewModel.unlockMessages(msgPin)
                                        navigateTo("chat")
                                    },
                                    onBack = { navigateTo("dashboard") }
                                )
                            }
                            "photos_lock" -> {
                                LockScreen(
                                    title = "Enter Password to open Photos",
                                    correctPin = photoPin,
                                    isDark = themeMode == "dark",
                                    coupleProfileUri = coupleProfileUri,
                                    onImageSelected = { viewModel.saveCoupleProfile(it) },
                                    onUnlockSuccess = {
                                        viewModel.unlockPhotos(photoPin)
                                        navigateTo("photos")
                                    },
                                    onBack = { navigateTo("dashboard") }
                                )
                            }
                            "gallery_lock" -> {
                                LockScreen(
                                    title = "Enter Password to open Gallery",
                                    correctPin = galleryPin,
                                    isDark = themeMode == "dark",
                                    coupleProfileUri = coupleProfileUri,
                                    onImageSelected = { viewModel.saveCoupleProfile(it) },
                                    onUnlockSuccess = {
                                        viewModel.unlockGallery(galleryPin)
                                        navigateTo("gallery")
                                    },
                                    onBack = { navigateTo("dashboard") }
                                )
                            }
                            "chat" -> {
                                ChatScreen(
                                    messages = messages,
                                    activeUser = activeUser,
                                    themeMode = themeMode,
                                    isTyping = isTyping,
                                    chatWallpaper = chatWallpaper,
                                    coupleProfileUri = coupleProfileUri,
                                    onImageSelected = { viewModel.saveCoupleProfile(it) },
                                    onSendMessage = { text, replyTo, type, uri ->
                                        viewModel.sendMessage(text, replyTo, type, uri)
                                    },
                                    onDeleteMessageForEveryone = { msg ->
                                        viewModel.deleteMessageForEveryone(msg)
                                    },
                                    onDeleteMessageForMe = { msg ->
                                        viewModel.deleteMessageForMe(msg)
                                    },
                                    onAddReaction = { msg, react ->
                                        viewModel.addReaction(msg, react)
                                    },
                                    onSetTyping = { typing ->
                                        viewModel.setTyping(typing)
                                    },
                                    onStartCall = { type ->
                                        viewModel.startCall(type)
                                    },
                                    onBack = { navigateTo("dashboard") }
                                )
                            }
                            "photos" -> {
                                PhotosScreen(
                                    photos = photos,
                                    activeUser = activeUser,
                                    themeMode = themeMode,
                                    onToggleFavourite = { photo ->
                                        viewModel.togglePhotoFavourite(photo)
                                    },
                                    onDeletePhoto = { photo ->
                                        viewModel.deletePhoto(photo)
                                    },
                                    onNavigateToAddPhoto = {
                                        navigateTo("add_photo")
                                    },
                                    onBack = { navigateTo("dashboard") }
                                )
                            }
                            "add_photo" -> {
                                UploadPhotoScreen(
                                    themeMode = themeMode,
                                    onUploadSuccess = { caption, uriStr, isHd ->
                                        viewModel.uploadPhoto(caption = caption, imageUri = uriStr, isHD = isHd)
                                    },
                                    onBack = { navigateTo("photos") }
                                )
                            }
                            "gallery" -> {
                                GalleryScreen(
                                    galleryItems = galleryItems,
                                    themeMode = themeMode,
                                    musicUri = galleryMusicUri,
                                    lastMusicPosition = galleryMusicPosition,
                                    musicVolume = galleryMusicVolume,
                                    onSaveMusicUri = { viewModel.saveGalleryMusicUri(it) },
                                    onSaveMusicPosition = { viewModel.saveGalleryMusicPosition(it) },
                                    onSaveMusicVolume = { viewModel.saveGalleryMusicVolume(it) },
                                    onUploadItem = { title, res, type, album ->
                                        viewModel.uploadGalleryItem(title, res, null, type, album)
                                    },
                                    onDeleteItem = { item ->
                                        viewModel.deleteGalleryItem(item)
                                    },
                                    onBack = { navigateTo("dashboard") }
                                )
                            }
                            "diary" -> {
                                DiaryScreen(
                                    diaryEntries = diaryEntries,
                                    activeUser = activeUser,
                                    themeMode = themeMode,
                                    onAddEntry = { title, body, mood ->
                                        viewModel.addDiaryEntry(title, body, mood)
                                    },
                                    onDeleteEntry = { entry ->
                                        viewModel.deleteDiaryEntry(entry)
                                    },
                                    onBack = { navigateTo("dashboard") }
                                )
                            }
                            "settings" -> {
                                SettingsScreen(
                                    themeMode = themeMode,
                                    coupleProfileUri = coupleProfileUri,
                                    onImageSelected = { viewModel.saveCoupleProfile(it) },
                                    onToggleTheme = {
                                        viewModel.toggleTheme()
                                    },
                                    onChangeAppPin = { pin ->
                                        viewModel.changeAppPin(pin)
                                    },
                                    onChangeMsgPin = { pin ->
                                        viewModel.changeMsgPin(pin)
                                    },
                                    onChangePhotoPin = { pin ->
                                        viewModel.changePhotoPin(pin)
                                    },
                                    onChangeGalleryPin = { pin ->
                                        viewModel.changeGalleryPin(pin)
                                    },
                                    onLogout = {
                                        viewModel.logout()
                                        navigateTo("auth")
                                    },
                                    onBack = { navigateTo("dashboard") }
                                )
                            }
                        }
                    }
                }
                }
            }
        }
    }
}
