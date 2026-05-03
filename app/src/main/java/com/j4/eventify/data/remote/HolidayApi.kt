package com.j4.eventify.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// 1. How the data looks coming from the internet
data class ApiHoliday(
    @SerializedName("date") val date: String,           // e.g., "2026-12-25"
    @SerializedName("localName") val localName: String, // e.g., "Araw ng Pasko"
    @SerializedName("name") val englishName: String
)

// 2. The instruction manual for talking to the API
interface HolidayApiService {
    // This hits: https://date.nager.at/api/v3/PublicHolidays/2026/PH
    @GET("api/v3/PublicHolidays/{year}/{countryCode}")
    suspend fun getHolidays(
        @Path("year") year: Int,
        @Path("countryCode") countryCode: String
    ): List<ApiHoliday>
}

// 3. The actual engine that connects to the internet
object RetrofitClient {
    private const val BASE_URL = "https://date.nager.at/"

    val holidayApi: HolidayApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HolidayApiService::class.java)
    }
}