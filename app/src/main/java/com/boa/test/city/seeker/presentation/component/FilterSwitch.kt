package com.boa.test.city.seeker.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boa.test.city.seeker.R

@Composable
fun FilterSwitch(
    showFavorites: Boolean,
    onShowFavoritesChanged: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.all_cities),
            modifier = Modifier.padding(start = 8.dp)
        )
        Switch(
            checked = showFavorites,
            onCheckedChange = onShowFavoritesChanged,
            modifier = Modifier.padding(horizontal = 8.dp)
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
        showFavorites = false,
        onShowFavoritesChanged = {}
    )
}

@Preview(showBackground = true)
@Composable
fun FilterSwitchPreview() {
    FilterSwitch(
        showFavorites = true,
        onShowFavoritesChanged = {}
    )
}