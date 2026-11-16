package com.example.azimuth_mobile.domain.usecase

import com.example.azimuth_mobile.domain.model.CountryListItem
import com.example.azimuth_mobile.domain.model.PagedResult
import com.example.azimuth_mobile.domain.repository.CountryRepository
import javax.inject.Inject

class GetCountriesUseCase @Inject constructor(
    private val repository: CountryRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        limit: Int = 20,
        region: String? = null
    ): PagedResult<CountryListItem> {
        return repository.getCountries(page, limit, region)
    }
}
