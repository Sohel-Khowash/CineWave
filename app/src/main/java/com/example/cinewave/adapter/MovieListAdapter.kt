package com.example.cinewave.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinewave.R
import com.example.cinewave.models.Result

class MovieListAdapter(
    private val onItemClick: ((Result) -> Unit)? = null
) : RecyclerView.Adapter<MovieListAdapter.MovieListViewHolder>() {

    private val movies = mutableListOf<Result>()

    inner class MovieListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val movieTitle: TextView = itemView.findViewById(R.id.movietitle)
        private val movieImg: ImageView = itemView.findViewById(R.id.movieimg)

        fun bind(movie: Result) {
            movieTitle.text = movie.title ?: "No Title"
            movieTitle.setTextColor(itemView.context.getColor(android.R.color.white))

            val posterPath = movie.poster_path
            if (!posterPath.isNullOrEmpty()) {
                val movieUrl = "https://image.tmdb.org/t/p/w500$posterPath"
                Glide.with(itemView.context)
                    .load(movieUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(movieImg)
            } else {
                movieImg.setImageResource(R.drawable.ic_launcher_foreground)
            }

            itemView.setOnClickListener {
                onItemClick?.invoke(movie)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.movie_item, parent, false)

        // Calculate screen width and spacing
        val displayMetrics = parent.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val totalHorizontalSpacingDp = 32 + 16  // 16dp start + 16dp end padding on RecyclerView
        val spacingPx = (20 * displayMetrics.density).toInt() // 8dp space between items
        val totalSpacing = ((totalHorizontalSpacingDp + spacingPx) * displayMetrics.density).toInt()
        val itemWidth = (screenWidth - totalSpacing) / 2

        val params = RecyclerView.LayoutParams(itemWidth, RecyclerView.LayoutParams.WRAP_CONTENT)
        params.marginStart = spacingPx / 2
        params.marginEnd = spacingPx / 2
        view.layoutParams = params

        return MovieListViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieListViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size

    fun setMovies(newMovies: List<Result>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }

    fun addMovies(newMovies: List<Result>) {
        val start = movies.size
        movies.addAll(newMovies)
        notifyItemRangeInserted(start, newMovies.size)
    }
}
