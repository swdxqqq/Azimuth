package com.example.azimuth_mobile.domain.repository

import com.example.azimuth_mobile.domain.model.AuthToken

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthToken
    suspend fun register(email: String, password: String): AuthToken
}
