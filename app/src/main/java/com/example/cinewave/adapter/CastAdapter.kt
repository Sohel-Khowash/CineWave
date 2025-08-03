package com.example.cinewave.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinewave.R
import com.example.cinewave.models.CastMember

class CastAdapter(private val castList: List<CastMember>) :
    RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    inner class CastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val castImage: ImageView = itemView.findViewById(R.id.castImage)
        val castName: TextView = itemView.findViewById(R.id.castName)
        val castCharacter: TextView = itemView.findViewById(R.id.castCharacter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cast_member, parent, false)
        return CastViewHolder(view)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        val cast = castList[position]
        holder.castName.text = cast.name
        holder.castCharacter.text = "as ${cast.character}"

        Glide.with(holder.itemView.context)
            .load("https://image.tmdb.org/t/p/w200${cast.profilePath}")
            .placeholder(R.drawable.placeholder) // You can use any placeholder image
            .into(holder.castImage)
    }

    override fun getItemCount(): Int = castList.size
}
