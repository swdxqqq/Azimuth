package com.example.azimuth_mobile.domain.usecase

import com.example.azimuth_mobile.domain.model.UserPreferences
import com.example.azimuth_mobile.domain.repository.UserRepository
import javax.inject.Inject

class GetPreferencesUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): UserPreferences {
        return repository.getPreferences()
    }
}
