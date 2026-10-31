package com.example.myproject

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class PlacesRepositoryTest {

    private class FakeWikiApi(
        private val search: Map<String, List<SearchPageDto>> = emptyMap(),
        private val nearby: List<PageDto> = emptyList()
    ) : WikiApi {
        override suspend fun searchPages(query: String, limit: Int) =
            SearchResponseDto(search[query].orEmpty())

        override suspend fun getPage(pageId: Int) = PageQueryResponseDto(batchComplete = true)

        override suspend fun searchNearby(coordinates: String, distanceFrom: String) =
            PageQueryResponseDto(query = PageQueryDto(nearby))
    }

    private fun page(id: Int) = SearchPageDto(id = id, title = "Сторінка $id")

    private fun List<Place>.assertUniqueIds() =
        assertEquals(map { it.id }.distinct(), map { it.id })

    @Test
    fun allCategories_overlappingPages_haveUniqueIds() = runBlocking {
        val api = FakeWikiApi(
            search = mapOf(
                PlaceCategory.CITY.searchQuery to listOf(page(1), page(2), page(3)),
                PlaceCategory.LANDMARK.searchQuery to listOf(page(3), page(4), page(1))
            )
        )

        val places = PlacesRepository(api).searchPlaces(null)

        places.assertUniqueIds()
        assertEquals(listOf(1, 2, 3, 4), places.map { it.id })
    }

    @Test
    fun singleCategory_duplicatePages_haveUniqueIds() = runBlocking {
        val api = FakeWikiApi(
            search = mapOf(
                PlaceCategory.CITY.searchQuery to listOf(page(5), page(6), page(5))
            )
        )

        val places = PlacesRepository(api).searchPlaces(PlaceCategory.CITY)

        places.assertUniqueIds()
        assertEquals(2, places.size)
    }

    @Test
    fun nearby_duplicatePages_haveUniqueIds() = runBlocking {
        val api = FakeWikiApi(
            nearby = listOf(
                PageDto(pageId = 7, title = "А", coordinates = listOf(CoordinatesDto(0.0, 0.0, 300.0))),
                PageDto(pageId = 8, title = "Б", coordinates = listOf(CoordinatesDto(0.0, 0.0, 100.0))),
                PageDto(pageId = 7, title = "А", coordinates = listOf(CoordinatesDto(0.0, 0.0, 300.0)))
            )
        )

        val places = PlacesRepository(api).searchNearby(0.0, 0.0)

        places.assertUniqueIds()
        assertEquals(listOf(8, 7), places.map { it.id })
    }
}
