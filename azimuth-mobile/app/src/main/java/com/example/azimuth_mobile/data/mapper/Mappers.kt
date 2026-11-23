package com.example.azimuth_mobile.data.mapper

import com.example.azimuth_mobile.data.local.entity.CountryEntity
import com.example.azimuth_mobile.data.local.entity.FavoriteEntity
import com.example.azimuth_mobile.data.remote.BackendConfig
import com.example.azimuth_mobile.data.remote.dto.AuthResponseDto
import com.example.azimuth_mobile.data.remote.dto.CountryCurrencyRateResponseDto
import com.example.azimuth_mobile.data.remote.dto.CountryDetailResponseDto
import com.example.azimuth_mobile.data.remote.dto.CountryListItemDto
import com.example.azimuth_mobile.data.remote.dto.CountryMetricsDto
import com.example.azimuth_mobile.data.remote.dto.EconomyResponseDto
import com.example.azimuth_mobile.data.remote.dto.FavoriteCountryDto
import com.example.azimuth_mobile.data.remote.dto.NewsArticleDto
import com.example.azimuth_mobile.data.remote.dto.PreferenceDto
import com.example.azimuth_mobile.data.remote.dto.RecommendationDto
import com.example.azimuth_mobile.data.remote.dto.TemperaturePreferenceDto
import com.example.azimuth_mobile.data.remote.dto.UserPreferencesResponseDto
import com.example.azimuth_mobile.domain.model.AuthToken
import com.example.azimuth_mobile.domain.model.Country
import com.example.azimuth_mobile.domain.model.CountryCurrencyRate
import com.example.azimuth_mobile.domain.model.CountryDetails
import com.example.azimuth_mobile.domain.model.CountryListItem
import com.example.azimuth_mobile.domain.model.CountryMetrics
import com.example.azimuth_mobile.domain.model.EconomicIndicatorDetail
import com.example.azimuth_mobile.domain.model.FavoriteCountry
import com.example.azimuth_mobile.domain.model.NewsArticle
import com.example.azimuth_mobile.domain.model.Preference
import com.example.azimuth_mobile.domain.model.Recommendation
import com.example.azimuth_mobile.domain.model.TemperaturePreference
import com.example.azimuth_mobile.domain.model.UserPreferences

// CountryListItem
fun CountryListItemDto.toDomain(): CountryListItem = CountryListItem(
    country = Country(
        id = id,
        name = name,
        code = code,
        capital = capital,
        region = region,
        flagUrl = BackendConfig.resolveAssetUrl(flagUrl)
    ),
    metrics = metrics?.toDomain()
)

// CountryMetrics
fun CountryMetricsDto.toDomain(): CountryMetrics = CountryMetrics(
    costOfLiving = costOfLiving,
    rentIndex = rentIndex,
    safety = safety,
    healthCare = healthcare,
    pollution = pollution,
    qualityOfLife = qualityOfLife,
    climate = climate,
    purchasingPower = purchasingPower,
    propertyPriceToIncome = propertyPriceToIncome,
    visaScore = visaScore,
    internetSpeed = internetSpeed
)

// CountryDetail
fun CountryDetailResponseDto.toDomain(): CountryDetails = CountryDetails(
    country = Country(
        id = id,
        name = name,
        code = code,
        capital = capital,
        region = region,
        flagUrl = BackendConfig.resolveAssetUrl(flagUrl),
        latitude = latitude,
        longitude = longitude
    ),
    metrics = metrics?.toDomain(),
    economicIndicators = economicIndicators?.mapValues { (_, detail) ->
        EconomicIndicatorDetail(
            value = detail.value,
            year = detail.year,
            source = detail.source
        )
    },
    news = news?.map { it.toDomain() },
    population = population,
    languages = languages,
    timezone = timezone
)

fun EconomyResponseDto.toEconomicIndicators(): Map<String, EconomicIndicatorDetail> = buildMap {
    gdpPerCapita?.let {
        put("gdp_per_capita", EconomicIndicatorDetail(it, year, "IMF WEO"))
    }
    inflation?.let { put("inflation", EconomicIndicatorDetail(it, year, "IMF WEO")) }
    unemployment?.let { put("unemployment", EconomicIndicatorDetail(it, year, "IMF WEO")) }
}

// NewsArticle
fun NewsArticleDto.toDomain(): NewsArticle = NewsArticle(
    title = title,
    description = description,
    url = url,
    category = category,
    publishedAt = publishedAt,
    source = source
)

// CountryListItem → CountryEntity (для кэширования)
fun CountryListItemDto.toEntity(): CountryEntity = CountryEntity(
    id = id,
    name = name,
    code = code,
    capital = capital,
    region = region,
    flagUrl = flagUrl,
    costOfLiving = metrics?.costOfLiving,
    rentIndex = metrics?.rentIndex,
    safety = metrics?.safety,
    healthcare = metrics?.healthcare,
    pollution = metrics?.pollution,
    qualityOfLife = metrics?.qualityOfLife,
    climate = metrics?.climate,
    purchasingPower = metrics?.purchasingPower,
    propertyPriceToIncome = metrics?.propertyPriceToIncome,
    visaScore = metrics?.visaScore,
    internetSpeed = metrics?.internetSpeed
)

// CountryEntity → CountryListItem (из кэша)
fun CountryEntity.toDomainListItem(): CountryListItem = CountryListItem(
    country = Country(
        id = id,
        name = name,
        code = code,
        capital = capital,
        region = region,
        flagUrl = BackendConfig.resolveAssetUrl(flagUrl),
        latitude = latitude,
        longitude = longitude
    ),
    metrics = CountryMetrics(
        costOfLiving = costOfLiving,
        rentIndex = rentIndex,
        safety = safety,
        healthCare = healthcare,
        pollution = pollution,
        qualityOfLife = qualityOfLife,
        climate = climate,
        purchasingPower = purchasingPower,
        propertyPriceToIncome = propertyPriceToIncome,
        visaScore = visaScore,
        internetSpeed = internetSpeed
    )
)

// FavoriteCountry
fun FavoriteCountryDto.toDomain(): FavoriteCountry = FavoriteCountry(
    id = id,
    name = name,
    code = code,
    capital = capital,
    flagUrl = BackendConfig.resolveAssetUrl(flagUrl).orEmpty(),
    addedAt = addedAt
)

fun FavoriteEntity.toDomain(): FavoriteCountry = FavoriteCountry(
    id = countryId,
    name = name,
    code = code,
    capital = capital,
    flagUrl = BackendConfig.resolveAssetUrl(flagUrl).orEmpty(),
    addedAt = addedAt
)

// Recommendation
fun RecommendationDto.toDomain(): Recommendation = Recommendation(
    countryId = countryId,
    name = name,
    score = score,
    breakdown = breakdown,
    confidence = confidence
)

// Preference
fun Preference.toDto(): PreferenceDto = PreferenceDto(
    metric = metric,
    priority = priority,
    enabled = enabled
)

fun PreferenceDto.toDomain(): Preference = Preference(
    metric = metric,
    priority = priority,
    enabled = enabled
)

// TemperaturePreference
fun TemperaturePreference.toDto(): TemperaturePreferenceDto = TemperaturePreferenceDto(
    target = target,
    enabled = enabled
)

fun TemperaturePreferenceDto.toDomain(): TemperaturePreference = TemperaturePreference(
    target = target,
    enabled = enabled
)

// CurrencyRate
fun CountryCurrencyRateResponseDto.toDomain(): CountryCurrencyRate = CountryCurrencyRate(
    country = country,
    currencyCode = currencyCode,
    rateToUsd = rateToUsd
)

// UserPreferences
fun UserPreferencesResponseDto.toDomain(): UserPreferences = UserPreferences(
    preferences = preferences.map { it.toDomain() },
    temperaturePreference = temperaturePreference?.toDomain(),
    updatedAt = updatedAt
)

// AuthToken
fun AuthResponseDto.toDomain(): AuthToken = AuthToken(
    accessToken = accessToken,
    tokenType = tokenType,
    expiresIn = expiresIn,
    userId = userId
)
