package com.aadi.aurajournal.feature

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aadi.aurajournal.JournalViewModel
import com.aadi.aurajournal.data.JournalEntry
import com.aadi.aurajournal.ui.components.AuraCard
import com.aadi.aurajournal.ui.components.VoiceNoteItem
import com.aadi.aurajournal.utils.rememberLazyBottomBarState
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TimelineScreen(
    viewModel: JournalViewModel,
    onNavigateToEditor: (Int?) -> Unit,
    onShowBottomBar: (Boolean) -> Unit,
    onNavigateToProfile:()->Unit
) {
    val context = LocalContext.current

    val currentUser = FirebaseAuth.getInstance().currentUser
    val googleName = currentUser?.displayName?.takeIf { it.isNotBlank() } 
        ?: currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } 
        ?: "User"
    val profilePicUrl = currentUser?.photoUrl

    // fetch and update from database
    val entries by viewModel.allEntries.collectAsState()
    val username by viewModel.username.collectAsState()

    // Sync google name to repository if not set
    LaunchedEffect(googleName) {
        if (username.isBlank() && googleName != "User" && currentUser != null) {
            viewModel.updateUsername(googleName)
        }
    }

    // entries to delete
    var entryToDelete by remember { mutableStateOf<JournalEntry?>(null) }

    // hook handles all scroll-hide logic
    val listState = rememberLazyBottomBarState(onShowBottomBar)



    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold { _ ->
            // lazycolumn that handles scrolling
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 100.dp,
                )
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Welcome, ${username.ifBlank { googleName }}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .size(48.dp).
                                clickable{onNavigateToProfile()},

                        ) {
                            if (profilePicUrl != null) {
                                AsyncImage(
                                    model = profilePicUrl,
                                    contentDescription = "profile",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = "profile",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    MoodCheckInCard()
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    Text(
                        text = "Saved Entries",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (entries.isEmpty()) {
                    item {
                        Text(
                            text = "Tap the + button to start journaling",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                } else {
                    items(entries) { entry ->
                        VoiceNoteItem(
                            entry = entry,
                            onClick = { onNavigateToEditor(entry.id) },
                            onDelete = { entryToDelete = entry }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Add some padding at the bottom so the FAB doesn't cover the last item
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // Gradient fade at the top over the status bar area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 32.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            Color.Transparent
                        )
                    )
                )
                .align(Alignment.TopCenter)
        )

        // Gradient fade at the bottom before the nav bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                        )
                    )
                )
                .align(Alignment.BottomCenter)
        )

//        // FAB moved outside Scaffold to be on top of the gradients and correctly aligned
//        FloatingActionButton(
//            onClick = { onNavigateToEditor(null) },
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(end = 24.dp, bottom = 100.dp),
//            containerColor = MaterialTheme.colorScheme.primaryContainer,
//            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
//        ) {
//            Icon(Icons.Default.Add, contentDescription = "new entry")
//        }
    }

    // delete dialog box
    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Entry?") },
            text = { Text("Are you sure you want to permanently delete this journal entry?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        entryToDelete?.let { entry ->
                            viewModel.deleteEntry(entry)
                            entryToDelete = null
                            Toast.makeText(context, "Entry deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { entryToDelete = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MoodCheckInCard() {
    var selectedMood by remember { mutableStateOf("Calm") }
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    val greeting = when (hour) {
        in 0..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        in 18..22 -> "Good Evening"
        else -> "Time for Bed"
    }

    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    )

    AuraCard(
        gradientBrush = gradient,
        contentPadding = 20.dp
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "How are you feeling?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val moods = listOf(
                Icons.Rounded.Bedtime to "Calm",
                Icons.Rounded.Search to "Curious",
                Icons.Rounded.RocketLaunch to "Motivated",
                Icons.Rounded.EmojiEmotions to "Happy"
            )

            moods.forEach { (icon, label) ->
                MoodAction(
                    icon = icon,
                    label = label,
                    isSelected = selectedMood == label,
                    onClick = { selectedMood = label }
                )
            }
        }
    }
}

@Composable
fun MoodAction(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            onClick = onClick,
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        )
    ) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
