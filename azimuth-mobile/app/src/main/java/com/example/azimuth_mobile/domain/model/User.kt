package com.example.azimuth_mobile.domain.model

data class AuthToken(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val userId: Int
)

data class FavoriteCountry(
    val id: Int,
    val name: String,
    val code: String,
    val capital: String,
    val flagUrl: String,
    val addedAt: String
)
