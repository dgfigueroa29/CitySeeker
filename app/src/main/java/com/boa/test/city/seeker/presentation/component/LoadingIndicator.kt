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
 * A composable function that displays a loading indicator.
 *
 * The loading indicator is a Lottie animation that is displayed in the center of the screen
 * when the `isLoading` parameter is true. The animation is loaded from the `R.raw.loading`
 * resource.
 *
 * @param isLoading A boolean value that indicates whether the loading indicator should be
 * displayed.
 */
@Composable
fun LoadingIndicator(isLoading: Boolean) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))

    if (isLoading) {
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
