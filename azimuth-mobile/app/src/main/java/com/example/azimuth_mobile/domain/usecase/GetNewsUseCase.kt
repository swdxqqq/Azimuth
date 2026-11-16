package com.example.azimuth_mobile.domain.usecase

import com.example.azimuth_mobile.domain.model.Country
import com.example.azimuth_mobile.domain.model.NewsArticle
import com.example.azimuth_mobile.domain.repository.DataRepository
import javax.inject.Inject

class GetNewsUseCase @Inject constructor(
    private val repository: DataRepository
) {
    suspend operator fun invoke(country: Country, limit: Int = 10): List<NewsArticle> {
        return repository.getNews(country.name, limit)
    }
}
