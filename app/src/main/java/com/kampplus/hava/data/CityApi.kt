package com.kampplus.hava.data

import com.kampplus.hava.model.CitySearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CityApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 20,
        @Query("language") language: String = "tr",
        @Query("country_code") countryCode: String = "TR", // Sadece Türkiye sonuçlarını çeker
        @Query("format") format: String = "json"
    ): CitySearchResponse
}
