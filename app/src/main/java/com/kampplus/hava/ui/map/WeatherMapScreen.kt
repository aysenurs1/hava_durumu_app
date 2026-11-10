package com.kampplus.hava.ui.map

import android.location.Geocoder
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.graphics.Point
import android.widget.FrameLayout
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.R
import com.kampplus.hava.model.City
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.WeatherAnimationIcon
import com.kampplus.hava.ui.component.getWeatherVisuals
import java.io.File
import java.io.IOException
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay

@Composable
fun WeatherMapRoute(
    defaultCity: City,
    onBack: () -> Unit,
    onCityClick: (City) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WeatherMapViewModel = viewModel()
) {
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val forecastState by viewModel.forecastState.collectAsStateWithLifecycle()

    WeatherMapScreen(
        selectedLocation = selectedLocation,
        forecastState = forecastState,
        onMapTap = viewModel::selectLocation,
        onDismissSheet = viewModel::clearSelection,
        onRetry = viewModel::retry,
        onCityClick = onCityClick,
        onResetToDefaultCity = {
            viewModel.clearSelection()
        },
        defaultCity = defaultCity,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
fun WeatherMapScreen(
    selectedLocation: MapLocation?,
    forecastState: LoadState<Forecast>,
    onMapTap: (Double, Double) -> Unit,
    onDismissSheet: () -> Unit,
    onRetry: () -> Unit,
    onCityClick: (City) -> Unit,
    defaultCity: City,
    onResetToDefaultCity: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var resolvedPlaceName by remember(selectedLocation?.latitude, selectedLocation?.longitude) {
        mutableStateOf(selectedLocation?.placeName)
    }

    LaunchedEffect(selectedLocation?.latitude, selectedLocation?.longitude) {
        val location = selectedLocation ?: return@LaunchedEffect
        resolvedPlaceName = withContext(Dispatchers.IO) {
            resolvePlaceName(context, location.latitude, location.longitude)
        }
    }

    val latestOnMapTap = rememberUpdatedState(onMapTap)
    val mapContainer = remember {
        val configuration = Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = File(context.filesDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
        }
        configuration.getOsmdroidTileCache(context)
        WeatherMapContainer(context) { point ->
            latestOnMapTap.value(point.latitude, point.longitude)
        }
    }
    val mapView = mapContainer.mapView

    DisposableEffect(mapContainer, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        mapView.onResume()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapContainer.onDetach()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapContainer },
            update = { container ->
                container.update(selectedLocation, forecastState)
            },
            modifier = Modifier.fillMaxSize()
        )
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            shape = RoundedCornerShape(50),
            color = Color.White.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.9f)),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Hava durumu ekranına dön",
                    tint = Color(0xFF168BFF)
                )
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.9f),
            contentColor = Color(0xFF1C2833),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.9f)),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Text(
                text = "© OpenStreetMap contributors",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF1C2833).copy(alpha = 0.75f),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
            )
        }
        FloatingActionButton(
            onClick = {
                mapContainer.resetTo(defaultCity)
                onResetToDefaultCity()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 56.dp),
            containerColor = Color.White.copy(alpha = 0.92f),
            contentColor = Color(0xFF168BFF),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = "Haritayı ${defaultCity.name} şehrine sıfırla"
            )
        }
    }

    if (selectedLocation != null) {
        MapWeatherBottomSheet(
            forecastState = forecastState,
            onDismiss = onDismissSheet,
            onRetry = onRetry,
            onDetails = {
                onCityClick(selectedLocation.copy(placeName = resolvedPlaceName).toCity())
            },
            placeName = resolvedPlaceName ?: "Yakındaki konum"
        )
    }
}

private fun resolvePlaceName(
    context: android.content.Context,
    latitude: Double,
    longitude: Double
): String? {
    if (!Geocoder.isPresent()) return null

    return try {
        @Suppress("DEPRECATION")
        val address = Geocoder(context, Locale.getDefault())
            .getFromLocation(latitude, longitude, 1)
            ?.firstOrNull()
            ?: return null

        val province = address.adminArea?.takeIf(String::isNotBlank)
        val district = (address.subAdminArea ?: address.locality)
            ?.takeIf(String::isNotBlank)

        when {
            province != null && district != null && !province.equals(district, ignoreCase = true) ->
                "$province, $district"
            province != null -> province
            district != null -> district
            else -> address.subLocality?.takeIf(String::isNotBlank)
        }
    } catch (exception: IOException) {
        null
    }
}

private class WeatherMapContainer(
    context: android.content.Context,
    onMapTap: (GeoPoint) -> Unit
) : FrameLayout(context) {
    val mapView = MapView(context).apply {
        setTileSource(
            TileSourceFactory.MAPNIK
        )
        isTilesScaledToDpi = true
        setMultiTouchControls(true)
        setBuiltInZoomControls(false)
        setScrollableAreaLimitDouble(TURKEY_MAP_BOUNDS)
        setMinZoomLevel(5.5)
        controller.setZoom(TURKEY_MAP_ZOOM)
        controller.setCenter(TURKEY_MAP_CENTER)
        addOnFirstLayoutListener { _, _, _, _, _ ->
            controller.setZoom(TURKEY_MAP_ZOOM)
            controller.setCenter(TURKEY_MAP_CENTER)
        }
        overlays.add(
            MapEventsOverlay(
                object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                        if (TURKEY_MAP_BOUNDS.contains(point)) {
                            onMapTap(point)
                        }
                        return true
                    }

                    override fun longPressHelper(point: GeoPoint): Boolean {
                        if (TURKEY_MAP_BOUNDS.contains(point)) {
                            onMapTap(point)
                        }
                        return true
                    }
                }
            )
        )
    }

    private var selectedLocation by mutableStateOf<MapLocation?>(null)
    private var forecastState by mutableStateOf<LoadState<Forecast>>(LoadState.Empty)
    private val markerView = ComposeView(context).apply {
        setContent {
            selectedLocation?.let { location ->
                WeatherMarkerChip(forecastState = forecastState)
            }
        }
        addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> positionMarker() }
    }

    init {
        addView(
            mapView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
        addView(
            markerView,
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        )
        mapView.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean {
                positionMarker()
                return false
            }

            override fun onZoom(event: ZoomEvent): Boolean {
                positionMarker()
                return false
            }
        })
    }

    fun update(location: MapLocation?, state: LoadState<Forecast>) {
        selectedLocation = location
        forecastState = state
        markerView.visibility = if (location == null) View.GONE else View.VISIBLE
        markerView.post { positionMarker() }
    }

    fun onDetach() {
        mapView.onDetach()
    }

    fun resetTo(city: City) {
        val cityPoint = GeoPoint(city.latitude, city.longitude)
        val target = if (TURKEY_MAP_BOUNDS.contains(cityPoint)) {
            cityPoint
        } else {
            TURKEY_MAP_CENTER
        }
        mapView.controller.setZoom(if (target == cityPoint) 7.5 else TURKEY_MAP_ZOOM)
        mapView.controller.animateTo(target)
    }

    private fun positionMarker() {
        val location = selectedLocation ?: return
        if (mapView.width == 0 || markerView.width == 0 || markerView.height == 0) return

        val screenPoint = mapView.projection.toPixels(
            GeoPoint(location.latitude, location.longitude),
            Point()
        )
        val params = markerView.layoutParams as? LayoutParams ?: return
        val left = screenPoint.x - markerView.width / 2
        val top = screenPoint.y - markerView.height - context.dpToPx(10)
        if (params.leftMargin != left || params.topMargin != top) {
            params.leftMargin = left
            params.topMargin = top
            markerView.layoutParams = params
        }
    }

    private fun android.content.Context.dpToPx(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}

private val TURKEY_MAP_BOUNDS = BoundingBox(42.1, 44.8, 35.8, 25.6)
private val TURKEY_MAP_CENTER = GeoPoint(39.0, 35.0)
private const val TURKEY_MAP_ZOOM = 6.0

@Composable
private fun WeatherMarkerChip(
    forecastState: LoadState<Forecast>
) {
    val forecast = (forecastState as? LoadState.Success)?.data
    val condition = forecast?.current?.let {
        WeatherCondition.fromWeatherCode(it.weatherCode)
    }
    val visuals = getWeatherVisuals(condition?.label ?: "açık")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF168BFF),
            contentColor = Color.White,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.9f)),
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                WeatherAnimationIcon(
                    visuals = visuals,
                    contentDescription = condition?.label ?: "Hava durumu",
                    size = 28.dp
                )
                Text(
                    text = forecast?.current?.let { "${it.temperatureC.toInt()}°" } ?: "…",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
        Canvas(modifier = Modifier.size(width = 14.dp, height = 8.dp)) {
            val tip = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height)
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(tip.x, tip.y)
                close()
            }
            drawPath(path, Color(0xFF168BFF))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapWeatherBottomSheet(
    placeName: String,
    forecastState: LoadState<Forecast>,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onDetails: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White.copy(alpha = 0.82f),
        contentColor = Color(0xFF1C2833),
        scrimColor = Color.Black.copy(alpha = 0.16f),
        tonalElevation = 0.dp,
        dragHandle = {
            androidx.compose.material3.BottomSheetDefaults.DragHandle(
                color = Color(0xFF1C2833).copy(alpha = 0.3f)
            )
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(22.dp),
            color = Color.White.copy(alpha = 0.82f),
            contentColor = Color(0xFF1C2833),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.9f)),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
          Column(
              modifier = Modifier
                  .fillMaxWidth()
                  .verticalScroll(rememberScrollState())
                  .padding(horizontal = 20.dp)
                  .padding(bottom = 24.dp, top = 12.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    tint = Color(0xFF168BFF)
                )
                Column {
                    Text(
                        text = placeName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C2833)
                    )
                }
            }

            when (forecastState) {
                LoadState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF168BFF))
                    }
                }
                LoadState.Empty -> Text(
                    "Haritada bir noktaya dokun.",
                    color = Color(0xFF1C2833)
                )
                is LoadState.Error -> {
                    Text(text = forecastState.message, color = Color(0xFF1C2833))
                    TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.action_retry), color = Color(0xFF168BFF))
                    }
                }
                is LoadState.Success -> {
                    MapForecastPreview(forecast = forecastState.data)
                    Button(
                        onClick = onDetails,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF168BFF),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Detayları İncele")
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun MapForecastPreview(forecast: Forecast) {
    val current = forecast.current
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.82f),
        contentColor = Color(0xFF1C2833),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.9f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${current.temperatureC.toInt()}°C",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C2833)
                    )
                    Text(
                        text = condition.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF1C2833).copy(alpha = 0.85f)
                    )
                    current.apparentTemperatureC?.let { feelsLike ->
                        Text(
                            text = "Hissedilen ${feelsLike.toInt()}°C",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1C2833).copy(alpha = 0.7f)
                        )
                    }
                }
                val visuals = getWeatherVisuals(condition.label)
                WeatherAnimationIcon(
                    visuals = visuals,
                    contentDescription = condition.label,
                    size = 72.dp
                )
            }

            val today = forecast.daily.firstOrNull()
            if (today != null) {
                Text(
                    text = "Bugün  ${today.minTemperatureC.toInt()}° / ${today.maxTemperatureC.toInt()}°",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1C2833)
                )
            }

            if (forecast.hourly.isNotEmpty()) {
                androidx.compose.material3.HorizontalDivider(
                    color = Color(0xFF1C2833).copy(alpha = 0.12f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    forecast.hourly.take(3).forEach { hour ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = hour.time.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF1C2833).copy(alpha = 0.65f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(WeatherCondition.fromWeatherCode(hour.weatherCode).emoji)
                            Text("${hour.temperatureC.toInt()}°", color = Color(0xFF1C2833))
                        }
                    }
                }
            }
        }
    }
}
