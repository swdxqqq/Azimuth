package com.example.azimuth_mobile.domain.usecase

import com.example.azimuth_mobile.domain.model.EconomicIndicatorDetail
import com.example.azimuth_mobile.domain.repository.DataRepository
import javax.inject.Inject

class GetCountryEconomyUseCase @Inject constructor(
    private val repository: DataRepository
) {
    suspend operator fun invoke(countryName: String): Map<String, EconomicIndicatorDetail> =
        repository.getCountryEconomy(countryName)
}
