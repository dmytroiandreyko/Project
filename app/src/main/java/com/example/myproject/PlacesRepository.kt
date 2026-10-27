package com.example.myproject

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class PlacesRepository(private val api: WikiApi = WikiNetwork.api) {

    suspend fun searchPlaces(category: PlaceCategory?): List<Place> {
        if (category != null) return searchCategory(category)
        return coroutineScope {
            PlaceCategory.entries
                .map { async { searchCategory(it) } }
                .awaitAll()
                .flatten()
                .distinctBy { it.id }
        }
    }

    suspend fun getPlaceDetails(id: Int): PlaceDetails? =
        api.getPage(id).pagesOrThrow().firstOrNull()?.toPlaceDetails()

    suspend fun searchNearby(latitude: Double, longitude: Double): List<Place> =
        api.searchNearby("$latitude|$longitude")
            .pagesOrThrow()
            .mapNotNull { it.toNearbyPlace() }
            .sortedBy { it.distanceMeters ?: Int.MAX_VALUE }

    private fun PageQueryResponseDto.pagesOrThrow(): List<PageDto> {
        error?.let { throw WikiApiException(it.info) }
        query?.let { return it.pages }
        if (batchComplete) return emptyList()
        throw BadResponseException()
    }

    private suspend fun searchCategory(category: PlaceCategory): List<Place> =
        api.searchPages(category.searchQuery).pages.mapNotNull { it.toPlace(category) }
}
