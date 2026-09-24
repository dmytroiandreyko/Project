package com.example.myproject

import androidx.annotation.DrawableRes

data class Place(
    val id: Int,
    val key: String,
    val title: String,
    val description: String,
    val excerpt: String,
    @DrawableRes val imageRes: Int
)

val samplePlaces = listOf(
    Place(
        id = 3624,
        key = "Львів",
        title = "Львів",
        description = "місто на заході України",
        excerpt = "Адміністративний центр Львівської області, історичний центр включено до списку ЮНЕСКО",
        imageRes = R.drawable.lviv
    ),
    Place(
        id = 12345,
        key = "Київ",
        title = "Київ",
        description = "столиця України",
        excerpt = "Найбільше місто країни, розташоване на річці Дніпро",
        imageRes = R.drawable.kyiv
    ),
    Place(
        id = 67890,
        key = "Одеса",
        title = "Одеса",
        description = "місто на узбережжі Чорного моря",
        excerpt = "Великий морський порт, відомий Потьомкінськими сходами",
        imageRes = R.drawable.odesa
    )
)