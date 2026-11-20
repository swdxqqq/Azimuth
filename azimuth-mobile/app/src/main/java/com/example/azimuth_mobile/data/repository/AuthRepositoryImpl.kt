package com.example.azimuth_mobile.data.repository

import com.example.azimuth_mobile.data.local.dao.FavoriteDao
import com.example.azimuth_mobile.data.local.datastore.AuthTokenStore
import com.example.azimuth_mobile.data.mapper.toDomain
import com.example.azimuth_mobile.data.remote.AzimuthApi
import com.example.azimuth_mobile.data.remote.dto.AuthRequestDto
import com.example.azimuth_mobile.domain.model.AuthToken
import com.example.azimuth_mobile.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AzimuthApi,
    private val authTokenStore: AuthTokenStore,
    private val favoriteDao: FavoriteDao
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthToken {
        return persistSession(api.login(AuthRequestDto(email, password)).toDomain())
    }

    override suspend fun register(email: String, password: String): AuthToken {
        return persistSession(api.register(AuthRequestDto(email, password)).toDomain())
    }

    private suspend fun persistSession(token: AuthToken): AuthToken {
        if (authTokenStore.getUserId() != token.userId) {
            favoriteDao.deleteAll()
        }
        authTokenStore.save(token)
        return token
    }
}
