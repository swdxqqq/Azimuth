package com.example.azimuth_mobile.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azimuth_mobile.domain.model.CountryListItem
import com.example.azimuth_mobile.domain.usecase.GetCountriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CatalogError {
    NoInternet,
    General
}

data class CatalogUiState(
    val countries: List<CountryListItem> = emptyList(),
    val query: String = "",
    val selectedRegion: String = "All",
    val favoriteIds: List<Int> = emptyList(),
    val isLoading: Boolean = true,
    val error: CatalogError? = null
) {
    val visibleCountries: List<CountryListItem>
        get() = countries.filter { item ->
            val regionMatches = when (selectedRegion) {
                "All" -> true
                "America" -> item.country.region.contains("America", ignoreCase = true)
                else -> item.country.region.equals(selectedRegion, ignoreCase = true)
            }
            val searchMatches = query.isBlank() || listOfNotNull(
                item.country.name,
                item.country.capital,
                item.country.region,
                item.country.code
            ).any { value -> value.contains(query.trim(), ignoreCase = true) }

            regionMatches && searchMatches
        }
}

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val getCountries: GetCountriesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCountries()
    }

    fun loadCountries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = getCountries(page = 1, limit = 100)
                _uiState.update {
                    it.copy(countries = result.items, isLoading = false, error = null)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: IOException) {
                _uiState.update { it.copy(isLoading = false, error = CatalogError.NoInternet) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, error = CatalogError.General) }
            }
        }
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onRegionSelected(region: String) {
        _uiState.update { it.copy(selectedRegion = region) }
    }

    fun toggleFavorite(countryId: Int) {
        _uiState.update { state ->
            val favorites = if (countryId in state.favoriteIds) {
                state.favoriteIds - countryId
            } else {
                state.favoriteIds + countryId
            }
            state.copy(favoriteIds = favorites)
        }
    }
}
