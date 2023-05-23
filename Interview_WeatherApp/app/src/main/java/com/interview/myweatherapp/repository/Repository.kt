package com.interview.myweatherapp.repository

import com.interview.myweatherapp.model.weather.WeatherModel
import com.interview.myweatherapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface Repository {

    fun getWeather(cityName: String) : Flow<Resource<WeatherModel>>
}