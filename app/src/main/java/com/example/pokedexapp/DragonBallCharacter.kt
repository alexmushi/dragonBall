package com.example.pokedexapp

import com.google.gson.annotations.SerializedName

data class DragonBallCharacter(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("ki") val ki: String,
    @SerializedName("maxKi") val maxKi: String,
    @SerializedName("race") val race: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("description") val description: String,
    @SerializedName("image") val imageUrl: String,
    @SerializedName("affiliation") val affiliation: String
)
