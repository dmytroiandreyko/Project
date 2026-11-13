package com.example.myproject

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val categories: List<PlaceCategory> = PlaceCategory.entries,
    val selectedCategory: PlaceCategory? = null,
    val isNearby: Boolean = false,
    val query: String = "",
    val activeQuery: String = "",
    val places: List<Place> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: LoadError? = null,
    val refreshError: LoadError? = null,
    val showDetails: Boolean = SettingsRepository.DEFAULT_SHOW_DETAILS
) {
    val isSearching: Boolean get() = activeQuery.isNotEmpty()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val placesRepository = PlacesRepository()
    private val locationProvider = LocationProvider(application)
    private val state = MutableStateFlow(MainUiState(isLoading = true))
    private var loadJob: Job? = null
    private var searchJob: Job? = null

    val uiState: StateFlow<MainUiState> = combine(
        state,
        settingsRepository.showDetails
    ) { current, showDetails ->
        current.copy(showDetails = showDetails)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(isLoading = true)
    )

    init {
        loadPlaces()
    }

    fun onCategorySelected(category: PlaceCategory?) {
        state.update { it.copy(selectedCategory = category, isNearby = false) }
        loadPlaces()
    }

    fun onNearbySelected() {
        state.update { it.copy(selectedCategory = null, isNearby = true) }
        loadPlaces()
    }

    fun onQueryChange(text: String) {
        state.update { it.copy(query = text) }
        searchJob?.cancel()
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            applyQuery("")
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            applyQuery(trimmed)
        }
    }

    fun onSearchSubmit() {
        searchJob?.cancel()
        applyQuery(state.value.query.trim())
    }

    fun onClearQuery() {
        onQueryChange("")
    }

    private fun applyQuery(trimmed: String) {
        if (trimmed == state.value.activeQuery) return
        state.update {
            it.copy(activeQuery = trimmed, isNearby = if (trimmed.isEmpty()) it.isNearby else false)
        }
        loadPlaces()
    }

    fun onRetry() {
        loadPlaces()
    }

    fun onRefresh() {
        loadPlaces(isRefresh = true)
    }

    fun onRefreshErrorShown() {
        state.update { it.copy(refreshError = null) }
    }

    private fun loadPlaces(isRefresh: Boolean = false) {
        val current = state.value
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            state.update {
                if (isRefresh) {
                    it.copy(isRefreshing = true, refreshError = null)
                } else {
                    it.copy(isLoading = true, isRefreshing = false, error = null, refreshError = null)
                }
            }
            try {
                val places = when {
                    current.isSearching ->
                        placesRepository.searchPlaces(current.selectedCategory, current.activeQuery)
                    current.isNearby -> {
                        val location = locationProvider.getCurrentLocation()
                        placesRepository.searchNearby(location.latitude, location.longitude)
                    }
                    else -> placesRepository.searchPlaces(current.selectedCategory)
                }
                state.update {
                    it.copy(places = places, isLoading = false, isRefreshing = false, error = null)
                }
                if (!current.isNearby) {
                    val detailed = placesRepository.loadMissingDetails(places)
                    state.update { it.copy(places = detailed) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state.update {
                    if (isRefresh && it.places.isNotEmpty() && it.error == null) {
                        it.copy(isRefreshing = false, refreshError = e.toLoadError())
                    } else {
                        it.copy(
                            places = emptyList(),
                            isLoading = false,
                            isRefreshing = false,
                            error = e.toLoadError()
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}
