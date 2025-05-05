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