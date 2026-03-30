package com.aadi.aurajournal.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aadi.aurajournal.Screen
import com.aadi.aurajournal.ui.theme.AuraJournalTheme

@Composable
fun AuraBottomBar(navController: NavHostController) {
    val items = listOf(
        Screen.Timeline,
        Screen.Calendar,
        Screen.Insights,
        Screen.Profile
    )

    NavigationBar(
        modifier = Modifier
            .padding(horizontal = 16.dp).navigationBarsPadding()
            .clip(RoundedCornerShape(32.dp)), // M3 Expressive Pill shape
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewAuraBottomBar(){
    AuraJournalTheme {
        AuraBottomBar(navController = rememberNavController())
    }
}