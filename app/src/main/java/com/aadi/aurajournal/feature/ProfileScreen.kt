package com.aadi.aurajournal.feature

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aadi.aurajournal.JournalViewModel
import com.aadi.aurajournal.data.JournalEntry
import com.aadi.aurajournal.ui.components.AuraCard
import com.aadi.aurajournal.ui.components.StatsCard
import com.aadi.aurajournal.utils.authenticateWithBiometrics
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: JournalViewModel,
    onShowBottomBar: (Boolean) -> Unit,
    onSignOut:()->Unit
) {

    val context = androidx.compose.ui.platform.LocalContext.current

    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val googleName = currentUser?.displayName ?: "user"
    val googleEmail = currentUser?.email
    val profilePicUrl = currentUser?.photoUrl

    //local username
    val username by viewModel.username.collectAsState()

    var showDailog by remember { mutableStateOf(false) }
    var tempname by remember { mutableStateOf(username.ifBlank { googleName }) }
    // States for the interactive elements
    var isDarkMode by remember { mutableStateOf(false) }

    val entries by viewModel.allEntries.collectAsState()

//    app lock
    val isAppLocked by viewModel.isAppLocked.collectAsState()

    // Hide the bottom bar when on the Profile Screen
    LaunchedEffect(Unit) {
        onShowBottomBar(false)
    }

    // Use normal scroll state as we don't want to toggle the bottom bar on scroll here
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {

                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()+24.dp))

                // --- 1. Header Area ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if(profilePicUrl !=null){
                        AsyncImage(
                            model = profilePicUrl,
                            contentDescription = "profile pic",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop


                        )
                    }
                    else{
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
                        }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.clickable {
                        tempname = username.ifBlank { googleName }
                        showDailog = true
                    }) {
                        Text(
                            text = username.ifBlank { googleName },
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if(googleEmail != null){
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = googleEmail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (showDailog) {
                    AlertDialog(
                        onDismissRequest = { showDailog = false },
                        title = {
                            Text(
                                "What should we call you?",
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
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
                                        Toast.makeText(context, "App Lock $action", Toast.LENGTH_SHORT)
                                            .show()
                                    },
                                    onError = {
                                        Toast.makeText(
                                            context,
                                            "Authentication Failed",
                                            Toast.LENGTH_SHORT
                                        ).show()
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
                                .clickable {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:prakashaaditya68@gmail.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "Help & Support")
                                        putExtra(Intent.EXTRA_TEXT, "Hi, I need help with...")
                                    }
                                    context.startActivity(intent)
                                }
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Help and Support",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))


//                lgoout button
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Log Out",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Sign Out")
                }

                Spacer(modifier = Modifier.height(48.dp))

                // --- 4. Footer Text ---
                Text(
                    text = "aura v-1.4",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 100.dp)) // Extra space to scroll past bar
            }
        }

        // Top Gradient Fade
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 32.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.background, Color.Transparent)
                    )
                )
                .align(Alignment.TopCenter)
        )

        // Bottom Gradient Fade
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
                    )
                )
                .align(Alignment.BottomCenter)
        )
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
        if (datesList.isNotEmpty()) {
            listState.scrollToItem(datesList.size - 1)
        }
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(datesList) { date ->
                    val isToday = date == today
                    val isJournaled = journaledDates.contains(date)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            shape = CircleShape,
                            color = when {
                                isJournaled -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            },
                            modifier = Modifier.size(36.dp),
                            border = if (isToday && !isJournaled) BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary
                            ) else null
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isJournaled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
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
            .padding(horizontal = 20.dp, vertical = 8.dp),
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
            onCheckedChange = onCheckedChange
        )
    }
}
