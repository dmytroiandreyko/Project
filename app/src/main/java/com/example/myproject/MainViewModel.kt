package com.example.myproject

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MainUiState(
    val categories: List<PlaceCategory> = PlaceCategory.entries,
    val selectedCategory: PlaceCategory? = null,
    val places: List<Place> = samplePlaces,
    val showDetails: Boolean = SettingsRepository.DEFAULT_SHOW_DETAILS
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)
    private val selectedCategory = MutableStateFlow<PlaceCategory?>(null)

    val uiState: StateFlow<MainUiState> = combine(
        selectedCategory,
        repository.showDetails
    ) { category, showDetails ->
        MainUiState(
            selectedCategory = category,
            places = if (category == null) {
                samplePlaces
            } else {
                samplePlaces.filter { it.category == category }
            },
            showDetails = showDetails
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState()
    )

    fun onCategorySelected(category: PlaceCategory?) {
        selectedCategory.value = category
    }
}
