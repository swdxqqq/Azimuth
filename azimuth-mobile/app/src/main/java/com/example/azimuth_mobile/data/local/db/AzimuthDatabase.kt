package com.example.azimuth_mobile.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.azimuth_mobile.data.local.dao.CountryDao
import com.example.azimuth_mobile.data.local.dao.FavoriteDao
import com.example.azimuth_mobile.data.local.entity.CountryEntity
import com.example.azimuth_mobile.data.local.entity.FavoriteEntity

@Database(
    entities = [CountryEntity::class, FavoriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AzimuthDatabase : RoomDatabase() {
    abstract fun countryDao(): CountryDao
    abstract fun favoriteDao(): FavoriteDao
}
