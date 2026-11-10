package com.kampplus.hava.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class WeatherVisuals(
    val icon: ImageVector,
    val iconTint: Color,
    val gradientColors: List<Color>,
    val lottieAsset: String? = null
)

fun getWeatherVisuals(conditionText: String, isNight: Boolean = false): WeatherVisuals {
    val text = conditionText.lowercase()
    return when {
        text.contains("güneş") || text.contains("açık") -> {
            if (isNight) {
                WeatherVisuals(
                    icon = Icons.Default.NightsStay,
                    iconTint = Color(0xFFFFD54F),
                    gradientColors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)),
                    lottieAsset = "weather_night.json"
                )
            } else {
                WeatherVisuals(
                    icon = Icons.Default.WbSunny,
                    iconTint = Color(0xFFFFCA28),
                    gradientColors = listOf(Color(0xFF2193B0), Color(0xFF6DD5ED)),
                    lottieAsset = "weather_sunny.json"
                )
            }
        }
        text.contains("sis") || text.contains("pus") -> {
            WeatherVisuals(
                icon = Icons.Default.WbCloudy,
                iconTint = Color(0xFFECEFF1),
                gradientColors = listOf(Color(0xFF536976), Color(0xFF292E49)),
                lottieAsset = "weather_fog.json"
            )
        }
        text.contains("yağmur") || text.contains("sağanak") -> {
            WeatherVisuals(
                icon = Icons.Default.WaterDrop,
                iconTint = Color(0xFF81D4FA),
                gradientColors = listOf(Color(0xFF1F1C2C), Color(0xFF4A569D)),
                lottieAsset = "weather_rainy.json"
            )
        }
        text.contains("fırtına") || text.contains("şimşek") -> {
            WeatherVisuals(
                icon = Icons.Default.Thunderstorm,
                iconTint = Color(0xFFFFEB3B),
                gradientColors = listOf(Color(0xFF141E30), Color(0xFF243B55)),
                lottieAsset = "weather_thunder.json"
            )
        }
        text.contains("kar") -> {
            WeatherVisuals(
                icon = Icons.Default.AcUnit,
                iconTint = Color(0xFFE1F5FE),
                gradientColors = listOf(Color(0xFF70E1F5), Color(0xFFE8EBFF)),
                lottieAsset = "weather_snowy.json"
            )
        }
        else -> { // Bulutlu / Kapalı / Parçalı Bulutlu
            WeatherVisuals(
                icon = Icons.Default.WbCloudy,
                iconTint = Color(0xFFE0E0E0),
                gradientColors = listOf(Color(0xFF3E5151), Color(0xFFDECBA4)),
                lottieAsset = "weather_cloudy.json"
            )
        }
    }
}
