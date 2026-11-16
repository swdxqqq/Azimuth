package com.example.azimuth_mobile.domain.repository

import com.example.azimuth_mobile.domain.model.CountryDetails
import com.example.azimuth_mobile.domain.model.CountryListItem
import com.example.azimuth_mobile.domain.model.PagedResult

interface CountryRepository {
    suspend fun getCountries(page: Int, limit: Int, region: String?): PagedResult<CountryListItem>
    suspend fun getCountryDetail(id: Int): CountryDetails
}
