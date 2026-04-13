package com.aadi.aurajournal

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.aadi.aurajournal.ui.components.LoginScreen

@Composable
fun AuraNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    viewModel: JournalViewModel,
    startDestination: String,
    loginViewModel: LoginViewModel,
    onShowBottomBar: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Timeline.route)
                }
            )
        }

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
                entryId = entryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Timeline.route) {
            TimelineScreen(
                viewModel = viewModel,
                onNavigateToEditor = { entryId ->
                    if (entryId == null) {
                        navController.navigate("editor")
                    } else {
                        navController.navigate("editor?entryId=$entryId")
                    }
                },
                onShowBottomBar = onShowBottomBar,
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Timeline.route)
                        launchSingleTop = true
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
                },
                onShowBottomBar = onShowBottomBar
            )
        }

        composable(Screen.Insights.route) {
            InsightsScreen(
                viewModel = viewModel,
                onNavigateToEditor = { navController.navigate("editor") },
                onShowBottomBar = onShowBottomBar
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = viewModel,
                onShowBottomBar = onShowBottomBar,
                onSignOut ={
                    loginViewModel.handleLogOut()
                    navController.navigate("login"){
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
