package com.aadi.aurajournal.feature

import android.widget.Toast
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aadi.aurajournal.JournalViewModel
import com.aadi.aurajournal.data.JournalEntry
import com.aadi.aurajournal.ui.components.AuraCard
import com.aadi.aurajournal.ui.components.VoiceNoteItem

import java.util.Calendar

@Composable
fun TimelineScreen(
    viewModel: JournalViewModel,
    onNavigateToEditor:(Int?)->Unit
){

    val context= LocalContext.current

//    fetch and update from database
    val entries by viewModel.allEntries.collectAsState()
    val username by viewModel.username.collectAsState()
//    entries to delete
    var entryToDelete by remember { mutableStateOf<JournalEntry?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditor(null) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "new entry")
            }
        }
    ) {
        innerPading->
//        lazycolumn that handles scrolling
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPading),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp)

        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Welcome, $username",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "profile")

                    }

                }
                Spacer(modifier = Modifier.height(24.dp))

            }
            item {
                MoodCheckInCard()
                Spacer(modifier = Modifier.height(32.dp))
            }

//            Saved Notes List

            item {
                Text(
                    text = "Saved Entries",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if(entries.isEmpty()){
                item {
                    Text(
                        text = "Tap the + button to start journaling",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            else {
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

    //            delete dialog box
    if(entryToDelete!=null){
        AlertDialog(
            onDismissRequest = {entryToDelete=null},
            title = { Text("Delete Entry?") },
            text = { Text("Are you sure you want to permanently delete this journal entry?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEntry(entryToDelete!!)
                        entryToDelete=null
                        Toast.makeText(context, "Entry deleted", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        entryToDelete=null
                    }
                ) {
                    Text("Cancel")
                }
            }


        )
    }

}

@Composable
fun MoodCheckInCard() {
    // State to track which mood is selected (for demonstration)
    var selectedMood by remember { mutableStateOf("Calm") }

    val hour= Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    val greeting=when(hour){
        in 0..11->"Good Morning"
        in 12..17->"Good Afternoon"
        in 18..22->"Good Evening"
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
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Evenly distribute mood buttons
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
            indication = null, // Removes gray ripple if desired
            interactionSource = remember { MutableInteractionSource() }
        )
    ) {
        Surface(
            shape = CircleShape,
            // Highlight color when selected
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
