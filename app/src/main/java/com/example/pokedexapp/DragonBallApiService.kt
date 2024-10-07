package com.example.pokedexapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DragonBallApiService {

    @GET("characters")
    fun getCharacters(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("name") name: String? = null,
        @Query("race") race: String? = null,
        @Query("gender") gender: String? = null,
        @Query("affiliation") affiliation: String? = null
    ): Call<CharacterResponse>
}
