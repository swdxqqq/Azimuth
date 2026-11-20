package com.example.azimuth_mobile.data.repository

import com.example.azimuth_mobile.data.local.dao.CountryDao
import com.example.azimuth_mobile.data.mapper.toDomain
import com.example.azimuth_mobile.data.mapper.toDomainListItem
import com.example.azimuth_mobile.data.mapper.toEntity
import com.example.azimuth_mobile.data.remote.AzimuthApi
import com.example.azimuth_mobile.domain.model.CountryDetails
import com.example.azimuth_mobile.domain.model.CountryListItem
import com.example.azimuth_mobile.domain.model.PagedResult
import com.example.azimuth_mobile.domain.repository.CountryRepository
import java.io.IOException
import javax.inject.Inject

class CountryRepositoryImpl @Inject constructor(
    private val api: AzimuthApi,
    private val countryDao: CountryDao
) : CountryRepository {

    override suspend fun getCountries(
        page: Int,
        limit: Int,
        region: String?
    ): PagedResult<CountryListItem> {
        return try {
            val response = api.getCountries(page, limit, region)
            countryDao.insertAll(response.countries.map { it.toEntity() })

            PagedResult(
                items = response.countries.map { it.toDomain() },
                total = response.total,
                page = response.page
            )
        } catch (error: IOException) {
            val cached = countryDao.getAll()
                .filter { region == null || it.region.equals(region, ignoreCase = true) }
                .sortedBy { it.name }

            if (cached.isEmpty()) throw error

            val pageItems = cached.drop((page - 1) * limit).take(limit)
            PagedResult(
                items = pageItems.map { it.toDomainListItem() },
                total = cached.size,
                page = page
            )
        }
    }

    override suspend fun getCountryDetail(id: Int): CountryDetails {
        return api.getCountryDetail(id).toDomain()
    }
}
