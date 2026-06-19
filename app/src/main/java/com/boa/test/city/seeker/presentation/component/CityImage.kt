package com.boa.test.city.seeker.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.boa.test.city.seeker.presentation.ui.theme.PrimaryContainerLight
import com.boa.test.city.seeker.presentation.ui.theme.PrimaryDark
import com.boa.test.city.seeker.presentation.ui.theme.PrimaryLight

@Composable
fun CityImage(
    imageUrl: String,
    cityName: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    val initials =
        cityName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }

    if (imageUrl.isNotBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = cityName,
            modifier =
                modifier
                    .size(size)
                    .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier =
                modifier
                    .size(size)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors = listOf(PrimaryLight, PrimaryDark, PrimaryContainerLight),
                            ),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}
