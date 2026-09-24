package com.example.myproject

import androidx.annotation.DrawableRes

enum class PlaceCategory(val label: String) {
    CITY("Місто"),
    LANDMARK("Пам'ятка"),
}

data class Place(
    val id: Int,
    val key: String,
    val title: String,
    val description: String,
    val excerpt: String,
    val category: PlaceCategory,
    @DrawableRes val imageRes: Int
)

val samplePlaces = listOf(
    Place(
        id = 3624,
        key = "Львів",
        title = "Львів",
        description = "місто на заході України",
        excerpt = "Адміністративний центр Львівської області, історичний центр включено до списку ЮНЕСКО",
        category = PlaceCategory.CITY,
        imageRes = R.drawable.lviv
    ),
    Place(
        id = 12345,
        key = "Київ",
        title = "Київ",
        description = "столиця України",
        excerpt = "Найбільше місто країни, розташоване на річці Дніпро",
        category = PlaceCategory.CITY,
        imageRes = R.drawable.kyiv
    ),
    Place(
        id = 67890,
        key = "Одеса",
        title = "Одеса",
        description = "місто на узбережжі Чорного моря",
        excerpt = "Великий морський порт, відомий Потьомкінськими сходами",
        category = PlaceCategory.CITY,
        imageRes = R.drawable.odesa
    ),
    Place(
        id = 501001,
        key = "Пам'ятник_засновникам_Києва",
        title = "Пам'ятник Кию, Щеку, Хориву та Либеді",
        description = "пам'ятка на честь легендарних засновників Києва",
        excerpt = "Скульптурна композиція на набережній Дніпра, зображує братів-засновників міста та їхню сестру Либідь",
        category = PlaceCategory.LANDMARK,
        imageRes = R.drawable.pamyatka3
    ),
    Place(
        id = 502002,
        key = "Ластівчине_гніздо",
        title = "Ластівчине гніздо",
        description = "пам'ятка архітектури на південному березі Криму",
        excerpt = "Замок на скелі Аврорина над Чорним морем, символ Криму",
        category = PlaceCategory.LANDMARK,
        imageRes = R.drawable.pamyatka2
    ),
    Place(
        id = 503003,
        key = "Ай-Петрі",
        title = "Ай-Петрі",
        description = "Археологічні пам'ятки античного і середньовічного Криму",
        excerpt = "Древні стовби",
        category = PlaceCategory.LANDMARK,
        imageRes = R.drawable.pamyatka1
    )
)