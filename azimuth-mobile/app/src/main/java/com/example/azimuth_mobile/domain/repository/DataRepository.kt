package com.example.azimuth_mobile.domain.repository

import com.example.azimuth_mobile.domain.model.CountryCurrencyRate
import com.example.azimuth_mobile.domain.model.EconomicIndicatorDetail
import com.example.azimuth_mobile.domain.model.NewsArticle

interface DataRepository {
    suspend fun getCurrencyRate(countryName: String): CountryCurrencyRate
    suspend fun getCountryEconomy(countryName: String): Map<String, EconomicIndicatorDetail>
    suspend fun getNews(countryName: String, limit: Int): List<NewsArticle>
}
