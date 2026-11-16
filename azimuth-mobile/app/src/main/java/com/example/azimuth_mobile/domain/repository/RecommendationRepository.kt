package com.example.azimuth_mobile.domain.repository

import com.example.azimuth_mobile.domain.model.Preference
import com.example.azimuth_mobile.domain.model.PreferencesUpdateRequest
import com.example.azimuth_mobile.domain.model.RecommendationResult
import com.example.azimuth_mobile.domain.model.TemperaturePreference

interface RecommendationRepository {
    suspend fun getRecommendations(
        preferences: List<Preference>,
        temperaturePreference: TemperaturePreference?,
        topN: Int
    ): RecommendationResult
}
