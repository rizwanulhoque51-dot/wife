package com.example.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

object GalleryMusicManager {
    private const val TAG = "GalleryMusicManager"

    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared = false
    private var fadeJob: Job? = null
    private val managerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // UI exposed states
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    private val _volume = MutableStateFlow(0.8f) // default 80% volume
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _musicTitle = MutableStateFlow<String?>(null)
    val musicTitle: StateFlow<String?> = _musicTitle.asStateFlow()

    private var progressTrackingJob: Job? = null

    /**
     * Copy chosen file into local application internal files directory.
     * Returns the local file path as a string.
     */
    fun copyUriToInternalStorage(context: Context, sourceUri: Uri): String? {
        val appContext = context.applicationContext
        try {
            val targetFile = File(appContext.filesDir, "gallery_bg_music.mp3")
            appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Log.d(TAG, "Successfully copied MP3 to internal storage: ${targetFile.absolutePath}")
            return targetFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy MP3: ${e.message}", e)
            return null
        }
    }

    /**
     * Plays the uploaded background music.
     * Returns true if a source was found and initialized, false otherwise.
     */
    fun startPlay(context: Context, customUriStr: String?, startPositionMs: Int): Boolean {
        // Stop any currently running instance first
        stop()

        if (customUriStr.isNullOrEmpty()) {
            _musicTitle.value = null
            return false
        }

        val appContext = context.applicationContext
        var initialized = false
        var title = "Gallery Theme"

        try {
            val player = MediaPlayer()
            mediaPlayer = player

            try {
                val uri = Uri.parse(customUriStr)
                if (uri.scheme == "file" || uri.scheme == null) {
                    val file = File(uri.path ?: customUriStr)
                    if (file.exists()) {
                        player.setDataSource(file.absolutePath)
                        title = file.name
                        initialized = true
                        Log.d(TAG, "Initialized with local file path: ${file.absolutePath}")
                    }
                } else {
                    player.setDataSource(appContext, uri)
                    title = getFileName(appContext, uri) ?: "Selected Custom MP3"
                    initialized = true
                    Log.d(TAG, "Initialized with content Uri: $customUriStr")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load custom background music Uri: ${e.message}")
            }

            if (!initialized) {
                Log.d(TAG, "No background MP3 file uploaded or found.")
                mediaPlayer?.release()
                mediaPlayer = null
                _musicTitle.value = null
                return false
            }

            _musicTitle.value = title
            player.isLooping = true
            player.setVolume(0f, 0f) // start at 0 for fade in

            player.setOnPreparedListener { mp ->
                isPrepared = true
                _duration.value = mp.duration
                if (startPositionMs > 0 && startPositionMs < mp.duration) {
                    mp.seekTo(startPositionMs)
                }
                mp.start()
                _isPlaying.value = true
                startProgressTracking()
                fadeIn(volume.value)
            }

            player.setOnErrorListener { mp, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                stop()
                false
            }

            player.prepareAsync()
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Error during media player setup: ${e.message}", e)
            stop()
            return false
        }
    }

    fun pause() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    _isPlaying.value = false
                    stopProgressTracking()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing player: ${e.message}")
        }
    }

    fun resume() {
        try {
            mediaPlayer?.let { player ->
                if (!player.isPlaying && isPrepared) {
                    player.start()
                    _isPlaying.value = true
                    startProgressTracking()
                    fadeIn(volume.value)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming player: ${e.message}")
        }
    }

    fun restart() {
        try {
            mediaPlayer?.let { player ->
                if (isPrepared) {
                    player.seekTo(0)
                    _currentPosition.value = 0
                    if (!player.isPlaying) {
                        player.start()
                        _isPlaying.value = true
                        startProgressTracking()
                    }
                    fadeIn(volume.value)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restarting player: ${e.message}")
        }
    }

    fun seekTo(positionMs: Int) {
        try {
            mediaPlayer?.let { player ->
                if (isPrepared) {
                    player.seekTo(positionMs)
                    _currentPosition.value = positionMs
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking player: ${e.message}")
        }
    }

    fun setVolume(vol: Float) {
        val boundedVol = vol.coerceIn(0f, 1f)
        _volume.value = boundedVol
        try {
            mediaPlayer?.let { player ->
                if (isPrepared) {
                    player.setVolume(boundedVol, boundedVol)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting volume: ${e.message}")
        }
    }

    fun stop() {
        stopProgressTracking()
        fadeJob?.cancel()
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping or releasing player: ${e.message}")
        } finally {
            mediaPlayer = null
            isPrepared = false
            _isPlaying.value = false
            _currentPosition.value = 0
            _duration.value = 0
        }
    }

    fun getCurrentPositionMs(): Int {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun fadeIn(targetVolume: Float) {
        fadeJob?.cancel()
        fadeJob = managerScope.launch {
            try {
                var currentVol = 0.0f
                mediaPlayer?.setVolume(0.0f, 0.0f)
                val steps = 15
                val fadeDurationMs = 1500L
                val delayTime = fadeDurationMs / steps
                val volStep = targetVolume / steps

                for (i in 1..steps) {
                    delay(delayTime)
                    currentVol += volStep
                    val finalVol = currentVol.coerceAtMost(targetVolume)
                    mediaPlayer?.setVolume(finalVol, finalVol)
                }
            } catch (e: CancellationException) {
                // job cancelled
            } catch (e: Exception) {
                Log.e(TAG, "Error fading in: ${e.message}")
            }
        }
    }

    private fun startProgressTracking() {
        progressTrackingJob?.cancel()
        progressTrackingJob = managerScope.launch {
            while (isActive) {
                try {
                    mediaPlayer?.let { player ->
                        if (player.isPlaying) {
                            _currentPosition.value = player.currentPosition
                        }
                    }
                } catch (e: Exception) {
                    // media player state error, ignore
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracking() {
        progressTrackingJob?.cancel()
        progressTrackingJob = null
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        return cursor.getString(nameIndex)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting filename from provider: ${e.message}")
            }
        }
        return uri.path?.substringAfterLast('/')
    }
}
