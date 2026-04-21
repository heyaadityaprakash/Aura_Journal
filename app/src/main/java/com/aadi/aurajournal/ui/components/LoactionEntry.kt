package com.aadi.aurajournal.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.aadi.aurajournal.utils.LocationDetails
import com.aadi.aurajournal.utils.fetchCurrentLocation
import com.aadi.aurajournal.utils.searchLocations
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionSheet(
    onDismissRequest: () -> Unit,
    onLocationSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<LocationDetails>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // Search logic with debounce
    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            isSearching = true
            delay(500) // Debounce
            searchResults = searchLocations(searchQuery)
            isSearching = false
        } else {
            searchResults = emptyList()
        }
    }

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            isFetchingLocation = true
            fetchCurrentLocation(
                context = context,
                onLocationFetched = { details ->
                    isFetchingLocation = false
                    val combinedString = "${details.name}|${details.lat}|${details.lng}"
                    onLocationSelected(combinedString)
                },
                onError = { isFetchingLocation = false }
            )
        }
    }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Add Location", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                },
                placeholder = { Text(text = "Search a Place") },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                item {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Use my current Location",
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        leadingContent = {
                            if (isFetchingLocation) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(
                                    Icons.Default.MyLocation,
                                    contentDescription = "Location",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                isFetchingLocation = true
                                fetchCurrentLocation(
                                    context = context,
                                    onLocationFetched = {
                                        isFetchingLocation = false
                                        val combinedString = "${it.name}|${it.lat}|${it.lng}"
                                        onLocationSelected(combinedString)
                                    },
                                    onError = { isFetchingLocation = false }
                                )
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    )
                    HorizontalDivider()
                }

                items(searchResults) { location ->
                    ListItem(
                        headlineContent = { Text(location.name) },
                        modifier = Modifier.clickable {
                            val combinedString = "${location.name}|${location.lat}|${location.lng}"
                            onLocationSelected(combinedString)
                        }
                    )
                }

                if (searchQuery.isNotBlank() && searchResults.isEmpty() && !isSearching) {
                    item {
                        ListItem(
                            headlineContent = { Text("Set to: \"$searchQuery\"") },
                            modifier = Modifier.clickable { onLocationSelected(searchQuery) }
                        )
                    }
                }
            }
        }
    }
}
