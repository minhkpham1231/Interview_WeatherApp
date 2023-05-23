package com.interview.myweatherapp.api

import com.interview.myweatherapp.model.weather.WeatherModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FetchAPI {

    @GET("weather")
    suspend fun getWeather(
        @Query("q") cityName: String,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = "b0a9269fa97fbd7bc26aaef29be036d2"
    ) : Response<WeatherModel>
}