package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.FloatingHeartsBackground
import com.example.ui.components.SharedProfileImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    themeMode: String,
    coupleProfileUri: String?,
    onImageSelected: (String) -> Unit,
    onToggleTheme: () -> Unit,
    onChangeAppPin: (String) -> Unit,
    onChangeMsgPin: (String) -> Unit,
    onChangePhotoPin: (String) -> Unit,
    onChangeGalleryPin: (String) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val isDark = themeMode == "dark"
    val coroutineScope = rememberCoroutineScope()

    // Dialog state
    var isPinDialogOpen by remember { mutableStateOf(false) }
    var pinDialogType by remember { mutableStateOf("") } // "App", "Messages", "Photos", "Gallery"
    var newPinText by remember { mutableStateOf("") }

    // Backup state
    var isBackupRunning by remember { mutableStateOf(false) }
    var backupProgress by remember { mutableFloatStateOf(0f) }

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
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF4F87)
                    )
                )
            }
        },
        containerColor = if (isDark) Color(0xFF1F1115) else Color(0xFFFFF5F7),
        modifier = Modifier.testTag("settings_screen")
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
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SharedProfileImage(
                    imageUri = coupleProfileUri,
                    onImageSelected = onImageSelected,
                    size = 100.dp,
                    borderSize = 1.dp,
                    paddingSize = 2.dp,
                    innerBorderSize = 1.5.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // General Settings List (Exactly like Page 12 reference cards)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF2C191E) else Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0x1AFF4F87)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Change App PIN Card Button
                        SettingsRowItem(
                            title = "Change App Password",
                            icon = Icons.Outlined.LockClock,
                            onClick = {
                                pinDialogType = "App"
                                newPinText = ""
                                isPinDialogOpen = true
                            }
                        )

                        Divider(color = Color(0x1AFF4F87), modifier = Modifier.padding(horizontal = 12.dp))

                        SettingsRowItem(
                            title = "Change Messages Password",
                            icon = Icons.Outlined.Lock,
                            onClick = {
                                pinDialogType = "Messages"
                                newPinText = ""
                                isPinDialogOpen = true
                            }
                        )

                        Divider(color = Color(0x1AFF4F87), modifier = Modifier.padding(horizontal = 12.dp))

                        SettingsRowItem(
                            title = "Change Photos Password",
                            icon = Icons.Outlined.PhotoCameraBack,
                            onClick = {
                                pinDialogType = "Photos"
                                newPinText = ""
                                isPinDialogOpen = true
                            }
                        )

                        Divider(color = Color(0x1AFF4F87), modifier = Modifier.padding(horizontal = 12.dp))

                        SettingsRowItem(
                            title = "Change Gallery Password",
                            icon = Icons.Outlined.Collections,
                            onClick = {
                                pinDialogType = "Gallery"
                                newPinText = ""
                                isPinDialogOpen = true
                            }
                        )

                        Divider(color = Color(0x1AFF4F87), modifier = Modifier.padding(horizontal = 12.dp))

                        // Theme switch item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleTheme() }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0x1AFF4F87), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isDark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                        contentDescription = null,
                                        tint = Color(0xFFFF4F87),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "Dark Mode Theme",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Switch(
                                checked = isDark,
                                onCheckedChange = { onToggleTheme() },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF4F87))
                            )
                        }

                        Divider(color = Color(0x1AFF4F87), modifier = Modifier.padding(horizontal = 12.dp))

                        // Backup and Restore indicator
                        SettingsRowItem(
                            title = "Backup & Restore Memories",
                            icon = Icons.Outlined.CloudSync,
                            onClick = {
                                coroutineScope.launch {
                                    isBackupRunning = true
                                    backupProgress = 0.1f
                                    delay(400)
                                    backupProgress = 0.4f
                                    delay(500)
                                    backupProgress = 0.85f
                                    delay(600)
                                    backupProgress = 1.0f
                                    delay(400)
                                    isBackupRunning = false
                                }
                            }
                        )

                        Divider(color = Color(0x1AFF4F87), modifier = Modifier.padding(horizontal = 12.dp))

                        // Privacy policy
                        SettingsRowItem(
                            title = "Privacy Policy",
                            icon = Icons.Outlined.PrivacyTip,
                            onClick = { /* Privacy policy click simulation */ }
                        )

                        Divider(color = Color(0x1AFF4F87), modifier = Modifier.padding(horizontal = 12.dp))

                        // Logout
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onLogout)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0x1AFF0000), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Logout,
                                        contentDescription = null,
                                        tint = Color.Red,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "Logout",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Red
                                    )
                                )
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.LightGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Backup progress display if active
                if (isBackupRunning) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE5EC)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Backing up memories safely...",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF4F87)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { backupProgress },
                                color = Color(0xFFFF4F87),
                                trackColor = Color(0xFFFFD6E0),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Page 12 footer couple visual
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFFF4F87),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Only Us 💕\nForever 💕",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF4F87),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Version 1.0.0 • Secure M.Rizwan-Privacy",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }

            // PIN change dialog
            if (isPinDialogOpen) {
                AlertDialog(
                    onDismissRequest = { isPinDialogOpen = false },
                    title = { Text("Change $pinDialogType Password", color = Color(0xFFFF4F87), fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Enter a new 6-digit numeric password for $pinDialogType Lock:")
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = newPinText,
                                onValueChange = { if (it.length <= 6) newPinText = it },
                                label = { Text("6-Digit PIN") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF4F87),
                                    cursorColor = Color(0xFFFF4F87)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newPinText.length == 6) {
                                    when (pinDialogType) {
                                        "App" -> onChangeAppPin(newPinText)
                                        "Messages" -> onChangeMsgPin(newPinText)
                                        "Photos" -> onChangePhotoPin(newPinText)
                                        "Gallery" -> onChangeGalleryPin(newPinText)
                                    }
                                    isPinDialogOpen = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4F87))
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isPinDialogOpen = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsRowItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0x1AFF4F87), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFFF4F87),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}
