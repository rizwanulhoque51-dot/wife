package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun SharedProfileImage(
    imageUri: String?,
    onImageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    borderSize: Dp = 1.dp,
    paddingSize: Dp = 3.dp,
    innerBorderSize: Dp = 1.5.dp
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onImageSelected(it.toString())
        }
    }

    var isImageLoaded by remember { mutableStateOf(false) }
    val imageAlpha by animateFloatAsState(
        targetValue = if (isImageLoaded) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "profile_image_fade"
    )

    // Reset load state if image URI changes
    LaunchedEffect(imageUri) {
        isImageLoaded = false
    }

    val backgroundModifier = if (imageUri == null) {
        Modifier.background(
            Brush.verticalGradient(
                colors = listOf(Color(0xFFFFF0F2), Color(0xFFFFD6E0))
            )
        )
    } else {
        Modifier.background(Color.White)
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(6.dp, CircleShape)
            .border(borderSize, Color(0xFFFF8FA3), CircleShape) // thin soft pink outer border
            .padding(paddingSize)
            .border(innerBorderSize, Color.White, CircleShape) // thin white inner border
            .clip(CircleShape)
            .then(backgroundModifier)
            .clickable { launcher.launch("image/*") }
            .testTag("shared_profile_image"),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Our Couple Profile",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(imageAlpha),
                contentScale = ContentScale.Crop,
                onSuccess = {
                    isImageLoaded = true
                },
                onError = {
                    isImageLoaded = false
                }
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Upload Photo icon",
                    tint = Color(0xFFFF4F87),
                    modifier = Modifier.size((size.value * 0.26f).dp)
                )
                Spacer(modifier = Modifier.height((size.value * 0.04f).dp))
                Text(
                    text = "Upload\nYour Photo",
                    color = Color(0xFFFF4F87),
                    fontSize = (size.value * 0.08f).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = (size.value * 0.095f).sp
                )
            }
        }
    }
}
