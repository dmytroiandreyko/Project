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
        api.getPage(id).query?.pages?.firstOrNull()?.toPlaceDetails()

    private suspend fun searchCategory(category: PlaceCategory): List<Place> =
        api.searchPages(category.searchQuery).pages.mapNotNull { it.toPlace(category) }
}
