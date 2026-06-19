package com.boa.test.city.seeker.presentation.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dimension tokens for the CitySeeker design system.
 *
 * Centralized spacing, sizing, and elevation tokens to ensure consistency
 * across the entire application.
 */
object Dimens {
    // Spacing scale (4dp base unit)
    val Space0 = 0.dp
    val SpaceXXXS = 2.dp
    val SpaceXXS = 4.dp
    val SpaceXS = 8.dp
    val SpaceS = 12.dp
    val SpaceM = 16.dp
    val SpaceL = 24.dp
    val SpaceXL = 32.dp
    val SpaceXXL = 48.dp
    val SpaceXXXL = 64.dp

    // Component-specific spacing
    val ScreenPadding = SpaceM
    val ContentSpacing = SpaceM
    val ComponentPadding = SpaceM
    val InsetSmall = SpaceXS
    val InsetMedium = SpaceM
    val InsetLarge = SpaceL

    // Icon sizes
    val IconXXS = 12.dp
    val IconXS = 16.dp
    val IconS = 20.dp
    val IconM = 24.dp
    val IconL = 32.dp
    val IconXL = 40.dp
    val IconXXL = 48.dp
    val IconXXXL = 56.dp

    // Component heights
    val ButtonHeight = 40.dp
    val ButtonHeightSmall = 32.dp
    val ButtonHeightLarge = 48.dp
    val TextFieldHeight = 48.dp
    val TextFieldHeightSmall = 40.dp
    val TopAppBarHeight = 64.dp
    val LargeTopAppBarHeight = 128.dp
    val BottomNavHeight = 72.dp
    val TabBarHeight = 48.dp
    val SnackbarHeight = 48.dp
    val ListItemHeightMin = 56.dp
    val FABSize = 56.dp
    val FABSizeSmall = 40.dp

    // Border widths
    val BorderHairline = 0.5.dp
    val BorderThin = 1.dp
    val BorderMedium = 2.dp
    val BorderThick = 3.dp

    // Elevation scale (Material 3)
    val ElevationNone = 0.dp
    val ElevationLevel0 = 0.dp
    val ElevationLevel1 = 1.dp
    val ElevationLevel2 = 3.dp
    val ElevationLevel3 = 6.dp
    val ElevationLevel4 = 8.dp
    val ElevationLevel5 = 12.dp

    // Corner radius (legacy - prefer Shapes)
    val RadiusNone = 0.dp
    val RadiusSmall = 4.dp
    val RadiusMedium = 8.dp
    val RadiusLarge = 12.dp
    val RadiusXLarge = 16.dp
    val RadiusXXLarge = 28.dp
    val RadiusFull = 9999.dp

    // Animation durations
    val DurationInstant = 0
    val DurationExtraFast = 50
    val DurationFast = 100
    val DurationMedium = 200
    val DurationStandard = 300
    val DurationSlow = 400
    val DurationExtraSlow = 500
    val DurationExtraExtraSlow = 1000

    // Typography
    val LineHeightBody = 24.sp
    val LineHeightHeadline = 32.sp
    val LineHeightDisplay = 40.sp

    // Map specific
    val MapMarkerRadius = 8.0
    val MapMarkerStrokeWidth = 2.0
    val MapDefaultZoom = 9.0
    val MapMinZoom = 3.0
    val MapMaxZoom = 18.0
}
