package com.example.azimuth_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CountryCurrencyRateResponseDto(
    val country: String,
    @SerializedName("currency_code") val currencyCode: String? = null,
    @SerializedName("rate_to_usd") val rateToUsd: Double
)

data class EconomyResponseDto(
    @SerializedName("country_id") val countryId: Int,
    val year: Int,
    @SerializedName("gdp_per_capita") val gdpPerCapita: Double?,
    val inflation: Double?,
    val unemployment: Double?
)

data class NewsListResponseDto(
    val country: String,
    val count: Int,
    val articles: List<NewsArticleDto>
)
