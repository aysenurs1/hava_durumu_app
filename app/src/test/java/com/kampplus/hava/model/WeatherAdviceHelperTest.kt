package com.kampplus.hava.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherAdviceHelperTest {
    @Test
    fun idealWeatherGetsTopOutdoorScore() {
        val result = advice(
            temperature = 19.0,
            wind = 5.0,
            humidity = 50,
            rain = 0
        )

        assertEquals(10, result.outdoorScore)
        assertTrue(result.outdoorMessage.contains("mükemmel"))
    }

    @Test
    fun coldOrWindyWeatherRecommendsWarmWindproofClothing() {
        val cold = advice(temperature = 9.0)
        val windy = advice(temperature = 18.0, wind = 21.0)

        assertTrue(cold.clothingItems.any { it.contains("Rüzgâr geçirmez") })
        assertTrue(windy.clothingItems.any { it.contains("Rüzgâr geçirmez") })
    }

    @Test
    fun rainAndSunnyHeatRecommendationsAreIncluded() {
        val rainy = advice(temperature = 18.0, rain = 41)
        val sunny = advice(temperature = 25.0, condition = "Az bulutlu")

        assertTrue(rainy.clothingItems.any { it.contains("şemsiye veya yağmurluk") })
        assertTrue(sunny.clothingItems.any { it.contains("Güneş gözlüğü ve şapka") })
    }

    @Test
    fun layeredClothingAndHighHumidityAreIncluded() {
        val result = advice(temperature = 15.0, humidity = 85)

        assertTrue(result.clothingItems.any { it.contains("Katmanlı giyinin") })
        assertTrue(result.clothingItems.any { it.contains("Yüksek nem") })
    }

    @Test
    fun severeWeatherScoreIsReducedAndNeverBelowOne() {
        val result = advice(
            temperature = -15.0,
            wind = 50.0,
            humidity = 95,
            rain = 100,
            condition = "Fırtına"
        )

        assertEquals(1, result.outdoorScore)
        assertTrue(result.outdoorMessage.contains("kapalı alan"))
    }

    private fun advice(
        temperature: Double,
        wind: Double? = 0.0,
        humidity: Int? = 50,
        rain: Int? = 0,
        condition: String = "Açık"
    ) = WeatherAdviceHelper.generate(
        temperatureC = temperature,
        windSpeedKmh = wind,
        humidityPercent = humidity,
        precipitationProbability = rain,
        conditionText = condition
    )
}
