package com.example.myproject

enum class PlaceCategory(val label: String, val searchQuery: String) {
    CITY("Місто", "incategory:\"Обласні центри України\""),
    LANDMARK("Пам'ятка", "incategory:\"Пам'ятки архітектури України національного значення\""),
}

data class Place(
    val id: Int,
    val key: String,
    val title: String,
    val description: String?,
    val excerpt: String,
    val category: PlaceCategory?,
    val imageUrl: String?,
    val distanceMeters: Int? = null
)

data class PlaceDetails(
    val id: Int,
    val title: String,
    val description: String?,
    val extract: String?,
    val imageUrl: String?
)
