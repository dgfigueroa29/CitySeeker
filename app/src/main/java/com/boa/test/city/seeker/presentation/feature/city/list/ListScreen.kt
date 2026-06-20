package com.boa.test.city.seeker.presentation.feature.city.list

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.boa.test.city.seeker.R
import com.boa.test.city.seeker.presentation.component.CityListSkeleton
import com.boa.test.city.seeker.presentation.component.DebugDrawer
import com.boa.test.city.seeker.presentation.component.EmptyState
import com.boa.test.city.seeker.presentation.component.ErrorState
import com.boa.test.city.seeker.presentation.component.ErrorType
import com.boa.test.city.seeker.presentation.component.FilterSwitch
import com.boa.test.city.seeker.presentation.component.SearchBar
import com.boa.test.city.seeker.presentation.component.SuccessSnackbar
import com.boa.test.city.seeker.presentation.component.ThemeToggle
import com.boa.test.city.seeker.presentation.feature.city.SwipeableCityItem
import com.boa.test.city.seeker.presentation.ui.theme.Dimens
import com.boa.test.city.seeker.presentation.ui.theme.LocalThemeMode
import com.boa.test.city.seeker.presentation.ui.theme.ShapeTokens
import com.boa.test.city.seeker.presentation.ui.theme.ThemeMode
import com.boa.test.city.seeker.presentation.util.Metrics
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

private enum class ListContentState {
    Loading,
    Offline,
    Content,
    Empty,
}

@Composable
fun ListScreen(
    viewModel: ListViewModel = hiltViewModel(),
    onCityClick: (String) -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit = {},
) {
    val loadingState = viewModel.listState.loadingState.collectAsState()
    val errorState = viewModel.listState.errorState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    val isLoading = loadingState.value
    val isOffline = errorState.value.isNotBlank()
    val cities =
        viewModel.listState.cityList
            .collectAsState()
            .value
    val query by viewModel.listState.queryState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDebug by remember { mutableStateOf(false) }
    val addedToFavorites = stringResource(R.string.added_to_favorites)
    val removedFromFavorites = stringResource(R.string.removed_from_favorites)
    val undoText = stringResource(R.string.undo)
    val noResultsTitle = stringResource(R.string.no_results)
    val tryDifferentSearch = stringResource(R.string.try_different_search)
    val clearSearch = stringResource(R.string.clear_search)

    LaunchedEffect(Unit) {
        snapshotFlow { viewModel.listState.queryState.value }
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .collect { queryValue ->
                Metrics.trackSearch(queryValue)
            }
    }

    LaunchedEffect(Unit) {
        viewModel.favoriteEvents.collect { event ->
            val message =
                when (event) {
                    FavoriteEvent.Added -> addedToFavorites
                    FavoriteEvent.Removed -> removedFromFavorites
                }
            val result =
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = undoText,
                        duration = SnackbarDuration.Short,
                    )
                }
            result.invokeOnCompletion {
                if (it == null) {
                    viewModel.toggleFavorite(
                        viewModel.listState.cityList.value
                            .firstOrNull { city ->
                                val wasAdded = event is FavoriteEvent.Added
                                city.isFavorite == wasAdded
                            }?.id
                            ?.toString() ?: return@invokeOnCompletion,
                    )
                }
            }
        }
    }

    val state by remember(isLoading, isOffline, cities, query) {
        derivedStateOf {
            when {
                isOffline -> ListContentState.Offline
                isLoading -> ListContentState.Loading
                cities.isEmpty() && query.isNotEmpty() -> ListContentState.Empty
                else -> ListContentState.Content
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                SuccessSnackbar(snackbarData = data)
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    fadeIn(tween(300)) + slideInVertically(tween(300)) togetherWith
                        fadeOut(tween(200)) + slideOutVertically(tween(200))
                },
                label = "content_transition",
            ) { contentState ->
                when (contentState) {
                    ListContentState.Loading -> CityListSkeleton()
                    ListContentState.Offline ->
                        ErrorState(
                            message = errorState.value,
                            onRetry = { viewModel.load() },
                            errorType = ErrorType.Network,
                        )

                    ListContentState.Empty ->
                        EmptyState(
                            title = noResultsTitle,
                            message = tryDifferentSearch,
                            icon = Icons.Default.Search,
                            actionText = clearSearch,
                            onAction = { viewModel.refreshQuery("") },
                        )

                    ListContentState.Content ->
                        ListStateful(
                            isLoading = isLoading,
                            listState = viewModel.listState,
                            onSearchQueryChanged = { viewModel.refreshQuery(it, debounce = true) },
                            onShowFavoritesChanged = { favoriteFilter, searchQuery ->
                                viewModel.refreshFavoriteFilter(favoriteFilter, searchQuery)
                            },
                            onCityClick = onCityClick,
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onRefresh = { viewModel.refresh() },
                            onThemeModeChanged = onThemeModeChanged,
                            onToggleDebug = { showDebug = !showDebug },
                        )
                }
            }

            DebugDrawer(
                visible = showDebug,
                currentThemeMode = LocalThemeMode.current,
                onThemeModeChanged = onThemeModeChanged,
                onDismiss = { showDebug = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListStateful(
    isLoading: Boolean,
    listState: ListState,
    onSearchQueryChanged: (String) -> Unit,
    onShowFavoritesChanged: (Boolean, String) -> Unit,
    onCityClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRefresh: () -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onToggleDebug: () -> Unit = {},
) {
    val cities = listState.cityList.collectAsState().value
    val query by listState.queryState.collectAsState()
    val isShowingFavorites by listState.favoriteFilterState.collectAsState()
    var favoriteFilter by remember { mutableStateOf(isShowingFavorites) }
    var searchQuery by remember { mutableStateOf(query) }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(searchQuery) {
        onSearchQueryChanged(searchQuery)
    }

    LaunchedEffect(favoriteFilter) {
        onShowFavoritesChanged(favoriteFilter, searchQuery)
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                Metrics.trackScrollDepth(index)
            }
    }

    val showScrollToTop by remember {
        derivedStateOf {
            !lazyListState.isScrollInProgress && lazyListState.firstVisibleItemIndex > 0
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val favoriteCount = cities.count { it.isFavorite }
    val currentThemeMode = LocalThemeMode.current

    val context = LocalContext.current
    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context)
    }
    var voiceSearchQuery by remember { mutableStateOf<String?>(null) }
    val voiceSearchHint = stringResource(R.string.voice_search_hint)

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                    )
                    putExtra(RecognizerIntent.EXTRA_PROMPT, voiceSearchHint)
                }
                speechRecognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onResults(results: Bundle) {
                        val matches = results.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )
                        if (!matches.isNullOrEmpty()) {
                            voiceSearchQuery = matches[0]
                        }
                    }
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {}
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
                speechRecognizer.startListening(intent)
            }
        },
    )

    val onVoiceSearch: () -> Unit = {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
                putExtra(RecognizerIntent.EXTRA_PROMPT, voiceSearchHint)
            }
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle) {
                    val matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )
                    if (!matches.isNullOrEmpty()) {
                        voiceSearchQuery = matches[0]
                    }
                }
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            speechRecognizer.startListening(intent)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(voiceSearchQuery) {
        voiceSearchQuery?.let { text ->
            searchQuery = text
            voiceSearchQuery = null
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) {
        LargeTopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.app_name),
                    modifier =
                        Modifier.pointerInput(Unit) {
                            detectTapGestures(onLongPress = { onToggleDebug() })
                        },
                )
            },
            actions = {
                ThemeToggle(
                    currentMode = currentThemeMode,
                    onModeChanged = onThemeModeChanged,
                )
                FilterSwitch(
                    isShowingFavorites = isShowingFavorites,
                    favoriteCount = favoriteCount,
                    onShowFavoritesChanged = { favoriteFilter = it },
                )
            },
            scrollBehavior = scrollBehavior,
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
        )

        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChanged = { searchQuery = it },
            onVoiceSearch = onVoiceSearch,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceXS),
        )

        Box(modifier = Modifier.weight(1f)) {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = onRefresh,
                state = pullToRefreshState,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    state = lazyListState,
                    contentPadding =
                        PaddingValues(
                            horizontal = Dimens.SpaceM,
                            vertical = Dimens.SpaceXS,
                        ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(cities.size, key = { index -> cities[index].id }) { index ->
                        SwipeableCityItem(
                            city = cities[index],
                            onCityClick = { onCityClick(cities[index].id.toString()) },
                            onToggleFavorite = onToggleFavorite,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }

            ListFooter(visible = showScrollToTop, listState = lazyListState)
        }
    }
}

@Composable
@SuppressLint("FrequentlyChangedStateReadInComposition")
private fun BoxScope.ListFooter(
    visible: Boolean,
    listState: LazyListState,
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollToTopDescription = stringResource(R.string.scroll_to_top)

    AnimatedVisibility(
        visible = visible,
        modifier =
            Modifier
                .align(Alignment.BottomEnd)
                .padding(Dimens.SpaceM),
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
    ) {
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    listState.scrollToItem(0)
                    listState.animateScrollToItem(0)
                }
            },
            modifier =
                Modifier.semantics {
                    contentDescription = scrollToTopDescription
                },
            shape = ShapeTokens.FAB,
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = stringResource(R.string.back_to_top),
            )
        }
    }
}

@Composable
@Preview(name = "List", showSystemUi = true, showBackground = true)
@Suppress("UnusedPrivateMember")
private fun ListScreenPreview(
    @PreviewParameter(ListStatePreviewParameterProvider::class)
    state: ListState,
) {
    val statePreview = state
    statePreview.previewList()
    ListStateful(
        isLoading = false,
        listState = statePreview,
        onSearchQueryChanged = { },
        onShowFavoritesChanged = { _, _ -> },
        onCityClick = { },
        onToggleFavorite = { },
        onRefresh = { },
        onThemeModeChanged = { },
    )
}

@Composable
@Preview(name = "ListEmpty", showSystemUi = true, showBackground = true)
@Suppress("UnusedPrivateMember")
private fun ListEmptyScreenPreview(
    @PreviewParameter(ListStatePreviewParameterProvider::class)
    state: ListState,
) {
    ListStateful(
        isLoading = false,
        listState = state,
        onSearchQueryChanged = { },
        onShowFavoritesChanged = { _, _ -> },
        onCityClick = { },
        onToggleFavorite = { },
        onRefresh = { },
        onThemeModeChanged = { },
    )
}

class ListStatePreviewParameterProvider : PreviewParameterProvider<ListState> {
    override val values: Sequence<ListState>
        get() = sequenceOf(ListState())
}
