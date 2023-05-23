package com.interview.myweatherapp.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.interview.myweatherapp.api.FetchAPI
import com.interview.myweatherapp.model.weather.MainModel
import com.interview.myweatherapp.model.weather.WeatherModel
import com.interview.myweatherapp.model.weather.WeatherModelX
import com.interview.myweatherapp.roomdb.WeatherDAO
import com.interview.myweatherapp.roomdb.WeatherEntity
import com.interview.myweatherapp.util.Resource
import com.interview.myweatherapp.util.isLoading
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryImplTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: RepositoryImpl
    private val fetchAPI: FetchAPI = mockk()
    private val weatherDAO: WeatherDAO = mockk(relaxed = true)

    @Before
    fun setUp() {
        repository = RepositoryImpl(fetchAPI, weatherDAO)
    }

    @Test
    fun `getWeather returns local data if available`() = runTest {
        // Given
        val cityName = "london"
        val weatherModel = WeatherModel(7, MainModel(5.5), "London", listOf(WeatherModelX("hot", "icon", 2, "very hot")))
        val weatherEntity = WeatherEntity(weatherModel, cityName)
        coEvery { weatherDAO.readWeather(cityName) } returns weatherEntity

        // When
        val result = repository.getWeather(cityName)

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        result.collect { resource ->
            when (resource) {
                is Resource.Loading -> assertEquals(true, resource.isLoading())
                is Resource.Success -> assertEquals(weatherModel, resource.data)
                else -> fail("Unexpected resource type")
            }
        }
        coVerify(exactly = 0) { fetchAPI.getWeather(any()) }
    }

    @Test
    fun `getWeather returns remote data if local data is not available`() = runTest {
        // Given
        val cityName = "london"
        val weatherModel = WeatherModel(7, MainModel(5.5), "London", listOf(WeatherModelX("hot", "icon", 2, "very hot")))
        val weatherEntity = WeatherEntity(weatherModel, cityName)
        coEvery { weatherDAO.readWeather(cityName) } returns null
        coEvery { fetchAPI.getWeather(cityName) } returns Response.success(weatherModel)

        // When
        val result = repository.getWeather(cityName)

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        result.collect { resource ->
            when (resource) {
                is Resource.Loading -> assertEquals(true, resource.isLoading())
                is Resource.Success -> assertEquals(weatherModel, resource.data)
                else -> fail("Unexpected resource type")
            }
        }

        coVerify { fetchAPI.getWeather(cityName) }
        coVerify { weatherDAO.insertWeather(weatherEntity) }
    }

    @Test
    fun `getWeather returns error if remote data is not successful`() = runTest {
        // Given
        val cityName = "Berlin"
        coEvery { weatherDAO.readWeather(cityName) } returns null
        coEvery { fetchAPI.getWeather(cityName) } returns Response.error(404, "Not found".toResponseBody("text/plain".toMediaTypeOrNull()))

        // When
        val result = repository.getWeather(cityName)

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        result.collect { resource ->
            when (resource) {
                is Resource.Loading -> assertEquals(true, resource.isLoading())
                is Resource.Error -> assertEquals("Not found", resource.message)
                else -> fail("Unexpected resource type")
            }
        }
    }

    @Test
    fun `getWeather returns error if remote data throws exception`() = runTest {
        // Given
        val cityName = "Tokyo"
        coEvery { weatherDAO.readWeather(cityName) } returns null
        coEvery { fetchAPI.getWeather(cityName) } throws IOException("Network error")

        // When
        val result = repository.getWeather(cityName)

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        result.collect { resource ->
            when (resource) {
                is Resource.Loading -> assertEquals(true, resource.isLoading())
                is Resource.Error -> assertEquals("Couldn't load data", resource.message)
                else -> fail("Unexpected resource type")
            }
        }
    }
}
