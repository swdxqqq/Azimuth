package com.example.azimuth_mobile.domain.repository

import com.example.azimuth_mobile.domain.model.FavoriteCountry
import com.example.azimuth_mobile.domain.model.PagedResult
import com.example.azimuth_mobile.domain.model.Preference
import com.example.azimuth_mobile.domain.model.PreferencesUpdateRequest
import com.example.azimuth_mobile.domain.model.TemperaturePreference
import com.example.azimuth_mobile.domain.model.UserPreferences

interface UserRepository {
    suspend fun getFavorites(): PagedResult<FavoriteCountry>
    suspend fun addFavorite(countryId: Int)
    suspend fun removeFavorite(countryId: Int)
    suspend fun getPreferences(): UserPreferences
    suspend fun updatePreferences(request: PreferencesUpdateRequest): UserPreferences
}
