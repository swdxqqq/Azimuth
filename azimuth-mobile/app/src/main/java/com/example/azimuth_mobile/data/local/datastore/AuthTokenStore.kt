package com.example.azimuth_mobile.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.azimuth_mobile.domain.model.AuthToken
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun save(token: AuthToken) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = token.accessToken
            preferences[TOKEN_TYPE] = token.tokenType
            preferences[USER_ID] = token.userId
        }
    }

    suspend fun getAuthorizationHeader(): String? {
        val preferences = dataStore.data.first()
        val accessToken = preferences[ACCESS_TOKEN] ?: return null
        val tokenType = preferences[TOKEN_TYPE].orEmpty().ifBlank { "Bearer" }
        return "$tokenType $accessToken"
    }

    suspend fun getUserId(): Int? = dataStore.data.first()[USER_ID]

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val TOKEN_TYPE = stringPreferencesKey("token_type")
        private val USER_ID = intPreferencesKey("user_id")
    }
}
