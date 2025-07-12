package com.boa.test.city.seeker.presentation.feature.city.detail

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.presentation.component.LoadingIndicator
import com.boa.test.city.seeker.presentation.component.OfflineIndicator
import com.boa.test.city.seeker.presentation.component.isLandscape
import com.boa.test.city.seeker.presentation.feature.city.CityItem
import com.boa.test.city.seeker.presentation.ui.theme.STRING_PRIMARY_DARK
import com.boa.test.city.seeker.presentation.ui.theme.STRING_WHITE_COLOR
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

private const val MAP_DEFAULT_ZOOM = 9.0

/**
 * Displays the detail screen for a specific city.
 *
 * This screen shows information about a city, including its location on a map.
 * It also allows the user to toggle the city as a favorite.
 *
 * @param navController The NavHostController for navigation.
 * @param viewModel The DetailViewModel for managing the screen's state.
 * @param cityId The ID of the city to display.
 */
@Composable
fun DetailScreen(
    navController: NavHostController? = null,
    viewModel: DetailViewModel = hiltViewModel(),
    cityId: String? = "0",
) {
    val loadingState = viewModel.detailState.loadingState.collectAsState()
    val errorState = viewModel.detailState.errorState.collectAsState()
    val id = cityId?.toLongOrNull() ?: 0L

    // Show loading indicator while fetching data
    val isLoading = loadingState.value
    LoadingIndicator(isLoading)

    LaunchedEffect(id) {
        viewModel.getCity(cityId = id)
    }

    // Display error dialog if needed
    val isOffline = errorState.value.isNotBlank()
    OfflineIndicator(isOffline)
    val city = viewModel.detailState.city.collectAsState().value
    val point = Point.fromLngLat(city.longitude, city.latitude)
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MapHeader(
                city = city,
                navController = navController,
                onToggleFavorite = {
                    viewModel.toggleFavorite(it)
                })
            MapContent(point)
        }
    }
}

/**
 * Displays the map with a marker at the given point.
 *
 * @param point The point to display on the map.
 */
@Composable
private fun MapContent(point: Point) {
    val cameraOptions = CameraOptions.Builder()
        .center(point)
        .zoom(MAP_DEFAULT_ZOOM)
        .build()
    val context = LocalContext.current
    val mapView = remember {
        MapView(context, MapInitOptions(context))
    }

    val mapStyle = if (isSystemInDarkTheme()) {
        Style.DARK
    } else {
        Style.LIGHT
    }

    Row {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.wrapContentSize(),
            update = { mapView ->
                @Suppress("DEPRECATION")
                mapView.mapboxMap.loadStyleUri(mapStyle) {
                    mapView.scalebar.enabled = false
                    mapView.mapboxMap.setCamera(cameraOptions)
                    val annotationApi = mapView.annotations
                    val circleAnnotationManager = annotationApi.createCircleAnnotationManager(
                        AnnotationConfig()
                    )
                    val circleAnnotationOptions: CircleAnnotationOptions = CircleAnnotationOptions()
                        .withPoint(point)
                        .withCircleRadius(8.0)
                        .withCircleColor(STRING_PRIMARY_DARK)
                        .withCircleStrokeWidth(2.0)
                        .withCircleStrokeColor(STRING_WHITE_COLOR)
                    circleAnnotationManager.create(circleAnnotationOptions)
                }
            }
        )
    }
}

/**
 * Displays the header for the map screen, which includes the city information and navigation
 * controls.
 * This header is only shown in portrait mode.
 *
 * @param city The [CityModel] to display information for.
 * @param navController The [NavHostController] for navigation.
 * @param onToggleFavorite A lambda function to be invoked when the favorite button is clicked.
 *                         It takes the city ID as a [String] parameter.
 */
@Composable
private fun MapHeader(
    city: CityModel,
    navController: NavHostController?,
    onToggleFavorite: (String) -> Unit
) {
    if (!isLandscape()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            CityItem(
                city = city,
                canGoBack = true,
                onFavoriteClick = onToggleFavorite,
                onCityClick = {
                    navController?.popBackStack()
                })
        }
    }
}
