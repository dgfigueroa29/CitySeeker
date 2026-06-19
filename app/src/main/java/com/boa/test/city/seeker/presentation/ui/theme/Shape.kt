package com.boa.test.city.seeker.presentation.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape tokens for the CitySeeker design system.
 *
 * Based on Material Design 3 shape scale with additional semantic shapes.
 */
val Shapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

object ShapeTokens {
    val None = RoundedCornerShape(0.dp)
    val ExtraSmall = RoundedCornerShape(4.dp)
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Large = RoundedCornerShape(16.dp)
    val ExtraLarge = RoundedCornerShape(28.dp)
    val Full = RoundedCornerShape(50.dp)

    val Card = Large
    val Button = ExtraLarge
    val TextField = ExtraLarge
    val Chip = Full
    val Dialog = ExtraLarge
    val BottomSheet = Large
    val TopAppBar = Medium
    val FAB = Full
    val Avatar = Full
    val MapMarker = Full
    val Snackbar = Medium
    val NavigationBar = Medium
}
