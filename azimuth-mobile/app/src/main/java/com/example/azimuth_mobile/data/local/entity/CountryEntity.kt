package com.example.azimuth_mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countries")
data class CountryEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val code: String,
    val capital: String,
    val region: String,
    val flagUrl: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val costOfLiving: Double? = null,
    val rentIndex: Double? = null,
    val safety: Double? = null,
    val healthcare: Double? = null,
    val pollution: Double? = null,
    val qualityOfLife: Double? = null,
    val climate: Double? = null,
    val purchasingPower: Double? = null,
    val propertyPriceToIncome: Double? = null,
    val visaScore: Int? = null,
    val internetSpeed: Double? = null
)
