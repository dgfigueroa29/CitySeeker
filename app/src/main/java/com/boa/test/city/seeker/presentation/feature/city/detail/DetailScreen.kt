package com.boa.test.city.seeker.presentation.feature.city.detail

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.boa.test.city.seeker.R
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.presentation.navigation.Screen
import com.boa.test.city.seeker.presentation.component.CityImage
import com.boa.test.city.seeker.presentation.ui.theme.LocalAnimatedVisibilityScope
import com.boa.test.city.seeker.presentation.ui.theme.LocalSharedTransitionScope
import com.boa.test.city.seeker.presentation.component.CityScatterPlot
import com.boa.test.city.seeker.presentation.component.ErrorState
import com.boa.test.city.seeker.presentation.component.LoadingIndicator
import com.boa.test.city.seeker.presentation.component.SuccessSnackbar
import com.boa.test.city.seeker.presentation.component.isLandscape
import com.boa.test.city.seeker.presentation.feature.city.CityItem
import com.boa.test.city.seeker.presentation.feature.city.list.FavoriteEvent
import com.boa.test.city.seeker.presentation.feature.journal.JournalEntryDialog
import com.boa.test.city.seeker.common.map.Map3DConfiguration
import com.boa.test.city.seeker.presentation.ui.theme.STRING_PRIMARY_DARK
import com.boa.test.city.seeker.presentation.ui.theme.STRING_WHITE_COLOR
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.scalebar.scalebar
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private const val MAP_DEFAULT_ZOOM = 13.0

private enum class DetailContentState { Loading, Error, Content }

@Composable
fun DetailScreen(
    navController: NavHostController? = null,
    viewModel: DetailViewModel = hiltViewModel(),
    cityId: String? = "0",
) {
    val loadingState = viewModel.detailState.loadingState.collectAsState()
    val errorState = viewModel.detailState.errorState.collectAsState()
    val city = viewModel.detailState.city.collectAsState()
    val id = cityId?.toLongOrNull() ?: 0L
    val isLoading = loadingState.value
    val isOffline = errorState.value.isNotBlank()
    val isOnline = rememberIsOnline()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val addedText = stringResource(R.string.added_to_favorites)
    val removedText = stringResource(R.string.removed_from_favorites)
    val undoText = stringResource(R.string.undo)

    LaunchedEffect(id) {
        viewModel.getCity(cityId = id)
    }

    LaunchedEffect(Unit) {
        viewModel.favoriteEvents.collect { event ->
            val message =
                when (event) {
                    FavoriteEvent.Added -> addedText
                    FavoriteEvent.Removed -> removedText
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
                    viewModel.toggleFavorite(city.value.id.toString())
                }
            }
        }
    }

    val contentState by remember(isLoading, isOffline, city.value.id) {
        derivedStateOf {
            when {
                isOffline -> DetailContentState.Error
                isLoading || city.value.id == 0L -> DetailContentState.Loading
                else -> DetailContentState.Content
            }
        }
    }

    AnimatedContent(
        targetState = contentState,
        transitionSpec = {
            fadeIn() + slideInVertically() togetherWith
                fadeOut() + slideOutVertically()
        },
        label = "detail_content",
    ) { state ->
        when (state) {
            DetailContentState.Loading -> LoadingIndicator(isLoading = true)
            DetailContentState.Error ->
                ErrorState(
                    message = errorState.value,
                    onRetry = { viewModel.getCity(cityId = id) },
                )

            DetailContentState.Content -> {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            SuccessSnackbar(snackbarData = data)
                        }
                    },
                ) { paddingValues ->
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            val detailImageModifier = run {
                                val scope = LocalSharedTransitionScope.current
                                val animScope = LocalAnimatedVisibilityScope.current
                                if (scope != null && animScope != null) {
                                    with(scope) {
                                        Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .sharedElement(
                                                sharedContentState = rememberSharedContentState(key = city.value.id.toString()),
                                                animatedVisibilityScope = animScope,
                                            )
                                    }
                                } else {
                                    Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                }
                            }
                            CityImage(
                                imageUrl = city.value.imageUrl,
                                cityName = city.value.name,
                                size = 200.dp,
                                modifier = detailImageModifier,
                            )
                            DetailHeader(
                                city = city.value,
                                navController = navController,
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                            )
                        }
                        val point = Point.fromLngLat(city.value.longitude, city.value.latitude)
                        if (isOnline) {
                            MapContent(point)
                        } else {
                            OfflineMapFallback(city.value)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineMapFallback(city: CityModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        CityScatterPlot(
            selectedCity = city,
            cities = listOf(city),
            onCityClick = {},
            modifier = Modifier.fillMaxSize(),
        )
        Text(
            text = stringResource(R.string.map_offline_notice),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
        )
    }
}

@Composable
private fun MapContent(point: Point) {
    val cameraOptions =
        CameraOptions
            .Builder()
            .center(point)
            .zoom(MAP_DEFAULT_ZOOM)
            .build()
    val context = LocalContext.current
    val mapView =
        remember {
            MapboxOptions.accessToken = com.boa.test.city.seeker.BuildConfig.MAPBOX_TOKEN
            MapView(context, MapInitOptions(context))
        }

    val mapStyle = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Style.DARK else Style.LIGHT
    val mapDescription = stringResource(R.string.map_view)

    Row {
        AndroidView(
            factory = { mapView },
            modifier =
                Modifier
                    .wrapContentSize()
                    .semantics { contentDescription = mapDescription },
            update = { mapView ->
                @Suppress("DEPRECATION")
                mapView.mapboxMap.loadStyleUri(mapStyle) {
                    mapView.scalebar.enabled = false
                    mapView.mapboxMap.setCamera(cameraOptions)
                    Map3DConfiguration.apply()
                    val annotationApi = mapView.annotations
                    val circleAnnotationManager = annotationApi.createCircleAnnotationManager(AnnotationConfig())
                    val circleAnnotationOptions =
                        CircleAnnotationOptions()
                            .withPoint(point)
                            .withCircleRadius(8.0)
                            .withCircleColor(STRING_PRIMARY_DARK)
                            .withCircleStrokeWidth(2.0)
                            .withCircleStrokeColor(STRING_WHITE_COLOR)
                    circleAnnotationManager.create(circleAnnotationOptions)
                }
            },
        )
    }
}

@Composable
private fun DetailHeader(
    city: CityModel,
    navController: NavHostController?,
    onToggleFavorite: (String) -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var showJournalDialog by remember { mutableStateOf(false) }
    val isOnline = rememberIsOnline()

    if (!isLandscape()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CityItem(
                city = city,
                canGoBack = true,
                onFavoriteClick = onToggleFavorite,
                onCityClick = { navController?.popBackStack() },
                modifier = Modifier.weight(1f),
            )
            if (isOnline) {
                IconButton(
                    onClick = { navController?.navigate("${Screen.AR.endpoint}/${city.id}") },
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = stringResource(R.string.ar_explore_title),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            IconButton(onClick = { showJournalDialog = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "Add journal entry",
                    tint = MaterialTheme.colorScheme.tertiary,
                )
            }
            ShareButton(context, city)
        }
    }

    if (showJournalDialog) {
        JournalEntryDialog(
            onDismiss = { showJournalDialog = false },
            onSave = { title, notes, rating, photoUri ->
                viewModel.addJournalEntry(title, notes, rating, photoUri)
                showJournalDialog = false
            },
        )
    }
}

@Composable
private fun ShareButton(
    context: android.content.Context,
    city: CityModel,
) {
    val shareLabel = stringResource(R.string.share_city)
    val discoverText = stringResource(R.string.share_city_discover)
    val shareText =
        remember(city, discoverText) {
            buildString {
                appendLine("📍 ${city.getTitle()}")
                appendLine()
                appendLine(city.getSubtitle())
                appendLine()
                appendLine(discoverText)
                append("cityseeker://city/${city.id}")
            }
        }
    val scope = rememberCoroutineScope()

    androidx.compose.material3.IconButton(
        onClick = {
            scope.launch {
                val imageUri = shareCityImage(context, city.imageUrl)
                context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = if (imageUri != null) "image/*" else "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            imageUri?.let { putExtra(Intent.EXTRA_STREAM, it) }
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        },
                        shareLabel,
                    ),
                )
            }
        },
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = shareLabel,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

private suspend fun shareCityImage(
    context: Context,
    imageUrl: String,
): Uri? {
    if (imageUrl.isBlank()) return null
    return withContext(Dispatchers.IO) {
        try {
            val loader = context.imageLoader
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()
            val result = loader.execute(request)
            if (result is SuccessResult) {
                val bitmap = result.drawable.toBitmap()
                val cacheDir = File(context.cacheDir, "shared")
                cacheDir.mkdirs()
                val file = File(cacheDir, "city_share_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else null
        } catch (_: Exception) {
            null
        }
    }
}

@Composable
private fun rememberIsOnline(): Boolean {
    val context = LocalContext.current
    return remember {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(network)
        caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
