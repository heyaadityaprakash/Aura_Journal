package com.aadi.aurajournal.feature

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aadi.aurajournal.JournalViewModel
import com.aadi.aurajournal.ui.components.AuraCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: JournalViewModel,
    onNavigateToEditor: (Int?) -> Unit
) {
    // 1. COLLECT THE STATES FROM THE VIEWMODEL
    val weeklySummary by viewModel.weeklySummary.collectAsState()
    val patterns by viewModel.patterns.collectAsState()
    val suggestedPrompt by viewModel.suggestedPrompt.collectAsState()
    val isLoading by viewModel.isInsightsLoading.collectAsState()

    // 2. TRIGGER THE AI GENERATION WHEN THE SCREEN OPENS
    LaunchedEffect(Unit) {
        viewModel.getInsights()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {

            // Header
            Text(
                text = "Insights",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "A gentle look back at your journey.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 3. CONDITIONAL RENDERING (SPINNER VS. CARDS)
            if (isLoading) {
                // Show a centered loading spinner while Gemini is thinking
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Reading your entries...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                // Show the cards.

                // --- 1. The Weekly Aura Card ---
                WeeklyAuraCard(summary = weeklySummary)

                Spacer(modifier = Modifier.height(24.dp))

                // --- 2. Connecting the Dots ---
                if (patterns.isNotEmpty()) {
                    PatternsCard(patterns = patterns)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- 3. Forward Focus ---
                ActionablePromptCard(
                    prompt = suggestedPrompt,
                    onWriteClick = { onNavigateToEditor(null) }
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom nav bar
        }
    }
}

// --- UI COMPONENTS FOR THE CARDS ---

@Composable
fun WeeklyAuraCard(summary: String) {
    val auraGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    )

    AuraCard(gradientBrush = auraGradient) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Sparkle",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Your Weekly Aura",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = summary,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
        )
    }
}

@Composable
fun PatternsCard(patterns: List<String>) {
    AuraCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = "Patterns",
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Connecting the Dots",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        patterns.forEach { pattern ->
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("💡", modifier = Modifier.padding(end = 12.dp))
                Text(
                    text = pattern,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ActionablePromptCard(prompt: String, onWriteClick: () -> Unit) {
    AuraCard(
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = "Prompt",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Unasked Question",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyLarge.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onWriteClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Create, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Write about this")
        }
    }
}