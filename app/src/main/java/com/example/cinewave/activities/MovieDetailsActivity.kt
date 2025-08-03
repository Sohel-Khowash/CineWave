package com.example.cinewave.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.cinewave.R
import com.example.cinewave.models.Result
import com.example.cinewave.viewmodel.CineWaveViewModel
import kotlinx.coroutines.launch

class MovieDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MOVIE = "extra_movie"
    }

    private lateinit var ivPoster: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvOverview: TextView
    private lateinit var tvReleaseDate: TextView
    private lateinit var tvRating: TextView
    private lateinit var btnWatchlist: Button
    private lateinit var btnPlayTrailer: Button
    private lateinit var movieViewModel: CineWaveViewModel

    private var isInWatchlist = false
    private var currentMovie: Result? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_details)

        ivPoster = findViewById(R.id.ivPosterDetail)
        tvTitle = findViewById(R.id.tvTitleDetail)
        tvOverview = findViewById(R.id.tvOverviewDetail)
        tvReleaseDate = findViewById(R.id.tvReleaseDateDetail)
        tvRating = findViewById(R.id.tvRatingDetail)
        btnWatchlist = findViewById(R.id.watchlistButton)
        btnPlayTrailer = findViewById(R.id.btnPlayTrailer)

        movieViewModel = ViewModelProvider(this)[CineWaveViewModel::class.java]

        currentMovie = intent.getParcelableExtra(EXTRA_MOVIE)

        currentMovie?.let { movie ->
            // Populate UI
            tvTitle.text = movie.title ?: "No Title"
            tvOverview.text = movie.overview ?: "No Overview"
            tvReleaseDate.text = "Release Date: ${movie.release_date ?: "Unknown"}"
            tvRating.text = "Rating: ${movie.vote_average ?: "N/A"}"
            Glide.with(this)
                .load("https://image.tmdb.org/t/p/w500${movie.poster_path}")
                .into(ivPoster)

            // Watchlist logic
            lifecycleScope.launch {
                isInWatchlist = movieViewModel.isInWatchlist(movie.id ?: 0)
                updateButtonStates()
            }

            btnWatchlist.setOnClickListener {
                lifecycleScope.launch {
                    if (isInWatchlist) {
                        movieViewModel.removeFromWatchlist(movie.id ?: 0)
                        isInWatchlist = false
                    } else {
                        movieViewModel.addToWatchlist(movie)
                        isInWatchlist = true
                    }
                    updateButtonStates()
                }
            }

            // ▶ Play Trailer logic
            btnPlayTrailer.setOnClickListener {
                val intent = Intent(this, TrailerActivity::class.java)
                intent.putExtra("movie", movie)
                startActivity(intent)
            }
        }
    }

    private fun updateButtonStates() {
        btnWatchlist.text = if (isInWatchlist) "Remove from Watchlist" else "Add to Watchlist"
    }
}
