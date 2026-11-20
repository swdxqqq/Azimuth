package com.example.azimuth_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CountryListResponseDto(
    val countries: List<CountryListItemDto>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class CountryListItemDto(
    val id: Int,
    val name: String,
    val code: String,
    val capital: String,
    val region: String,
    @SerializedName("flag_url") val flagUrl: String,
    val metrics: CountryMetricsDto? = null
)

data class CountryMetricsDto(
    @SerializedName("cost_of_living") val costOfLiving: Double?,
    @SerializedName(value = "rent", alternate = ["rent_index"]) val rentIndex: Double?,
    val safety: Double?,
    val healthcare: Double?,
    val pollution: Double?,
    @SerializedName("quality_of_life") val qualityOfLife: Double?,
    val climate: Double?,
    @SerializedName("purchasing_power") val purchasingPower: Double?,
    @SerializedName("property_price_to_income") val propertyPriceToIncome: Double?,
    @SerializedName("visa_score") val visaScore: Int?,
    @SerializedName("internet_speed") val internetSpeed: Double?
)

data class CountryDetailResponseDto(
    val id: Int,
    val name: String,
    val code: String,
    val capital: String,
    val region: String,
    @SerializedName("flag_url") val flagUrl: String,
    val latitude: Double,
    val longitude: Double,
    val metrics: CountryMetricsDto?,
    @SerializedName("economic_indicators") val economicIndicators: Map<String, EconomicIndicatorDetailDto>?,
    val news: List<NewsArticleDto>?
)

data class EconomicIndicatorDetailDto(
    val value: Double,
    val year: Int,
    val source: String?
)

data class NewsArticleDto(
    val title: String,
    val description: String?,
    val url: String,
    val category: String?,
    @SerializedName("published_at") val publishedAt: String,
    val source: String?
)
