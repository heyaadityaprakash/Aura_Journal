package com.aadi.aurajournal.feature
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aadi.aurajournal.JournalViewModel
import com.aadi.aurajournal.ui.components.TimelineEntryCard
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: JournalViewModel,
    onNavigateToEditor: (Int?) -> Unit
) {
    val entries by viewModel.allEntries.collectAsState()

    val context= LocalContext.current

    // Manage current month being viewed
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // THE FIX: Default to exactly today's date!
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Filter entries for the selected day
    val selectedDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val selectedDateString = selectedDateFormatter.format(
        Date.from(selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
    )

    val entriesForSelectedDay = entries.filter { entry ->
        val entryDateString = selectedDateFormatter.format(Date(entry.timeStamp))
        entryDateString == selectedDateString
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- 1. Top Bar & Month Selector ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.clip(CircleShape).clickable { /* TODO: Open Month Picker */ }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${currentMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${currentMonth.year}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Row {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month")
                    }
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Month")
                    }
                }
            }

            // --- 2. The Calendar Grid ---
            CalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate, // Pass the full date here
                onDaySelected = { newDate -> selectedDate = newDate }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. The Entries List ---
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    if (entriesForSelectedDay.isEmpty()) {
                        item {
                            Text(
                                text = "No entries for this date.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        itemsIndexed(entriesForSelectedDay) { index, entry ->
                            val moodIcon: ImageVector = when (entry.mood) {
                                "Calm" -> Icons.Rounded.Bedtime
                                "Curious" -> Icons.Rounded.Search
                                "Motivated" -> Icons.Rounded.RocketLaunch
                                "Happy" -> Icons.Rounded.EmojiEmotions
                                else -> Icons.Rounded.Bedtime
                            }

                            TimelineEntryCard(
                                entry = entry,
                                isLastItem = index == entriesForSelectedDay.size - 1,
                                onEditClick = { onNavigateToEditor(entry.id) },
                                onDeleteClick = { viewModel.deleteEntry(entry)
                                    Toast.makeText(context, "Entry deleted", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                // Mood icon as leading content
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(48.dp).padding(bottom = 8.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = moodIcon,
                                            contentDescription = entry.mood,
                                            modifier = Modifier.size(24.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// --- The Visual Calendar Grid ---
@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDaySelected: (LocalDate) -> Unit
) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val daysInMonth = currentMonth.lengthOfMonth()

    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        var currentDay = 1
        for (week in 0..5) {
            if (currentDay > daysInMonth) break

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (dayOfWeek in 0..6) {
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (week == 0 && dayOfWeek < firstDayOfWeek || currentDay > daysInMonth) {
                            // Empty box
                        } else {
                            val day = currentDay

                            // THE FIX: Check if the year, month, AND day all match
                            val isSelected = currentMonth.year == selectedDate.year &&
                                    currentMonth.month == selectedDate.month &&
                                    day == selectedDate.dayOfMonth

                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable { onDaySelected(currentMonth.atDay(day)) } // Return the full LocalDate
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = day.toString(),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            currentDay++
                        }
                    }
                }
            }
        }
    }
}