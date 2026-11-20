package com.example.azimuth_mobile.data.di

import android.content.Context
import androidx.room.Room
import com.example.azimuth_mobile.data.local.dao.CountryDao
import com.example.azimuth_mobile.data.local.dao.FavoriteDao
import com.example.azimuth_mobile.data.local.db.AzimuthDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AzimuthDatabase {
        return Room.databaseBuilder(
            context,
            AzimuthDatabase::class.java,
            "azimuth_database"
        ).build()
    }

    @Provides
    fun provideCountryDao(database: AzimuthDatabase): CountryDao {
        return database.countryDao()
    }

    @Provides
    fun provideFavoriteDao(database: AzimuthDatabase): FavoriteDao {
        return database.favoriteDao()
    }
}
