package com.example.azimuth_mobile.domain.usecase

import com.example.azimuth_mobile.domain.model.PreferencesUpdateRequest
import com.example.azimuth_mobile.domain.model.TemperaturePreference
import com.example.azimuth_mobile.domain.model.UserPreferences
import com.example.azimuth_mobile.domain.repository.UserRepository
import javax.inject.Inject

class UpdatePreferencesUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        request: PreferencesUpdateRequest
    ): UserPreferences {
        return repository.updatePreferences(request)
    }
}
