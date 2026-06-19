package com.boa.test.city.seeker.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.boa.test.city.seeker.R
import com.boa.test.city.seeker.presentation.ui.theme.Dimens
import com.boa.test.city.seeker.presentation.ui.theme.PrimaryDark
import com.boa.test.city.seeker.presentation.ui.theme.PrimaryLight
import com.boa.test.city.seeker.presentation.ui.theme.PrimaryOffDark

/**
 * A composable function that displays a switch to toggle between showing all cities and favorite cities.
 *
 * @param isShowingFavorites A boolean indicating whether to show only favorite cities (true) or all cities (false).
 * @param onShowFavoritesChanged A callback function that is invoked when the switch state changes.
 *                                It provides the new boolean value indicating whether to show favorites.
 *                                This should be used to update the state of the parent composable.
 */
@Composable
fun FilterSwitch(
    modifier: Modifier = Modifier,
    isShowingFavorites: Boolean,
    favoriteCount: Int = 0,
    onShowFavoritesChanged: (Boolean) -> Unit,
) {
    val switchDescription =
        if (isShowingFavorites) {
            stringResource(R.string.favorite_filter_active)
        } else {
            stringResource(R.string.showing_all_cities)
        }

    Row(
        modifier =
            modifier.semantics {
                contentDescription = switchDescription
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.all_cities),
            modifier = Modifier.padding(start = Dimens.SpaceS),
        )
        Switch(
            checked = isShowingFavorites,
            onCheckedChange = onShowFavoritesChanged,
            modifier = Modifier.padding(horizontal = Dimens.SpaceS),
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = PrimaryDark,
                    uncheckedThumbColor = PrimaryDark,
                    checkedTrackColor = PrimaryLight,
                    uncheckedTrackColor = PrimaryLight,
                    checkedBorderColor = PrimaryOffDark,
                    uncheckedBorderColor = PrimaryOffDark,
                ),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.fav_cities),
                modifier = Modifier.padding(end = Dimens.SpaceXS),
            )
            AnimatedVisibility(
                visible = isShowingFavorites && favoriteCount > 0,
                enter = scaleIn(animationSpec = spring(dampingRatio = 0.5f)),
                exit = scaleOut(),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(Dimens.SpaceL)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = favoriteCount.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FilterAllSwitchPreview() {
    FilterSwitch(
        isShowingFavorites = false,
        onShowFavoritesChanged = {},
    )
}

@Preview(showBackground = true)
@Composable
fun FilterSwitchPreview() {
    FilterSwitch(
        isShowingFavorites = true,
        onShowFavoritesChanged = {},
    )
}
