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
    val places: List<Place> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showDetails: Boolean = SettingsRepository.DEFAULT_SHOW_DETAILS
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val placesRepository = PlacesRepository()
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
        loadPlaces(null)
    }

    fun onCategorySelected(category: PlaceCategory?) {
        state.update { it.copy(selectedCategory = category) }
        loadPlaces(category)
    }

    fun onRetry() {
        loadPlaces(state.value.selectedCategory)
    }

    private fun loadPlaces(category: PlaceCategory?) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val places = placesRepository.searchPlaces(category)
                state.update { it.copy(places = places, isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state.update {
                    it.copy(
                        places = emptyList(),
                        isLoading = false,
                        errorMessage = "Не вдалося завантажити дані. Перевірте з'єднання з інтернетом."
                    )
                }
            }
        }
    }
}
