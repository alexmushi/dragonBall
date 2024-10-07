package com.example.pokedexapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedexapp.databinding.ItemDragonballCharacterBinding
import com.squareup.picasso.Picasso

class DragonBallAdapter : RecyclerView.Adapter<DragonBallAdapter.DragonBallCharacterViewHolder>() {

    private val data = mutableListOf<DragonBallCharacter>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DragonBallCharacterViewHolder {
        val binding = ItemDragonballCharacterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DragonBallCharacterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DragonBallCharacterViewHolder, position: Int) {
        val character = data[position]
        holder.bind(character)
    }

    override fun getItemCount(): Int = data.size

    fun setData(newData: List<DragonBallCharacter>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    inner class DragonBallCharacterViewHolder(private val binding: ItemDragonballCharacterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(character: DragonBallCharacter) {
            binding.characterName.text = character.name
            binding.characterKi.text = "Ki: ${character.ki}"
            binding.characterMaxKi.text = "Max Ki: ${character.maxKi}"
            binding.characterRace.text = "Race: ${character.race}"
            binding.characterGender.text = "Gender: ${character.gender}"
            binding.characterDescription.text = character.description
            binding.characterAffiliation.text = "Affiliation: ${character.affiliation}"

            // Load character image using Picasso
            Picasso.get().load(character.imageUrl).into(binding.characterImage)
        }
    }
}
