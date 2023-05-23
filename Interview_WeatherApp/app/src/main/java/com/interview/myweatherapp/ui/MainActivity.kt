package com.interview.myweatherapp.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.interview.myweatherapp.R
import com.interview.myweatherapp.databinding.ActivityMainBinding
import com.interview.myweatherapp.util.Resource
import com.interview.myweatherapp.workers.DatabaseClearerWorker
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Duration
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel : MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private val sharedPref by lazy {
        getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uploadWorkRequest = PeriodicWorkRequestBuilder<DatabaseClearerWorker>(Duration.ofMinutes(1)).build()
        WorkManager.getInstance(applicationContext).enqueue(uploadWorkRequest)

        subscribeObserver()

        setSearchSubmitListener()

        loadLastSearched()
    }

    private fun loadLastSearched() {
        val defaultValue = getString(R.string.saved_search_default)
        val searchQuery = sharedPref.getString(getString(R.string.saved_search_key), defaultValue)

        if (searchQuery != defaultValue) {
            if (searchQuery != null) {
                viewModel.getWeather(searchQuery)
            }
        }
    }

    private fun setSearchSubmitListener() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.getWeather(query)

                    with (sharedPref.edit()) {
                        putString(getString(R.string.saved_search_key), query)
                        apply()
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun subscribeObserver() {
        lifecycleScope.launch {
            viewModel.weather.collectLatest { state ->
                when(state) {
                    is Resource.Loading -> {
                        Log.i("API Response: ", "Loading...")
                    }
                    is Resource.Success -> {
                        Log.i("API Response: ", "Success")
                        Picasso.get()
                            .load("https://openweathermap.org/img/wn/${state.data?.weather?.get(0)?.icon}@4x.png")
                            .into(binding.icon)
                        binding.cityName.text = state.data?.name
                        binding.temperature.text = "${state.data?.main?.temp?.roundToInt()}\u00B0"
                        binding.condition.text = state.data?.weather?.get(0)?.main
                    }
                    is Resource.Error -> {
                        Log.i("API Response: ", "Error -> ${state.message}")
                        Toast.makeText(this@MainActivity, "Sorry, no results found.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}