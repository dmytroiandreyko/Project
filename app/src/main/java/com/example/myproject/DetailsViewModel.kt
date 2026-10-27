package com.example.myproject

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data object NotFound : DetailsUiState
    data class Error(val error: LoadError) : DetailsUiState
    data class Loaded(val place: PlaceDetails) : DetailsUiState
}

class DetailsViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val placeId: Int = savedStateHandle[Routes.PLACE_ID_ARG] ?: -1
    private val placesRepository = PlacesRepository()

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading
            _uiState.value = try {
                placesRepository.getPlaceDetails(placeId)
                    ?.let { DetailsUiState.Loaded(it) }
                    ?: DetailsUiState.NotFound
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DetailsUiState.Error(e.toLoadError())
            }
        }
    }
}
