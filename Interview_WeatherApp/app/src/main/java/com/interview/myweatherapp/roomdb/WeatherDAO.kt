package com.interview.myweatherapp.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weatherEntity: WeatherEntity)

    @Query("SELECT * FROM weather WHERE :query LIKE '%' || query || '%' OR query LIKE '%' || :query || '%'")
    suspend fun readWeather(query: String) : WeatherEntity?

    @Query("DELETE FROM weather")
    fun delete()
}