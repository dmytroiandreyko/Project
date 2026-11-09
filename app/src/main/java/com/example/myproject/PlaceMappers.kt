package com.example.myproject

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
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
        imageUrl = normalizeImageUrl(thumbnail?.url)?.replace(thumbnailWidth, "/500px-")
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
        imageUrl = normalizeImageUrl(thumbnail?.source)
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
        imageUrl = normalizeImageUrl(thumbnail?.source),
        distanceMeters = coordinates.firstOrNull()?.dist?.roundToInt()
    )
}

fun Place.withDetails(details: PlaceDetails): Place = copy(
    description = description ?: details.description,
    excerpt = details.extract ?: excerpt,
    imageUrl = imageUrl ?: details.imageUrl
)

fun normalizeImageUrl(raw: String?): String? {
    val url = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val secure = when {
        url.startsWith("//") -> "https:$url"
        url.startsWith("http://", ignoreCase = true) -> "https://" + url.substring("http://".length)
        url.startsWith("https://", ignoreCase = true) -> url
        else -> return null
    }
    return secure.toHttpUrlOrNull()?.toString()
}

private fun stripHtml(html: String): String =
    html.replace(htmlTag, "")
        .replace("&quot;", "\"")
        .replace("&#039;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")
        .trim()
