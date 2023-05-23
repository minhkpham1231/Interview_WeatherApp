package com.interview.myweatherapp.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.interview.myweatherapp.model.weather.WeatherModel

@Entity(tableName = "weather")
data class WeatherEntity(
    val weatherModel: WeatherModel,
    @PrimaryKey(autoGenerate = false)
    val query: String
    ) {
    var cityName: String = weatherModel.name
    var icon: String = weatherModel.weather[0].icon
    var temperature: Double = weatherModel.main.temp
    var condition: String = weatherModel.weather[0].main
}