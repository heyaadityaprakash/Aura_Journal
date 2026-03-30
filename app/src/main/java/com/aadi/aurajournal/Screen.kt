package com.aadi.aurajournal

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Timeline : Screen("timeline", "Home", Icons.Default.Home)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.DateRange)
    object Insights : Screen("insights", "Insights", Icons.Default.Star)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    // Add this to your Screen sealed class in Screen.kt
    object Editor : Screen("editor", "Editor", Icons.Default.Check) // Icon doesn't matter much here, won't be on bottom bar
}