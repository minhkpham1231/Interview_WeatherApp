package com.interview.myweatherapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.interview.myweatherapp.model.weather.WeatherModel
import com.interview.myweatherapp.repository.Repository
import com.interview.myweatherapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: Repository): ViewModel(){

    private val _weather : MutableStateFlow<Resource<WeatherModel>> = MutableStateFlow(Resource.Loading())
    val weather: StateFlow<Resource<WeatherModel>> get() = _weather

    fun getWeather(cityName: String) {
        repository.getWeather(cityName)
            .onEach { _weather.value = it }
            .launchIn(viewModelScope)
    }
}