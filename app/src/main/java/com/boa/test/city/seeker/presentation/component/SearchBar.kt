package com.boa.test.city.seeker.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boa.test.city.seeker.R

/**
 * A composable function that renders a search bar with a text field and a search icon.
 *
 * This search bar allows users to input text, which is then propagated to the `onSearchQueryChanged` callback.
 * It features a search icon, a placeholder label, and a styled appearance with a rounded shape and background color.
 *
 * @param searchQuery The current text in the search bar.
 * @param onSearchQueryChanged A callback function invoked when the search query changes.
 *                             It receives the new search query as a `String`.
 *
 * Example Usage:
 * ```
 * var mySearchQuery by remember { mutableStateOf("") }
 * SearchBar(
 *     searchQuery = mySearchQuery,
 *     onSearchQueryChanged = { newQuery ->
 *         mySearchQuery = newQuery
 *         // Perform search or other actions with the new query here.
 *     }
 * )
 * ```
 */
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit
) {
    TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        label = { Text(stringResource(R.string.search_cities)) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray)
            .padding(8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    SearchBar(
        searchQuery = "",
        onSearchQueryChanged = {}
    )
}
