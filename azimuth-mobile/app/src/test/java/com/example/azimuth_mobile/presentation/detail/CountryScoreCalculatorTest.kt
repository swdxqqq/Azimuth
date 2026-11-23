package com.example.azimuth_mobile.presentation.detail

import com.example.azimuth_mobile.domain.model.Country
import com.example.azimuth_mobile.domain.model.CountryListItem
import com.example.azimuth_mobile.domain.model.CountryMetrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CountryScoreCalculatorTest {
    @Test
    fun financialScoreUsesMetricDirectionInsteadOfRawScale() {
        val bestMetrics = metrics(
            cost = 10.0,
            rent = 10.0,
            purchasingPower = 100.0,
            propertyPriceToIncome = 5.0
        )
        val worseMetrics = metrics(
            cost = 20.0,
            rent = 20.0,
            purchasingPower = 50.0,
            propertyPriceToIncome = 10.0
        )

        val score = CountryScoreCalculator.financialScore(
            countryId = 1,
            metrics = bestMetrics,
            catalog = listOf(countryItem(1, bestMetrics), countryItem(2, worseMetrics))
        )

        assertEquals(10.0, score!!, 0.001)
    }

    @Test
    fun qualityScoreRewardsLowerPollutionAndHigherOtherMetrics() {
        val betterMetrics = metrics(
            qualityOfLife = 100.0,
            safety = 100.0,
            healthCare = 100.0,
            pollution = 10.0,
            internetSpeed = 100.0
        )
        val worseMetrics = metrics(
            qualityOfLife = 50.0,
            safety = 50.0,
            healthCare = 50.0,
            pollution = 80.0,
            internetSpeed = 50.0
        )

        val score = CountryScoreCalculator.qualityOfLifeScore(
            countryId = 1,
            metrics = betterMetrics,
            catalog = listOf(countryItem(1, betterMetrics), countryItem(2, worseMetrics))
        )

        assertEquals(10.0, score!!, 0.001)
    }

    @Test
    fun equalMetricsProduceNeutralScoreAndMissingPeersProduceNoScore() {
        val metrics = metrics(cost = 20.0)
        val equalScore = CountryScoreCalculator.financialScore(
            countryId = 1,
            metrics = metrics,
            catalog = listOf(countryItem(1, metrics), countryItem(2, metrics))
        )
        val withoutComparablePeer = CountryScoreCalculator.financialScore(
            countryId = 1,
            metrics = metrics,
            catalog = listOf(countryItem(1, metrics))
        )

        assertEquals(5.0, equalScore!!, 0.001)
        assertNull(withoutComparablePeer)
    }

    private fun countryItem(id: Int, metrics: CountryMetrics) = CountryListItem(
        country = Country(
            id = id,
            name = "Country $id",
            code = "C$id",
            capital = null,
            region = "Test",
            flagUrl = null
        ),
        metrics = metrics
    )

    private fun metrics(
        cost: Double? = null,
        rent: Double? = null,
        purchasingPower: Double? = null,
        propertyPriceToIncome: Double? = null,
        qualityOfLife: Double? = null,
        safety: Double? = null,
        healthCare: Double? = null,
        pollution: Double? = null,
        internetSpeed: Double? = null
    ) = CountryMetrics(
        costOfLiving = cost,
        rentIndex = rent,
        safety = safety,
        healthCare = healthCare,
        pollution = pollution,
        qualityOfLife = qualityOfLife,
        climate = null,
        purchasingPower = purchasingPower,
        propertyPriceToIncome = propertyPriceToIncome,
        visaScore = null,
        internetSpeed = internetSpeed
    )
}
