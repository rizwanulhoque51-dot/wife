package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.GalleryEntity
import com.example.ui.components.FloatingHeartsBackground
import com.example.ui.components.GalleryMusicManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.graphicsLayer
import android.net.Uri
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GalleryScreen(
    galleryItems: List<GalleryEntity>,
    themeMode: String,
    musicUri: String?,
    lastMusicPosition: Int,
    musicVolume: Float,
    onSaveMusicUri: (String?) -> Unit,
    onSaveMusicPosition: (Int) -> Unit,
    onSaveMusicVolume: (Float) -> Unit,
    onUploadItem: (String, Int, String, String) -> Unit,
    onDeleteItem: (GalleryEntity) -> Unit,
    onBack: () -> Unit
) {
    val isDark = themeMode == "dark"
    val context = LocalContext.current

    // Observe player states
    val isPlaying by GalleryMusicManager.isPlaying.collectAsState()
    val musicTitle by GalleryMusicManager.musicTitle.collectAsState()
    val volume by GalleryMusicManager.volume.collectAsState()
    val currentPosition by GalleryMusicManager.currentPosition.collectAsState()
    val duration by GalleryMusicManager.duration.collectAsState()

    // Floating music control state
    var isMusicControlExpanded by remember { mutableStateOf(false) }

    // Infinite rotation transition for playing status icon
    val infiniteTransition = rememberInfiniteTransition(label = "music_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )

    // Launcher for MP3 selection
    val pickMusicLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val localPath = GalleryMusicManager.copyUriToInternalStorage(context, uri)
            if (localPath != null) {
                onSaveMusicUri(localPath)
                // Immediately start playing the newly uploaded file!
                GalleryMusicManager.startPlay(context, localPath, 0)
            }
        }
    }

    // Helper to format track times
    fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // Initialize player when entering screen, and release/pause when leaving
    DisposableEffect(musicUri) {
        // Apply saved volume
        GalleryMusicManager.setVolume(musicVolume)
        
        // Start playing
        GalleryMusicManager.startPlay(
            context = context,
            customUriStr = musicUri,
            startPositionMs = lastMusicPosition
        )

        onDispose {
            // Save last position and volume preference before stopping
            val pos = GalleryMusicManager.getCurrentPositionMs()
            onSaveMusicPosition(pos)
            onSaveMusicVolume(GalleryMusicManager.volume.value)
            GalleryMusicManager.stop()
        }
    }

    var activeFilter by remember { mutableStateOf("All") } // "All", "Photos", "Videos", "Albums"
    var activeAlbum by remember { mutableStateOf("All") } // For album sub-filtering
    var searchQuery by remember { mutableStateOf("") }

    var selectedItemForFullView by remember { mutableStateOf<GalleryEntity?>(null) }
    var isUploadDialogOpen by remember { mutableStateOf(false) }

    // Dynamic list of albums represented in existing database
    val existingAlbums = remember(galleryItems) {
        listOf("All") + galleryItems.map { it.album }.distinct().filter { it.isNotEmpty() }
    }

    // Filtered gallery items
    val filteredItems = galleryItems.filter { item ->
        val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) || item.album.contains(searchQuery, ignoreCase = true)
        val matchesType = when (activeFilter) {
            "Photos" -> item.mediaType == "photo"
            "Videos" -> item.mediaType == "video"
            else -> true
        }
        val matchesAlbum = if (activeFilter == "Albums" && activeAlbum != "All") {
            item.album == activeAlbum
        } else {
            true
        }
        matchesSearch && matchesType && matchesAlbum
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(if (isDark) Color(0xFF2C191E) else Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Main Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFFFF4F87))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Shared Gallery",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF4F87)
                            )
                        )
                    }

                    // Add content icon
                    IconButton(onClick = { isUploadDialogOpen = true }) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = "Upload Memory", tint = Color(0xFFFF4F87))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search photos & videos...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFFFF4F87)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF4F87),
                        cursorColor = Color(0xFFFF4F87)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Pinterest/Google Photos Style horizontal scrolling filters (Page 11 design)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("All", "Photos", "Videos", "Albums").forEach { filter ->
                        val isSelected = activeFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFFFF4F87) else Color(0x1AFF4F87))
                                .clickable {
                                    activeFilter = filter
                                    if (filter != "Albums") activeAlbum = "All"
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) Color.White else Color(0xFFFF4F87),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // If Album sub-selector is active, show the sub-albums scrolling bar
                if (activeFilter == "Albums") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        existingAlbums.forEach { album ->
                            val isSelected = activeAlbum == album
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFFFF85A1) else Color(0x1AFF85A1))
                                    .clickable { activeAlbum = album }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = album,
                                    color = if (isSelected) Color.White else Color(0xFFFF85A1),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = if (isDark) Color(0xFF1F1115) else Color(0xFFFFF5F7),
        modifier = Modifier.testTag("gallery_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FloatingHeartsBackground(heartColor = Color(0x1AFF4F87))

            if (filteredItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🖼", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No gallery items found",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF4F87)
                        )
                    )
                }
            } else {
                // Sort and organize by Date headers (Page 11 Google Photos style e.g. "May 2024", "April 2024")
                val groupedByMonth = filteredItems.groupBy { item ->
                    val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    sdf.format(Date(item.timestamp))
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Google Photos date header integration
                    groupedByMonth.forEach { (monthHeader, itemsInMonth) ->
                        // Span date header fully across all 3 grid columns
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                            Text(
                                text = monthHeader,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFFFF4F87),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(start = 6.dp, top = 14.dp, bottom = 6.dp)
                            )
                        }

                        items(itemsInMonth) { item ->
                            GalleryGridItem(
                                item = item,
                                onClick = { selectedItemForFullView = item }
                            )
                        }
                    }
                }
            }

            // FLOATING MUSIC CONTROLLER (VERY HIGH FIDELITY)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 76.dp, end = 16.dp), // raise it slightly above the screen edge
                contentAlignment = Alignment.BottomEnd
            ) {
                AnimatedVisibility(
                    visible = !isMusicControlExpanded,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    FloatingActionButton(
                        onClick = { isMusicControlExpanded = true },
                        containerColor = Color(0xFFFF4F87),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(8.dp, CircleShape)
                            .testTag("music_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = "Music Controls",
                            modifier = Modifier
                                .size(28.dp)
                                .graphicsLayer(rotationZ = if (isPlaying) rotationAngle else 0f)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isMusicControlExpanded,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .width(290.dp)
                            .shadow(12.dp, RoundedCornerShape(16.dp))
                            .testTag("music_controller_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF2D1B22) else Color(0xFFFFF0F3)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.MusicNote,
                                        contentDescription = null,
                                        tint = Color(0xFFFF4F87),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Gallery Background Music",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isDark) Color.White else Color(0xFF4A1521)
                                    )
                                }
                                IconButton(
                                    onClick = { isMusicControlExpanded = false },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Close",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Song Title & Upload Status
                            if (musicTitle != null) {
                                Text(
                                    text = musicTitle ?: "",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFFFF4F87),
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Playback time progress
                                val currentStr = formatTime(currentPosition)
                                val durationStr = formatTime(duration)
                                Text(
                                    text = "$currentStr / $durationStr",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Progress Slider (clickable to seek)
                                var isSeeking by remember { mutableStateOf(false) }
                                var sliderValue by remember { mutableStateOf(0f) }
                                val displayProgress = if (isSeeking) sliderValue else (if (duration > 0) currentPosition.toFloat() / duration else 0f)

                                Slider(
                                    value = displayProgress,
                                    onValueChange = {
                                        isSeeking = true
                                        sliderValue = it
                                    },
                                    onValueChangeFinished = {
                                        isSeeking = false
                                        GalleryMusicManager.seekTo((sliderValue * duration).toInt())
                                    },
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFFFF4F87),
                                        activeTrackColor = Color(0xFFFF4F87),
                                        inactiveTrackColor = Color(0xFFFFD6E0)
                                    ),
                                    modifier = Modifier.height(24.dp).padding(horizontal = 8.dp)
                                )
                            } else {
                                Text(
                                    text = "No Gallery Music Uploaded.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFFFF4F87),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp).testTag("no_music_text")
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Please tap the cloud upload button below to select an MP3 file.",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Controls Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Upload/Pick MP3 button
                                IconButton(
                                    onClick = { pickMusicLauncher.launch("audio/mpeg") },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = Color(0x1AFF4F87)
                                    ),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CloudUpload,
                                        contentDescription = "Upload MP3",
                                        tint = Color(0xFFFF4F87),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Play/Pause button (Only clickable if music is initialized)
                                val playIcon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow
                                IconButton(
                                    onClick = {
                                        if (musicTitle != null) {
                                            if (isPlaying) GalleryMusicManager.pause() else GalleryMusicManager.resume()
                                        }
                                    },
                                    enabled = musicTitle != null,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = if (musicTitle != null) Color(0xFFFF4F87) else Color.LightGray.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = playIcon,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Restart button (Only clickable if music is initialized)
                                IconButton(
                                    onClick = {
                                        if (musicTitle != null) {
                                            GalleryMusicManager.restart()
                                        }
                                    },
                                    enabled = musicTitle != null,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = Color(0x1AFF4F87)
                                    ),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Replay,
                                        contentDescription = "Restart",
                                        tint = Color(0xFFFF4F87),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Volume Controller Slider
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (volume > 0f) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                                    contentDescription = "Volume",
                                    tint = Color(0xFFFF4F87),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Slider(
                                    value = volume,
                                    onValueChange = { GalleryMusicManager.setVolume(it) },
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFFFF4F87),
                                        activeTrackColor = Color(0xFFFF4F87),
                                        inactiveTrackColor = Color(0xFFFFD6E0)
                                    ),
                                    modifier = Modifier.weight(1f).height(24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${(volume * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF4A1521)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Gallery Upload Dialog popup helper
            if (isUploadDialogOpen) {
                var newTitle by remember { mutableStateOf("") }
                var selectedMediaType by remember { mutableStateOf("photo") }
                var selectedAlbumName by remember { mutableStateOf("Anniversary") }

                AlertDialog(
                    onDismissRequest = { isUploadDialogOpen = false },
                    title = { Text("Upload Memory to Gallery", color = Color(0xFFFF4F87), fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = newTitle,
                                onValueChange = { newTitle = it },
                                label = { Text("Memory Title") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = selectedAlbumName,
                                onValueChange = { selectedAlbumName = it },
                                label = { Text("Album/Folder Name") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text("Media Type:")
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = selectedMediaType == "photo", onClick = { selectedMediaType = "photo" })
                                    Text("Photo 📸")
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = selectedMediaType == "video", onClick = { selectedMediaType = "video" })
                                    Text("Video 🎥")
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newTitle.isNotEmpty()) {
                                    // Choose corresponding mockup drawable
                                    val mockupRes = if (selectedMediaType == "video") {
                                        R.drawable.img_couple_park_1783693790541
                                    } else {
                                        R.drawable.img_couple_sunset_1783693769302
                                    }
                                    onUploadItem(newTitle, mockupRes, selectedMediaType, selectedAlbumName)
                                    isUploadDialogOpen = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4F87))
                        ) {
                            Text("Upload")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isUploadDialogOpen = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }

            // High Fidelity zoomable display modal for Gallery Item
            selectedItemForFullView?.let { item ->
                AlertDialog(
                    onDismissRequest = { selectedItemForFullView = null },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                    confirmButton = {},
                    dismissButton = {},
                    containerColor = Color.Black.copy(alpha = 0.95f),
                    modifier = Modifier.fillMaxSize(),
                    text = {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (item.mediaType == "video") Icons.Filled.Movie else Icons.Filled.Image,
                                        contentDescription = null,
                                        tint = Color(0xFFFF4F87)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }

                                Row {
                                    IconButton(onClick = { /* Simulated download */ }) {
                                        Icon(Icons.Filled.Download, contentDescription = "Download", tint = Color.White)
                                    }
                                    IconButton(onClick = {
                                        onDeleteItem(item)
                                        selectedItemForFullView = null
                                    }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                    IconButton(onClick = { selectedItemForFullView = null }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                                    }
                                }
                            }

                            // Memory image / video thumbnail
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = item.mediaUri ?: item.mediaRes,
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                                    contentScale = ContentScale.Fit
                                )

                                if (item.mediaType == "video") {
                                    // Play icon overlay
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .background(Color(0x99000000), CircleShape)
                                            .border(2.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = "Play Video",
                                            tint = Color.White,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                }
                            }

                            // Footer details card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1115)),
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "Album: ${item.album}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color(0xFFFF4F87),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val sdf = SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.getDefault())
                                    Text(
                                        text = "Synced: " + sdf.format(Date(item.timestamp)),
                                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun GalleryGridItem(
    item: GalleryEntity,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = item.mediaUri ?: item.mediaRes,
            contentDescription = item.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Album overlay banner at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xDD000000))
                    )
                )
                .padding(6.dp)
        ) {
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }

        // Media Type Icon overlay
        Box(
            modifier = Modifier
                .background(Color(0x99000000), CircleShape)
                .padding(4.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 4.dp)
        ) {
            Icon(
                imageVector = if (item.mediaType == "video") Icons.Filled.Videocam else Icons.Filled.PhotoCamera,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
