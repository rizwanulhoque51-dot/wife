package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun DashboardScreen(
    activeUser: String,
    themeMode: String,
    coupleProfileUri: String?,
    onImageSelected: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onToggleUser: () -> Unit,
    onLogout: () -> Unit
) {
    val isDark = themeMode == "dark"

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = if (isDark) Color(0xFF2C191E) else Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Home is selected */ },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home", tint = Color(0xFFFF4F87)) },
                    label = { Text("Home", color = Color(0xFFFF4F87), fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0x33FF4F87)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate("messages_lock") },
                    icon = { Icon(Icons.Outlined.ChatBubble, contentDescription = "Messages") },
                    label = { Text("Messages") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate("photos_lock") },
                    icon = { Icon(Icons.Outlined.PhotoLibrary, contentDescription = "Gallery") },
                    label = { Text("Gallery") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate("settings") },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
        containerColor = if (isDark) Color(0xFF1F1115) else Color(0xFFFFF5F7),
        modifier = Modifier.testTag("dashboard_screen")
    ) { innerPadding ->
        var isVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            isVisible = true
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(1000, easing = FastOutSlowInEasing)) +
                    slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(1000, easing = FastOutSlowInEasing)
                    )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
            FloatingHeartsBackground(heartColor = Color(0x1AFF4F87))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Dashboard Header (Top bar with notifications, menu, profile switcher)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = Color(0xFFFF4F87))
                        }

                        // Premium Title
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color(0xFFFF4F87),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )

                        // Notification Icon (Page 4 top right)
                        Box {
                            IconButton(onClick = { /* Alert notification demo */ }) {
                                Icon(Icons.Filled.NotificationsActive, contentDescription = "Notifications", tint = Color(0xFFFF4F87))
                            }
                            // Dot indicator
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.Red, CircleShape)
                                    .border(1.5.dp, Color.White, CircleShape)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SharedProfileImage(
                        imageUri = coupleProfileUri,
                        onImageSelected = onImageSelected,
                        size = 140.dp,
                        borderSize = 1.dp,
                        paddingSize = 3.dp,
                        innerBorderSize = 1.5.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // "Welcome ❤️" & "Only Us" Branding
                    Text(
                        text = "Welcome ❤️",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = if (isDark) Color.White else Color(0xFF5A4449),
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Only Us header exactly like page 4
                    Text(
                        text = "❤ Only Us ❤",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF4F87),
                            letterSpacing = (-0.5).sp
                        )
                    )

                    Text(
                        text = "You & Me Forever 💕",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFFF8FA3),
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Profile quick-toggle panel for local sync simulation
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFE5EC)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { onToggleUser() }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFFF4F87), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (activeUser == "Husband") "👨" else "👩",
                                        fontSize = 20.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Viewing as: $activeUser",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color(0xFF2C191E),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Tap to switch to your partner's view",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF8A7175)
                                        )
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = "Switch profile",
                                tint = Color(0xFFFF4F87)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Grid/List of Dashboard Action Cards (Page 4 Style)
                item {
                    DashboardCard(
                        title = "Messages",
                        subtitle = "Chat privately and instantly with your love",
                        icon = Icons.Filled.Chat,
                        color = Color(0xFFFF4F87),
                        onClick = { onNavigate("messages_lock") },
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DashboardCard(
                        title = "Photos",
                        subtitle = "Share and rate your moments",
                        icon = Icons.Filled.Favorite,
                        color = Color(0xFFFF5D8F),
                        onClick = { onNavigate("photos_lock") },
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DashboardCard(
                        title = "Gallery",
                        subtitle = "Explore structured memories & albums",
                        icon = Icons.Filled.Collections,
                        color = Color(0xFFFF85A1),
                        onClick = { onNavigate("gallery_lock") },
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DashboardCard(
                        title = "Private Diary",
                        subtitle = "Write your romantic notes and daily log",
                        icon = Icons.Filled.Book,
                        color = Color(0xFFFFA5AB),
                        onClick = { onNavigate("diary") },
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DashboardCard(
                        title = "Settings",
                        subtitle = "Configure passwords, wallpapers & themes",
                        icon = Icons.Filled.Settings,
                        color = Color(0xFFFF4F87),
                        onClick = { onNavigate("settings") },
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
}

@Composable
fun DashboardCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    isDark: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0x1AFF4F87) else Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0x1AFF4F87)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rounded pink icon backing
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (isDark) Color.White else Color(0xFF2C191E),
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isDark) Color(0xFFC7B1B5) else Color(0xFF7A6568)
                    )
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Open",
                tint = Color(0xFFFF4F87)
            )
        }
    }
}
