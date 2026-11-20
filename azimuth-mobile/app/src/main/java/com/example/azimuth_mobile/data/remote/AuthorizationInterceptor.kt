package com.example.azimuth_mobile.data.remote

import com.example.azimuth_mobile.data.local.datastore.AuthTokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthorizationInterceptor @Inject constructor(
    private val tokenStore: AuthTokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val isAuthenticationRequest = path.endsWith("/auth/login") ||
            path.endsWith("/auth/register")
        val authorization = if (isAuthenticationRequest) {
            null
        } else {
            runBlocking { tokenStore.getAuthorizationHeader() }
        }

        val authorizedRequest = if (authorization == null) {
            request
        } else {
            request.newBuilder()
                .header("Authorization", authorization)
                .build()
        }

        return chain.proceed(authorizedRequest)
    }
}
