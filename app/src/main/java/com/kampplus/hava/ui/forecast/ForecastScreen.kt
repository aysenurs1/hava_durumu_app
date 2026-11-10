package com.kampplus.hava.ui.forecast

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.R
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.DailyForecast
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.HourlyForecast
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.model.WeatherAdviceHelper
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.component.GlassmorphicCard
import com.kampplus.hava.ui.component.WeatherAnimationIcon
import com.kampplus.hava.ui.component.WeatherUiTokens
import com.kampplus.hava.ui.component.getWeatherVisuals
import com.kampplus.hava.ui.theme.HavaTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ForecastRoute(
    onSearch: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenFavorites: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForecastViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ForecastScreen(
        cityName = viewModel.cityName,
        subtitle = viewModel.subtitle,
        uiState = uiState,
        onSearch = onSearch,
        onOpenMap = onOpenMap,
        onOpenFavorites = onOpenFavorites,
        onRefresh = viewModel::onRetry,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    cityName: String,
    subtitle: String,
    uiState: LoadState<Forecast>,
    onSearch: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenFavorites: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRefreshing = uiState is LoadState.Loading

    Scaffold(
        modifier = modifier.background(
            Brush.verticalGradient(WeatherUiTokens.skyGradient)
        ),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onSearch) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Şehir ara",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    LoadState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                    LoadState.Empty -> ErrorView(message = "Bu şehir için tahmin bulunamadı.")
                    is LoadState.Error -> ErrorView(message = uiState.message, onRetry = onRefresh)
                    is LoadState.Success -> ForecastContent(
                        cityName = cityName,
                        subtitle = subtitle,
                        forecast = uiState.data,
                        modifier = Modifier.padding(bottom = 84.dp)
                    )
                }

                GlassCornerButton(
                    icon = Icons.Filled.Map,
                    contentDescription = "Harita görünümü",
                    onClick = onOpenMap,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 16.dp)
                )
                GlassCornerButton(
                    icon = Icons.Filled.Favorite,
                    contentDescription = "Favorilerim",
                    onClick = onOpenFavorites,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun ForecastContent(
    cityName: String,
    subtitle: String,
    forecast: Forecast,
    modifier: Modifier = Modifier
) {
    val condition = WeatherCondition.fromWeatherCode(forecast.current.weatherCode)
    val todayForecast = forecast.daily.firstOrNull()
    val precipitationProbability = forecast.hourly.minByOrNull {
        kotlin.math.abs(ChronoUnit.MINUTES.between(forecast.current.observedAt, it.time))
    }?.precipitationProbability ?: todayForecast?.precipitationProbability

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MainWeatherCard(
            cityName = cityName,
            subtitle = subtitle,
            current = forecast.current,
            precipitationProbability = precipitationProbability,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle(stringResource(R.string.detail_hourly))
        HourlyRow(hours = forecast.hourly)

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle(stringResource(R.string.detail_daily))
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                forecast.daily.forEachIndexed { index, day ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    DailyRow(day = day, isToday = index == 0)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ClothingAdviceCard(
            conditionText = condition.label,
            tempC = forecast.current.temperatureC,
            windSpeedKmh = forecast.current.windSpeedKmh,
            humidityPercent = forecast.current.humidityPercent,
            precipitationProbability = precipitationProbability,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun MainWeatherCard(
    cityName: String,
    subtitle: String,
    current: CurrentWeather,
    precipitationProbability: Int?,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurrentWeatherHeader(
                cityName = cityName,
                subtitle = subtitle,
                current = current,
                modifier = Modifier.fillMaxWidth()
            )
            WeatherMetricsCard(
                current = current,
                precipitationProbability = precipitationProbability
            )
        }
    }
}

@Composable
private fun CurrentWeatherHeader(
    cityName: String,
    subtitle: String,
    current: CurrentWeather,
    modifier: Modifier = Modifier
) {
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)
    val isNight = current.observedAt.hour < 6 || current.observedAt.hour >= 20
    val visuals = getWeatherVisuals(condition.label, isNight = isNight)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = cityName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Lottie animasyonlu ikon bileşeni
        WeatherAnimationIcon(
            visuals = visuals,
            contentDescription = condition.label,
            size = 80.dp
        )

        Text(
            text = degrees(current.temperatureC),
            fontSize = 52.sp,
            fontWeight = FontWeight.ExtraLight,
            color = Color.White
        )

        Text(
            text = condition.label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun WeatherMetricsCard(
    current: CurrentWeather,
    precipitationProbability: Int?
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Metric(
                    icon = Icons.Rounded.Grain,
                    label = "Yağış İhtimali",
                    value = precipitationProbability?.let { "%$it" } ?: "—"
                )
                current.apparentTemperatureC?.let {
                    Metric(
                        icon = Icons.Default.Thermostat,
                        label = stringResource(R.string.detail_feels_like),
                        value = degrees(it)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                current.humidityPercent?.let {
                    Metric(
                        icon = Icons.Default.WaterDrop,
                        label = stringResource(R.string.detail_humidity),
                        value = "%$it"
                    )
                }
                current.windSpeedKmh?.let {
                    Metric(
                        icon = Icons.Default.Air,
                        label = stringResource(R.string.detail_wind),
                        value = "${it.roundToInt()} km/sa"
                    )
                }
            }
        }
    }
}

@Composable
fun ClothingAdviceCard(
    conditionText: String,
    tempC: Double,
    windSpeedKmh: Double?,
    humidityPercent: Int?,
    precipitationProbability: Int?,
    modifier: Modifier = Modifier
) {
    val advice = WeatherAdviceHelper.generate(
        temperatureC = tempC,
        windSpeedKmh = windSpeedKmh,
        humidityPercent = humidityPercent,
        precipitationProbability = precipitationProbability,
        conditionText = conditionText
    )
    val scoreColor = when {
        advice.outdoorScore >= 8 -> Color(0xFF22C8B8)
        advice.outdoorScore >= 5 -> Color(0xFF54A8FF)
        else -> Color(0xFFC62828)
    }

    GlassmorphicCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Checkroom,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Giyim & Aksesuar Önerisi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = scoreColor.copy(alpha = 0.28f),
                    contentColor = Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        scoreColor.copy(alpha = 0.55f)
                    )
                ) {
                    Text(
                        text = "🏃 ${advice.outdoorScore}/10",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            advice.clothingItems.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "•", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Text(
                text = advice.outdoorMessage,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun HourlyRow(hours: List<HourlyForecast>, modifier: Modifier = Modifier) {
    val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
    val upcoming = hours.filter { !it.time.isBefore(now) }.take(HOURLY_COUNT)

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items = upcoming, key = { it.time.toString() }) { hour ->
            val condition = WeatherCondition.fromWeatherCode(hour.weatherCode)
            val visuals = getWeatherVisuals(condition.label)

            GlassmorphicCard(
                modifier = Modifier.width(76.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = hour.time.format(HOUR_FORMAT),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Icon(
                        imageVector = visuals.icon,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = visuals.iconTint
                    )
                    Text(
                        text = degrees(hour.temperatureC),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = percent(hour.precipitationProbability) ?: " ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF81D4FA)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyRow(day: DailyForecast, isToday: Boolean, modifier: Modifier = Modifier) {
    val condition = WeatherCondition.fromWeatherCode(day.weatherCode)
    val visuals = getWeatherVisuals(condition.label)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isToday) stringResource(R.string.today) else day.date.format(DAY_FORMAT).replaceFirstChar(Char::titlecase),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = percent(day.precipitationProbability).orEmpty(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF81D4FA),
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
        Icon(
            imageVector = visuals.icon,
            contentDescription = null,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(24.dp),
            tint = visuals.iconTint
        )
        Text(
            text = degrees(day.minTemperatureC),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = degrees(day.maxTemperatureC),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun Metric(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        Text(text = label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

private fun degrees(celsius: Double) = "${celsius.roundToInt()}°"

private fun percent(value: Int?) = value?.takeIf { it > 0 }?.let { "%$it" }

private const val HOURLY_COUNT = 24
private val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("tr"))

@Preview(showBackground = true)
@Composable
private fun ForecastScreenPreview() {
    HavaTheme {
        ForecastScreen(
            cityName = "Ankara",
            subtitle = "Ankara, Türkiye",
            uiState = LoadState.Success(
                Forecast(
                    current = CurrentWeather(
                        temperatureC = 21.0,
                        weatherCode = 0,
                        observedAt = LocalDateTime.of(2026, 1, 15, 14, 0),
                        apparentTemperatureC = 20.0,
                        humidityPercent = 45,
                        windSpeedKmh = 12.0
                    ),
                    hourly = List(8) {
                        HourlyForecast(
                            time = LocalDateTime.now().plusHours(it.toLong()),
                            temperatureC = 18.0 + it,
                            weatherCode = 2,
                            precipitationProbability = it * 5
                        )
                    },
                    daily = List(7) {
                        DailyForecast(
                            date = LocalDate.of(2026, 1, 15).plusDays(it.toLong()),
                            minTemperatureC = 9.0 + it,
                            maxTemperatureC = 19.0 + it,
                            weatherCode = 61,
                            precipitationProbability = 40
                        )
                    }
                )
            ),
            onSearch = {},
            onOpenMap = {},
            onOpenFavorites = {},
            onRefresh = {}
        )
    }

}

@Composable
private fun GlassCornerButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(56.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.20f),
        contentColor = Color.White,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
