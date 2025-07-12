package com.boa.test.city.seeker.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.boa.test.city.seeker.R

/**
 * A composable function that displays an offline indicator.
 *
 * The indicator consists of a Lottie animation that is displayed when the device is offline.
 * The animation is displayed in a white box that fills the entire screen.
 *
 * @param isOffline A boolean value that indicates whether the device is offline.
 */
@Composable
fun OfflineIndicator(isOffline: Boolean) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.offline))

    if (isOffline) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                modifier = Modifier.fillMaxSize(.9f),
                composition = composition,
                iterations = LottieConstants.IterateForever
            )
        }
    }
}
