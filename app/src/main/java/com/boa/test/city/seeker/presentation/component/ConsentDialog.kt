package com.boa.test.city.seeker.presentation.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.boa.test.city.seeker.R

@Composable
fun ConsentDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDecline,
        title = { Text(text = stringResource(R.string.consent_title)) },
        text = { Text(text = stringResource(R.string.consent_body)) },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text(text = stringResource(R.string.consent_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(text = stringResource(R.string.consent_decline))
            }
        },
    )
}
