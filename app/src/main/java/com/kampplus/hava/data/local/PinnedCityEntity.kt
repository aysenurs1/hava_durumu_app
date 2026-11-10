package com.kampplus.hava.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kampplus.hava.model.City

@Entity(tableName = "pinned_city")
data class PinnedCityEntity(
    @PrimaryKey
    val slot: Int = DEFAULT_SLOT,
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val countryCode: String?,
    val country: String?,
    val admin1: String?
) {
    fun toCity() = City(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        countryCode = countryCode,
        country = country,
        admin1 = admin1
    )

    companion object {
        const val DEFAULT_SLOT = 1

        fun fromCity(city: City) = PinnedCityEntity(
            id = city.id,
            name = city.name,
            latitude = city.latitude,
            longitude = city.longitude,
            countryCode = city.countryCode,
            country = city.country,
            admin1 = city.admin1
        )
    }
}
