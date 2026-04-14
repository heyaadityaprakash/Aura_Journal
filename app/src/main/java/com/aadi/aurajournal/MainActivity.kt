package com.aadi.aurajournal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aadi.aurajournal.data.AuraDatabase
import com.aadi.aurajournal.data.FirestoreManager
import com.aadi.aurajournal.data.JournalRepository
import com.aadi.aurajournal.ui.AuraBottomBar
import com.aadi.aurajournal.ui.theme.AuraJournalTheme
import com.aadi.aurajournal.utils.ReminderWorker
import com.aadi.aurajournal.utils.authenticateWithBiometrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.TimeUnit

class MainActivity : FragmentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scheduleDailyReminder()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val database = AuraDatabase.getDatabase(context)

            // Better external scope (not tied directly to Activity lifecycle)
            val externalScope = remember {
                CoroutineScope(SupervisorJob() + Dispatchers.IO)
            }

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
                        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return LoginViewModel(authRepository) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class")
                    }
                }
            )

            val journalViewModel: JournalViewModel = viewModel(
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

                    // FIXED: removed remember (so it updates correctly)
                    val startDest = remember {
                        if (authRepository.isUserSignedIn() || authRepository.isGuestMode()) {
                            Screen.Timeline.route
                        } else {
                            "login"
                        }
                    }

                    val isBaseRoute =
                        currentRoute != null &&
                                !currentRoute.startsWith("editor") &&
                                currentRoute != "login"

                    var isBottomBarVisible by remember { mutableStateOf(true) }

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
                                AuraBottomBar(
                                    navController = navController,
                                    onNavigateToEditor = {
                                        navController.navigate("editor")
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        AuraNavGraph(
                            navController = navController,
                            innerPadding = innerPadding,
                            viewModel = journalViewModel,
                            onShowBottomBar = { isBottomBarVisible = it },
                            startDestination = startDest,
                            loginViewModel = loginViewModel
                        )
                    }

                } else {
                    // Locked screen background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    )
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                scheduleDailyReminder()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            scheduleDailyReminder()
        }
    }

    private fun scheduleDailyReminder() {
        val reminderRequest =
            PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyJournalReminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderRequest
        )
    }
}

