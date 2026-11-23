package com.example.azimuth_mobile.data.remote

import com.example.azimuth_mobile.data.remote.dto.AuthRequestDto
import com.example.azimuth_mobile.data.remote.dto.AuthResponseDto
import com.example.azimuth_mobile.data.remote.dto.CountryCurrencyRateResponseDto
import com.example.azimuth_mobile.data.remote.dto.CountryDetailResponseDto
import com.example.azimuth_mobile.data.remote.dto.CountryListResponseDto
import com.example.azimuth_mobile.data.remote.dto.EconomyResponseDto
import com.example.azimuth_mobile.data.remote.dto.FavoriteListResponseDto
import com.example.azimuth_mobile.data.remote.dto.FavoriteResponseDto
import com.example.azimuth_mobile.data.remote.dto.NewsListResponseDto
import com.example.azimuth_mobile.data.remote.dto.PreferencesUpdateRequestDto
import com.example.azimuth_mobile.data.remote.dto.RecommendationRequestDto
import com.example.azimuth_mobile.data.remote.dto.RecommendationResponseDto
import com.example.azimuth_mobile.data.remote.dto.UserPreferencesResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AzimuthApi {

    @GET("countries")
    suspend fun getCountries(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("region") region: String? = null
    ): CountryListResponseDto

    @GET("countries/{id}")
    suspend fun getCountryDetail(@Path("id") id: Int): CountryDetailResponseDto

    @POST("recommend")
    suspend fun getRecommendations(@Body request: RecommendationRequestDto): RecommendationResponseDto

    @GET("currency/rates/country/{countryName}")
    suspend fun getCurrencyRate(@Path("countryName") countryName: String): CountryCurrencyRateResponseDto

    @GET("economy/{countryName}")
    suspend fun getCountryEconomy(@Path("countryName") countryName: String): EconomyResponseDto

    @GET("news/{countryName}")
    suspend fun getNews(
        @Path("countryName") countryName: String,
        @Query("limit") limit: Int = 10
    ): NewsListResponseDto

    @POST("auth/register")
    suspend fun register(@Body request: AuthRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequestDto): AuthResponseDto

    @GET("user/favorites")
    suspend fun getFavorites(): FavoriteListResponseDto

    @POST("user/favorites/{countryId}")
    suspend fun addFavorite(@Path("countryId") countryId: Int): FavoriteResponseDto

    @DELETE("user/favorites/{countryId}")
    suspend fun removeFavorite(@Path("countryId") countryId: Int): FavoriteResponseDto

    @GET("user/preferences")
    suspend fun getPreferences(): UserPreferencesResponseDto

    @PUT("user/preferences")
    suspend fun updatePreferences(@Body request: PreferencesUpdateRequestDto): UserPreferencesResponseDto
}
