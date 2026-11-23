package com.example.azimuth_mobile.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azimuth_mobile.domain.model.CountryCurrencyRate
import com.example.azimuth_mobile.domain.model.CountryListItem
import com.example.azimuth_mobile.domain.model.CountryDetails
import com.example.azimuth_mobile.domain.usecase.GetCountryDetailsUseCase
import com.example.azimuth_mobile.domain.usecase.GetCurrencyRateUseCase
import com.example.azimuth_mobile.domain.usecase.GetCountryEconomyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    val financialScore: Double? = null,
    val qualityOfLifeScore: Double? = null,
    val isLoading: Boolean = true,
    val isCurrencyLoading: Boolean = false,
    val isEconomyLoading: Boolean = false,
    val error: CountryDetailError? = null
)

@HiltViewModel
class CountryDetailViewModel @Inject constructor(
    private val getCountryDetails: GetCountryDetailsUseCase,
    private val getCurrencyRate: GetCurrencyRateUseCase,
    private val getCountryEconomy: GetCountryEconomyUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(CountryDetailUiState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun loadCountry(countryId: Int, catalog: List<CountryListItem> = emptyList()) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = CountryDetailUiState(countryId = countryId)
            try {
                val details = getCountryDetails(countryId)
                val financialScore = CountryScoreCalculator.financialScore(
                    countryId = countryId,
                    metrics = details.metrics,
                    catalog = catalog
                )
                val qualityOfLifeScore = CountryScoreCalculator.qualityOfLifeScore(
                    countryId = countryId,
                    metrics = details.metrics,
                    catalog = catalog
                )
                _uiState.update {
                    it.copy(
                        details = details,
                        financialScore = financialScore,
                        qualityOfLifeScore = qualityOfLifeScore,
                        isLoading = false,
                        isCurrencyLoading = true,
                        isEconomyLoading = true
                    )
                }

                val (rate, economy) = coroutineScope {
                    val rateRequest = async {
                        optionalRequest { getCurrencyRate(details.country) }
                    }
                    val economyRequest = async {
                        optionalRequest { getCountryEconomy(details.country.name) }
                    }
                    rateRequest.await() to economyRequest.await()
                }
                _uiState.update {
                    it.copy(
                        details = details.copy(
                            economicIndicators = details.economicIndicators.orEmpty() + economy.orEmpty()
                        ),
                        currencyRate = rate,
                        isCurrencyLoading = false,
                        isEconomyLoading = false
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCurrencyLoading = false,
                        isEconomyLoading = false,
                        error = CountryDetailError.NoInternet
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCurrencyLoading = false,
                        isEconomyLoading = false,
                        error = CountryDetailError.General
                    )
                }
            }
        }
    }

    private suspend fun <T> optionalRequest(request: suspend () -> T): T? = try {
        request()
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        null
    }
}
