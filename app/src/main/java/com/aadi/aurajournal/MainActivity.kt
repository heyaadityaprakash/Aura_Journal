package com.aadi.aurajournal

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aadi.aurajournal.ui.AuraBottomBar
import com.aadi.aurajournal.ui.theme.AuraJournalTheme
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aadi.aurajournal.data.AuraDatabase
import com.aadi.aurajournal.data.FirestoreManager
import com.aadi.aurajournal.data.JournalRepository
import com.aadi.aurajournal.utils.authenticateWithBiometrics

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val database = AuraDatabase.getDatabase(context)
            
            // Background scope for repository sync
            val externalScope = lifecycleScope 
            
            val repository = remember { 
                JournalRepository(
                    journalDao = database.journalDao(), 
                    firestoreManager = FirestoreManager(),
                    context = context,
                    externalScope = externalScope
                ) 
            }

            val navController = rememberNavController()

            val authRepository = remember { AuthRepository(context) }
            val loginViewModel: LoginViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return LoginViewModel(authRepository) as T
                    }
                }
            )

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


                    val startDest = remember {
                        if(authRepository.isUserSignedIn()) Screen.Timeline.route else "login"
                    }

                    // Route-based visibility
                    val isBaseRoute = currentRoute != null && !currentRoute.startsWith("editor") && currentRoute!=("login")

                    // Scroll-based visibility
                    var isBottomBarVisible by remember { mutableStateOf(true) }

                    // Reset visibility when route changes
                    LaunchedEffect(currentRoute) {
                        isBottomBarVisible = true
                    }

                    Scaffold(
                        bottomBar = {
                            AnimatedVisibility(
                                visible = isBaseRoute && isBottomBarVisible,
                                enter = slideInVertically(initialOffsetY = { it }),
                                exit = slideOutVertically(targetOffsetY = { it }),
                            ) {
                                AuraBottomBar(navController = navController, onNavigateToEditor ={navController.navigate("editor")})
                            }
                        }
                    ) { innerPadding ->
                        AuraNavGraph(
                            navController = navController,
                            innerPadding = innerPadding,
                            viewModel = journalviewModel,
                            onShowBottomBar = { isBottomBarVisible = it },
                            startDestination = startDest,
                            loginViewModel = loginViewModel
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
