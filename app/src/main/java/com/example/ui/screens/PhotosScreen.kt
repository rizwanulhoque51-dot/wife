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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.PhotoEntity
import com.example.ui.components.FloatingHeartsBackground
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PhotosScreen(
    photos: List<PhotoEntity>,
    activeUser: String,
    themeMode: String,
    onToggleFavourite: (PhotoEntity) -> Unit,
    onDeletePhoto: (PhotoEntity) -> Unit,
    onNavigateToAddPhoto: () -> Unit,
    onBack: () -> Unit
) {
    val isDark = themeMode == "dark"
    var activeTab by remember { mutableStateOf("All Photos") } // "All Photos" or "Favourites"
    var searchQuery by remember { mutableStateOf("") }
    var selectedPhotoForFullView by remember { mutableStateOf<PhotoEntity?>(null) }

    // Date sorted photos
    val sortedPhotos = photos.sortedByDescending { it.timestamp }

    // Search & Tab filtered photos
    val filteredPhotos = sortedPhotos.filter { photo ->
        val matchesSearch = photo.caption.contains(searchQuery, ignoreCase = true) || photo.uploadedBy.contains(searchQuery, ignoreCase = true)
        val matchesTab = if (activeTab == "Favourites") photo.isFavourite else true
        matchesSearch && matchesTab
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(if (isDark) Color(0xFF2C191E) else Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Header with back & search
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
                            text = "Photos",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF4F87)
                            )
                        )
                    }

                    // Add Photo (+) button at top right like Page 8
                    IconButton(onClick = onNavigateToAddPhoto) {
                        Icon(Icons.Filled.Add, contentDescription = "Upload Photo", tint = Color(0xFFFF4F87), modifier = Modifier.size(28.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search photos...") },
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

                // Tab selectors (All Photos vs Albums/Favourites) (Page 8 style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("All Photos", "Favourites").forEach { tab ->
                        val isSelected = activeTab == tab
                        Button(
                            onClick = { activeTab = tab },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFFFF4F87) else Color(0x1AFF4F87)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = tab,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isSelected) Color.White else Color(0xFFFF4F87),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        },
        containerColor = if (isDark) Color(0xFF1F1115) else Color(0xFFFFF5F7),
        modifier = Modifier.testTag("photos_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FloatingHeartsBackground(heartColor = Color(0x1AFF4F87))

            if (filteredPhotos.isEmpty()) {
                // Friendly Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("💝", fontSize = 60.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No matching moments found" else "No photos uploaded yet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF4F87)
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the '+' button to capture and share your first beautiful memory!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // 3-Column Instagram Style grid (Page 8 style)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredPhotos) { photo ->
                        PhotoGridItem(
                            photo = photo,
                            onClick = { selectedPhotoForFullView = photo }
                        )
                    }
                }
            }

            // Fullscreen View Modal Dialog (Page 8 features: Zoom, download, fav, delete)
            selectedPhotoForFullView?.let { photo ->
                FullscreenPhotoDialog(
                    photo = photo,
                    activeUser = activeUser,
                    onDismiss = { selectedPhotoForFullView = null },
                    onToggleFav = {
                        onToggleFavourite(photo)
                        // Live update in-dialog state
                        selectedPhotoForFullView = photo.copy(isFavourite = !photo.isFavourite)
                    },
                    onDelete = {
                        onDeletePhoto(photo)
                        selectedPhotoForFullView = null
                    }
                )
            }
        }
    }
}

@Composable
fun PhotoGridItem(
    photo: PhotoEntity,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = photo.imageUri ?: photo.imageRes,
            contentDescription = photo.caption,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // HD flag (Page 8 / Page 9 HD option)
        if (photo.isHD) {
            Box(
                modifier = Modifier
                    .background(Color(0x99000000), RoundedCornerShape(bottomEnd = 8.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = "HD",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Favourites indicator (heart)
        if (photo.isFavourite) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Favourite",
                tint = Color(0xFFFF4F87),
                modifier = Modifier
                    .size(20.dp)
                    .padding(2.dp)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun FullscreenPhotoDialog(
    photo: PhotoEntity,
    activeUser: String,
    onDismiss: () -> Unit,
    onToggleFav: () -> Unit,
    onDelete: () -> Unit
) {
    var isZoomed by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {},
        dismissButton = {},
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black.copy(alpha = 0.95f),
        text = {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top control bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                    }

                    Row {
                        IconButton(onClick = onToggleFav) {
                            Icon(
                                imageVector = if (photo.isFavourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favourite",
                                tint = Color(0xFFFF4F87)
                            )
                        }

                        // Simulated download
                        IconButton(onClick = { /* Simulated download */ }) {
                            Icon(Icons.Filled.Download, contentDescription = "Download", tint = Color.White)
                        }

                        IconButton(onClick = { /* Simulated share */ }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.White)
                        }

                        // Allow uploader to delete
                        if (photo.uploadedBy == activeUser) {
                            IconButton(onClick = onDelete) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }

                // Main Image Container (Zoom toggle on click)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { isZoomed = !isZoomed },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = photo.imageUri ?: photo.imageRes,
                        contentDescription = photo.caption,
                        modifier = if (isZoomed) Modifier.fillMaxSize() else Modifier.fillMaxWidth().aspectRatio(1f),
                        contentScale = ContentScale.Fit
                    )

                    if (isZoomed) {
                        Box(
                            modifier = Modifier
                                .background(Color(0x77000000), CircleShape)
                                .padding(8.dp)
                                .align(Alignment.BottomCenter)
                                .offset(y = (-20).dp)
                        ) {
                            Text("Zoomed In (Tap to Zoom Out)", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                // Bottom details card (Caption, Uploader name, Timestamp)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1115)),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Uploaded by: ${photo.uploadedBy}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFFFF4F87),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                            Text(
                                text = sdf.format(Date(photo.timestamp)),
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = photo.caption.ifEmpty { "No caption provided ❤️" },
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    )
}
