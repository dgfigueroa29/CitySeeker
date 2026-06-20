package com.boa.test.city.seeker.presentation.feature.ar

import android.animation.ValueAnimator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.boa.test.city.seeker.R
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
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.scalebar.scalebar

private const val AR_ZOOM = 14.0
private const val ORBIT_DURATION_MS = 12000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArCityScreen(
    cityId: Long,
    onBack: () -> Unit,
    viewModel: ArCityViewModel = hiltViewModel(),
) {
    val city by viewModel.city.collectAsState()

    LaunchedEffect(cityId) {
        viewModel.loadCity(cityId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        city?.let { data ->
            ArMapView(point = Point.fromLngLat(data.longitude, data.latitude))

            TopAppBar(
                title = { Text(stringResource(R.string.ar_explore_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Explore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                            Text(
                                text = data.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        }
                        Text(
                            text = data.country,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 36.dp, top = 4.dp),
                        )
                        Text(
                            text = "${"%.4f".format(data.latitude)}, ${"%.4f".format(data.longitude)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 36.dp, top = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArMapView(point: Point) {
    val context = LocalContext.current
    val mapView = remember {
        MapboxOptions.accessToken = com.boa.test.city.seeker.BuildConfig.MAPBOX_TOKEN
        MapView(context, MapInitOptions(context))
    }

    LaunchedEffect(Unit) {
        mapView.mapboxMap.loadStyleUri(Style.LIGHT) {
            mapView.scalebar.enabled = false
            mapView.gestures.scrollEnabled = false
            mapView.gestures.pitchEnabled = false
            mapView.gestures.rotateEnabled = false
            mapView.gestures.doubleTapToZoomInEnabled = false
            mapView.gestures.doubleTouchToZoomOutEnabled = false

            Map3DConfiguration.apply()

            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(point)
                    .zoom(AR_ZOOM)
                    .pitch(60.0)
                    .bearing(0.0)
                    .build(),
            )

            val annotationApi = mapView.annotations
            val circleAnnotationManager = annotationApi.createCircleAnnotationManager(AnnotationConfig())
            val circleAnnotationOptions = CircleAnnotationOptions()
                .withPoint(point)
                .withCircleRadius(10.0)
                .withCircleColor(STRING_PRIMARY_DARK)
                .withCircleStrokeWidth(3.0)
                .withCircleStrokeColor(STRING_WHITE_COLOR)
            circleAnnotationManager.create(circleAnnotationOptions)

            val animator = ValueAnimator.ofFloat(0f, 360f).apply {
                duration = ORBIT_DURATION_MS
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                interpolator = null
                addUpdateListener { anim ->
                    mapView.mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .bearing((anim.animatedValue as Float).toDouble())
                            .build(),
                    )
                }
            }
            animator.start()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize(),
    )
}
