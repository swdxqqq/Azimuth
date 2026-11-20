package com.example.azimuth_mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val countryId: Int,
    val name: String,
    val code: String,
    val capital: String,
    val flagUrl: String,
    val addedAt: String
)
