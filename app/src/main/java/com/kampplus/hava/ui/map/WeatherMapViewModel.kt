package com.kampplus.hava.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.data.OpenMeteoWeatherRepository
import com.kampplus.hava.data.WeatherRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class MapLocation(
    val latitude: Double,
    val longitude: Double,
    val placeName: String? = null
) {
    fun toCity(): City = City(
        id = 0L,
        name = placeName ?: "Seçilen Konum",
        latitude = latitude,
        longitude = longitude,
        countryCode = null,
        country = null,
        admin1 = null
    )
}

class WeatherMapViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    constructor() : this(OpenMeteoWeatherRepository())

    private val _selectedLocation = MutableStateFlow<MapLocation?>(null)
    val selectedLocation: StateFlow<MapLocation?> = _selectedLocation.asStateFlow()

    private val _forecastState = MutableStateFlow<LoadState<Forecast>>(LoadState.Empty)
    val forecastState: StateFlow<LoadState<Forecast>> = _forecastState.asStateFlow()

    private var forecastJob: Job? = null

    fun selectLocation(latitude: Double, longitude: Double) {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90." }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180." }

        forecastJob?.cancel()
        _selectedLocation.value = MapLocation(latitude, longitude)
        forecastJob = viewModelScope.launch {
            _forecastState.value = LoadState.Loading
            _forecastState.value = try {
                LoadState.Success(repository.getForecast(latitude, longitude))
            } catch (e: IOException) {
                Log.e(TAG, "Harita konumu için tahmin alınamadı: bağlantı hatası", e)
                LoadState.Error("İnternet bağlantını kontrol et.")
            } catch (e: HttpException) {
                Log.e(TAG, "Harita konumu için tahmin alınamadı: HTTP ${e.code()}", e)
                LoadState.Error("Sunucu şu an cevap vermiyor.")
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Harita konumu için tahmin yanıtı çözümlenemedi", e)
                LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Harita konumu için tahmin verisi eksik", e)
                LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
            }
        }
    }

    fun retry() {
        _selectedLocation.value?.let { selectLocation(it.latitude, it.longitude) }
    }

    fun clearSelection() {
        forecastJob?.cancel()
        forecastJob = null
        _selectedLocation.value = null
        _forecastState.value = LoadState.Empty
    }

    private companion object {
        const val TAG = "WeatherMapViewModel"
    }
}
