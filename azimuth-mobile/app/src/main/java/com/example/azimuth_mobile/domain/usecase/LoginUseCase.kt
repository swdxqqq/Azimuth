package com.example.azimuth_mobile.domain.usecase

import com.example.azimuth_mobile.domain.model.AuthToken
import com.example.azimuth_mobile.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthToken {
        return repository.login(email, password)
    }
}
