package com.kampplus.hava.ui.map

import com.kampplus.hava.data.WeatherRepository
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.testing.MainDispatcherRule
import com.kampplus.hava.testing.sampleForecast
import com.kampplus.hava.ui.LoadState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherMapViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun selectedCoordinatesAreRequestedAndCanOpenDetails() = runTest {
        var requestedCoordinates: Pair<Double, Double>? = null
        val viewModel = WeatherMapViewModel(
            fakeWeatherRepository { latitude, longitude ->
                requestedCoordinates = latitude to longitude
                sampleForecast()
            }
        )

        viewModel.selectLocation(39.92, 32.85)
        advanceUntilIdle()

        assertEquals(39.92 to 32.85, requestedCoordinates)
        assertTrue(viewModel.forecastState.value is LoadState.Success)
        assertEquals(39.92, viewModel.selectedLocation.value?.toCity()?.latitude)
        assertEquals(32.85, viewModel.selectedLocation.value?.toCity()?.longitude)
    }

    @Test
    fun invalidCoordinatesAreRejected() {
        val viewModel = WeatherMapViewModel(fakeWeatherRepository { _, _ -> sampleForecast() })

        assertThrows(IllegalArgumentException::class.java) {
            viewModel.selectLocation(91.0, 0.0)
        }
    }

    private fun fakeWeatherRepository(block: suspend (Double, Double) -> Forecast): WeatherRepository =
        object : WeatherRepository {
            override suspend fun getForecast(latitude: Double, longitude: Double): Forecast =
                block(latitude, longitude)
        }
}
