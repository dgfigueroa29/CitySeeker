package com.boa.test.city.seeker.presentation.feature.city.list

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.boa.test.city.seeker.R
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.presentation.component.FilterSwitch
import com.boa.test.city.seeker.presentation.component.LoadingIndicator
import com.boa.test.city.seeker.presentation.component.OfflineIndicator
import com.boa.test.city.seeker.presentation.component.SearchBar
import com.boa.test.city.seeker.presentation.feature.city.CityItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Composable function that displays the main list screen of the application.
 *
 * This screen is responsible for:
 * - Observing and displaying the loading state.
 * - Observing and displaying any error states (e.g., offline).
 * - Triggering the initial data load.
 * - Delegating the display of the actual list content to [ListStateful] when data is available
 * and there are no errors.
 *
 * @param viewModel The [ListViewModel] instance used to manage the state and logic of this screen.
 *                  It is typically provided by Hilt.
 * @param onCityClick A callback function that is invoked when a city item in the list is clicked.
 *                    It receives the ID of the clicked city as a [String].
 */
@Composable
fun ListScreen(
    viewModel: ListViewModel = hiltViewModel(),
    onCityClick: (String) -> Unit
) {
    val loadingState = viewModel.listState.loadingState.collectAsState()
    val errorState = viewModel.listState.errorState.collectAsState()

    // Show loading indicator while fetching data
    val isLoading = loadingState.value
    LoadingIndicator(isLoading)

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    // Display error dialog if needed
    val isOffline = errorState.value.isNotBlank()
    OfflineIndicator(isOffline)

    if (!isOffline && !isLoading) {
        ListStateful(
            listState = viewModel.listState,
            onSearchQueryChanged = {
                viewModel.refreshQuery(it)
            },
            onShowFavoritesChanged = { favoriteFilter, searchQuery ->
                viewModel.refreshFavoriteFilter(favoriteFilter, searchQuery)
            },
            onCityClick = onCityClick,
            onToggleFavorite = {
                viewModel.toggleFavorite(it)
            })
    }
}

/**
 * A stateful composable function that displays the list of cities.
 *
 * This composable is responsible for:
 * - Observing the list of cities, search query, and favorite filter state from [listState].
 * - Providing UI elements for searching and filtering (delegated to [ListHeader]).
 * - Displaying the list of cities using a [LazyColumn].
 * - Handling user interactions such as clicking on a city or toggling its favorite status.
 * - Displaying a "scroll to top" button (delegated to [ListFooter]).
 * - Debouncing search query changes to avoid excessive updates.
 *
 * @param listState The [ListState] object containing the current state of the list (cities, query,
 * filter).
 * @param onSearchQueryChanged A callback function invoked when the search query changes.
 *                             It receives the new search query as a [String].
 * @param onShowFavoritesChanged A callback function invoked when the favorite filter is changed.
 *                               It receives a [Boolean] indicating whether to show only favorites
 *                               and the current search query as a [String].
 * @param onCityClick A callback function invoked when a city item is clicked.
 *                    It receives the ID of the clicked city as a [String].
 * @param onToggleFavorite A callback function invoked when the favorite icon of a city is clicked.
 *                         It receives the ID of the city whose favorite status is to be toggled as
 *                         a [String].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListStateful(
    listState: ListState,
    onSearchQueryChanged: (String) -> Unit,
    onShowFavoritesChanged: (Boolean, String) -> Unit,
    onCityClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    val cities = listState.cityList.collectAsState().value
    val query by listState.queryState.collectAsState()
    val isShowingFavorites by listState.favoriteFilterState.collectAsState()
    var favoriteFilter by remember { mutableStateOf(isShowingFavorites) }
    var searchQuery by remember { mutableStateOf(query) }

    LaunchedEffect(searchQuery) {
        delay(200)
        onSearchQueryChanged(searchQuery)
    }

    val listState = rememberLazyListState()

    LaunchedEffect(favoriteFilter) {
        onShowFavoritesChanged(favoriteFilter, searchQuery)
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column {
                ListHeader(
                    isShowingFavorites = isShowingFavorites,
                    onShowFavoritesChanged = {
                        favoriteFilter = it
                    },
                    searchQuery = searchQuery,
                    cities = cities
                ) { query ->
                    searchQuery = query
                }

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(cities.size, key = { index -> cities[index].id }) { index ->
                        val city = cities[index]
                        CityItem(
                            city = city,
                            onCityClick = {
                                onCityClick(city.id.toString())
                            },
                            onFavoriteClick = onToggleFavorite
                        )
                    }
                }
            }

            ListFooter(listState)
        }
    }
}

/**
 * Composable function that displays a Floating Action Button (FAB) to scroll to the top of the
 * list.
 *
 * This FAB is only visible when:
 * - The list is not currently being scrolled.
 * - The first visible item in the list is not the first item (index > 0).
 *
 * The FAB has an animation for appearing (scale in and fade in) and disappearing (scale out and
 * fade out).
 * When clicked, it smoothly scrolls the list to the top.
 *
 * @param listState The [LazyListState] of the list to be controlled.
 */
@Composable
@SuppressLint("FrequentlyChangedStateReadInComposition")
private fun BoxScope.ListFooter(listState: LazyListState) {
    val coroutineScope = rememberCoroutineScope()
    AnimatedVisibility(
        visible = !listState.isScrollInProgress && listState.firstVisibleItemIndex > 0,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp),
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    listState.scrollToItem(0)
                    listState.animateScrollToItem(0)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = stringResource(R.string.back_to_top),
            )
        }
    }
}

/**
 * Composable function that displays the header section of the city list.
 *
 * This header includes:
 * - The application name.
 * - A [FilterSwitch] to toggle between showing all cities and only favorite cities.
 * - A [SearchBar] to allow users to search for cities.
 * - An animated [NoResultsFound] message that appears when the search query is not empty and
 *   the list of cities is empty.
 *
 * @param isShowingFavorites A boolean indicating whether the favorite filter is currently active.
 * @param onShowFavoritesChanged A callback function that is invoked when the state of the favorite
 * filter changes.
 *                               It receives a boolean value: `true` if favorites should be shown,
 *                               `false` otherwise.
 * @param searchQuery The current text entered in the search bar.
 * @param cities The current list of [CityModel] objects to be displayed or filtered.
 * @param onSearchQueryChanged A callback function that is invoked when the search query text
 * changes.
 *                             It receives the new search query as a [String].
 */
@Composable
private fun ListHeader(
    isShowingFavorites: Boolean,
    onShowFavoritesChanged: (Boolean) -> Unit,
    searchQuery: String,
    cities: List<CityModel>,
    onSearchQueryChanged: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .weight(0.5f)
                .padding(horizontal = 16.dp)
        )
        FilterSwitch(
            isShowingFavorites = isShowingFavorites,
            onShowFavoritesChanged = onShowFavoritesChanged,
            modifier = Modifier
                .weight(0.5f)
                .padding(horizontal = 8.dp)
        )
    }
    SearchBar(
        searchQuery = searchQuery,
        onSearchQueryChanged = onSearchQueryChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
    AnimatedVisibility(
        visible = cities.isEmpty() && searchQuery.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        NoResultsFound()
    }
}

/**
 * Composable function that displays a message indicating that no results were found for a search.
 *
 * This typically includes a search icon and a text message.
 * It is displayed when a search query yields no matching cities.
 */
@Composable
fun NoResultsFound() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_results),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Composable function for previewing the [ListStateful] composable with sample data.
 *
 * This preview utilizes a [PreviewParameterProvider] ([ListStatePreviewParameterProvider])
 * to supply different [ListState] instances, allowing for visualization of the list
 * in various states (e.g., with data, empty).
 *
 * It initializes a preview version of the [ListState] and then renders the [ListStateful]
 * composable with stubbed callback functions.
 *
 * @param state The [ListState] instance provided by the [ListStatePreviewParameterProvider].
 *              This state contains the data and configuration for the list preview.
 */
@Composable
@Preview(name = "List", showSystemUi = true, showBackground = true)
@Suppress("UnusedPrivateMember")
private fun ListScreenPreview(
    @PreviewParameter(ListStatePreviewParameterProvider::class)
    state: ListState
) {
    val statePreview = state
    statePreview.previewList()
    ListStateful(
        listState = statePreview,
        onSearchQueryChanged = { },
        onShowFavoritesChanged = { _, _ -> },
        onCityClick = { },
        onToggleFavorite = { }
    )
}

/**
 * Preview composable function for displaying the list screen in an empty state.
 *
 * This preview utilizes a [ListStatePreviewParameterProvider] to inject a [ListState]
 * instance representing an empty list. It then renders the [ListStateful] composable
 * with this empty state, allowing for visual inspection of how the UI appears when no
 * cities are available or match the current search criteria.
 *
 * The `@Preview` annotation configures the preview environment, setting its name,
 * enabling the system UI, and showing a background for better context.
 *
 * The `@Suppress("UnusedPrivateMember")` annotation is used because this preview function
 * is private and only intended for use by the Android Studio Preview tool.
 *
 * @param state The [ListState] instance provided by the [ListStatePreviewParameterProvider].
 *              This state will typically represent an empty list or a scenario where
 *              no search results are found.
 */
@Composable
@Preview(name = "ListEmpty", showSystemUi = true, showBackground = true)
@Suppress("UnusedPrivateMember")
private fun ListEmptyScreenPreview(
    @PreviewParameter(ListStatePreviewParameterProvider::class)
    state: ListState
) {
    ListStateful(
        listState = state,
        onSearchQueryChanged = { },
        onShowFavoritesChanged = { _, _ -> },
        onCityClick = { },
        onToggleFavorite = { }
    )
}

/**
 * PreviewParameter Provider for ListScreen Preview
 * Add values to the sequence to see the preview in different states
 **/
class ListStatePreviewParameterProvider : PreviewParameterProvider<ListState> {
    override val values: Sequence<ListState>
        get() = sequenceOf(
            ListState(),
        )
}

@Preview(name = "EmptyState", showSystemUi = true, showBackground = true)
@Composable
@Suppress("UnusedPrivateMember")
private fun EmptyState() {
    NoResultsFound()
}
