package com.j4.eventify.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// 1. How the location data looks coming from OpenStreetMap
data class OsmPlace(
    @SerializedName("display_name") val displayName: String,
    @SerializedName("lat") val lat: String,
    @SerializedName("lon") val lon: String
)

// 2. The instruction manual for searching places
interface LocationApiService {
    @GET("search")
    suspend fun searchPlaces(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5,
        @retrofit2.http.Header("User-Agent") userAgent: String = "EventifyApp/1.0"
    ): List<OsmPlace>

    // ── ADD THIS NEW FUNCTION ──
    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json",
        @retrofit2.http.Header("User-Agent") userAgent: String = "EventifyApp/1.0"
    ): OsmPlace
}

// 3. The engine
object LocationClient {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    val api: LocationApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocationApiService::class.java)
    }
}