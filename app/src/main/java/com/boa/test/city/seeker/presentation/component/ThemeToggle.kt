package com.boa.test.city.seeker.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boa.test.city.seeker.R
import com.boa.test.city.seeker.presentation.ui.theme.Dimens
import com.boa.test.city.seeker.presentation.ui.theme.ThemeMode

@Composable
fun ThemeToggle(
    currentMode: ThemeMode,
    onModeChanged: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon =
        when (currentMode) {
            ThemeMode.System -> Icons.Outlined.DarkMode
            ThemeMode.Dark -> Icons.Outlined.LightMode
            ThemeMode.Light -> Icons.Outlined.DarkMode
        }
    val contentDescription =
        stringResource(
            when (currentMode) {
                ThemeMode.System -> R.string.theme_system
                ThemeMode.Dark -> R.string.theme_light
                ThemeMode.Light -> R.string.theme_dark
            },
        )

    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier =
            modifier
                .padding(Dimens.SpaceXS)
                .clickable {
                    val next =
                        when (currentMode) {
                            ThemeMode.System -> ThemeMode.Dark
                            ThemeMode.Dark -> ThemeMode.Light
                            ThemeMode.Light -> ThemeMode.System
                        }
                    onModeChanged(next)
                },
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
