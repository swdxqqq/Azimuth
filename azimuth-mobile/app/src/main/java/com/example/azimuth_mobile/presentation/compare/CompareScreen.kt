package com.example.azimuth_mobile.presentation.compare

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.azimuth_mobile.R
import com.example.azimuth_mobile.domain.model.CountryListItem
import com.example.azimuth_mobile.domain.model.CountryMetrics
import com.example.azimuth_mobile.presentation.catalog.CatalogError
import com.example.azimuth_mobile.presentation.catalog.CatalogUiState
import com.example.azimuth_mobile.presentation.theme.nunitoFamily
import com.example.azimuth_mobile.presentation.theme.nunitoSansFamily
import kotlin.math.roundToInt

private data class ComparisonMetric(
    val label: String,
    val value: (CountryMetrics) -> Double?
)

private val comparisonMetrics = listOf(
    ComparisonMetric("Cost of Living") { it.costOfLiving },
    ComparisonMetric("Safety") { it.safety },
    ComparisonMetric("Climate") { it.climate }
)

@Composable
fun CompareScreen(
    state: CatalogUiState,
    onAddCountry: (Int) -> Unit,
    onRemoveCountry: (Int) -> Unit,
    onRetry: () -> Unit
) {
    var showAddCountry by remember { mutableStateOf(false) }
    val comparedCountries = state.compareIds.mapNotNull { countryId ->
        state.countries.firstOrNull { it.country.id == countryId }
    }

    when {
        state.isLoading && state.countries.isEmpty() -> CompareLoadingState()
        state.error != null && state.countries.isEmpty() -> CompareFailureState(
            error = state.error,
            onRetry = onRetry
        )
        else -> CompareContent(
            comparedCountries = comparedCountries,
            onAddCountry = { showAddCountry = true },
            onRemoveCountry = onRemoveCountry
        )
    }

    if (showAddCountry) {
        AddCountryBottomSheet(
            countries = state.countries,
            selectedIds = state.compareIds,
            onDismiss = { showAddCountry = false },
            onAddCountry = onAddCountry
        )
    }
}

@Composable
private fun CompareContent(
    comparedCountries: List<CountryListItem>,
    onAddCountry: () -> Unit,
    onRemoveCountry: (Int) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Compare",
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = nunitoFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )

            if (comparedCountries.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 4.dp)
                ) {
                    items(comparedCountries, key = { it.country.id }) { item ->
                        ComparedCountryChip(
                            item = item,
                            onRemove = { onRemoveCountry(item.country.id) }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item {
                        VisualComparisonCard(countries = comparedCountries)
                    }
                }
            }
        }

        if (comparedCountries.isEmpty()) {
            EmptyCompareState(Modifier.align(Alignment.Center))
        }

        Surface(
            onClick = onAddCountry,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add country to compare",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ComparedCountryChip(
    item: CountryListItem,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 10.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CountryFlag(
            item = item,
            modifier = Modifier.size(width = 20.dp, height = 15.dp),
            cornerRadius = 3.dp
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = item.country.name,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = nunitoSansFamily,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(5.dp))
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Remove ${item.country.name}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun VisualComparisonCard(countries: List<CountryListItem>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Visual Comparison",
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = nunitoFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(18.dp))
            comparisonMetrics.forEachIndexed { index, metric ->
                if (index > 0) Spacer(Modifier.height(18.dp))
                Text(
                    text = metric.label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = nunitoSansFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                countries.forEach { item ->
                    ComparisonBarRow(
                        item = item,
                        value = item.metrics?.let(metric.value)
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun ComparisonBarRow(item: CountryListItem, value: Double?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CountryFlag(
            item = item,
            modifier = Modifier.size(width = 14.dp, height = 10.dp),
            cornerRadius = 2.dp
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (value != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((value.coerceIn(0.0, 100.0) / 100.0).toFloat())
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = value?.roundToInt()?.toString() ?: "—",
            modifier = Modifier.width(24.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = nunitoSansFamily,
            fontSize = 13.sp,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun EmptyCompareState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
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
                    painter = painterResource(id = R.drawable.ic_compare),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(38.dp)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "No countries selected",
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = nunitoFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Add countries to compare them side by side",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = nunitoSansFamily,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CompareLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        LoadingBlock(Modifier.padding(top = 16.dp).size(width = 150.dp, height = 34.dp))
        Row(
            modifier = Modifier.padding(top = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(2) { LoadingBlock(Modifier.size(width = 112.dp, height = 38.dp)) }
        }
        LoadingBlock(
            Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .height(260.dp)
        )
    }
}

@Composable
private fun CompareFailureState(error: CatalogError, onRetry: () -> Unit) {
    val offline = error == CatalogError.NoInternet
    Box(Modifier.fillMaxSize()) {
        Text(
            text = "Compare",
            modifier = Modifier.padding(start = 20.dp, top = 12.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = nunitoFamily,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
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
                text = if (offline) "No internet connection" else "Something went wrong",
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = nunitoFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (offline) "Check your connection and try again"
                else "We couldn't load countries for comparison.",
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
private fun LoadingBlock(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {}
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun AddCountryBottomSheet(
    countries: List<CountryListItem>,
    selectedIds: List<Int>,
    onDismiss: () -> Unit,
    onAddCountry: (Int) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val availableCountries = countries.filter { item ->
        item.country.id !in selectedIds &&
            (query.isBlank() || item.country.name.contains(query.trim(), ignoreCase = true))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            Text(
                text = "Add Country",
                modifier = Modifier.padding(top = 20.dp),
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = nunitoFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            AddCountrySearchField(query = query, onQueryChanged = { query = it })

            if (availableCountries.isEmpty()) {
                Text(
                    text = when {
                        countries.isEmpty() -> "No countries available"
                        countries.all { it.country.id in selectedIds } -> "All countries are already selected"
                        else -> "No countries found"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = nunitoSansFamily,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 460.dp)
                        .padding(top = 12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableCountries, key = { it.country.id }) { item ->
                        AddCountryRow(item = item, onAdd = { onAddCountry(item.country.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCountrySearchField(query: String, onQueryChanged: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = nunitoSansFamily,
                fontSize = 14.sp
            ),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search countries...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = nunitoSansFamily,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun AddCountryRow(item: CountryListItem, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CountryFlag(
            item = item,
            modifier = Modifier.size(width = 40.dp, height = 30.dp),
            cornerRadius = 4.dp
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = item.country.name,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = nunitoSansFamily,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(role = Role.Button, onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "Add ${item.country.name}",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CountryFlag(
    item: CountryListItem,
    modifier: Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (item.country.flagUrl.isNullOrBlank()) {
            Text(
                text = item.country.code,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = nunitoFamily,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        } else {
            AsyncImage(
                model = item.country.flagUrl,
                contentDescription = "${item.country.name} flag",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
