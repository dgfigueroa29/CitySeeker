package com.boa.test.city.seeker.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boa.test.city.seeker.R

/**
 * A composable function that displays a search bar.
 *
 * This search bar includes a text field for inputting search queries,
 * a leading search icon, a trailing clear icon (when text is present),
 * and supports keyboard search actions.
 *
 * @param searchQuery The current text in the search bar.
 * @param onSearchQueryChanged A callback that is invoked when the text in the search bar changes.
 *                             It provides the new text as a parameter.
 */
@Composable
fun SearchBar(
    searchQuery: String = "",
    onSearchQueryChanged: (String) -> Unit
) {
    val keyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Search,
        keyboardType = KeyboardType.Text,
    )
    OutlinedTextField(
        value = searchQuery,
        singleLine = true,
        enabled = true,
        maxLines = 1,
        onValueChange = { onSearchQueryChanged(it) },
        label = { Text(text = stringResource(R.string.search_cities)) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
        },
        trailingIcon = {
            SearchBarTrailingIcon(searchQuery) { onSearchQueryChanged("") }
        },
        keyboardOptions = keyboardOptions,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .padding(8.dp)
    )
}

/**
 * Composable function that displays the trailing icon in a search bar.
 *
 * This function shows a clear icon (an 'x') when there is text in the search bar.
 * Clicking the clear icon will trigger the provided [clearText] callback to remove
 * the current text. If the search bar text is empty, no icon will be displayed.
 *
 * @param text The current text in the search bar. Used to determine if the clear icon
 * should be displayed.
 * @param clearText A lambda function to be executed when the clear icon is clicked.
 * This function should handle clearing the text in the search bar.
 */
@Composable
private fun SearchBarTrailingIcon(text: String, clearText: () -> Unit) {
    Row(
        Modifier.padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (text.isNotEmpty()) {
            IconButton(onClick = { clearText.invoke() }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.clear_search)
                )
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun SearchBarEmptyPreview() {
    SearchBar(
        searchQuery = "",
        onSearchQueryChanged = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    SearchBar(
        searchQuery = "Test",
        onSearchQueryChanged = {}
    )
}
