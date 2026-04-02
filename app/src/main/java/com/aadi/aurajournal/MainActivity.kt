package com.aadi.aurajournal

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aadi.aurajournal.ui.AuraBottomBar
import com.aadi.aurajournal.ui.theme.AuraJournalTheme
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aadi.aurajournal.data.AuraDatabase
import com.aadi.aurajournal.data.JournalRepository
import com.aadi.aurajournal.utils.authenticateWithBiometrics

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val database = AuraDatabase.getDatabase(context)
            val repository = remember { JournalRepository(database.journalDao(), context) }

            val navController = rememberNavController()

            val journalviewModel: JournalViewModel = viewModel(
                factory = JournalViewModelFactory(repository)
            )

            var isUnlocked by remember { mutableStateOf(!repository.isAppLockEnabled()) }

            LaunchedEffect(Unit) {
                if (!isUnlocked) {
                    authenticateWithBiometrics(
                        context = context,
                        onSuccess = { isUnlocked = true },
                        onError = {
                            Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            AuraJournalTheme {
                if (isUnlocked) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    // Hide bottom bar on the editor screen
                    val showBottomBar = currentRoute != null && !currentRoute.startsWith("editor")

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                AuraBottomBar(navController = navController)
                            }
                        }
                    ) { innerPadding ->
                        AuraNavGraph(
                            navController = navController,
                            innerPadding = innerPadding,
                            viewModel = journalviewModel
                        )
                    }
                } else {
                    // Splash or Lock screen background while waiting for authentication
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    )
                }
            }
        }
    }
}
