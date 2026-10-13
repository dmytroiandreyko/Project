package com.example.myproject

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponseDto(
    val pages: List<SearchPageDto> = emptyList()
)

@Serializable
data class SearchPageDto(
    val id: Int,
    val key: String? = null,
    val title: String? = null,
    val excerpt: String? = null,
    val description: String? = null,
    val thumbnail: SearchThumbnailDto? = null
)

@Serializable
data class SearchThumbnailDto(
    val url: String? = null
)

@Serializable
data class PageQueryResponseDto(
    val query: PageQueryDto? = null
)

@Serializable
data class PageQueryDto(
    val pages: List<PageDto> = emptyList()
)

@Serializable
data class PageDto(
    @SerialName("pageid") val pageId: Int? = null,
    val title: String? = null,
    val extract: String? = null,
    val description: String? = null,
    val thumbnail: PageThumbnailDto? = null,
    val missing: Boolean = false
)

@Serializable
data class PageThumbnailDto(
    val source: String? = null
)
