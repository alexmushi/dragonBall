package com.example.pokedexapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pokedexapp.databinding.ActivityMainBinding
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = DragonBallAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeBinding()
        setupRecyclerView()
        fetchCharacters()  // Call fetchCharacters here to load data
    }

    private fun initializeBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupRecyclerView() {
        binding.RVPokemon.setHasFixedSize(true)
        binding.RVPokemon.layoutManager = LinearLayoutManager(this)
        binding.RVPokemon.adapter = adapter
    }

    private fun fetchCharacters() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dragonball-api.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(DragonBallApiService::class.java)
        val allCharacters = mutableListOf<DragonBallCharacter>()
        var currentPage = 1
        var totalPages: Int

        // Recursive function to fetch all pages
        fun fetchPage(page: Int) {
            api.getCharacters(page).enqueue(object : Callback<CharacterResponse> {
                override fun onResponse(call: Call<CharacterResponse>, response: Response<CharacterResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { characterResponse ->
                            // Add characters to the list
                            allCharacters.addAll(characterResponse.items)

                            // Update total pages from the response metadata
                            totalPages = characterResponse.meta.totalPages

                            // Check if more pages are left to fetch
                            if (page < totalPages) {
                                fetchPage(page + 1) // Fetch the next page
                            } else {
                                // All pages are fetched, set data to the adapter
                                adapter.setData(allCharacters)
                            }
                        }
                    } else {
                        Log.e("MainActivity", "API response unsuccessful: ${response.errorBody()}")
                    }
                }

                override fun onFailure(call: Call<CharacterResponse>, t: Throwable) {
                    Log.e("MainActivity", "API call failed: ${t.message}")
                }
            })
        }

        // Start fetching from page 1
        fetchPage(currentPage)
    }
}
