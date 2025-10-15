package com.example.coffeerankingapk.tomtom

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

// TomTom REST API models
data class SearchResponse(
    val results: List<SearchResult>
)

data class SearchResult(
    val poi: Poi?,
    val address: Address,
    val position: Position
)

data class Poi(
    val name: String,
    val categories: List<String>?
)

data class Address(
    val freeformAddress: String
)

data class Position(
    val lat: Double,
    val lon: Double
)

data class RouteResponse(
    val routes: List<Route>
)

data class Route(
    val summary: RouteSummary,
    val legs: List<RouteLeg>
)

data class RouteSummary(
    val lengthInMeters: Int,
    val travelTimeInSeconds: Int
)

data class RouteLeg(
    val points: List<Position>
)

// TomTom REST APIs
interface TomTomSearchApi {
    @GET("search/2/search/{query}.json")
    suspend fun search(
        @Path("query") query: String,
        @Query("key") apiKey: String,
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("radius") radius: Int = 5000,
        @Query("limit") limit: Int = 10
    ): SearchResponse
    
    @GET("search/2/categorySearch/{category}.json")
    suspend fun categorySearch(
        @Path("category") category: String,
        @Query("key") apiKey: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Int = 5000,
        @Query("limit") limit: Int = 10
    ): SearchResponse
}

interface TomTomRoutingApi {
    @GET("routing/1/calculateRoute/{locations}/json")
    suspend fun calculateRoute(
        @Path("locations") locations: String, // "lat1,lon1:lat2,lon2"
        @Query("key") apiKey: String,
        @Query("routeType") routeType: String = "fastest",
        @Query("traffic") traffic: Boolean = true
    ): RouteResponse
}

object TomTomApiClient {
    private const val BASE_URL = "https://api.tomtom.com/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val searchApi: TomTomSearchApi = retrofit.create(TomTomSearchApi::class.java)
    val routingApi: TomTomRoutingApi = retrofit.create(TomTomRoutingApi::class.java)
}
