package com.example.azimuth_mobile.data.di

import android.content.Context
import android.content.pm.ApplicationInfo
import com.example.azimuth_mobile.data.remote.AuthorizationInterceptor
import com.example.azimuth_mobile.data.remote.AzimuthApi
import com.example.azimuth_mobile.data.repository.AuthRepositoryImpl
import com.example.azimuth_mobile.data.repository.CountryRepositoryImpl
import com.example.azimuth_mobile.data.repository.DataRepositoryImpl
import com.example.azimuth_mobile.data.repository.RecommendationRepositoryImpl
import com.example.azimuth_mobile.data.repository.UserRepositoryImpl
import com.example.azimuth_mobile.domain.repository.AuthRepository
import com.example.azimuth_mobile.domain.repository.CountryRepository
import com.example.azimuth_mobile.domain.repository.DataRepository
import com.example.azimuth_mobile.domain.repository.RecommendationRepository
import com.example.azimuth_mobile.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        authorizationInterceptor: AuthorizationInterceptor
    ): OkHttpClient {
        val isDebuggable =
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        return OkHttpClient.Builder()
            .addInterceptor(authorizationInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (isDebuggable) {
                        HttpLoggingInterceptor.Level.BASIC
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://azimuth-5rph.onrender.com/api/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAzimuthApi(retrofit: Retrofit): AzimuthApi {
        return retrofit.create(AzimuthApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCountryRepository(impl: CountryRepositoryImpl): CountryRepository

    @Binds
    @Singleton
    abstract fun bindRecommendationRepository(impl: RecommendationRepositoryImpl): RecommendationRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDataRepository(impl: DataRepositoryImpl): DataRepository
}
