package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.ui.components.FloatingHeartsBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(3000) // 3 seconds splash
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFFFF0F3), // Soft pink hint
                        Color(0xFFFFD6E0)  // Warm light pink base
                    )
                )
            )
            .testTag("splash_screen")
    ) {
        // Heart animation background
        FloatingHeartsBackground(heartColor = Color(0xFFFF4F87))

        // Glass overlay or wave design (Page 1 reference bottom curve)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.2f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x33FF4F87),
                            Color(0x66FF4F87)
                        )
                    )
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(1200)) + scaleIn(animationSpec = tween(1200)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // Official App Logo framed elegantly
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .shadow(16.dp, CircleShape)
                        .border(6.dp, Color.White, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_rm_logo),
                        contentDescription = "Official RM Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // App Logo text
                Text(
                    text = "Only Us",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF4F87),
                        letterSpacing = (-1).sp
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "M.Rizwan-Privacy",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF5A4449),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Subtitle
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x33FF4F87))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Private • Secure • Only Us ❤️",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFF4F87)
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "A private space\njust for you and me",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF8A7175),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
