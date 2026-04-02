package com.aadi.aurajournal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aadi.aurajournal.data.JournalEntry
import com.aadi.aurajournal.data.MoodType
import java.util.concurrent.TimeUnit

@Composable
fun StatsCard(
    entries: List<JournalEntry>
) {

    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    )


    val totalEntries = entries.size

    val totalWords = remember(entries) {
        entries.sumOf { it.content.split(Regex("\\s+")).count { word -> word.isNotBlank() } }
    }

    val topMood = remember(entries) {
        entries.mapNotNull { it.mood }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?.let { moodName ->
                MoodType.entries.find { it.name == moodName }?.emoji
            } ?: "😶"
    }


    val entryDays=remember(entries) {
        entries
            .filter { it.timeStamp >0L }
            .map { TimeUnit.MILLISECONDS.toDays(it.timeStamp) }
            .toSortedSet()

    }

    val longestStreak = remember(entryDays) {
        if (entryDays.isEmpty()) return@remember 0
        var best = 1; var current = 1
        entryDays.zipWithNext { a, b ->
            if (b - a == 1L) { current++; if (current > best) best = current }
            else current = 1
        }
        best
    }

    // Current streak: walk backwards from today.
    // A streak is still alive if the user journaled today OR yesterday (grace period so
    // writing at midnight doesn't unfairly reset a long streak).
    val currentStreak = remember(entryDays) {
        if (entryDays.isEmpty()) return@remember 0
        val today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        // Start from today if there's an entry today, otherwise try yesterday
        var day = if (entryDays.contains(today)) today else today - 1
        if (!entryDays.contains(day)) return@remember 0
        var streak = 0
        while (entryDays.contains(day)) {
            streak++
            day--
        }
        streak
    }

    AuraCard(
        gradientBrush = gradient,
        contentPadding = 0.dp,
    ){
//
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Your Journaling Habit",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            // Top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // FIX: weight(1f) gives each card a defined width so aspectRatio(1f) resolves correctly
                StatCard(
                    title = "Top Mood",
                    value = topMood,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Entries",
                    value = totalEntries.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Words Written",
                    value = if (totalWords > 1000) "${totalWords / 1000}k" else totalWords.toString(),
                    modifier = Modifier.weight(1f)
                )

            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Streak 🔥 ",
                    value = "$currentStreak",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Best Streak",
                    value = "🏆 $longestStreak",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.height(72.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}