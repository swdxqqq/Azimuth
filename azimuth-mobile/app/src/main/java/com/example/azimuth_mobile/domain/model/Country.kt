package com.example.azimuth_mobile.domain.model

data class Country(
    val id: Int,
    val name: String,
    val code: String,
    val capital: String?,
    val region: String,
    val flagUrl: String?,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class CountryMetrics(
    val costOfLiving: Double?,
    val rentIndex: Double?,
    val safety: Double?,
    val healthCare: Double?,
    val pollution: Double?,
    val qualityOfLife: Double?,
    val climate: Double?,
    val purchasingPower: Double?,
    val propertyPriceToIncome: Double?,
    val visaScore: Int?,
    val internetSpeed: Double?
)

data class CountryListItem(
    val country: Country,
    val metrics: CountryMetrics
)

data class EconomicIndicator(
    val year: Int,
    val gdpPerCapita: Double,
    val inflation: Double,
    val unemployment: Double
)

data class

data class CountryDetails(
    val country: Country,
    val metrics: CountryMetrics,
    val economicIndicators:
)