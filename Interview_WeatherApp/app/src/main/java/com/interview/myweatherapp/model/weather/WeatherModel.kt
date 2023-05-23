package com.interview.myweatherapp.model.weather


import com.google.gson.annotations.SerializedName

data class WeatherModel(
    @SerializedName("id")
    val id: Int,
    @SerializedName("main")
    val main: MainModel,
    @SerializedName("name")
    val name: String,
    @SerializedName("weather")
    val weather: List<WeatherModelX>
)