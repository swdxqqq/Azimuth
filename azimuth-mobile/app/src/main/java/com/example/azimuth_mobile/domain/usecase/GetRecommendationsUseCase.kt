package com.example.azimuth_mobile.domain.usecase

import com.example.azimuth_mobile.domain.model.Preference
import com.example.azimuth_mobile.domain.model.RecommendationResult
import com.example.azimuth_mobile.domain.model.TemperaturePreference
import com.example.azimuth_mobile.domain.repository.RecommendationRepository
import javax.inject.Inject

class GetRecommendationsUseCase @Inject constructor(
    private val repository: RecommendationRepository
) {
    suspend operator fun invoke(
        preferences: List<Preference>,
        temperaturePreference: TemperaturePreference? = null,
        topN: Int = 10
    ): RecommendationResult {
        return repository.getRecommendations(preferences, temperaturePreference, topN)
    }
}
