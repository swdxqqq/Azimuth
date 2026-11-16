package com.example.azimuth_mobile.domain.usecase

import com.example.azimuth_mobile.domain.model.FavoriteCountry
import com.example.azimuth_mobile.domain.model.PagedResult
import com.example.azimuth_mobile.domain.repository.UserRepository
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): PagedResult<FavoriteCountry> {
        return repository.getFavorites()
    }
}
