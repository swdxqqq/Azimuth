package com.example.azimuth_mobile.domain.model

data class Recommendation(
    val countryId: Int,
    val name: String,
    val score: Double,
    val breakdown: Map<String, Double>,
    val confidence: Double?
)

data class RecommendationResult(
    val recommendations: List<Recommendation>,
    val algorithm: String,
    val parameters: Map<String, Double>
)
