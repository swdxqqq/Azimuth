package com.example.azimuth_mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.azimuth_mobile.data.local.entity.CountryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {
    @Query("SELECT * FROM countries")
    fun observeAll(): Flow<List<CountryEntity>>

    @Query("SELECT * FROM countries")
    suspend fun getAll(): List<CountryEntity>

    @Query("SELECT * FROM countries WHERE id = :id")
    suspend fun getById(id: Int): CountryEntity?

    @Query("SELECT * FROM countries WHERE region = :region")
    fun getByRegion(region: String): Flow<List<CountryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(countries: List<CountryEntity>)

    @Query("DELETE FROM countries")
    suspend fun deleteAll()
}
