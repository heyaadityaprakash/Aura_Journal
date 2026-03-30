package com.aadi.aurajournal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aadi.aurajournal.data.JournalEntry
import com.aadi.aurajournal.data.MoodType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A reusable timeline entry card used on the Timeline and Calendar screens.
 *
 * The left column is provided via [leadingContent] so each screen can
 * supply its own badge (date badge OR mood icon). The right side (entry card)
 * and the vertical connecting line are shared.
 *
 * @param entry           The journal entry to display.
 * @param isLastItem      Whether this is the last item (hides the connecting line).
 * @param onEditClick     Callback invoked on a normal click.
 * @param onDeleteClick   Callback invoked on a long press.
 * @param leadingContent  Composable for the left-column badge / icon.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TimelineEntryCard(
    entry: JournalEntry,
    isLastItem: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    moodLabel: String? = null,
    leadingContent: @Composable ColumnScope.() -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val fullDateFormatter = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())

    // Map saved mood name to MoodType to get the emoji and title
    val moodType = MoodType.entries.find { it.name == entry.mood }
    val moodEmoji = moodType?.emoji ?: "😌"
    val moodDisplayText = moodLabel ?: moodType?.title ?: entry.mood ?: "Calm"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // --- Left column: badge + connecting line ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(60.dp)
        ) {
            leadingContent()

            if (!isLastItem) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // --- Right column: entry card ---
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .combinedClickable(
                    onClick = onEditClick,
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDeleteClick()
                    }
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = fullDateFormatter.format(Date(entry.timeStamp)),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Pill container for the mood
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = moodEmoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = moodDisplayText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}
