package com.example.myproject

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class PlacesRepository(
    private val api: WikiApi = WikiNetwork.api,
    maxConcurrentRequests: Int = MAX_CONCURRENT_REQUESTS
) {

    private val requestLimit = Semaphore(maxConcurrentRequests)

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

    suspend fun loadMissingDetails(
        places: List<Place>,
        count: Int = DETAILS_PREFETCH_COUNT
    ): List<Place> = coroutineScope {
        val details = places.take(count)
            .map { place -> async { getPlaceDetailsOrNull(place.id) } }
            .awaitAll()
            .filterNotNull()
            .associateBy { it.id }
        places.map { place -> details[place.id]?.let(place::withDetails) ?: place }
    }

    suspend fun getPlaceDetails(id: Int): PlaceDetails? =
        limited { api.getPage(id) }.pagesOrThrow().firstOrNull()?.toPlaceDetails()

    suspend fun searchNearby(latitude: Double, longitude: Double): List<Place> =
        limited { api.searchNearby("$latitude|$longitude") }
            .pagesOrThrow()
            .mapNotNull { it.toNearbyPlace() }
            .distinctBy { it.id }
            .sortedBy { it.distanceMeters ?: Int.MAX_VALUE }

    private suspend fun getPlaceDetailsOrNull(id: Int): PlaceDetails? =
        try {
            getPlaceDetails(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    private suspend fun <T> limited(block: suspend () -> T): T =
        requestLimit.withPermit { block() }

    private fun PageQueryResponseDto.pagesOrThrow(): List<PageDto> {
        error?.let { throw WikiApiException(it.info) }
        query?.let { return it.pages }
        if (batchComplete) return emptyList()
        throw BadResponseException()
    }

    private suspend fun searchCategory(category: PlaceCategory): List<Place> =
        limited { api.searchPages(category.searchQuery) }.pages
            .mapNotNull { it.toPlace(category) }
            .distinctBy { it.id }

    companion object {
        const val MAX_CONCURRENT_REQUESTS = 4
        const val DETAILS_PREFETCH_COUNT = 12
    }
}
