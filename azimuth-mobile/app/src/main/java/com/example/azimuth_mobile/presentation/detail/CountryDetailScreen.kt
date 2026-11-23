package com.example.azimuth_mobile.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.azimuth_mobile.R
import com.example.azimuth_mobile.domain.model.Country
import com.example.azimuth_mobile.domain.model.CountryCurrencyRate
import com.example.azimuth_mobile.domain.model.CountryDetails
import com.example.azimuth_mobile.domain.model.CountryMetrics
import com.example.azimuth_mobile.domain.model.EconomicIndicatorDetail
import com.example.azimuth_mobile.presentation.theme.nunitoFamily
import com.example.azimuth_mobile.presentation.theme.nunitoSansFamily
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CountryDetailScreen(
    state: CountryDetailUiState,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onCompareClick: () -> Unit,
    onRetry: () -> Unit
) {
    when {
        state.isLoading -> CountryDetailLoadingState()
        state.error != null -> CountryDetailFailureState(state.error, onRetry)
        state.details != null -> CountryDetailContent(
            details = state.details,
            currencyRate = state.currencyRate,
            isCurrencyLoading = state.isCurrencyLoading,
            isEconomyLoading = state.isEconomyLoading,
            financialScore = state.financialScore,
            qualityOfLifeScore = state.qualityOfLifeScore,
            isFavorite = isFavorite,
            onFavoriteClick = onFavoriteClick,
            onCompareClick = onCompareClick
        )
    }
}

@Composable
private fun CountryDetailContent(
    details: CountryDetails,
    currencyRate: CountryCurrencyRate?,
    isCurrencyLoading: Boolean,
    isEconomyLoading: Boolean,
    financialScore: Double?,
    qualityOfLifeScore: Double?,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onCompareClick: () -> Unit
) {
    val countryMetrics = details.metrics
    val financialMetrics = listOfNotNull(
        indexMetric("Cost of Living Index", countryMetrics?.costOfLiving),
        indexMetric("Rent Index", countryMetrics?.rentIndex),
        indexMetric("Purchasing Power Index", countryMetrics?.purchasingPower),
        indexMetric("Property Price to Income Ratio", countryMetrics?.propertyPriceToIncome)
    ) + economicIndicatorMetrics(details.economicIndicators.orEmpty(), isEconomyLoading)
    val qualityMetrics = listOfNotNull(
        indexMetric("Quality of Life Index", countryMetrics?.qualityOfLife),
        indexMetric("Safety Index", countryMetrics?.safety),
        indexMetric("Healthcare Index", countryMetrics?.healthCare),
        indexMetric("Pollution Index", countryMetrics?.pollution),
        countryMetrics?.internetSpeed?.let {
            DetailMetric("Internet Speed", "${formatNumber(it)} Mbps")
        }
    )
    var showClimateInfo by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CountryHeaderCard(
                country = details.country,
                isFavorite = isFavorite,
                onFavoriteClick = onFavoriteClick,
                onCompareClick = onCompareClick
            )
        }
        if (financialMetrics.isNotEmpty()) {
            item {
                MetricDataCard(
                    title = "Financials",
                    overallScore = financialScore,
                    metrics = financialMetrics
                )
            }
        }
        if (qualityMetrics.isNotEmpty()) {
            item {
                MetricDataCard(
                    title = "Quality of Life",
                    overallScore = qualityOfLifeScore,
                    metrics = qualityMetrics
                )
            }
        }
        countryMetrics?.climate?.let { climateScore ->
            item {
                ClimateScoreCard(
                    score = climateScore,
                    onInfoClick = { showClimateInfo = true }
                )
            }
        }
        item {
            ReferenceInfoCard(
                details = details,
                currencyRate = currencyRate,
                isCurrencyLoading = isCurrencyLoading
            )
        }
    }

    if (showClimateInfo) {
        ClimateScoreInfoDialog(onDismiss = { showClimateInfo = false })
    }
}

@Composable
private fun CountryHeaderCard(
    country: Country,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onCompareClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CountryFlag(country = country, modifier = Modifier.size(width = 80.dp, height = 60.dp))
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = country.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = nunitoFamily,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    val location = listOfNotNull(
                        country.capital?.takeIf(String::isNotBlank),
                        country.region.takeIf(String::isNotBlank)
                    ).joinToString(" · ")
                    Text(
                        text = location,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = nunitoSansFamily,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCompareClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_compare),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Compare",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = nunitoFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                        .clickable(role = Role.Button, onClick = onFavoriteClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite
                        ),
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(if (isFavorite) 21.dp else 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClimateScoreCard(score: Double, onInfoClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Climate Score",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = nunitoFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    onClick = onInfoClick,
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.HelpOutline,
                            contentDescription = "How Climate Score is calculated",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            ScoreValue(score / 10.0, "out of 10")
        }
    }
}

@Composable
private fun MetricDataCard(
    title: String,
    overallScore: Double?,
    metrics: List<DetailMetric>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = nunitoFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            if (overallScore != null) {
                ScoreValue(overallScore, "relative score · out of 10")
            } else {
                Text(
                    text = "Overall score unavailable",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = nunitoSansFamily,
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(16.dp))
            MetricGrid(metrics)
        }
    }
}

private data class DetailMetric(
    val label: String,
    val value: String
)

@Composable
private fun MetricGrid(metrics: List<DetailMetric>) {
    metrics.chunked(2).forEachIndexed { index, rowMetrics ->
        if (index > 0) Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            rowMetrics.forEach { metric ->
                MetricValueCell(metric, Modifier.weight(1f))
            }
            if (rowMetrics.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricValueCell(metric: DetailMetric, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = metric.label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = nunitoSansFamily,
            fontSize = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = metric.value,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = nunitoFamily,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ScoreValue(score: Double, caption: String) {
    Column {
        Text(
            text = formatNumber(score),
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = nunitoFamily,
            fontSize = 52.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 56.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = nunitoSansFamily,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ReferenceInfoCard(
    details: CountryDetails,
    currencyRate: CountryCurrencyRate?,
    isCurrencyLoading: Boolean
) {
    val currencyCode = currencyRate?.currencyCode?.takeIf { it.isNotBlank() }
    val currencyValue = when {
        isCurrencyLoading -> "Loading..."
        currencyCode != null -> currencyCode.uppercase(Locale.ROOT)
        else -> "Not available"
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Reference Info",
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = nunitoFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            ReferenceInfoRow(
                "Population",
                details.population?.let(::formatPopulation) ?: "Not available"
            )
            ReferenceInfoRow("Language", formatLanguages(details.languages))
            ReferenceInfoRow("Currency", currencyValue)
            ReferenceInfoRow("Time Zone", details.timezone?.takeIf(String::isNotBlank) ?: "Not available")
            ReferenceInfoRow(
                "Visa-free destinations",
                details.metrics?.visaScore?.let { "$it countries" } ?: "Not available"
            )
        }
    }
}

@Composable
private fun ReferenceInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.8f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = nunitoSansFamily,
            fontSize = 13.sp
        )
        Text(
            text = value,
            modifier = Modifier.weight(1.2f),
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = nunitoSansFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CountryFlag(country: Country, modifier: Modifier = Modifier) {
    if (country.flagUrl.isNullOrBlank()) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = country.code,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = nunitoFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        AsyncImage(
            model = country.flagUrl,
            contentDescription = "${country.name} flag",
            modifier = modifier.clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun CountryDetailLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LoadingCard(Modifier.fillMaxWidth().height(170.dp))
        LoadingCard(Modifier.fillMaxWidth().height(150.dp))
        LoadingCard(Modifier.fillMaxWidth().height(180.dp))
        LoadingCard(Modifier.fillMaxWidth().height(180.dp))
    }
}

@Composable
private fun CountryDetailFailureState(error: CountryDetailError, onRetry: () -> Unit) {
    val offline = error == CountryDetailError.NoInternet
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(280.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(88.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(
                            id = if (offline) R.drawable.ic_wifi_off else R.drawable.ic_error
                        ),
                        contentDescription = null,
                        tint = if (offline) MaterialTheme.colorScheme.onSurfaceVariant else Color.Unspecified,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = if (offline) "No internet connection" else "Failed to load country",
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = nunitoFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (offline) "Check your connection and try again"
                else "We couldn't load the country details. Please try again.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = nunitoSansFamily,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Surface(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_retry),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (offline) "Retry" else "Try Again",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontFamily = nunitoFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingCard(modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {}
}

private fun indexMetric(label: String, value: Double?): DetailMetric? =
    value?.let { DetailMetric(label, formatNumber(it)) }

private fun economicIndicatorMetrics(
    indicators: Map<String, EconomicIndicatorDetail>,
    isLoading: Boolean
): List<DetailMetric> {
    fun metric(key: String, title: String, format: (Double) -> String): DetailMetric {
        val indicator = indicators[key]
        return when {
            indicator != null -> DetailMetric("$title · ${indicator.year}", format(indicator.value))
            isLoading -> DetailMetric(title, "Loading...")
            else -> DetailMetric(title, "Not available")
        }
    }

    return listOf(
        metric("gdp_per_capita", "GDP per Capita", ::formatUsd),
        metric("inflation", "Inflation", { "${formatNumber(it)}%" }),
        metric("unemployment", "Unemployment", { "${formatNumber(it)}%" })
    )
}

@Composable
private fun ClimateScoreInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "What does Climate Score mean?",
                fontFamily = nunitoFamily,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "It estimates how comfortable the temperature is in the country's capital " +
                    "throughout the year. We average daily high and low temperatures for each month " +
                    "from 2020 to 2024. A monthly average from 15°C to 25°C scores 100 points. " +
                    "For each degree outside that range, 10 points are deducted, down to zero. " +
                    "The 12 monthly scores are averaged and shown on a 0–10 scale. It measures " +
                    "temperature comfort only, not humidity, wind or rainfall.",
                fontFamily = nunitoSansFamily
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it", fontFamily = nunitoFamily)
            }
        }
    )
}

private fun formatPopulation(value: Long): String =
    NumberFormat.getIntegerInstance(Locale.US).format(value)

private fun formatLanguages(languages: List<String>?): String {
    val available = languages.orEmpty().filter(String::isNotBlank)
    if (available.isEmpty()) return "Not available"
    val visible = available.take(3).joinToString(", ")
    val remaining = available.size - 3
    return if (remaining > 0) "$visible +$remaining" else visible
}

private fun formatNumber(value: Double): String = String.format(Locale.US, "%.1f", value)

private fun formatUsd(value: Double): String =
    "\$" + NumberFormat.getNumberInstance(Locale.US).format(value.toLong())
