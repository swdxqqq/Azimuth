package com.example.azimuth_mobile.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azimuth_mobile.domain.model.CountryCurrencyRate
import com.example.azimuth_mobile.domain.model.CountryDetails
import com.example.azimuth_mobile.domain.usecase.GetCountryDetailsUseCase
import com.example.azimuth_mobile.domain.usecase.GetCurrencyRateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CountryDetailError {
    NoInternet,
    General
}

data class CountryDetailUiState(
    val countryId: Int? = null,
    val details: CountryDetails? = null,
    val currencyRate: CountryCurrencyRate? = null,
    val isLoading: Boolean = true,
    val isCurrencyLoading: Boolean = false,
    val error: CountryDetailError? = null
)

@HiltViewModel
class CountryDetailViewModel @Inject constructor(
    private val getCountryDetails: GetCountryDetailsUseCase,
    private val getCurrencyRate: GetCurrencyRateUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(CountryDetailUiState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun loadCountry(countryId: Int) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = CountryDetailUiState(countryId = countryId)
            try {
                val details = getCountryDetails(countryId)
                _uiState.update {
                    it.copy(details = details, isLoading = false, isCurrencyLoading = true)
                }

                val rate = try {
                    getCurrencyRate(details.country)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    null
                }
                _uiState.update {
                    it.copy(currencyRate = rate, isCurrencyLoading = false)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: IOException) {
                _uiState.update {
                    it.copy(isLoading = false, isCurrencyLoading = false, error = CountryDetailError.NoInternet)
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, isCurrencyLoading = false, error = CountryDetailError.General)
                }
            }
        }
    }
}
