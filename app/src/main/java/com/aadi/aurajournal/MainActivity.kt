package com.aadi.aurajournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aadi.aurajournal.ui.AuraBottomBar
import com.aadi.aurajournal.ui.theme.AuraJournalTheme
import androidx.navigation.compose.rememberNavController
import com.aadi.aurajournal.data.AuraDatabase
import com.aadi.aurajournal.data.JournalRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val database = AuraDatabase.getDatabase(context)
            val repository = JournalRepository(database.journalDao(), context)

            val navController = rememberNavController()

            val journalviewModel: JournalViewModel= viewModel(
                factory = JournalViewModelFactory(repository)
            )

            AuraJournalTheme {
                Scaffold(
                    bottomBar = { AuraBottomBar(navController = navController) }

                ) { innerPadding ->
                    AuraNavGraph(
                        navController = navController,
                        innerPadding=innerPadding,
                        viewModel = journalviewModel
                    )

                }
            }
        }
    }
}
