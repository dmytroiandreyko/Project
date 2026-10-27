package com.example.myproject

import kotlin.math.roundToInt

private val htmlTag = Regex("<[^>]*>")
private val thumbnailWidth = Regex("/\\d+px-")

fun SearchPageDto.toPlace(category: PlaceCategory): Place? {
    val title = title?.takeIf { it.isNotBlank() } ?: return null
    return Place(
        id = id,
        key = key?.takeIf { it.isNotBlank() } ?: title.replace(' ', '_'),
        title = title,
        description = description?.takeIf { it.isNotBlank() },
        excerpt = excerpt?.let(::stripHtml).orEmpty(),
        category = category,
        imageUrl = thumbnail?.url?.let { toAbsoluteUrl(it).replace(thumbnailWidth, "/500px-") }
    )
}

fun PageDto.toPlaceDetails(): PlaceDetails? {
    if (missing) return null
    val id = pageId ?: return null
    val title = title?.takeIf { it.isNotBlank() } ?: return null
    return PlaceDetails(
        id = id,
        title = title,
        description = description?.takeIf { it.isNotBlank() },
        extract = extract?.trim()?.takeIf { it.isNotEmpty() },
        imageUrl = thumbnail?.source?.let(::toAbsoluteUrl)
    )
}

fun PageDto.toNearbyPlace(): Place? {
    val id = pageId ?: return null
    val title = title?.takeIf { it.isNotBlank() } ?: return null
    return Place(
        id = id,
        key = title.replace(' ', '_'),
        title = title,
        description = description?.takeIf { it.isNotBlank() },
        excerpt = extract?.trim().orEmpty(),
        category = null,
        imageUrl = thumbnail?.source?.let(::toAbsoluteUrl),
        distanceMeters = coordinates.firstOrNull()?.dist?.roundToInt()
    )
}

private fun toAbsoluteUrl(url: String): String =
    if (url.startsWith("//")) "https:$url" else url

private fun stripHtml(html: String): String =
    html.replace(htmlTag, "")
        .replace("&quot;", "\"")
        .replace("&#039;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")
        .trim()
