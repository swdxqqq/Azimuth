package com.example.azimuth_mobile.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.azimuth_mobile.R
import com.example.azimuth_mobile.domain.model.CountryListItem
import com.example.azimuth_mobile.presentation.theme.nunitoFamily
import com.example.azimuth_mobile.presentation.theme.nunitoSansFamily

private val regions = listOf("All", "Europe", "Asia", "Africa", "America", "Oceania")

@Composable
fun CatalogScreen(
    state: CatalogUiState,
    onQueryChanged: (String) -> Unit,
    onRegionSelected: (String) -> Unit,
    onFavoriteClicked: (Int) -> Unit,
    onCountryClicked: (Int) -> Unit,
    onRetry: () -> Unit,
    onFilterClick: () -> Unit
) {
    when {
        state.isLoading -> CatalogLoadingState()
        state.error != null -> CatalogFailureState(
            error = state.error,
            onRetry = onRetry,
            onFilterClick = onFilterClick
        )
        else -> CatalogContent(
            state = state,
            onQueryChanged = onQueryChanged,
            onRegionSelected = onRegionSelected,
            onFavoriteClicked = onFavoriteClicked,
            onCountryClicked = onCountryClicked,
            onFilterClick = onFilterClick
        )
    }
}

@Composable
private fun CatalogContent(
    state: CatalogUiState,
    onQueryChanged: (String) -> Unit,
    onRegionSelected: (String) -> Unit,
    onFavoriteClicked: (Int) -> Unit,
    onCountryClicked: (Int) -> Unit,
    onFilterClick: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            CatalogHeader(
                title = "Find your country",
                onFilterClick = onFilterClick,
                modifier = Modifier.padding(top = 12.dp)
            )
            Spacer(Modifier.height(20.dp))
            SearchField(value = state.query, onValueChange = onQueryChanged)
            Spacer(Modifier.height(16.dp))
            RegionChips(
                selectedRegion = state.selectedRegion,
                onRegionSelected = onRegionSelected
            )
            Spacer(Modifier.height(16.dp))

            if (state.visibleCountries.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.visibleCountries, key = { it.country.id }) { item ->
                        CountryCard(
                            item = item,
                            isFavorite = item.country.id in state.favoriteIds,
                            onFavoriteClick = { onFavoriteClicked(item.country.id) },
                            onCountryClick = { onCountryClicked(item.country.id) }
                        )
                    }
                }
            }
        }

        if (state.visibleCountries.isEmpty()) {
            CatalogEmptyState(Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun CatalogHeader(
    title: String,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = nunitoFamily,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 29.sp
        )
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(role = Role.Button, onClick = onFilterClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_tune),
                contentDescription = "Set priorities",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
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
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = nunitoSansFamily,
                fontSize = 14.sp
            ),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
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
private fun RegionChips(
    selectedRegion: String,
    onRegionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        regions.forEach { region ->
            val selected = selectedRegion == region
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface
                    )
                    .then(
                        if (selected) Modifier
                        else Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        )
                    )
                    .clickable(role = Role.Button) { onRegionSelected(region) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = region,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = nunitoSansFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun CountryCard(
    item: CountryListItem,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onCountryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val country = item.country

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .then(
                onCountryClick?.let { click -> Modifier.clickable(role = Role.Button, onClick = click) }
                    ?: Modifier
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (country.flagUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
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
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = nunitoFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append(country.name)
                    }
                    country.capital?.takeIf(String::isNotBlank)?.let { capital ->
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = nunitoSansFamily,
                                fontSize = 13.sp
                            )
                        ) {
                            append(" · $capital")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = nunitoFamily, fontSize = 17.sp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = country.region,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = nunitoSansFamily,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
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
                modifier = if (isFavorite) {
                    Modifier.size(width = 21.dp, height = 18.5.dp)
                } else {
                    Modifier.size(24.dp)
                }
            )
        }
    }
}

@Composable
private fun CatalogEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        StateIconCircle {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "No countries found",
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = nunitoFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Try a different search term or change filters",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = nunitoSansFamily,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CatalogFailureState(
    error: CatalogError,
    onRetry: () -> Unit,
    onFilterClick: () -> Unit
) {
    val isOffline = error == CatalogError.NoInternet
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            CatalogHeader(
                title = "Find your country",
                onFilterClick = onFilterClick,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(280.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            StateIconCircle {
                Icon(
                    painter = painterResource(
                        id = if (isOffline) R.drawable.ic_wifi_off else R.drawable.ic_error
                    ),
                    contentDescription = null,
                    tint = if (isOffline) MaterialTheme.colorScheme.onSurfaceVariant
                    else Color.Unspecified,
                    modifier = Modifier.size(38.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = if (isOffline) "No internet connection" else "Something went wrong",
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = nunitoFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isOffline) "Check your connection and try again"
                else "We couldn't load the catalog. Please try again.",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = nunitoSansFamily,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(role = Role.Button, onClick = onRetry)
                    .padding(horizontal = 24.dp)
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_retry),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isOffline) "Retry" else "Try Again",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontFamily = nunitoFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StateIconCircle(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}

@Composable
private fun CatalogLoadingState() {
    val placeholder = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SkeletonBlock(
                modifier = Modifier
                    .width(205.dp)
                    .height(24.dp),
                color = placeholder,
                shape = RoundedCornerShape(8.dp)
            )
            SkeletonBlock(
                modifier = Modifier.size(44.dp),
                color = placeholder,
                shape = RoundedCornerShape(12.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            color = placeholder,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(64.dp, 78.dp, 70.dp, 80.dp).forEach { width ->
                SkeletonBlock(
                    modifier = Modifier
                        .width(width)
                        .height(34.dp),
                    color = placeholder,
                    shape = CircleShape
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonBlock(
                        modifier = Modifier.size(width = 64.dp, height = 48.dp),
                        color = placeholder,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SkeletonBlock(
                            modifier = Modifier
                                .fillMaxWidth(0.72f)
                                .height(14.dp),
                            color = placeholder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        SkeletonBlock(
                            modifier = Modifier
                                .fillMaxWidth(0.42f)
                                .height(11.dp),
                            color = placeholder,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    SkeletonBlock(
                        modifier = Modifier.size(28.dp),
                        color = placeholder,
                        shape = CircleShape
                    )
                }
            }
        }
    }
}

@Composable
private fun SkeletonBlock(
    modifier: Modifier,
    color: Color,
    shape: androidx.compose.ui.graphics.Shape
) {
    Box(modifier = modifier.clip(shape).background(color))
}
