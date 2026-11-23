package com.example.azimuth_mobile.presentation.navigation

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azimuth_mobile.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import com.example.azimuth_mobile.presentation.catalog.CatalogScreen
import com.example.azimuth_mobile.presentation.catalog.CatalogViewModel
import com.example.azimuth_mobile.presentation.catalog.CountryCard
import com.example.azimuth_mobile.presentation.catalog.CatalogUiState
import com.example.azimuth_mobile.presentation.catalog.PriorityBottomSheet
import com.example.azimuth_mobile.presentation.compare.CompareScreen
import com.example.azimuth_mobile.presentation.detail.CountryDetailScreen
import com.example.azimuth_mobile.presentation.detail.CountryDetailViewModel
import com.example.azimuth_mobile.presentation.theme.nunitoFamily
import com.example.azimuth_mobile.presentation.theme.nunitoSansFamily

private object Routes {
    const val Catalog = "catalog"
    const val Favorites = "favorites"
    const val Compare = "compare"
    const val CountryDetail = "country_detail"
    const val CountryDetailPattern = "$CountryDetail/{countryId}"
}

@Composable
fun AzimuthNavigation() {
    val navController = rememberNavController()
    val viewModel: CatalogViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: Routes.Catalog
    var showPriorities by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(
                selectedRoute = if (currentRoute == Routes.CountryDetailPattern) {
                    Routes.Catalog
                } else {
                    currentRoute
                },
                onNavigate = { route -> navigateTo(navController, route) }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Catalog,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Routes.Catalog) {
                CatalogScreen(
                    state = state,
                    onQueryChanged = viewModel::onQueryChanged,
                    onRegionSelected = viewModel::onRegionSelected,
                    onFavoriteClicked = viewModel::toggleFavorite,
                    onCountryClicked = { countryId ->
                        navController.navigate("${Routes.CountryDetail}/$countryId")
                    },
                    onRetry = viewModel::loadCountries,
                    onFilterClick = { showPriorities = true }
                )
            }
            composable(Routes.Favorites) {
                FavoritesScreen(
                    state = state,
                    onFavoriteClicked = viewModel::toggleFavorite
                )
            }
            composable(Routes.Compare) {
                CompareScreen(
                    state = state,
                    onAddCountry = viewModel::addToCompare,
                    onRemoveCountry = viewModel::removeFromCompare,
                    onRetry = viewModel::loadCountries
                )
            }
            composable(
                route = Routes.CountryDetailPattern,
                arguments = listOf(navArgument("countryId") { type = NavType.IntType })
            ) { backStackEntry ->
                val countryId = requireNotNull(backStackEntry.arguments?.getInt("countryId"))
                val detailViewModel: CountryDetailViewModel = hiltViewModel()
                val detailState by detailViewModel.uiState.collectAsState()
                LaunchedEffect(countryId) {
                    detailViewModel.loadCountry(countryId)
                }
                CountryDetailScreen(
                    state = detailState,
                    isFavorite = countryId in state.favoriteIds,
                    onFavoriteClick = { viewModel.toggleFavorite(countryId) },
                    onCompareClick = {
                        viewModel.addToCompare(countryId)
                        navigateTo(navController, Routes.Compare)
                    },
                    onRetry = { detailViewModel.loadCountry(countryId) }
                )
            }
        }
    }

    if (showPriorities) {
        PriorityBottomSheet(
            onDismiss = { showPriorities = false },
            onApply = { showPriorities = false }
        )
    }
}

private fun navigateTo(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun BottomNavigationBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .height(59.dp),
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationItem(
                    label = "Home",
                    route = Routes.Catalog,
                    iconRes = R.drawable.ic_home,
                    selectedIconRes = R.drawable.ic_home,
                    selectedRoute = selectedRoute,
                    onClick = onNavigate,
                    modifier = Modifier.weight(1f)
                )
                NavigationItem(
                    label = "Favorites",
                    route = Routes.Favorites,
                    iconRes = R.drawable.ic_favorite,
                    selectedIconRes = R.drawable.ic_favorite,
                    selectedRoute = selectedRoute,
                    onClick = onNavigate,
                    modifier = Modifier.weight(1f)
                )
                NavigationItem(
                    label = "Compare",
                    route = Routes.Compare,
                    iconRes = R.drawable.ic_compare,
                    selectedIconRes = R.drawable.ic_compare,
                    selectedRoute = selectedRoute,
                    onClick = onNavigate,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavigationItem(
    label: String,
    route: String,
    iconRes: Int,
    selectedIconRes: Int,
    selectedRoute: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selected = route == selectedRoute
    val iconResource = if (selected) selectedIconRes else iconRes
    val iconCanvasSize = when (iconResource) {
        R.drawable.ic_home -> 22.dp
        R.drawable.ic_favorite -> 26.dp
        R.drawable.ic_compare -> 33.dp
        else -> 22.dp
    }
    Column(
        modifier = modifier
            .height(59.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(role = Role.Tab) { onClick(route) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(33.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconResource),
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(iconCanvasSize)
                    .offset(y = 2.dp)
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = nunitoSansFamily,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

@Composable
private fun FavoritesScreen(
    state: CatalogUiState,
    onFavoriteClicked: (Int) -> Unit
) {
    var alphabetical by remember { mutableStateOf(false) }
    val favorites = state.favoriteIds
        .asReversed()
        .mapNotNull { countryId -> state.countries.find { it.country.id == countryId } }
        .let { items -> if (alphabetical) items.sortedBy { it.country.name } else items }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
        Text(
            text = "Favorites",
            modifier = Modifier.padding(top = 12.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = nunitoFamily,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )
            Row(
                modifier = Modifier.padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortChip("By date", selected = !alphabetical) { alphabetical = false }
                SortChip("A-Z", selected = alphabetical) { alphabetical = true }
            }
            if (favorites.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(favorites, key = { it.country.id }) { item ->
                        CountryCard(
                            item = item,
                            isFavorite = true,
                            onFavoriteClick = { onFavoriteClicked(item.country.id) }
                        )
                    }
                }
            }
        }
        if (favorites.isEmpty()) {
            EmptyFavoritesState(Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun SortChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .height(34.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = nunitoSansFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptyFavoritesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmptyMessageIcon {
            Icon(
                painter = painterResource(id = R.drawable.ic_favorite),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "No favorites yet",
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = nunitoFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Start adding countries to your favorites to see them here",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = nunitoSansFamily,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun EmptyMessageIcon(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.size(88.dp),
        shape = RoundedCornerShape(44.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(contentAlignment = Alignment.Center, content = { content() })
    }
}
