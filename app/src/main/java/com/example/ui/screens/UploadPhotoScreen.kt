package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.FloatingHeartsBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UploadPhotoScreen(
    themeMode: String,
    onUploadSuccess: (String, String?, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val isDark = themeMode == "dark"
    val coroutineScope = rememberCoroutineScope()

    var captionText by remember { mutableStateOf("") }
    var isHdEnabled by remember { mutableStateOf(true) }

    // Dynamic picked photo URI
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it.toString()
        }
    }

    // Upload progress state
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatusMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF2C191E) else Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFFFF4F87))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Photo",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF4F87)
                    )
                )
            }
        },
        containerColor = if (isDark) Color(0xFF1F1115) else Color(0xFFFFF5F7),
        modifier = Modifier.testTag("upload_photo_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FloatingHeartsBackground(heartColor = Color(0x1AFF4F87))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Image Preview or Dynamic Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .shadow(12.dp, RoundedCornerShape(20.dp))
                        .border(
                            2.dp,
                            if (selectedImageUri == null) Color(0xFFFF8FA3) else Color.White,
                            RoundedCornerShape(20.dp)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selectedImageUri == null) Color(0xFFFFF0F2) else Color.White)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Upload Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // "Change Photo" banner overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(Color(0x99000000))
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Selected Photo - Tap to Change",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Add Icon",
                                tint = Color(0xFFFF4F87),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tap to Select Photo\nfrom Your Device",
                                color = Color(0xFFFF4F87),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Only your exact couple photos will be added to your shared gallery.",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Caption Box
                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    placeholder = { Text("Write a romantic caption for this photo...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF4F87),
                        cursorColor = Color(0xFFFF4F87)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("caption_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // HD Upload Option Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0x1AFF4F87) else Color.White)
                        .border(1.dp, Color(0x33FF4F87), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Hd, contentDescription = "HD option", tint = Color(0xFFFF4F87))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "HD Upload",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF2C191E)
                                )
                            )
                            Text(
                                text = "Preserve maximum photo resolution",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }
                    }
                    Switch(
                        checked = isHdEnabled,
                        onCheckedChange = { isHdEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF4F87))
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Upload Progress Bar Simulation
                if (isUploading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { uploadProgress },
                            color = Color(0xFFFF4F87),
                            trackColor = Color(0xFFFFD6E0),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uploadStatusMessage,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF4F87)
                            )
                        )
                    }
                } else {
                    // Upload Button
                    Button(
                        onClick = {
                            if (selectedImageUri == null) {
                                launcher.launch("image/*")
                                return@Button
                            }
                            coroutineScope.launch {
                                isUploading = true
                                // Step 1: Compress photo
                                uploadStatusMessage = "Compressing photo (HD)..."
                                uploadProgress = 0.15f
                                delay(800)

                                // Step 2: Establish cloud sync
                                uploadStatusMessage = "Connecting to Firebase Storage..."
                                uploadProgress = 0.45f
                                delay(600)

                                // Step 3: Secure encryption upload
                                uploadStatusMessage = "Uploading end-to-end encrypted packet..."
                                uploadProgress = 0.85f
                                delay(1000)

                                // Step 4: Finished
                                uploadStatusMessage = "Upload Successful! Syncing with partner."
                                uploadProgress = 1.0f
                                delay(500)

                                onUploadSuccess(captionText, selectedImageUri, isHdEnabled)
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4F87)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("upload_submit_button")
                    ) {
                        Text(
                            text = if (selectedImageUri == null) "Select Photo first" else "Upload Photo to Shared Space",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
