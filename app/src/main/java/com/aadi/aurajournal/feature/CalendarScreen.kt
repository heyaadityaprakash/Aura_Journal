package com.aadi.aurajournal.feature

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aadi.aurajournal.JournalViewModel
import com.aadi.aurajournal.data.JournalEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: JournalViewModel,
    onNavigateToEditor: (Int?) -> Unit
) {
    val entries by viewModel.allEntries.collectAsState()
    val context = LocalContext.current

    // Sort entries so the newest ones (like "Today") appear at the top
    val sortedEntries = remember(entries) {
        entries.sortedByDescending { it.timeStamp }
    }

    val todayDateString = remember {
        SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(Date())
    }

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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- 1. Header Area ---
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    text = "Memories",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))


                Text(
                    text = todayDateString,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. Timeline List ---
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (sortedEntries.isEmpty()) {
                    item {
                        Text(
                            text = "No memories yet. Start journaling!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                    }
                } else {
                    itemsIndexed(sortedEntries) { index, entry ->
                        TimelineListItem(
                            entry = entry,
                            isLastItem = index == sortedEntries.size - 1,
                            onClick = { onNavigateToEditor(entry.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineListItem(
    entry: JournalEntry,
    isLastItem: Boolean,
    onClick: () -> Unit
) {
    // Format dates for the left column (e.g., "MAR", "22")
    val date = Date(entry.timeStamp)
    val monthStr = SimpleDateFormat("MMM", Locale.getDefault()).format(date).uppercase()
    val dayStr = SimpleDateFormat("dd", Locale.getDefault()).format(date)

    // IntrinsicSize.Min is the magic that makes the vertical line stretch exactly to the card's height
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 24.dp)
    ) {
        // --- Left Side: Date & Timeline ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Text(
                text = monthStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = dayStr,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // The Timeline Dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )

            // The Timeline Line
            if (!isLastItem) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // --- Right Side: The Memory Card ---
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 24.dp) // Spacing between cards
                .clickable { onClick() }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // 1. Image Preview (Only shows if entry has images)
                if (entry.images.isNotEmpty()) {
                    AsyncImage(
                        model = entry.images.first(), // Grabs only the first image
                        contentDescription = "Memory Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 2. Text Content
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                // 3. Mood Pill (Like the "Calm" tag in your screenshot)
                if (entry.mood != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = entry.mood,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
