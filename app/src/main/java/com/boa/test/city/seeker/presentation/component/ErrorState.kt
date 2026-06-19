package com.boa.test.city.seeker.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boa.test.city.seeker.R

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = message,
        message = "",
        icon = Icons.Default.ErrorOutline,
        actionText = stringResource(R.string.retry),
        onAction = onRetry,
        modifier = modifier,
    )
}
