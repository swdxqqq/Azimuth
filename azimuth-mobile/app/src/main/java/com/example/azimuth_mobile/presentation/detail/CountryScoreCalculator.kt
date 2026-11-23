package com.example.azimuth_mobile.presentation.detail

import com.example.azimuth_mobile.domain.model.CountryListItem
import com.example.azimuth_mobile.domain.model.CountryMetrics

internal object CountryScoreCalculator {
    private data class MetricRule(
        val higherIsBetter: Boolean,
        val value: (CountryMetrics) -> Double?
    )

    private val financialRules = listOf(
        MetricRule(higherIsBetter = false) { it.costOfLiving },
        MetricRule(higherIsBetter = false) { it.rentIndex },
        MetricRule(higherIsBetter = true) { it.purchasingPower },
        MetricRule(higherIsBetter = false) { it.propertyPriceToIncome }
    )

    private val qualityOfLifeRules = listOf(
        MetricRule(higherIsBetter = true) { it.qualityOfLife },
        MetricRule(higherIsBetter = true) { it.safety },
        MetricRule(higherIsBetter = true) { it.healthCare },
        MetricRule(higherIsBetter = false) { it.pollution },
        MetricRule(higherIsBetter = true) { it.internetSpeed }
    )

    fun financialScore(
        countryId: Int,
        metrics: CountryMetrics?,
        catalog: List<CountryListItem>
    ): Double? = calculate(countryId, metrics, catalog, financialRules)

    fun qualityOfLifeScore(
        countryId: Int,
        metrics: CountryMetrics?,
        catalog: List<CountryListItem>
    ): Double? = calculate(countryId, metrics, catalog, qualityOfLifeRules)

    /** Equal-weight average of each available metric's direction-aware catalog percentile. */
    private fun calculate(
        countryId: Int,
        metrics: CountryMetrics?,
        catalog: List<CountryListItem>,
        rules: List<MetricRule>
    ): Double? {
        metrics ?: return null

        val metricScores = rules.mapNotNull { rule ->
            val currentValue = rule.value(metrics)?.takeIf { it.isFinite() }
                ?: return@mapNotNull null
            val values = catalog.asSequence()
                .filter { it.country.id != countryId }
                .mapNotNull { item -> item.metrics?.let(rule.value) }
                .filter { it.isFinite() }
                .toList() + currentValue

            if (values.size < 2) return@mapNotNull null

            val worseCount = values.count { value ->
                if (rule.higherIsBetter) value < currentValue else value > currentValue
            }
            val equalCount = values.count { it == currentValue }
            val rank = worseCount + (equalCount - 1) / 2.0
            rank / (values.size - 1) * 10.0
        }

        return metricScores.takeIf { it.isNotEmpty() }?.average()
    }
}
