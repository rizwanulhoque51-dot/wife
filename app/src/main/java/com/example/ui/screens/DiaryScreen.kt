package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.DiaryEntity
import com.example.ui.components.FloatingHeartsBackground
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DiaryScreen(
    diaryEntries: List<DiaryEntity>,
    activeUser: String,
    themeMode: String,
    onAddEntry: (String, String, String) -> Unit,
    onDeleteEntry: (DiaryEntity) -> Unit,
    onBack: () -> Unit
) {
    val isDark = themeMode == "dark"
    var isAddDialogOpen by remember { mutableStateOf(false) }

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
                    text = "Private Diary",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF4F87)
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddDialogOpen = true },
                containerColor = Color(0xFFFF4F87),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.shadow(8.dp, CircleShape)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New Entry", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = if (isDark) Color(0xFF1F1115) else Color(0xFFFFF5F7),
        modifier = Modifier.testTag("diary_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FloatingHeartsBackground(heartColor = Color(0x1AFF4F87))

            if (diaryEntries.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("📖", fontSize = 60.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your private diary is empty",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF4F87)
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the '+' button to write your first beautiful memory or thought together.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(diaryEntries) { entry ->
                        DiaryCardItem(
                            entry = entry,
                            activeUser = activeUser,
                            isDark = isDark,
                            onDelete = { onDeleteEntry(entry) }
                        )
                    }
                }
            }

            // Add Diary Entry Dialog
            if (isAddDialogOpen) {
                var diaryTitle by remember { mutableStateOf("") }
                var diaryContent by remember { mutableStateOf("") }
                var selectedMood by remember { mutableStateOf("❤️") }

                val moods = listOf("❤️", "💖", "🥰", "🌸", "😊", "⭐")

                AlertDialog(
                    onDismissRequest = { isAddDialogOpen = false },
                    title = { Text("New Diary Log", color = Color(0xFFFF4F87), fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = diaryTitle,
                                onValueChange = { diaryTitle = it },
                                label = { Text("Title") },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF4F87)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = diaryContent,
                                onValueChange = { diaryContent = it },
                                label = { Text("What's on your mind?") },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF4F87)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            )

                            Text("Select Your Mood today:", fontWeight = FontWeight.SemiBold)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                moods.forEach { m ->
                                    val isSelected = selectedMood == m
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                if (isSelected) Color(0xFFFF4F87) else Color(0x1AFF4F87),
                                                CircleShape
                                            )
                                            .clickable { selectedMood = m },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = m, fontSize = 20.sp)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (diaryTitle.isNotEmpty() && diaryContent.isNotEmpty()) {
                                    onAddEntry(diaryTitle, diaryContent, selectedMood)
                                    isAddDialogOpen = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4F87))
                        ) {
                            Text("Save Entry")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isAddDialogOpen = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DiaryCardItem(
    entry: DiaryEntity,
    activeUser: String,
    isDark: Boolean,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF2C191E) else Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0x1AFF4F87)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row (Mood, Title, Writer badge)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0x1AFF4F87), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(entry.mood, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF4F87)
                        )
                    )
                }

                // Writer badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x33FF4F87))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = entry.writtenBy,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFFF4F87),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body text
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = if (isDark) Color.White else Color(0xFF2C191E),
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer (Timestamp, delete if own)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy h:mm a", Locale.getDefault())
                Text(
                    text = sdf.format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                )

                if (entry.writtenBy == activeUser) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
