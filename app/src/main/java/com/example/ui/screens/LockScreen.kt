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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
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
import com.example.ui.components.FloatingHeartsBackground
import com.example.ui.components.SharedProfileImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LockScreen(
    title: String,
    correctPin: String,
    isDark: Boolean,
    coupleProfileUri: String?,
    onImageSelected: (String) -> Unit,
    onUnlockSuccess: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    var pinValue by remember { mutableStateOf("") }
    var isWrongPin by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Shake Offset animation for wrong PIN
    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        coroutineScope.launch {
            isWrongPin = true
            // Shake back and forth
            repeat(4) {
                shakeOffset.animateTo(25f, tween(50, easing = LinearEasing))
                shakeOffset.animateTo(-25f, tween(50, easing = LinearEasing))
            }
            shakeOffset.animateTo(0f, tween(50, easing = LinearEasing))
            pinValue = ""
            isWrongPin = false
        }
    }

    fun onKeyClick(digit: String) {
        if (pinValue.length < 6) {
            pinValue += digit
        }
    }

    fun onDeleteClick() {
        if (pinValue.isNotEmpty()) {
            pinValue = pinValue.dropLast(1)
        }
    }

    // Auto-validate pin when length reaches 6 digits
    LaunchedEffect(pinValue) {
        if (pinValue.length == 6) {
            delay(150) // Small delay so the user sees the last bullet filled
            if (pinValue == correctPin) {
                onUnlockSuccess()
            } else {
                triggerShake()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(Color(0xFF2C191E), Color(0xFF150A0C))
                    } else {
                        listOf(Color.White, Color(0xFFFFF0F3))
                    }
                )
            )
            .testTag("lock_screen_${title.lowercase().replace(" ", "_")}")
    ) {
        FloatingHeartsBackground(heartColor = Color(0x22FF4F87))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            // Header with optional back
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.background(Color(0x1AFF4F87), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFF4F87)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Text(
                    text = "Security Lock",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFFFF4F87),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            SharedProfileImage(
                imageUri = coupleProfileUri,
                onImageSelected = onImageSelected,
                size = 120.dp,
                borderSize = 1.dp,
                paddingSize = 2.5.dp,
                innerBorderSize = 1.5.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title "Enter App Password", "Enter Messages Password" etc.
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isWrongPin) Color.Red else Color(0xFFFF4F87),
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(x = shakeOffset.value.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bullet Indicators (6 dots)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .offset(x = shakeOffset.value.dp)
            ) {
                for (i in 0 until 6) {
                    val isFilled = i < pinValue.length
                    val color = if (isWrongPin) Color.Red else if (isFilled) Color(0xFFFF4F87) else Color(0xFFD4C5C7)
                    val size = if (isFilled) 16.dp else 12.dp

                    Box(
                        modifier = Modifier
                            .size(size)
                            .shadow(if (isFilled) 4.dp else 0.dp, CircleShape)
                            .background(color, CircleShape)
                            .border(1.5.dp, if (isFilled) Color.Transparent else Color(0x66FF4F87), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Keypad (3x4 grid)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                // Rows of keypad
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )

                keys.forEach { rowKeys ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowKeys.forEach { key ->
                            KeypadButton(
                                text = key,
                                onClick = { onKeyClick(key) },
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )
                        }
                    }
                }

                // Bottom Row (Fingerprint, 0, Backspace)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Simulated Fingerprint unlock
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .clip(CircleShape)
                            .clickable {
                                coroutineScope.launch {
                                    // Fingerprint verified simulation
                                    pinValue = correctPin
                                }
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = "Fingerprint Unlock",
                            tint = Color(0xFFFF4F87),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Key 0
                    KeypadButton(
                        text = "0",
                        onClick = { onKeyClick("0") },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )

                    // Backspace delete
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .clip(CircleShape)
                            .clickable { onDeleteClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Backspace,
                            contentDescription = "Backspace",
                            tint = Color(0xFFFF4F87),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1.2f)
            .shadow(4.dp, CircleShape)
            .background(
                if (isDark) Color(0xFF2C191E) else Color.White,
                CircleShape
            )
            .border(1.dp, Color(0x33FF4F87), CircleShape)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF4F87),
                fontSize = 26.sp
            )
        )
    }
}
