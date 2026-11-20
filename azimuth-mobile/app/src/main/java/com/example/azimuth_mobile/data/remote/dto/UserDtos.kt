package com.example.azimuth_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthRequestDto(
    val email: String,
    val password: String
)

data class AuthResponseDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("user_id") val userId: Int
)

data class FavoriteListResponseDto(
    val favorites: List<FavoriteCountryDto>,
    val total: Int
)

data class FavoriteCountryDto(
    val id: Int,
    val name: String,
    val code: String,
    val capital: String,
    @SerializedName("flag_url") val flagUrl: String,
    @SerializedName("added_at") val addedAt: String
)

data class FavoriteResponseDto(
    val message: String,
    @SerializedName("country_id") val countryId: Int,
    @SerializedName("added_at") val addedAt: String? = null
)

data class UserPreferencesResponseDto(
    val preferences: List<PreferenceDto>,
    @SerializedName("temperature_preference") val temperaturePreference: TemperaturePreferenceDto?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class PreferencesUpdateRequestDto(
    val preferences: List<PreferenceDto>,
    @SerializedName("temperature_preference") val temperaturePreference: TemperaturePreferenceDto?
)
