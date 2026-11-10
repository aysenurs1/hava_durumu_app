package com.kampplus.hava.data

import android.content.Context
import com.kampplus.hava.data.local.AppDatabase
import com.kampplus.hava.data.local.PinnedCityEntity
import com.kampplus.hava.model.City

class PinnedCityRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).pinnedCityDao()

    suspend fun getPinnedCityOrCreateDefault(): City {
        return dao.getPinnedCity(PinnedCityEntity.DEFAULT_SLOT)?.toCity()
            ?: DEFAULT_CITY.also { dao.setPinnedCity(PinnedCityEntity.fromCity(it)) }
    }

    suspend fun setPinnedCity(city: City) {
        dao.setPinnedCity(PinnedCityEntity.fromCity(city))
    }

    companion object {
        val DEFAULT_CITY = City(
            id = 745044L,
            name = "İstanbul",
            latitude = 41.0082,
            longitude = 28.9784,
            countryCode = "TR",
            country = "Türkiye",
            admin1 = "İstanbul"
        )
    }
}
