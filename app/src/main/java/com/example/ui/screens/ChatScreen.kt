package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.data.db.MessageEntity
import com.example.ui.components.SharedProfileImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    messages: List<MessageEntity>,
    activeUser: String,
    themeMode: String,
    isTyping: Boolean,
    chatWallpaper: String,
    coupleProfileUri: String?,
    onImageSelected: (String) -> Unit,
    onSendMessage: (String, MessageEntity?, String, String?) -> Unit,
    onDeleteMessageForEveryone: (MessageEntity) -> Unit,
    onDeleteMessageForMe: (MessageEntity) -> Unit,
    onAddReaction: (MessageEntity, String) -> Unit,
    onSetTyping: (Boolean) -> Unit,
    onStartCall: (String) -> Unit,
    onBack: () -> Unit
) {
    val isDark = themeMode == "dark"
    var inputText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<MessageEntity?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Message selection state for reactions popup and options
    var selectedMessageForOptions by remember { mutableStateOf<MessageEntity?>(null) }

    // Scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Auto partner replies for interactive demo
    var partnerTypingText by remember { mutableStateOf("") }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            val lastMsg = messages.last()
            if (lastMsg.sender == activeUser) {
                // Partner (not activeUser) is typing...
                onSetTyping(true)
                partnerTypingText = if (activeUser == "Husband") "Ayesha is typing..." else "Rizwan is typing..."
                delay(2000)
                onSetTyping(false)

                // Automatic cute simulation reply
                val loveReplies = listOf(
                    "You make me smile so much! 😊",
                    "Aww love, that is so sweet! ❤️",
                    "I am counting the minutes until I see you! ⏳",
                    "Love you to the moon and back! 🌙",
                    "Sending you a warm hug and a thousand kisses! 😘",
                    "You are my absolute favorite person! 💕"
                )
                val randomReply = loveReplies.random()
                val partnerName = if (activeUser == "Husband") "Wife" else "Husband"

                onSendMessage(randomReply, null, "text", null)
            }
        }
    }

    // Filter messages if search is active
    val displayMessages = if (isSearchActive && searchQuery.isNotEmpty()) {
        messages.filter { it.content.contains(searchQuery, ignoreCase = true) }
    } else {
        messages
    }

    Scaffold(
        topBar = {
            Column {
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

                    // Partner's Avatar (Page 6 Style)
                    SharedProfileImage(
                        imageUri = coupleProfileUri,
                        onImageSelected = onImageSelected,
                        size = 45.dp,
                        borderSize = 1.dp,
                        paddingSize = 1.dp,
                        innerBorderSize = 1.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Name, online, typing status
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        val partnerDisplayName = if (activeUser == "Husband") "My Wife ❤️" else "My Husband 👨"
                        Text(
                            text = partnerDisplayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF2C191E)
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTyping) "Typing..." else "Online",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isTyping) Color(0xFFFF4F87) else Color(0xFF4CAF50),
                                    fontWeight = if (isTyping) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }

                    // Search, Pin, and Call Icons
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFFFF4F87))
                    }
                    IconButton(onClick = { onStartCall("voice") }, modifier = Modifier.testTag("voice_call_button")) {
                        Icon(Icons.Filled.Call, contentDescription = "Voice Call", tint = Color(0xFFFF4F87))
                    }
                    IconButton(onClick = { onStartCall("video") }, modifier = Modifier.testTag("video_call_button")) {
                        Icon(Icons.Filled.Videocam, contentDescription = "Video Call", tint = Color(0xFFFF4F87))
                    }
                }

                if (isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search messages...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFFFF4F87)) },
                        trailingIcon = {
                            IconButton(onClick = {
                                searchQuery = ""
                                isSearchActive = false
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close search", tint = Color(0xFFFF4F87))
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF4F87),
                            cursorColor = Color(0xFFFF4F87)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDark) Color(0xFF1F1115) else Color(0xFFFFF0F3))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Security lock banner (Page 6/Page 12 indicator)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x1AFF4F87))
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFFFF4F87), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "End-to-end encrypted private data",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFFF4F87),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        },
        containerColor = if (isDark) Color(0xFF1F1115) else Color(0xFFFFF5F7),
        modifier = Modifier.testTag("chat_screen")
    ) { innerPadding ->
        // Background wallpaper handling
        val bgModifier = when (chatWallpaper) {
            "pink" -> Modifier.background(Color(0xFFFFF0F3))
            "dark" -> Modifier.background(Color(0xFF1F1115))
            else -> Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF5F7), Color(0xFFFDE2E4))
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .then(bgModifier)
        ) {
            // Main Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
            ) {
                items(displayMessages) { message ->
                    val isOwn = message.sender == activeUser
                    MessageBubble(
                        message = message,
                        isOwn = isOwn,
                        isDark = isDark,
                        onClick = { selectedMessageForOptions = message },
                        onReact = { reaction -> onAddReaction(message, reaction) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (isTyping) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0x33FF4F87), CircleShape)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💖")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0x1AFF4F87)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = partnerTypingText.ifEmpty { "Typing..." },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFFFF4F87),
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Message Options Modal Dialog (Delete, Pin, Reply, React)
            selectedMessageForOptions?.let { message ->
                AlertDialog(
                    onDismissRequest = { selectedMessageForOptions = null },
                    title = { Text("Message Options", color = Color(0xFFFF4F87), fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text(text = "\"${message.content}\"", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            Spacer(modifier = Modifier.height(16.dp))

                            // Emoji reactions bar
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf("❤️", "😆", "😮", "😢", "😡", "👍").forEach { emoji ->
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0x1AFF4F87), CircleShape)
                                            .clickable {
                                                onAddReaction(message, emoji)
                                                selectedMessageForOptions = null
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = emoji, fontSize = 20.sp)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            replyingTo = message
                            selectedMessageForOptions = null
                        }) {
                            Text("Reply", color = Color(0xFFFF4F87))
                        }
                    },
                    dismissButton = {
                        Column {
                            TextButton(onClick = {
                                onDeleteMessageForEveryone(message)
                                selectedMessageForOptions = null
                            }) {
                                Text("Delete for Everyone", color = Color.Red)
                            }
                            TextButton(onClick = {
                                onDeleteMessageForMe(message)
                                selectedMessageForOptions = null
                            }) {
                                Text("Delete for Me", color = Color.Gray)
                            }
                        }
                    }
                )
            }

            // Bottom Input bar (WhatsApp Style)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                // Reply Preview Header if replying
                replyingTo?.let { reply ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF0F3))
                            .border(BorderStroke(1.dp, Color(0x33FF4F87)))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Replying to ${reply.sender}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFFF4F87),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = reply.content,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                            )
                        }
                        IconButton(onClick = { replyingTo = null }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel reply", tint = Color.Gray)
                        }
                    }
                }

                // Dynamic Input Row with Glassmorphism
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF2C191E) else Color.White
                        ),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, Color(0x33FF4F87)),
                        modifier = Modifier
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(28.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            // Emoji trigger (quick populates input with cute heart)
                            IconButton(onClick = { inputText += "❤️" }) {
                                Icon(Icons.Outlined.SentimentSatisfiedAlt, contentDescription = "Emoji picker", tint = Color(0xFFFF4F87))
                            }

                            // Text input field
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = { Text("Type a message...", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    cursorColor = Color(0xFFFF4F87)
                                ),
                                maxLines = 4,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_field"),
                                singleLine = false
                            )

                            // Quick media buttons
                            IconButton(onClick = {
                                // Simulate sending random funny romantic GIF
                                onSendMessage("Sent a romantic sticker 💖", replyingTo, "sticker", null)
                                replyingTo = null
                            }) {
                                Icon(Icons.Outlined.Gif, contentDescription = "GIF sticker", tint = Color(0xFFFF4F87))
                            }

                            IconButton(onClick = {
                                // Simulate sending voice note
                                onSendMessage("🎤 Voice Note (0:12)", replyingTo, "voice", null)
                                replyingTo = null
                            }) {
                                Icon(Icons.Outlined.Mic, contentDescription = "Voice note", tint = Color(0xFFFF4F87))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Round Floating Send Button
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(6.dp, CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFF4F87), Color(0xFFFF85A1))
                                ),
                                shape = CircleShape
                            )
                            .clickable {
                                if (inputText.isNotEmpty()) {
                                    onSendMessage(inputText, replyingTo, "text", null)
                                    inputText = ""
                                    replyingTo = null
                                }
                            }
                            .testTag("send_msg_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    isOwn: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    onReact: (String) -> Unit
) {
    // Hide deleted messages
    if (message.isDeletedForMe == "Both" || (message.isDeletedForMe == "Husband" && !isOwn) || (message.isDeletedForMe == "Wife" && isOwn)) {
        return
    }

    val alignment = if (isOwn) Alignment.CenterEnd else Alignment.CenterStart
    val bgColors = if (isOwn) {
        listOf(Color(0xFFFF4F87), Color(0xFFFF7597))
    } else {
        if (isDark) listOf(Color(0xFF3B242A), Color(0xFF2C191E)) else listOf(Color(0xFFF1E4E6), Color(0xFFFFFFFF))
    }
    val textColor = if (isOwn) Color.White else (if (isDark) Color.White else Color(0xFF2C191E))
    val bubbleShape = if (isOwn) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 2.dp, bottomEnd = 20.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start
        ) {
            // Reply context header
            if (message.replyToText != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x22FF4F87)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .offset(y = 4.dp)
                ) {
                    Text(
                        text = "Reply to ${message.replyToSender}: ${message.replyToText}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFFF4F87),
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Main Message card bubble
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = bubbleShape,
                modifier = Modifier
                    .shadow(4.dp, bubbleShape)
                    .background(Brush.linearGradient(bgColors), bubbleShape)
                    .clickable { onClick() }
                    .widthIn(max = 280.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Message sender tag if not own
                    if (!isOwn) {
                        Text(
                            text = message.sender,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFF4F87),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    // Audio/Media or standard text
                    if (message.mediaType == "voice") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = textColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🎤 Voice Note",
                                style = MaterialTheme.typography.bodyMedium.copy(color = textColor, fontWeight = FontWeight.SemiBold)
                            )
                        }
                    } else if (message.mediaType == "sticker") {
                        Text(
                            text = message.content,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else if (message.mediaType == "call_ended") {
                        val isVideo = message.content.contains("Video", ignoreCase = true)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isVideo) Icons.Filled.Videocam else Icons.Filled.Call,
                                contentDescription = "Call Ended",
                                tint = if (isOwn) Color.White else Color(0xFF4CAF50),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = textColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else if (message.mediaType == "call_missed") {
                        val isVideo = message.content.contains("Video", ignoreCase = true)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isVideo) Icons.Filled.MissedVideoCall else Icons.Filled.CallMissed,
                                contentDescription = "Call Missed",
                                tint = Color.Red,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = textColor,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Date & read indicators row
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                        Text(
                            text = sdf.format(Date(message.timestamp)),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isOwn) Color(0xFFFDE2E4) else Color.Gray,
                                fontSize = 10.sp
                            )
                        )
                        if (isOwn) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (message.isSeen) Icons.Filled.DoneAll else Icons.Filled.Check,
                                contentDescription = if (message.isSeen) "Seen" else "Sent",
                                tint = if (message.isSeen) Color(0xFF4CAF50) else Color(0xFFD4C5C7),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Reactions badges
            if (message.reactions.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.offset(y = (-6).dp, x = if (isOwn) (-8).dp else 8.dp)
                ) {
                    message.reactions.split(",").forEach { react ->
                        Box(
                            modifier = Modifier
                                .shadow(2.dp, CircleShape)
                                .background(Color.White, CircleShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = react, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
