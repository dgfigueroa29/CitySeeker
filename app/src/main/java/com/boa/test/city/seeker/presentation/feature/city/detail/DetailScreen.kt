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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.boa.test.city.seeker.R
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.presentation.component.LoadingIndicator
import com.boa.test.city.seeker.presentation.component.OfflineIndicator
import com.boa.test.city.seeker.presentation.component.isLandscape
import com.boa.test.city.seeker.presentation.feature.city.CityItem
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.style.GenericStyle
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions

private const val MAP_DEFAULT_ZOOM = 9.0
private const val DURATION = 3000L

@OptIn(MapboxExperimental::class)
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

    LaunchedEffect(Unit) {
        viewModel.getCity(cityId = id)
    }

    // Display error dialog if needed
    val isOffline = errorState.value.isNotBlank()
    OfflineIndicator(isOffline)
    val city = viewModel.detailState.city.collectAsState().value
    val point = Point.fromLngLat(city.longitude, city.latitude)
    val cameraOptions = CameraOptions.Builder()
        .center(point)
        .zoom(MAP_DEFAULT_ZOOM)
        .build()
    var markerResourceId by remember {
        mutableIntStateOf(R.drawable.pin_24)
    }
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
            MapContent(markerResourceId, point, cameraOptions)
        }
    }
}

@Composable
private fun MapContent(
    markerResourceId: Int,
    point: Point,
    cameraOptions: CameraOptions
) {
    Row {
        MapboxMap(
            modifier = Modifier.wrapContentSize(),
            mapViewportState = rememberMapViewportState {
                flyTo(
                    cameraOptions,
                    mapAnimationOptions { duration(DURATION) }
                )
            },
            style = {
                GenericStyle(
                    style = if (isSystemInDarkTheme()) {
                        Style.DARK
                    } else {
                        Style.LIGHT
                    }
                )
            },
            compass = {},
            scaleBar = {},
            attribution = {},
            logo = {}
        ) {
            val marker =
                rememberIconImage(
                    key = markerResourceId,
                    painter = painterResource(markerResourceId)
                )
            PointAnnotation(point = point) {
                iconImage = marker
            }
            cameraOptions {
                cameraOptions
            }
            mapAnimationOptions {
                duration(DURATION)
            }
        }
    }
}

@Composable
private fun MapHeader(
    city: CityModel,
    navController: NavHostController?,
    onToggleFavorite: (Long) -> Unit
) {
    if (!isLandscape()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            CityItem(
                city = city,
                canGoBack = true,
                onFavoriteClick = {
                    onToggleFavorite(city.id)
                },
                onCityClick = {
                    navController?.popBackStack()
                })
        }
    }
}
