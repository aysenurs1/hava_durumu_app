package com.kampplus.hava.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.hava.data.FavoritesRepository
import com.kampplus.hava.model.City
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = FavoritesRepository(application)

    val favorites: StateFlow<List<City>> = repository.favorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun toggleFavorite(city: City) {
        viewModelScope.launch {
            repository.toggleFavorite(city)
        }
    }

}
