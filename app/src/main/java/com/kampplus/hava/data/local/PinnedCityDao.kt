package com.kampplus.hava.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PinnedCityDao {
    @Query("SELECT * FROM pinned_city WHERE slot = :slot LIMIT 1")
    suspend fun getPinnedCity(slot: Int): PinnedCityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPinnedCity(city: PinnedCityEntity)
}
