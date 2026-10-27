package com.example.myproject

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
    val places: List<Place> = emptyList(),
    val isLoading: Boolean = false,
    val error: LoadError? = null,
    val showDetails: Boolean = SettingsRepository.DEFAULT_SHOW_DETAILS
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val placesRepository = PlacesRepository()
    private val locationProvider = LocationProvider(application)
    private val state = MutableStateFlow(MainUiState(isLoading = true))
    private var loadJob: Job? = null

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

    fun onRetry() {
        loadPlaces()
    }

    private fun loadPlaces() {
        val current = state.value
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            state.update { it.copy(isLoading = true, error = null) }
            try {
                val places = if (current.isNearby) {
                    val location = locationProvider.getCurrentLocation()
                    placesRepository.searchNearby(location.latitude, location.longitude)
                } else {
                    placesRepository.searchPlaces(current.selectedCategory)
                }
                state.update { it.copy(places = places, isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state.update {
                    it.copy(places = emptyList(), isLoading = false, error = e.toLoadError())
                }
            }
        }
    }
}
