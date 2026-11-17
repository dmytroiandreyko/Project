package com.example.myproject

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale

data class FavoritesUiState(
    val places: List<Place> = emptyList(),
    val favoriteIds: Set<Int> = emptySet(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: LoadError? = null,
    val refreshError: LoadError? = null,
    val showDetails: Boolean = SettingsRepository.DEFAULT_SHOW_DETAILS
)

private data class FavoritesLoadStatus(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: LoadError? = null,
    val refreshError: LoadError? = null
)

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val favoritesRepository = FavoritesRepository(application)
    private val settingsRepository = SettingsRepository(application)
    private val placesRepository = PlacesRepository()

    private val loaded = MutableStateFlow<Map<Int, Place>>(emptyMap())
    private val status = MutableStateFlow(FavoritesLoadStatus())
    private var requestedIds: Set<Int> = emptySet()
    private var currentIds: Set<Int> = emptySet()
    private var refreshJob: Job? = null
    private val collator = Collator.getInstance(Locale.forLanguageTag("uk"))

    val uiState: StateFlow<FavoritesUiState> = combine(
        favoritesRepository.favoriteIds,
        loaded,
        status,
        settingsRepository.showDetails
    ) { ids, places, current, showDetails ->
        FavoritesUiState(
            places = ids.mapNotNull { places[it] }.sortedWith(compareBy(collator) { it.title }),
            favoriteIds = ids,
            isLoading = current.isLoading,
            isRefreshing = current.isRefreshing,
            error = current.error,
            refreshError = current.refreshError,
            showDetails = showDetails
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoritesUiState()
    )

    init {
        viewModelScope.launch {
            favoritesRepository.favoriteIds.collectLatest { ids ->
                currentIds = ids
                val missing = ids - requestedIds
                if (missing.isEmpty()) {
                    status.update { it.copy(isLoading = false) }
                } else {
                    load(missing, isRefresh = false)
                }
            }
        }
    }

    fun onToggleFavorite(place: Place) {
        viewModelScope.launch { favoritesRepository.toggle(place.id) }
    }

    fun onRestore(place: Place) {
        viewModelScope.launch { favoritesRepository.setFavorite(place.id, true) }
    }

    fun onRetry() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch { load(currentIds - requestedIds, isRefresh = false) }
    }

    fun onRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch { load(currentIds, isRefresh = true) }
    }

    fun onRefreshErrorShown() {
        status.update { it.copy(refreshError = null) }
    }

    private suspend fun load(ids: Set<Int>, isRefresh: Boolean) {
        status.update {
            if (isRefresh) {
                it.copy(isRefreshing = true, refreshError = null)
            } else {
                it.copy(isLoading = true, error = null, refreshError = null)
            }
        }
        try {
            val places = placesRepository.getPlacesByIds(ids).associateBy { it.id }
            loaded.update { if (isRefresh) places else it + places }
            requestedIds = if (isRefresh) ids else requestedIds + ids
            status.update { it.copy(isLoading = false, isRefreshing = false, error = null) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val hasPlaces = currentIds.any { it in loaded.value }
            status.update {
                if (hasPlaces) {
                    it.copy(isLoading = false, isRefreshing = false, refreshError = e.toLoadError())
                } else {
                    it.copy(isLoading = false, isRefreshing = false, error = e.toLoadError())
                }
            }
        }
    }
}
