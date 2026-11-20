package com.example.azimuth_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CountryCurrencyRateResponseDto(
    val country: String,
    @SerializedName("rate_to_usd") val rateToUsd: Double
)

data class NewsListResponseDto(
    val country: String,
    val count: Int,
    val articles: List<NewsArticleDto>
)
