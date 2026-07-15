package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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

@Composable
fun CallScreen(
    activeUser: String,
    callState: String, // "ringing", "connected"
    callType: String, // "voice", "video"
    caller: String,
    callee: String,
    isMuted: Boolean,
    isCameraEnabled: Boolean,
    isSpeakerOn: Boolean,
    isFrontCamera: Boolean,
    callDuration: Int,
    coupleProfileUri: String?,
    onImageSelected: (String) -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onEnd: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleFrontCamera: () -> Unit
) {
    val isOutgoing = activeUser == caller
    val isIncoming = activeUser == callee

    // Pulser animation for ringing states
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Formatted time: mm:ss
    val minutes = callDuration / 60
    val seconds = callDuration % 60
    val durationText = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2C191E),
                        Color(0xFF150A0C)
                    )
                )
            )
            .testTag("call_screen")
    ) {
        // Soft animated background
        FloatingHeartsBackground(heartColor = Color(0x1AFF4F87))

        // Call Mode Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Section (Encryption Badge & Title)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x22FFFFFF))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Encrypted",
                        tint = Color(0xFFFF4F87),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "End-to-End Encrypted Private Call",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xCCFFFFFF),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Call type descriptor
                Text(
                    text = if (callState == "ringing") {
                        if (isOutgoing) "Calling..." else "Incoming Call..."
                    } else {
                        "Connected • HD ${if (callType == "video") "Video" else "Voice"}"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFFFF8FA3),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Partner's Name
                val partnerName = if (activeUser == "Husband") "My Wife ❤️" else "My Husband 👨"
                Text(
                    text = partnerName,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                if (callState == "connected") {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Call timer
                    Text(
                        text = durationText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 2. Center Content (Avatar or Video Preview)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (callState == "connected" && callType == "video") {
                    // Connected Video Call UI (Full screen simulated remote camera with local camera preview pip)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(32.dp))
                            .border(2.dp, Color(0x33FF4F87), RoundedCornerShape(32.dp))
                    ) {
                        if (isCameraEnabled) {
                            // Simulated remote partner video (their beautiful profile with soft zoom effect)
                            val cameraZoomTransition = rememberInfiniteTransition(label = "zoom")
                            val cameraZoomScale by cameraZoomTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.08f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(4000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "zoom_scale"
                            )

                            if (coupleProfileUri != null) {
                                coil.compose.AsyncImage(
                                    model = coupleProfileUri,
                                    contentDescription = "Remote Video",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(
                                            scaleX = cameraZoomScale,
                                            scaleY = cameraZoomScale
                                        )
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF2C191E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SharedProfileImage(
                                        imageUri = null,
                                        onImageSelected = onImageSelected,
                                        size = 130.dp
                                    )
                                }
                            }

                            // Glassmorphism overlay info on partner video
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x66000000))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (activeUser == "Husband") "Ayesha (Live)" else "Rizwan (Live)",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            // Camera disabled state
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF150A0C)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Filled.VideocamOff,
                                        contentDescription = "Camera Off",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Camera is disabled",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Local User Video PIP Card (Top right corner, elegant rounded picture-in-picture)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(width = 90.dp, height = 130.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF2C191E))
                        ) {
                            // Show active user's avatar
                            if (coupleProfileUri != null) {
                                coil.compose.AsyncImage(
                                    model = coupleProfileUri,
                                    contentDescription = "Local Video",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF1F1115)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "User Placeholder",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Local label
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color(0x99000000))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("You", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Ringing or Voice Call (Shows Pulsating Profile/Logo Container)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Pulsating background circles (Only during Ringing)
                        if (callState == "ringing") {
                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .scale(pulseScale)
                                    .graphicsLayer { alpha = pulseAlpha }
                                    .background(Color(0x33FF4F87), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(190.dp)
                                    .scale(pulseScale * 0.85f)
                                    .graphicsLayer { alpha = pulseAlpha * 1.2f }
                                    .background(Color(0x22FF4F87), CircleShape)
                            )
                        }

                        SharedProfileImage(
                            imageUri = coupleProfileUri,
                            onImageSelected = onImageSelected,
                            size = 150.dp,
                            borderSize = 1.5.dp,
                            paddingSize = 3.5.dp,
                            innerBorderSize = 2.dp
                        )
                    }
                }
            }

            // 3. Bottom Controls Panel
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (callState == "connected") {
                    // Connected Controls Row (Mute, Speaker, Camera toggles, Front Camera swap, End Call)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0x19FFFFFF))
                            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        // 1. Mute
                        IconButton(
                            onClick = onToggleMute,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .background(
                                    if (isMuted) Color(0xFFFF4F87) else Color(0x33FFFFFF),
                                    RoundedCornerShape(16.dp)
                                )
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                                contentDescription = "Mute",
                                tint = Color.White
                            )
                        }

                        // 2. Speaker
                        IconButton(
                            onClick = onToggleSpeaker,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .background(
                                    if (isSpeakerOn) Color(0xFFFF4F87) else Color(0x33FFFFFF),
                                    RoundedCornerShape(16.dp)
                                )
                        ) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeDown,
                                contentDescription = "Speaker Mode",
                                tint = Color.White
                            )
                        }

                        if (callType == "video") {
                            // 3. Camera On/Off
                            IconButton(
                                onClick = onToggleCamera,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .background(
                                        if (isCameraEnabled) Color(0x33FFFFFF) else Color(0xFFFF4F87),
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isCameraEnabled) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                                    contentDescription = "Toggle Camera",
                                    tint = Color.White
                                )
                            }

                            // 4. Camera Switch (Front/Back)
                            IconButton(
                                onClick = onToggleFrontCamera,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .background(Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FlipCameraAndroid,
                                    contentDescription = "Switch Camera",
                                    tint = Color.White
                                )
                            }
                        }

                        // 5. End Call FAB
                        IconButton(
                            onClick = onEnd,
                            modifier = Modifier
                                .size(54.dp)
                                .background(Color.Red, CircleShape)
                                .shadow(4.dp, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CallEnd,
                                contentDescription = "End Call",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                } else if (callState == "ringing") {
                    // Ringing Controls Layout (Depends on Outgoing vs Incoming)
                    if (isOutgoing) {
                        // Outgoing: Single End Call FAB
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = onEnd,
                                modifier = Modifier
                                    .size(76.dp)
                                    .background(Color.Red, CircleShape)
                                    .shadow(8.dp, CircleShape)
                                    .testTag("decline_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CallEnd,
                                    contentDescription = "Cancel Call",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Cancel Call",
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        // Incoming: Dual Decline and Accept FABs
                        Row(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Decline Button
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = onDecline,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .background(Color.Red, CircleShape)
                                        .shadow(8.dp, CircleShape)
                                        .testTag("decline_incoming_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CallEnd,
                                        contentDescription = "Decline Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Decline",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            // Pulse simulated HD Voice/Video Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x33FFFFFF))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (callType == "video") "🎥 HD Video" else "📞 HD Voice",
                                    color = Color(0xFFFF8FA3),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Accept Button
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = onAccept,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .background(Color(0xFF4CAF50), CircleShape)
                                        .shadow(8.dp, CircleShape)
                                        .testTag("accept_call_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Call,
                                        contentDescription = "Accept Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Accept",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
