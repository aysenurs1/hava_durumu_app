package com.kampplus.hava.data

import android.content.Context
import com.kampplus.hava.data.local.AppDatabase
import com.kampplus.hava.data.local.FavoriteCityEntity
import com.kampplus.hava.model.City
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).favoriteCityDao()

    val favorites: Flow<List<City>> = dao.getAllFavorites().map { entities ->
        entities.map { it.toCity() }
    }

    suspend fun toggleFavorite(city: City) {
        val exists = dao.isFavorite(city.id)
        if (exists) {
            dao.deleteFavoriteById(city.id)
        } else {
            dao.insertFavorite(FavoriteCityEntity.fromCity(city))
        }
    }

    suspend fun isFavorite(cityId: Long): Boolean {
        return dao.isFavorite(cityId)
    }
}
