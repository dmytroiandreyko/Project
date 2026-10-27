package com.example.myproject

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WikiApi {

    @GET("w/rest.php/v1/search/page")
    suspend fun searchPages(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): SearchResponseDto

    @GET("w/api.php?action=query&prop=extracts|pageimages|description&exintro=1&explaintext=1&pithumbsize=960&format=json&formatversion=2")
    suspend fun getPage(
        @Query("pageids") pageId: Int
    ): PageQueryResponseDto

    @GET("w/api.php?action=query&generator=geosearch&ggsradius=10000&ggslimit=20&prop=extracts|pageimages|description|coordinates&exintro=1&explaintext=1&exlimit=20&pithumbsize=500&format=json&formatversion=2")
    suspend fun searchNearby(
        @Query("ggscoord") coordinates: String,
        @Query("codistancefrompoint") distanceFrom: String = coordinates
    ): PageQueryResponseDto
}

object WikiNetwork {

    private const val BASE_URL = "https://uk.wikipedia.org/"
    private const val USER_AGENT = "MistaPamyatky/1.0 (https://github.com/dmytroiandreyko/Project)"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .build()
            )
        }
        .build()

    val api: WikiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WikiApi::class.java)
    }
}
