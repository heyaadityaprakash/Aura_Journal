package com.aadi.aurajournal.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.aadi.aurajournal.data.MoodType

@Composable
fun MoodPicker(
    selectedMood: MoodType?,
    onMoodSelected:(MoodType)-> Unit
){
    var isMenuExpanded by remember { mutableStateOf(false) }


    //pil button
    Box(
        contentAlignment = Alignment.Center
    ){
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .clip(CircleShape)
                .clickable { isMenuExpanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)

            ) {
                Text(
                    text = selectedMood?.emoji ?: "😶",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = selectedMood?.title ?: "How are you feeling?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }

    if(isMenuExpanded){
        Popup(
            alignment = Alignment.TopEnd,
            onDismissRequest = { isMenuExpanded = false }
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.padding(bottom = 60.dp) // Pushes popup up
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MoodType.values().forEach { mood->
                        Text(
                            text = mood.emoji,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .clickable{
                                    onMoodSelected(mood)
                                    isMenuExpanded=false
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }

}