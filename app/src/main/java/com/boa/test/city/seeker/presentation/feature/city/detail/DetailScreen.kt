package com.boa.test.city.seeker.presentation.feature.city.detail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.boa.test.city.seeker.R
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.presentation.component.LoadingIndicator
import com.boa.test.city.seeker.presentation.component.OfflineIndicator
import com.boa.test.city.seeker.presentation.component.isLandscape
import com.boa.test.city.seeker.presentation.feature.city.CityItem
import com.boa.test.city.seeker.presentation.ui.theme.stringPrimaryDark
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

private const val MAP_DEFAULT_ZOOM = 9.0

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
    val markerResourceId by remember {
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
            MapContent(markerResourceId, point, city.getTitle())
        }
    }
}

@Composable
private fun MapContent(
    markerResourceId: Int,
    point: Point,
    title: String
) {
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
                mapView.mapboxMap.loadStyleUri(mapStyle) {
                    mapView.getMapboxMap().setCamera(cameraOptions)
                    val annotationApi = mapView.annotations
                    val pointAnnotationManager = annotationApi.createPointAnnotationManager()

                    val bitmap = drawableToBitmap(
                        context,
                        markerResourceId
                    )

                    val pointAnnotationOptions = PointAnnotationOptions()
                        .withPoint(point)
                        .withIconImage(bitmap)
                        .withTextField(title)
                        .withTextAnchor(TextAnchor.TOP)
                        .withTextColor(stringPrimaryDark)
                        .withIconColor(stringPrimaryDark)
                        .withTextOffset(listOf(0.0, -2.0))

                    pointAnnotationManager.create(pointAnnotationOptions)
                }
            }
        )
    }
}

private fun drawableToBitmap(context: Context, drawableId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(context, drawableId) ?: return Bitmap.createBitmap(
        1,
        1,
        Bitmap.Config.ARGB_8888
    )

    val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 100
    val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 100

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
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
