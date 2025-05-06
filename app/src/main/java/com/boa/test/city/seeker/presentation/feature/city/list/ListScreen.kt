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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.boa.test.city.seeker.R
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.presentation.component.ConnectivityStatus
import com.boa.test.city.seeker.presentation.component.FilterSwitch
import com.boa.test.city.seeker.presentation.component.LoadingIndicator
import com.boa.test.city.seeker.presentation.component.OfflineIndicator
import com.boa.test.city.seeker.presentation.component.SearchBar
import com.boa.test.city.seeker.presentation.feature.city.CityItem
import kotlinx.coroutines.delay

@Composable
fun ListScreen(
    viewModel: ListViewModel = hiltViewModel()
) {
    val loadingState = viewModel.listState.loadingState.collectAsStateWithLifecycle()
    val errorState = viewModel.listState.errorState.collectAsStateWithLifecycle()

    //Checking internet connection
    ConnectivityStatus { isConnected ->
        viewModel.updateConnectionStatus(isConnected)
    }

    //Show loading indicator while fetching data
    val isLoading = loadingState.value
    LoadingIndicator(isLoading)

    LaunchedEffect(Unit) {
        viewModel.getCities("")
    }

    //Display error dialog if needed
    val isOffline = errorState.value.isNotBlank()
    OfflineIndicator(isOffline)

    if (!isOffline && !isLoading) {
        ListStateful(
            listState = viewModel.listState,
            onSearchQueryChanged = {
                viewModel.refreshQuery(it)
            },
            onShowFavoritesChanged = {
                viewModel.refreshFavoriteFilter(it)
            })
    }
}

@SuppressLint("FrequentlyChangedStateReadInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListStateful(
    listState: ListState,
    onSearchQueryChanged: (String) -> Unit,
    onShowFavoritesChanged: (Boolean) -> Unit
) {
    val cities = listState.cityList.collectAsLazyPagingItems()
    val query by listState.queryState.collectAsStateWithLifecycle()
    val isShowingFavorites by listState.favoriteFilterState.collectAsState()
    var showDetailDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(query) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            delay(300)
            onSearchQueryChanged(searchQuery)
        }
    }

    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(0.5f)
                            .padding(horizontal = 16.dp)
                    )
                    FilterSwitch(
                        isShowingFavorites = isShowingFavorites,
                        onShowFavoritesChanged = onShowFavoritesChanged,
                        modifier = Modifier.weight(0.5f)
                            .padding(horizontal = 8.dp)
                    )
                }
                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChanged = { query ->
                        searchQuery = query
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                AnimatedVisibility(
                    visible = cities.itemCount == 0 && searchQuery.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    NoResultsFound()
                }

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(cities.itemCount) { index ->
                        val city = cities[index] ?: CityModel()
                        CityItem(
                            city = city,
                            onCityClick = {
                                showDetailDialog = true
                            },
                            onFavoriteClick = {  }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !listState.isScrollInProgress && listState.firstVisibleItemIndex > 0,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = stringResource(R.string.back_to_top),
                    )
                }
            }
        }
    }
}

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

@Composable
@Preview(name = "List", showSystemUi = true, showBackground = true)
private fun ListScreenPreview(
    @PreviewParameter(ListStatePreviewParameterProvider::class)
    state: ListState
) {
    val statePreview = state
    statePreview.previewList()
    ListStateful(
        listState = statePreview,
        onSearchQueryChanged = { },
        onShowFavoritesChanged = { }
    )
}

@Composable
@Preview(name = "ListEmpty", showSystemUi = true, showBackground = true)
private fun ListEmptyScreenPreview(
    @PreviewParameter(ListStatePreviewParameterProvider::class)
    state: ListState
) {
    ListStateful(
        listState = state,
        onSearchQueryChanged = { },
        onShowFavoritesChanged = { }
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
private fun EmptyState() {
    NoResultsFound()
}
