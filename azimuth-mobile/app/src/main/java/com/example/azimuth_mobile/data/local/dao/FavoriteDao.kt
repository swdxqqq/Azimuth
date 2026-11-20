package com.example.azimuth_mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.azimuth_mobile.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(favorites: List<FavoriteEntity>)

    @Query("SELECT * FROM favorites")
    suspend fun getAll(): List<FavoriteEntity>

    @Query("DELETE FROM favorites")
    suspend fun deleteAll()

    @Query("DELETE FROM favorites WHERE countryId = :countryId")
    suspend fun deleteByCountryId(countryId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE countryId = :countryId)")
    suspend fun isFavorite(countryId: Int): Boolean
}
