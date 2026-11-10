package com.kampplus.hava.model

import java.util.Locale

data class WeatherAdviceResult(
    val clothingItems: List<String>,
    val outdoorScore: Int,
    val outdoorMessage: String
)

object WeatherAdviceHelper {
    fun generate(
        temperatureC: Double,
        windSpeedKmh: Double?,
        humidityPercent: Int?,
        precipitationProbability: Int?,
        conditionText: String
    ): WeatherAdviceResult {
        val wind = windSpeedKmh ?: 0.0
        val humidity = humidityPercent ?: 0
        val precipitation = precipitationProbability?.coerceIn(0, 100) ?: 0
        val condition = conditionText.lowercase(Locale.forLanguageTag("tr"))
        val isSunnyOrMostlyClear = condition.contains("açık") ||
            condition.contains("az bulutlu") ||
            condition.contains("güneş")
        val isWet = condition.contains("yağmur") ||
            condition.contains("sağanak") ||
            condition.contains("fırtına") ||
            condition.contains("kar")

        val clothingItems = buildList {
            if (temperatureC < 10.0 || wind > 20.0) {
                add("Rüzgâr geçirmez kalın giysi veya mont giyin.")
            }
            if (precipitation > 40) {
                add("Yanınıza şemsiye veya yağmurluk alın.")
            }
            if (temperatureC > 24.0 && isSunnyOrMostlyClear) {
                add("Güneş gözlüğü ve şapka alın.")
            }
            if (temperatureC in 10.0..20.0) {
                add("Katmanlı giyinin; sweatshirt veya trençkot tercih edin.")
            }
            if (temperatureC > 24.0 && !isSunnyOrMostlyClear) {
                add("Hafif ve nefes alabilen kıyafetler tercih edin.")
            }
            if (humidity >= 80) {
                add("Yüksek nem için nefes alabilen kumaşlar tercih edin.")
            }
            if (isEmpty()) {
                add("Hava koşullarına uygun, rahat kıyafetler tercih edin.")
            }
        }

        val temperatureScore = when {
            temperatureC in 16.0..22.0 -> 10
            temperatureC >= 13.0 && temperatureC < 16.0 || temperatureC > 22.0 && temperatureC <= 25.0 -> 8
            temperatureC >= 10.0 && temperatureC < 13.0 || temperatureC > 25.0 && temperatureC <= 29.0 -> 7
            temperatureC >= 5.0 && temperatureC < 10.0 || temperatureC > 29.0 && temperatureC <= 34.0 -> 5
            temperatureC >= 0.0 && temperatureC < 5.0 || temperatureC > 34.0 && temperatureC <= 38.0 -> 3
            else -> 1
        }
        val windPenalty = when {
            wind > 35.0 -> 4
            wind > 20.0 -> 2
            wind > 10.0 -> 1
            else -> 0
        }
        val precipitationPenalty = when {
            precipitation > 70 -> 4
            precipitation > 40 -> 2
            else -> 0
        }
        val conditionPenalty = if (isWet && precipitation <= 40) 2 else 0
        val humidityPenalty = if (humidity > 85) 1 else 0
        val score = (temperatureScore - windPenalty - precipitationPenalty - conditionPenalty - humidityPenalty)
            .coerceIn(1, 10)

        val message = when (score) {
            in 8..10 -> "Yürüyüş ve koşu için mükemmel bir hava!"
            in 6..7 -> "Açık hava etkinliği için uygun; koşullara göre hazırlıklı olun."
            in 4..5 -> "Kısa ve hafif bir yürüyüş tercih edilebilir."
            else -> "Hava koşulları zorlayıcı; kapalı alan sporu tercih edilebilir."
        }

        return WeatherAdviceResult(
            clothingItems = clothingItems,
            outdoorScore = score,
            outdoorMessage = message
        )
    }
}
