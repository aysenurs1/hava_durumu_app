package com.kampplus.hava.data

import com.kampplus.hava.model.City
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface CityRepository {
    suspend fun search(query: String): List<City>
}

class OpenMeteoCityRepository(
    private val cityApi: CityApi = ApiClient.cityApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CityRepository {

    override suspend fun search(query: String): List<City> = withContext(ioDispatcher) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty() || cleanQuery.length < 2) return@withContext emptyList()

        // 1. Open-Meteo'dan doğrudan TR filtreli şehirleri çek
        val response = try {
            cityApi.search(name = cleanQuery, countryCode = "TR")
        } catch (e: Exception) {
            android.util.Log.e("CityRepo", "Arama hatası: ${e.message}")
            null
        }

        val allCities = response?.results.orEmpty()
        if (allCities.isEmpty()) return@withContext emptyList()

        // 2. Yalnızca ve kesin olarak TR olanları filtrele (Yabancı ülkeler kesin elenir)
        allCities.filter { it.countryCode.equals("TR", ignoreCase = true) }
    }
}
