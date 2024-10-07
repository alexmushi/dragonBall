package com.example.pokedexapp

import com.google.gson.annotations.SerializedName

data class CharacterResponse(
    @SerializedName("items") val items: List<DragonBallCharacter>, // List of characters
    @SerializedName("meta") val meta: MetaData                     // Pagination info
)

data class MetaData(
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("itemCount") val itemCount: Int,
    @SerializedName("itemsPerPage") val itemsPerPage: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("currentPage") val currentPage: Int
)
