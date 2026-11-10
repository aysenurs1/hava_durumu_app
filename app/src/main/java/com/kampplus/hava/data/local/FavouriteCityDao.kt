package com.kampplus.hava.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteCityDao {

    @Query("SELECT * FROM favorite_cities ORDER BY name ASC")
    fun getAllFavorites(): Flow<List<FavoriteCityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(city: FavoriteCityEntity)

    @Query("DELETE FROM favorite_cities WHERE id = :cityId")
    suspend fun deleteFavoriteById(cityId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_cities WHERE id = :cityId)")
    suspend fun isFavorite(cityId: Long): Boolean
}
