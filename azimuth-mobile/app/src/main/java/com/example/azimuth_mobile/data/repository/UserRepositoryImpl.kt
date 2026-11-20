package com.example.azimuth_mobile.data.repository

import com.example.azimuth_mobile.data.local.dao.CountryDao
import com.example.azimuth_mobile.data.local.dao.FavoriteDao
import com.example.azimuth_mobile.data.local.entity.FavoriteEntity
import com.example.azimuth_mobile.data.mapper.toDomain
import com.example.azimuth_mobile.data.mapper.toDto
import com.example.azimuth_mobile.data.remote.AzimuthApi
import com.example.azimuth_mobile.data.remote.dto.PreferencesUpdateRequestDto
import com.example.azimuth_mobile.domain.model.FavoriteCountry
import com.example.azimuth_mobile.domain.model.PagedResult
import com.example.azimuth_mobile.domain.model.Preference
import com.example.azimuth_mobile.domain.model.PreferencesUpdateRequest
import com.example.azimuth_mobile.domain.model.TemperaturePreference
import com.example.azimuth_mobile.domain.model.UserPreferences
import com.example.azimuth_mobile.domain.repository.UserRepository
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: AzimuthApi,
    private val favoriteDao: FavoriteDao,
    private val countryDao: CountryDao
) : UserRepository {

    override suspend fun getFavorites(): PagedResult<FavoriteCountry> {
        return try {
            val response = api.getFavorites()
            val favorites = response.favorites.map { favorite ->
                FavoriteEntity(
                    countryId = favorite.id,
                    name = favorite.name,
                    code = favorite.code,
                    capital = favorite.capital,
                    flagUrl = favorite.flagUrl,
                    addedAt = favorite.addedAt
                )
            }
            favoriteDao.deleteAll()
            favoriteDao.insertAll(favorites)

            PagedResult(
                items = response.favorites.map { it.toDomain() },
                total = response.total,
                page = 1
            )
        } catch (error: IOException) {
            val cached = favoriteDao.getAll()
            if (cached.isEmpty()) throw error

            PagedResult(
                items = cached.map { it.toDomain() },
                total = cached.size,
                page = 1
            )
        }
    }

    override suspend fun addFavorite(countryId: Int) {
        val response = api.addFavorite(countryId)
        val country = countryDao.getById(countryId)
        val addedAt = response.addedAt
        if (country != null && addedAt != null) {
            favoriteDao.insert(
                FavoriteEntity(
                    countryId = country.id,
                    name = country.name,
                    code = country.code,
                    capital = country.capital,
                    flagUrl = country.flagUrl,
                    addedAt = addedAt
                )
            )
        }
    }

    override suspend fun removeFavorite(countryId: Int) {
        api.removeFavorite(countryId)
        favoriteDao.deleteByCountryId(countryId)
    }

    override suspend fun getPreferences(): UserPreferences {
        return api.getPreferences().toDomain()
    }

    override suspend fun updatePreferences(request: PreferencesUpdateRequest): UserPreferences {
        val requestDto = PreferencesUpdateRequestDto(
            preferences = request.preferences.map { it.toDto() },
            temperaturePreference = request.temperaturePreference?.toDto()
        )
        return api.updatePreferences(requestDto).toDomain()
    }
}
