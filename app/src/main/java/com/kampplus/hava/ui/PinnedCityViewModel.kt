package com.kampplus.hava.ui

import android.app.Application
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.hava.data.PinnedCityRepository
import com.kampplus.hava.model.City
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PinnedCityViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PinnedCityRepository(application)

    private val _defaultCity = MutableStateFlow<LoadState<City>>(LoadState.Loading)
    val defaultCity: StateFlow<LoadState<City>> = _defaultCity.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    init {
        loadDefaultCity()
    }

    fun loadDefaultCity() {
        _defaultCity.value = LoadState.Loading
        viewModelScope.launch {
            try {
                _defaultCity.value = LoadState.Success(repository.getPinnedCityOrCreateDefault())
            } catch (exception: SQLiteException) {
                Log.e(TAG, "Varsayılan şehir veritabanından yüklenemedi.", exception)
                _defaultCity.value = LoadState.Error("Varsayılan şehir yüklenemedi. Tekrar deneyin.")
            }
        }
    }

    suspend fun pinCity(city: City): Boolean {
        _saveError.value = null
        return try {
            repository.setPinnedCity(city)
            _defaultCity.value = LoadState.Success(city)
            true
        } catch (exception: SQLiteException) {
            Log.e(TAG, "Seçilen şehir varsayılan olarak kaydedilemedi.", exception)
            _saveError.value = "Şehir kaydedilemedi. Lütfen tekrar deneyin."
            false
        }
    }

    fun clearSaveError() {
        _saveError.value = null
    }

    companion object {
        private const val TAG = "PinnedCityViewModel"
    }
}
