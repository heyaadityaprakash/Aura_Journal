package com.aadi.aurajournal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A reusable card container used throughout the app for consistent styling.
 *
 * @param modifier Modifier applied to the outer Surface.
 * @param containerColor Background color of the card.
 * @param gradientBrush Optional gradient brush; when provided, overrides [containerColor].
 * @param cornerRadius Corner radius of the card shape.
 * @param contentPadding Inner padding for the card content.
 * @param content Composable content placed inside the card.
 */
@Composable
fun AuraCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    gradientBrush: Brush? = null,
    cornerRadius: Dp = 24.dp,
    contentPadding: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(cornerRadius),
        color = if (gradientBrush != null) Color.Transparent else containerColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .then(
                    if (gradientBrush != null) Modifier.background(gradientBrush) else Modifier
                )
                .padding(contentPadding),
            content = content
        )
    }
}
