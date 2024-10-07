package com.example.pokedexapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
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

        dialogView.findViewById<Button>(R.id.applyFiltersButton).setOnClickListener {
            val name = dialogView.findViewById<EditText>(R.id.filterName).text.toString().ifBlank { null }
            val race = dialogView.findViewById<EditText>(R.id.filterRace).text.toString().ifBlank { null }
            val gender = dialogView.findViewById<EditText>(R.id.filterGender).text.toString().ifBlank { null }
            val affiliation = dialogView.findViewById<EditText>(R.id.filterAffiliation).text.toString().ifBlank { null }

            // Capture sort order choice
            val sortOrder = when (dialogView.findViewById<RadioGroup>(R.id.sortOrderGroup).checkedRadioButtonId) {
                R.id.sortAscending -> "ascending"
                R.id.sortDescending -> "descending"
                else -> null
            }

            // Fetch characters with specified filters and sort order
            fetchCharacters(name, race, gender, affiliation, sortOrder)
            filterDialog.dismiss()
        }

        filterDialog.show()
    }

    private fun fetchCharacters(
        name: String? = null,
        race: String? = null,
        gender: String? = null,
        affiliation: String? = null,
        sortOrder: String? = null
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
                                    val sortedCharacters = allCharacters.sortedWith { char1, char2 ->
                                        val ki1 = parseKiValue(char1.ki)
                                        val ki2 = parseKiValue(char2.ki)

                                        if (sortOrder == "ascending") {
                                            ki1.compareTo(ki2)
                                        } else {
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
        // Define a map of common suffixes and their power of ten multipliers
        val multipliers = mapOf(
            "thousand" to 1_000.0,
            "million" to 1_000_000.0,
            "billion" to 1_000_000_000.0,
            "trillion" to 1_000_000_000_000.0,
            "quadrillion" to 1_000_000_000_000_000.0,
            "quintillion" to 1_000_000_000_000_000_000.0,
            "sextillion" to 1_000_000_000_000_000_000_000.0,
            "septillion" to 1_000_000_000_000_000_000_000_000.0,
            "octillion" to 1_000_000_000_000_000_000_000_000_000.0,
            "nonillion" to 1_000_000_000_000_000_000_000_000_000_000.0,
            "decillion" to 1e33,
            "undecillion" to 1e36,
            "duodecillion" to 1e39,
            "tredecillion" to 1e42,
            "quattuordecillion" to 1e45,
            "quindecillion" to 1e48,
            "sexdecillion" to 1e51,
            "septendecillion" to 1e54,
            "octodecillion" to 1e57,
            "novemdecillion" to 1e60,
            "vigintillion" to 1e63,
            "googol" to 1e100,
            "googolplex" to 1e10000
        )

        return try {
            // Remove commas and periods from the numeric part
            val cleanedKi = ki.replace(",", "").replace(".", "")

            // Extract the numeric base and any suffix present
            val regex = Regex("([0-9]+\\.?[0-9]*)(\\s*[A-Za-z]*)")
            val matchResult = regex.matchEntire(cleanedKi)

            if (matchResult != null) {
                val (baseStr, suffix) = matchResult.destructured

                // Parse the base value to double
                val baseValue = baseStr.toDoubleOrNull() ?: return 0.0

                // Retrieve the multiplier based on the lowercase suffix (default to 1 if no suffix or unknown suffix)
                val multiplier = multipliers[suffix.trim().lowercase()] ?: 1.0

                // Calculate the final value by applying the multiplier
                baseValue * multiplier
            } else {
                0.0
            }
        } catch (e: NumberFormatException) {
            0.0 // Default to 0 if parsing fails
        }
    }


}
