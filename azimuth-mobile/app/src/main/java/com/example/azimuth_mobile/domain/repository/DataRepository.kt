package com.example.azimuth_mobile.domain.repository

import com.example.azimuth_mobile.domain.model.CountryCurrencyRate
import com.example.azimuth_mobile.domain.model.NewsArticle

interface DataRepository {
    suspend fun getCurrencyRate(countryName: String): CountryCurrencyRate
    suspend fun getNews(countryName: String, limit: Int): List<NewsArticle>
}
