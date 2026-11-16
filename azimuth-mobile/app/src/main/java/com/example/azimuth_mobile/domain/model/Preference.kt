package com.example.azimuth_mobile.domain.model

data class Preference(
    val metric: String,
    val priority: Int,
    val enabled: Boolean
)

data class TemperaturePreference(
    val target: Double,
    val enabled: Boolean
)

data class UserPreferences(
    val preferences: List<Preference>,
    val temperaturePreference: TemperaturePreference?,
    val updatedAt: String?
)

data class PreferencesUpdateRequest(
    val preferences: List<Preference>,
    val temperaturePreference: TemperaturePreference?
)
