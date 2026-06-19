package com.boa.test.city.seeker.presentation.feature.city

import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boa.test.city.seeker.R
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.presentation.component.CityImage
import com.boa.test.city.seeker.presentation.ui.previewCities
import com.boa.test.city.seeker.presentation.ui.theme.PrimaryDark
import com.boa.test.city.seeker.presentation.ui.theme.PrimaryLight
import com.boa.test.city.seeker.presentation.ui.theme.PrimaryOffDark
import com.boa.test.city.seeker.presentation.ui.theme.PrimaryOffLight
import com.boa.test.city.seeker.presentation.util.HapticFeedbackManager

private val ColorConverter =
    TwoWayConverter<Color, AnimationVector4D>(
        convertToVector = { color -> AnimationVector4D(color.red, color.green, color.blue, color.alpha) },
        convertFromVector = { vector -> Color(vector.v1, vector.v2, vector.v3, vector.v4) },
    )

/**
 * A composable function that displays a single city item
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityItem(
    modifier: Modifier = Modifier,
    city: CityModel,
    canGoBack: Boolean = false,
    onCityClick: () -> Unit,
    onFavoriteClick: (String) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
    )

    val favoriteState =
        if (city.isFavorite) {
            stringResource(R.string.favorite_selected)
        } else {
            stringResource(R.string.favorite_unselected)
        }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .scale(scale)
                .clip(RoundedCornerShape(16.dp))
                .semantics {
                    contentDescription = "${city.getTitle()}, ${city.getSubtitle()}"
                    stateDescription = favoriteState
                    customActions =
                        listOf(
                            CustomAccessibilityAction(favoriteState) {
                                onFavoriteClick(city.id.toString())
                                true
                            },
                        )
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onCityClick,
                ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (isPressed) 0.dp else 2.dp,
                pressedElevation = 0.dp,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp, start = 0.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackIcon(
                canGoBack = canGoBack,
                onClick = onCityClick,
            )
            CityImage(
                imageUrl = city.imageUrl,
                cityName = city.name,
                modifier = Modifier.padding(end = 12.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = city.getTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = city.getSubtitle(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FavoriteIcon(onFavoriteClick, city)
        }
    }
}

/**
 * A composable function that displays a back icon if the user can go back, otherwise it displays a
 * spacer.
 *
 * @param canGoBack A boolean value that indicates whether the user can go back or not.
 * @param onClick A lambda function that is called when the back icon is clicked.
 */
@Composable
private fun BackIcon(
    canGoBack: Boolean,
    onClick: () -> Unit,
) {
    if (canGoBack) {
        IconButton(
            onClick = onClick,
            modifier =
                Modifier
                    .size(48.dp)
                    .padding(4.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint =
                    if (isSystemInDarkTheme()) {
                        PrimaryOffLight
                    } else {
                        PrimaryOffDark
                    },
            )
        }
    } else {
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
private fun FavoriteIcon(
    onFavoriteClick: (String) -> Unit,
    city: CityModel,
) {
    val context = LocalContext.current
    val isFavorite = city.isFavorite

    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.2f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "favorite_scale",
    )

    val rotation by animateFloatAsState(
        targetValue = if (isFavorite) 360f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "favorite_rotation",
    )

    val color by animateValueAsState(
        targetValue = if (isFavorite) PrimaryDark else PrimaryLight,
        typeConverter = ColorConverter,
        animationSpec = tween(200),
        label = "favorite_color",
    )

    IconButton(
        onClick = {
            onFavoriteClick(city.id.toString())
            if (isFavorite) {
                HapticFeedbackManager.performReject(context)
            } else {
                HapticFeedbackManager.performConfirm(context)
            }
        },
        modifier =
            Modifier
                .size(48.dp)
                .padding(4.dp),
    ) {
        Icon(
            imageVector =
                if (isFavorite) {
                    Icons.Filled.Star
                } else {
                    Icons.Outlined.Star
                },
            contentDescription =
                if (isFavorite) {
                    stringResource(R.string.favorite_selected)
                } else {
                    stringResource(R.string.favorite_unselected)
                },
            tint = color,
            modifier =
                Modifier
                    .scale(scale)
                    .graphicsLayer { rotationZ = rotation },
        )
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

@Preview(showBackground = true)
@Composable
fun FavoriteCityDetailItemPreview() {
    CityItem(
        city = previewCities()[0],
        canGoBack = true,
        onCityClick = { },
        onFavoriteClick = { },
    )
}

@Preview(showBackground = true)
@Composable
fun CityItemDetailPreview() {
    CityItem(
        city = previewCities()[1],
        canGoBack = true,
        onCityClick = { },
        onFavoriteClick = { },
    )
}
