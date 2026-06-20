package com.boa.test.city.seeker.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boa.test.city.seeker.R

enum class ErrorType(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val defaultMessage: Int,
) {
    Network(
        icon = Icons.Default.CloudOff,
        defaultMessage = R.string.offline_mode,
    ),
    NotFound(
        icon = Icons.Default.SearchOff,
        defaultMessage = R.string.no_results,
    ),
    Generic(
        icon = Icons.Default.ErrorOutline,
        defaultMessage = R.string.retry,
    ),
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    errorType: ErrorType = ErrorType.Generic,
    secondaryAction: (@Composable () -> Unit)? = null,
) {
    val descriptionRes =
        when (errorType) {
            ErrorType.Network -> R.string.offline_mode
            ErrorType.NotFound -> R.string.try_different_search
            ErrorType.Generic -> R.string.retry
        }

    EmptyState(
        title = message,
        message = stringResource(descriptionRes),
        icon = errorType.icon,
        modifier = modifier,
        actions = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onRetry) {
                    Text(text = stringResource(R.string.retry))
                }
                secondaryAction?.invoke()
            }
        },
    )
}
