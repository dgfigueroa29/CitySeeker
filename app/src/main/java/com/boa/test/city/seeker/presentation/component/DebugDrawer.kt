package com.boa.test.city.seeker.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.boa.test.city.seeker.presentation.ui.theme.Dimens
import com.boa.test.city.seeker.presentation.ui.theme.ThemeMode

@Composable
fun DebugDrawer(
    visible: Boolean,
    currentThemeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
                    .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(enabled = false) {},
                tonalElevation = Dimens.ElevationLevel3,
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier =
                        Modifier
                            .padding(Dimens.SpaceM)
                            .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = "🔧 Debug",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpaceM))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(Dimens.SpaceS))

                    DebugToggle(
                        label = "Theme: ${currentThemeMode.name}",
                        checked = currentThemeMode != ThemeMode.System,
                        onCheckedChange = {
                            val next =
                                when (currentThemeMode) {
                                    ThemeMode.System -> ThemeMode.Dark
                                    ThemeMode.Dark -> ThemeMode.Light
                                    ThemeMode.Light -> ThemeMode.System
                                }
                            onThemeModeChanged(next)
                        },
                    )

                    Spacer(modifier = Modifier.height(Dimens.SpaceM))

                    Text(
                        text = "Providers active: Timber ✓  Sentry ${if (isSentryConfigured()) "✓" else "✗"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun DebugToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(Dimens.SpaceM))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun isSentryConfigured(): Boolean =
    try {
        io.sentry.Sentry.isEnabled()
    } catch (_: Exception) {
        false
    }
