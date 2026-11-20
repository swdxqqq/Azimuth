package com.example.azimuth_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RecommendationRequestDto(
    val preferences: List<PreferenceDto>,
    @SerializedName("temperature_preference") val temperaturePreference: TemperaturePreferenceDto?,
    @SerializedName("top_n") val topN: Int
)

data class PreferenceDto(
    val metric: String,
    val priority: Int,
    val enabled: Boolean
)

data class TemperaturePreferenceDto(
    val target: Double,
    val enabled: Boolean
)

data class RecommendationResponseDto(
    val recommendations: List<RecommendationDto>,
    val algorithm: String,
    val parameters: Map<String, Double>
)

data class RecommendationDto(
    @SerializedName("country_id") val countryId: Int,
    val name: String,
    val score: Double,
    val breakdown: Map<String, Double>,
    val confidence: Double?
)
