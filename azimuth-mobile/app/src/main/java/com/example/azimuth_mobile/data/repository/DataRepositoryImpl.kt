package com.example.azimuth_mobile.data.repository

import com.example.azimuth_mobile.data.mapper.toDomain
import com.example.azimuth_mobile.data.mapper.toEconomicIndicators
import com.example.azimuth_mobile.data.remote.AzimuthApi
import com.example.azimuth_mobile.domain.model.CountryCurrencyRate
import com.example.azimuth_mobile.domain.model.NewsArticle
import com.example.azimuth_mobile.domain.repository.DataRepository
import javax.inject.Inject

class DataRepositoryImpl @Inject constructor(
    private val api: AzimuthApi
) : DataRepository {

    override suspend fun getCurrencyRate(countryName: String): CountryCurrencyRate {
        return api.getCurrencyRate(countryName).toDomain()
    }

    override suspend fun getCountryEconomy(countryName: String) =
        api.getCountryEconomy(countryName).toEconomicIndicators()

    override suspend fun getNews(countryName: String, limit: Int): List<NewsArticle> {
        return api.getNews(countryName, limit).articles.map { it.toDomain() }
    }
}
