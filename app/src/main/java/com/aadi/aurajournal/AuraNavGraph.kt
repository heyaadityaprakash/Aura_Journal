package com.aadi.aurajournal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aadi.aurajournal.feature.CalendarScreen
import com.aadi.aurajournal.feature.ComposeEntryScreen
import com.aadi.aurajournal.feature.InsightsScreen
import com.aadi.aurajournal.feature.ProfileScreen
import com.aadi.aurajournal.feature.TimelineScreen

@Composable
fun AuraNavGraph(navController: NavHostController,
                 innerPadding: PaddingValues,
                 viewModel: JournalViewModel) {
    NavHost(
        navController = navController,
        startDestination = Screen.Timeline.route,
        modifier = Modifier.padding(innerPadding)
    ) {

        composable(
            route = "editor?entryId={entryId}",
            arguments = listOf(navArgument("entryId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getInt("entryId") ?: -1

            ComposeEntryScreen(
                viewModel = viewModel,
                entryId = entryId, // Pass the ID into the screen
                onNavigateBack = { navController.popBackStack() }
            )
        }


        // We will replace these Placeholders with actual screen files on Day 4
        composable(Screen.Timeline.route) {
            TimelineScreen(
                viewModel=viewModel,
                onNavigateToEditor = {
                    entryId->
                    if(entryId==null){
                        //open new entry page
                        navController.navigate("editor")
                    }else{
                        //edit page
                        navController.navigate("editor?entryId=$entryId")

                    }
                }
            )
            
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(
                viewModel = viewModel,
                onNavigateToEditor = { entryId ->
                    if (entryId == null) navController.navigate("editor")
                    else navController.navigate("editor?entryId=$entryId")
                }
            )
        }
        composable(Screen.Insights.route) {
            InsightsScreen(
                viewModel=viewModel,
                onNavigateToEditor = {navController.navigate("editor")}
            )
        }


        composable(Screen.Profile.route) {
            ProfileScreen(viewModel = viewModel)
        }

    }
}

// Temporary Composable to test routing
@Composable
fun PlaceholderScreen(title: String) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
        }
    }
}