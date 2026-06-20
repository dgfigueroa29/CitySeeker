package com.boa.test.city.seeker.presentation.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.boa.test.city.seeker.presentation.ui.theme.Dimens

@Composable
fun AnimatedFavoriteCount(
    count: Int,
    modifier: Modifier = Modifier,
) {
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        label = "favorite_count",
    )

    if (count > 0) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary,
        ) {
            Text(
                text = animatedCount.toString(),
                modifier = Modifier.padding(horizontal = Dimens.SpaceS, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
