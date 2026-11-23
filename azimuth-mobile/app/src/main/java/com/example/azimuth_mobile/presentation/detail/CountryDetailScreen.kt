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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.azimuth_mobile.presentation.theme.nunitoFamily
import com.example.azimuth_mobile.presentation.theme.nunitoSansFamily
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
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onCompareClick: () -> Unit
) {
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
        item {
            ClimateScoreCard(details.metrics?.climate)
        }
        item {
            MetricScoreCard(
                title = "Financials",
                score = averageScore(details.metrics?.rentIndex, details.metrics?.purchasingPower),
                metrics = listOf(
                    "Rent Index" to details.metrics?.rentIndex,
                    "Purchasing Power" to details.metrics?.purchasingPower
                )
            )
        }
        item {
            MetricScoreCard(
                title = "Quality of Life",
                score = averageScore(details.metrics?.safety, details.metrics?.healthCare),
                metrics = listOf(
                    "Safety" to details.metrics?.safety,
                    "Healthcare" to details.metrics?.healthCare
                )
            )
        }
        item {
            ReferenceInfoCard(
                metrics = details.metrics,
                currencyRate = currencyRate,
                isCurrencyLoading = isCurrencyLoading
            )
        }
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
private fun ClimateScoreCard(score: Double?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Climate Score",
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = nunitoFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            ScoreValue(score)
        }
    }
}

@Composable
private fun MetricScoreCard(
    title: String,
    score: Double?,
    metrics: List<Pair<String, Double?>>
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
            Spacer(Modifier.height(8.dp))
            ScoreValue(score)
            Spacer(Modifier.height(16.dp))
            metrics.forEachIndexed { index, (label, value) ->
                if (index > 0) {
                    androidx.compose.material3.HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(Modifier.height(12.dp))
                }
                MetricValueRow(label = label, value = value)
            }
        }
    }
}

@Composable
private fun ScoreValue(score: Double?) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = formatScore(score),
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = nunitoFamily,
            fontSize = 52.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 56.sp
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "/ 10",
            modifier = Modifier.padding(bottom = 7.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = nunitoSansFamily,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun MetricValueRow(label: String, value: Double?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = nunitoSansFamily,
            fontSize = 11.sp
        )
        Text(
            text = formatScore(value),
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = nunitoFamily,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ReferenceInfoCard(
    metrics: CountryMetrics?,
    currencyRate: CountryCurrencyRate?,
    isCurrencyLoading: Boolean
) {
    val currencyValue = when {
        isCurrencyLoading -> "Loading..."
        currencyRate != null -> "1 unit = ${formatRate(currencyRate.rateToUsd)} USD"
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
            ReferenceInfoRow("Population", "Not available")
            ReferenceInfoRow("Language", "Not available")
            ReferenceInfoRow("Currency", currencyValue)
            ReferenceInfoRow("Timezone", "Not available")
            ReferenceInfoRow(
                "Visa-free destinations",
                metrics?.visaScore?.let { "$it countries" } ?: "Not available"
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Population, language and timezone are not provided by the current API.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = nunitoSansFamily,
                fontSize = 11.sp
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = nunitoSansFamily,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = nunitoSansFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun CountryFlag(country: Country, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (country.flagUrl.isNullOrBlank()) {
            Text(
                text = country.code,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = nunitoFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            AsyncImage(
                model = country.flagUrl,
                contentDescription = "${country.name} flag",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
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

private fun averageScore(first: Double?, second: Double?): Double? {
    val values = listOfNotNull(first, second)
    return values.takeIf { it.isNotEmpty() }?.average()
}

private fun formatScore(score: Double?): String =
    score?.coerceIn(0.0, 100.0)?.div(10.0)?.let { String.format(Locale.US, "%.1f", it) } ?: "—"

private fun formatRate(rate: Double): String = String.format(Locale.US, "%.4f", rate)
