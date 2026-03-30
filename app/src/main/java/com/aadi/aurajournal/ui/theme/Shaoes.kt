package com.aadi.aurajournal.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp


val AuraShapes= Shapes(
    // Extra Small: For chips and small badges
    extraSmall = RoundedCornerShape(8.dp),
    // Small: For standard buttons
    small = RoundedCornerShape(12.dp),
    // Medium: For the Floating Nav Bar and Context Snapshots
    medium = RoundedCornerShape(24.dp),
    // Large: The signature "Squircle" for your Journal Entry Cards
    large = RoundedCornerShape(32.dp),
    // Extra Large: For full-screen bottom sheets
    extraLarge = RoundedCornerShape(40.dp)
)
