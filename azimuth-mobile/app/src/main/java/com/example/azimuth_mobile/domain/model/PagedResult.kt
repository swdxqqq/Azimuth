package com.example.azimuth_mobile.domain.model

data class PagedResult<T>(
    val items: List<T>,
    val total: Int,
    val page: Int
)
