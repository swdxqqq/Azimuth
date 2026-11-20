package com.example.azimuth_mobile.data.repository

import com.example.azimuth_mobile.data.mapper.toDomain
import com.example.azimuth_mobile.data.mapper.toDto
import com.example.azimuth_mobile.data.remote.AzimuthApi
import com.example.azimuth_mobile.data.remote.dto.RecommendationRequestDto
import com.example.azimuth_mobile.domain.model.Preference
import com.example.azimuth_mobile.domain.model.RecommendationResult
import com.example.azimuth_mobile.domain.model.TemperaturePreference
import com.example.azimuth_mobile.domain.repository.RecommendationRepository
import javax.inject.Inject

class RecommendationRepositoryImpl @Inject constructor(
    private val api: AzimuthApi
) : RecommendationRepository {

    override suspend fun getRecommendations(
        preferences: List<Preference>,
        temperaturePreference: TemperaturePreference?,
        topN: Int
    ): RecommendationResult {
        val request = RecommendationRequestDto(
            preferences = preferences.map { it.toDto() },
            temperaturePreference = temperaturePreference?.toDto(),
            topN = topN
        )
        val response = api.getRecommendations(request)

        return RecommendationResult(
            recommendations = response.recommendations.map { it.toDomain() },
            algorithm = response.algorithm,
            parameters = response.parameters
        )
    }
}
