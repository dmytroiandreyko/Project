package com.example.myproject

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainUiState(
    val categories: List<PlaceCategory> = PlaceCategory.entries,
    val selectedCategory: PlaceCategory? = null,
    val places: List<Place> = samplePlaces
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun onCategorySelected(category: PlaceCategory?) {
        _uiState.update { state ->
            state.copy(
                selectedCategory = category,
                places = if (category == null) {
                    samplePlaces
                } else {
                    samplePlaces.filter { it.category == category }
                }
            )
        }
    }
}
