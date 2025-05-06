package com.boa.test.city.seeker.presentation.feature.city

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boa.test.city.seeker.R
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.presentation.ui.previewCities
import com.boa.test.city.seeker.presentation.ui.theme.PrimaryDark
import com.boa.test.city.seeker.presentation.ui.theme.PrimaryLight


/**
 * A composable function that displays a single city item
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityItem(
    city: CityModel,
    onCityClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onCityClick
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 0.dp else 2.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = city.getTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = city.getSubtitle(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (city.isFavorite) {
                        Icons.Filled.Star
                    } else {
                        Icons.Outlined.Star
                    },
                    contentDescription = if (city.isFavorite) {
                        stringResource(R.string.favorite_selected)
                    } else {
                        stringResource(
                            R.string.favorite_unselected
                        )
                    },
                    tint = if (city.isFavorite) {
                        PrimaryDark
                    } else {
                        PrimaryLight
                    },
                    modifier = Modifier.scale(
                        animateFloatAsState(
                            targetValue = if (city.isFavorite) {
                                1.2f
                            } else {
                                1f
                            },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ).value
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FavoriteCityItemPreview() {
    CityItem(
        city = previewCities()[0],
        onCityClick = { },
        onFavoriteClick = { },
    )
}

@Preview(showBackground = true)
@Composable
fun CityItemPreview() {
    CityItem(
        city = previewCities()[1],
        onCityClick = { },
        onFavoriteClick = { },
    )
}
