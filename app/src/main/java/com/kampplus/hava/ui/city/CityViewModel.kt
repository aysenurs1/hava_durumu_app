package com.kampplus.hava.ui.city

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.data.CityRepository
import com.kampplus.hava.data.OpenMeteoCityRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(FlowPreview::class)
class CityViewModel(
    private val repository: CityRepository = OpenMeteoCityRepository()
) : ViewModel() {

    private var lastRequestedQuery: String? = null
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<LoadState<List<City>>>(LoadState.Empty)
    val uiState: StateFlow<LoadState<List<City>>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _query
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { text ->
                    val trimmed = text.trim()
                    if (trimmed.length >= 2) {
                        if (trimmed != lastRequestedQuery) {
                            searchCity(trimmed)
                        }
                    } else if (trimmed.isEmpty()) {
                        _uiState.value = LoadState.Empty
                    }
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.trim() != lastRequestedQuery) {
            lastRequestedQuery = null
        }
        if (newQuery.trim().length < 2) {
            _uiState.value = LoadState.Empty
        }
    }

    fun search() {
        val trimmed = _query.value.trim()
        if (trimmed.length >= 2) {
            viewModelScope.launch {
                searchCity(trimmed)
            }
        }
    }

    private suspend fun searchCity(name: String) {
        lastRequestedQuery = name
        _uiState.value = LoadState.Loading
        try {
            val cities = repository.search(name)
            if (cities.isEmpty()) {
                _uiState.value = LoadState.Empty
            } else {
                _uiState.value = LoadState.Success(cities)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "Şehir araması başarısız: bağlantı hatası", e)
            _uiState.value = LoadState.Error("İnternet bağlantını kontrol et.")
        } catch (e: HttpException) {
            Log.e(TAG, "Şehir araması başarısız: HTTP ${e.code()}", e)
            _uiState.value = LoadState.Error("Sunucu şu an cevap vermiyor.")
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Şehir araması başarısız: yanıt çözümlenemedi", e)
            _uiState.value = LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
        } catch (e: Exception) {
            Log.e(TAG, "Şehir araması başarısız", e)
            _uiState.value = LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
        }
    }

    private companion object {
        const val TAG = "CityViewModel"
    }
}
