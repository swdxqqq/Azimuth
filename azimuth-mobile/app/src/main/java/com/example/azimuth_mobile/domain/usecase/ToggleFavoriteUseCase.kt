package com.example.azimuth_mobile.domain.usecase

import com.example.azimuth_mobile.domain.repository.UserRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend fun add(countryId: Int) = repository.addFavorite(countryId)
    suspend fun remove(countryId: Int) = repository.removeFavorite(countryId)
}
