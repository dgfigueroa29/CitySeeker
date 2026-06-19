package com.boa.test.city.seeker.presentation.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween

/**
 * Motion constants for the CitySeeker design system.
 *
 * Defines reusable animation durations, easings, and animation specs
 * based on Material Design 3 motion guidelines.
 *
 * Timing guidelines:
 * - Screen-to-screen transitions: 300-600ms
 * - Dialogs and medium objects: 250-400ms
 * - Small objects: 50-200ms
 * - Enter transitions should be longer than exit transitions
 */
object MotionDuration {
    val Instant = 0
    val ExtraFast = 50
    val Fast = 100
    val Medium = 200
    val Standard = 300
    val Slow = 400
    val ExtraSlow = 500
    val ExtraExtraSlow = 1000
    val ScreenTransition = 400
    val DialogTransition = 300
    val SmallElement = 150
}

object MotionEasing {
    val Standard = FastOutSlowInEasing
    val Linear = LinearEasing
    val Decelerate = androidx.compose.animation.core.FastOutLinearInEasing
    val Accelerate = androidx.compose.animation.core.LinearOutSlowInEasing
    val Emphasis = FastOutSlowInEasing
}

object MotionSpec {
    val ScreenEnter =
        tween<Float>(
            durationMillis = MotionDuration.ScreenTransition,
            easing = MotionEasing.Decelerate,
        )
    val ScreenExit =
        tween<Float>(
            durationMillis = MotionDuration.Standard,
            easing = MotionEasing.Accelerate,
        )
    val ElementAppear =
        tween<Float>(
            durationMillis = MotionDuration.Medium,
            easing = MotionEasing.Standard,
        )
    val ElementDisappear =
        tween<Float>(
            durationMillis = MotionDuration.Fast,
            easing = MotionEasing.Standard,
        )
    val ButtonPress =
        tween<Float>(
            durationMillis = MotionDuration.SmallElement,
            easing = MotionEasing.Standard,
        )
    val StateTransition =
        tween<Float>(
            durationMillis = MotionDuration.Standard,
            easing = MotionEasing.Standard,
        )
}
