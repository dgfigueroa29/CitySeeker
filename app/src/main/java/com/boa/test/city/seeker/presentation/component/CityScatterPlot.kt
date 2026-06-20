package com.boa.test.city.seeker.presentation.component

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.boa.test.city.seeker.domain.model.CityModel
import kotlin.math.sqrt

private val OceanLight = Color(0xFFE8F4FD)
private val OceanDark = Color(0xFF1E2D3D)
private val GridLight = Color(0xFFB0D4E9)
private val GridDark = Color(0xFF2E4050)
private val PinColor = Color(0xFF6750A4)
private val PinColorAlpha = Color(0xFF6750A4).copy(alpha = 0.3f)
private val PinDim = Color(0xFF6750A4).copy(alpha = 0.4f)
private const val ASPECT = 2f
private const val MIN_SCALE = 0.5f
private const val MAX_SCALE = 5f

@Composable
fun CityScatterPlot(
    selectedCity: CityModel?,
    cities: List<CityModel>,
    onCityClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f,
) {
    val oceanColor = if (isDark) OceanDark else OceanLight
    val gridColor = if (isDark) GridDark else GridLight
    val density = LocalDensity.current

    var scale by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val canvasWidthPx = with(density) { maxWidth.toPx() }
        val canvasHeightPx = with(density) { maxHeight.toPx() }
        val (mapLeft, mapTop, mapW, mapH) = computeMapBounds(canvasWidthPx, canvasHeightPx)

        val textPaint =
            remember {
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isDark) Color.White.toArgb() else Color.Black.toArgb()
                    textSize = with(density) { 14.dp.toPx() }
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
            }

        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, panDelta, zoom, _ ->
                            scale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                            pan += panDelta
                        }
                    }.pointerInput(cities, mapLeft, mapTop, mapW, mapH, scale, pan) {
                        detectTapGestures { tapOffset ->
                            cities.forEach { city ->
                                val px = projectX(mapLeft, mapW, city.longitude, scale, pan.x)
                                val py = projectY(mapTop, mapH, city.latitude, scale, pan.y)
                                val dx = tapOffset.x - px
                                val dy = tapOffset.y - py
                                if (sqrt(dx * dx + dy * dy) < 24f) {
                                    onCityClick(city.id.toString())
                                    return@detectTapGestures
                                }
                            }
                        }
                    },
        ) {
            val dash = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

            drawRect(color = oceanColor)

            for (lat in -90..90 step 30) {
                val y = projectY(mapTop, mapH, lat.toDouble(), scale, pan.y)
                drawLine(
                    gridColor,
                    Offset(mapLeft, y),
                    Offset(mapLeft + mapW, y),
                    strokeWidth = 1f,
                    pathEffect = dash,
                )
            }
            for (lon in -180..180 step 30) {
                val x = projectX(mapLeft, mapW, lon.toDouble(), scale, pan.x)
                drawLine(
                    gridColor,
                    Offset(x, mapTop),
                    Offset(x, mapTop + mapH),
                    strokeWidth = 1f,
                    pathEffect = dash,
                )
            }

            cities.forEach { city ->
                val px = projectX(mapLeft, mapW, city.longitude, scale, pan.x)
                val py = projectY(mapTop, mapH, city.latitude, scale, pan.y)
                val isSelected = city.id == selectedCity?.id

                if (isSelected) {
                    val r = 18f * scale
                    val shadowR = 22f * scale
                    drawCircle(
                        color = PinColorAlpha,
                        radius = shadowR,
                        center = Offset(px + 2f * scale, py + 2f * scale),
                    )
                    drawCircle(color = PinColor, radius = r, center = Offset(px, py))
                    drawCircle(
                        color = Color.White,
                        radius = 7f * scale,
                        center = Offset(px, py),
                    )
                    with(textPaint) { textSize = 14.dp.toPx() * scale }
                    drawContext.canvas.nativeCanvas.drawText(city.name, px, py + 36f * scale, textPaint)
                } else {
                    drawCircle(
                        color = PinDim,
                        radius = 5f * scale,
                        center = Offset(px, py),
                    )
                }
            }
        }
    }
}

private fun projectX(
    left: Float,
    w: Float,
    lon: Double,
    scale: Float,
    panX: Float,
): Float = left + ((lon + 180.0) / 360.0 * w * scale + panX).toFloat()

private fun projectY(
    top: Float,
    h: Float,
    lat: Double,
    scale: Float,
    panY: Float,
): Float = top + ((90.0 - lat) / 180.0 * h * scale + panY).toFloat()

private data class MapBounds(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
)

private fun computeMapBounds(
    cw: Float,
    ch: Float,
): MapBounds {
    val ca = cw / ch
    return if (ca > ASPECT) {
        val h = ch
        val w = h * ASPECT
        MapBounds((cw - w) / 2f, 0f, w, h)
    } else {
        val w = cw
        val h = w / ASPECT
        MapBounds(0f, (ch - h) / 2f, w, h)
    }
}
