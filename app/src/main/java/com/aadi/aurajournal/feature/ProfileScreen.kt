package com.aadi.aurajournal.feature

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aadi.aurajournal.JournalViewModel
import com.aadi.aurajournal.data.JournalEntry
import com.aadi.aurajournal.ui.components.AuraCard
import com.aadi.aurajournal.ui.components.StatsCard
import com.aadi.aurajournal.utils.authenticateWithBiometrics
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: JournalViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    //username
    val username by viewModel.username.collectAsState()
    var showDailog by remember { mutableStateOf(false) }
    var tempname by remember { mutableStateOf(username) }
    // States for the interactive elements
    var isDarkMode by remember { mutableStateOf(false) }

    val entries by viewModel.allEntries.collectAsState()

//    app lock
    val isAppLocked by viewModel.isAppLocked.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {

            // --- 1. Header Area ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Avatar",
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.clickable{
                    tempname=username
                    showDailog=true
                }) {
                    Text(
                        text = "Welcome,",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = username,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            if (showDailog) {
                AlertDialog(
                    onDismissRequest = { showDailog = false },
                    title = { Text("What should we call you?", style = MaterialTheme.typography.titleLarge) },
                    text = {
                        OutlinedTextField(
                            value = tempname,
                            onValueChange = { tempname = it },
                            singleLine = true,
                            shape = CircleShape,

                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (tempname.isNotBlank()) {
                                viewModel.updateUsername(tempname)
                                showDailog = false
                            }
                        }) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDailog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 2. Streak Count Card ---
            StreakCountCard(entries = entries)

            Spacer(modifier = Modifier.height(24.dp))

            StatsCard(entries = entries)
            
            Spacer(modifier = Modifier.height(24.dp))


            // --- 3. Settings & Toggles ---
            AuraCard(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
                contentPadding = 0.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    // App Lock
                    SettingToggleRow(
                        icon = Icons.Default.Lock,
                        title = "App Lock",
                        isChecked = isAppLocked,
                        onCheckedChange = { newValue ->
                            val action = if (newValue) "Enabled" else "Disabled"

                            authenticateWithBiometrics(
                                context = context,
                                title = "$action App Lock",
                                onSuccess = {
                                    viewModel.setAppLock(newValue)
                                    Toast.makeText(context, "App Lock $action", Toast.LENGTH_SHORT).show()
                                },
                                onError = {
                                    Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )

                    // Dark Mode
                    SettingToggleRow(
                        icon = Icons.Rounded.DarkMode,
                        title = "Dark Mode",
                        isChecked = isDarkMode,
                        onCheckedChange = { isDarkMode = it }
                    )

                    // Help & Support
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* TODO: Open Help */ }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Help and Support",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- 4. Footer Text ---
            Text(
                text = "aura v-1.2.2 (beta)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun StreakCountCard(entries: List<JournalEntry>) {
    val journaledDates = remember(entries) {
        entries.map { entry ->
            Instant.ofEpochMilli(entry.timeStamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.toSet()
    }

    val today = LocalDate.now()
    val datesList = remember {
        (29 downTo 0).map { daysAgo -> today.minusDays(daysAgo.toLong()) }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(datesList.size - 1)
    }

    AuraCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        contentPadding = 0.dp
    ) {
        Column(modifier = Modifier.padding(vertical = 20.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Streak Count",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Icon(Icons.Default.ChevronRight, contentDescription = "Details", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Consecutive journaling for days",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(datesList) { date ->
                    val hasEntry = journaledDates.contains(date)
                    val isToday = date == today

                    val dayStr = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    val dateStr = date.dayOfMonth.toString()

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = if (hasEntry) MaterialTheme.colorScheme.primary else Color.Transparent,
                            border = if (!hasEntry && isToday) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = if (hasEntry || isToday) FontWeight.Bold else FontWeight.Normal),
                                    color = if (hasEntry) MaterialTheme.colorScheme.onPrimary
                                    else if (isToday) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = dayStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
            )
        )
    }
}