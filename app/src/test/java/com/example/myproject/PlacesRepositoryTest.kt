package com.example.myproject

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlacesRepositoryTest {

    private class FakeWikiApi(
        private val search: Map<String, List<SearchPageDto>> = emptyMap(),
        private val nearby: List<PageDto> = emptyList(),
        private val page: suspend (Int) -> PageQueryResponseDto = { PageQueryResponseDto(batchComplete = true) }
    ) : WikiApi {
        override suspend fun searchPages(query: String, limit: Int) =
            SearchResponseDto(search[query].orEmpty())

        override suspend fun getPage(pageId: Int) = page(pageId)

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

    @Test
    fun missingDetails_loadedConcurrently_withinLimit() = runBlocking {
        var active = 0
        var maxActive = 0
        val api = FakeWikiApi(page = { id ->
            active++
            maxActive = maxOf(maxActive, active)
            delay(20)
            active--
            PageQueryResponseDto(
                query = PageQueryDto(listOf(PageDto(pageId = id, title = "Сторінка $id", extract = "Вступ $id")))
            )
        })
        val places = (1..10).map { page(it).toPlace(PlaceCategory.CITY)!! }

        val detailed = PlacesRepository(api, maxConcurrentRequests = 3).loadMissingDetails(places, count = 10)

        assertEquals(3, maxActive)
        assertEquals((1..10).map { "Вступ $it" }, detailed.map { it.excerpt })
    }

    @Test
    fun missingDetails_failedRequest_keepsPlace() = runBlocking {
        val api = FakeWikiApi(page = { id ->
            if (id == 2) throw java.io.IOException()
            PageQueryResponseDto(
                query = PageQueryDto(listOf(PageDto(pageId = id, title = "Т", extract = "Вступ")))
            )
        })
        val places = (1..3).map { page(it).toPlace(PlaceCategory.CITY)!! }

        val detailed = PlacesRepository(api).loadMissingDetails(places)

        assertEquals(listOf(1, 2, 3), detailed.map { it.id })
        assertEquals(listOf("Вступ", "", "Вступ"), detailed.map { it.excerpt })
    }

    @Test
    fun imageUrl_isRepairedInMapper() {
        assertEquals(
            "https://upload.wikimedia.org/a/60px-B.jpg",
            normalizeImageUrl("//upload.wikimedia.org/a/60px-B.jpg")
        )
        assertEquals("https://upload.wikimedia.org/a.jpg", normalizeImageUrl(" http://upload.wikimedia.org/a.jpg "))
        assertEquals("https://upload.wikimedia.org/a%20b.jpg", normalizeImageUrl("https://upload.wikimedia.org/a b.jpg"))
        assertNull(normalizeImageUrl(null))
        assertNull(normalizeImageUrl("   "))
        assertNull(normalizeImageUrl("not a url"))
        assertNull(normalizeImageUrl("https://"))
    }

    @Test
    fun searchThumbnail_httpUrl_becomesHttps() {
        val dto = SearchPageDto(id = 1, title = "А", thumbnail = SearchThumbnailDto("http://upload.wikimedia.org/x/60px-A.jpg"))

        assertEquals("https://upload.wikimedia.org/x/500px-A.jpg", dto.toPlace(PlaceCategory.CITY)?.imageUrl)
    }
}
