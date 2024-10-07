package com.example.pokedexapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pokedexapp.databinding.ActivityMainBinding
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = DragonBallAdapter()
    private var selectedSortOption: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeBinding()
        setupRecyclerView()

        // Fetch all characters initially without filters or sorting
        fetchCharacters()

        // Set up the filter button to show the filter dialog
        binding.filterButton.setOnClickListener {
            showFilterDialog()
        }
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

    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.filter_dialog, null)
        val filterDialog = AlertDialog.Builder(this)
            .setTitle("Filter Characters")
            .setView(dialogView)
            .create()

        // Configure the sort type Spinner
        val sortSpinner = dialogView.findViewById<Spinner>(R.id.sortTypeSpinner)
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSortOption = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedSortOption = "Sort by Ki" // Default sorting option
            }
        }

        dialogView.findViewById<Button>(R.id.applyFiltersButton).setOnClickListener {
            // Fetch characters based on the selected sort option
            fetchCharacters(sortType = selectedSortOption)
            filterDialog.dismiss()
        }

        filterDialog.show()
    }

    private fun fetchCharacters(
        name: String? = null,
        race: String? = null,
        gender: String? = null,
        affiliation: String? = null,
        sortType: String? = "Sort by Ki"
    ) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dragonball-api.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(DragonBallApiService::class.java)
        val allCharacters = mutableListOf<DragonBallCharacter>()
        var currentPage = 1
        var totalPages: Int

        fun fetchPage(page: Int) {
            api.getCharacters(page, name = name, race = race, gender = gender, affiliation = affiliation)
                .enqueue(object : Callback<CharacterResponse> {
                    override fun onResponse(call: Call<CharacterResponse>, response: Response<CharacterResponse>) {
                        if (response.isSuccessful) {
                            response.body()?.let { characterResponse ->
                                allCharacters.addAll(characterResponse.items)
                                totalPages = characterResponse.meta.totalPages

                                if (page < totalPages) {
                                    fetchPage(page + 1)
                                } else {
                                    // Sort based on the selected sort type
                                    val sortedCharacters = when (sortType) {
                                        "Sort Alphabetically" -> allCharacters.sortedBy { it.name }
                                        "Sort by Race" -> allCharacters.sortedBy { it.race }
                                        "Sort by Gender" -> allCharacters.sortedBy { it.gender }
                                        "Sort by Affiliation" -> allCharacters.sortedBy { it.affiliation }
                                        else -> allCharacters.sortedWith { char1, char2 ->
                                            val ki1 = parseKiValue(char1.ki)
                                            val ki2 = parseKiValue(char2.ki)
                                            ki2.compareTo(ki1)
                                        }
                                    }
                                    adapter.setData(sortedCharacters)
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

        fetchPage(currentPage)
    }

    private fun parseKiValue(ki: String): Double {
        val multipliers = mapOf(
            "thousand" to 1_000.0,
            "million" to 1_000_000.0,
            "billion" to 1_000_000_000.0,
            "trillion" to 1_000_000_000_000.0,
            "quadrillion" to 1_000_000_000_000_000.0,
            "googol" to 1e100,
            "googolplex" to 1e10000
        )

        return try {
            val cleanedKi = ki.replace(",", "").replace(".", "")
            val regex = Regex("([0-9]+\\.?[0-9]*)(\\s*[A-Za-z]*)")
            val matchResult = regex.matchEntire(cleanedKi)

            if (matchResult != null) {
                val (baseStr, suffix) = matchResult.destructured
                val baseValue = baseStr.toDoubleOrNull() ?: return 0.0
                val multiplier = multipliers[suffix.trim().lowercase()] ?: 1.0
                baseValue * multiplier
            } else {
                0.0
            }
        } catch (e: NumberFormatException) {
            0.0
        }
    }
}
