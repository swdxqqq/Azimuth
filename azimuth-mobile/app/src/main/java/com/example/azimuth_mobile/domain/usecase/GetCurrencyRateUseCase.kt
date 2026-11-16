package com.example.azimuth_mobile.domain.usecase

import com.example.azimuth_mobile.domain.model.Country
import com.example.azimuth_mobile.domain.model.CountryCurrencyRate
import com.example.azimuth_mobile.domain.repository.DataRepository
import javax.inject.Inject

class GetCurrencyRateUseCase @Inject constructor(
    private val repository: DataRepository
) {
    suspend operator fun invoke(country: Country): CountryCurrencyRate {
        return repository.getCurrencyRate(country.name)
    }
}
