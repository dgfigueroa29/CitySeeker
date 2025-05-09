package com.boa.test.city.seeker.presentation.feature.city.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boa.test.city.seeker.R
import com.boa.test.city.seeker.presentation.feature.city.CityItem
import com.boa.test.city.seeker.presentation.ui.previewCities
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.style.GenericStyle
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location

private const val MAP_DEFAULT_ZOOM = 10.0

@OptIn(MapboxExperimental::class)
@Composable
fun DetailScreen() {
    val city = previewCities().first()
    val point = Point.fromLngLat(city.longitude, city.latitude)
    val mapViewportState: MapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(MAP_DEFAULT_ZOOM)
            center(point)
        }
    }
    var markerResourceId by remember {
        mutableIntStateOf(R.drawable.pin_24)
    }
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(Modifier.padding(16.dp)) {
                CityItem(city = city, canGoBack = true, onCityClick = {}, onFavoriteClick = {})
            }
            Row {
                MapboxMap(
                    modifier = Modifier.wrapContentSize(),
                    mapViewportState = mapViewportState,
                    style = { GenericStyle(style = Style.STANDARD) },
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
                    MapEffect(Unit) { mapView ->
                        mapView.location.updateSettings {
                            locationPuck = createDefault2DPuck(withBearing = true)
                            enabled = true
                            puckBearing = PuckBearing.COURSE
                            puckBearingEnabled = true
                        }
                        mapViewportState.transitionToFollowPuckState()
                    }
                }
            }
        }
    }
}

@Composable
@Preview(name = "Detail")
private fun DetailScreenPreview(
    @Suppress("unused") @PreviewParameter(DetailStatePreviewParameterProvider::class)
    state: DetailState
) {
    DetailScreen()
}

/**
 * PreviewParameter Provider for DetailScreen Preview
 * Add values to the sequence to see the preview in different states
 **/
class DetailStatePreviewParameterProvider : PreviewParameterProvider<DetailState> {
    override val values: Sequence<DetailState>
        get() = sequenceOf(
            DetailState(),
        )
}
