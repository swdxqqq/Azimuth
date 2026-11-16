package com.example.azimuth_mobile.domain.usecase

import com.example.azimuth_mobile.domain.model.CountryDetails
import com.example.azimuth_mobile.domain.repository.CountryRepository
import javax.inject.Inject

class GetCountryDetailsUseCase @Inject constructor(
    private val repository: CountryRepository
) {
    suspend operator fun invoke(id: Int): CountryDetails {
        return repository.getCountryDetail(id)
    }
}
