package com.boa.test.city.seeker.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boa.test.city.seeker.R
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
    onShowFavoritesChanged: (Boolean) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.all_cities),
            modifier = Modifier.padding(start = 8.dp)
        )
        Switch(
            checked = isShowingFavorites,
            onCheckedChange = onShowFavoritesChanged,
            modifier = Modifier.padding(horizontal = 8.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryDark,
                uncheckedThumbColor = PrimaryDark,
                checkedTrackColor = PrimaryLight,
                uncheckedTrackColor = PrimaryLight,
                checkedBorderColor = PrimaryOffDark,
                uncheckedBorderColor = PrimaryOffDark
            )
        )
        Text(
            text = stringResource(R.string.fav_cities),
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FilterAllSwitchPreview() {
    FilterSwitch(
        isShowingFavorites = false,
        onShowFavoritesChanged = {}
    )
}

@Preview(showBackground = true)
@Composable
fun FilterSwitchPreview() {
    FilterSwitch(
        isShowingFavorites = true,
        onShowFavoritesChanged = {}
    )
}
