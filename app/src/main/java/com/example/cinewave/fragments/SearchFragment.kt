package com.example.cinewave.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cinewave.BuildConfig
import com.example.cinewave.R
import com.example.cinewave.activities.MovieDetailsActivity
import com.example.cinewave.adapter.MovieListAdapter
import com.example.cinewave.models.Result
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URLEncoder
import java.net.URL

class SearchFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchAdapter: MovieListAdapter

    private val apiKey = BuildConfig.API_KEY.toString()
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchView = view.findViewById(R.id.searchView)
        searchRecyclerView = view.findViewById(R.id.searchRecyclerView)

        // Set SearchView text color to white
        val searchText = searchView.findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
        searchText.setTextColor(Color.WHITE)
        searchText.setHintTextColor(Color.LTGRAY)

        searchAdapter = MovieListAdapter { movie -> openMovieDetails(movie) }

        // GridLayoutManager with 2 columns
        searchRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        searchRecyclerView.adapter = searchAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.length > 2) {
                        searchMovies(it)
                    } else {
                        searchAdapter.setMovies(emptyList())
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(300) // debounce
                    newText?.let {
                        if (it.length > 2) {
                            searchMovies(it)
                        } else {
                            searchAdapter.setMovies(emptyList())
                        }
                    }
                }
                return true
            }
        })

        return view
    }

    private fun openMovieDetails(movie: Result) {
        val intent = Intent(requireContext(), MovieDetailsActivity::class.java)
        intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE, movie)
        startActivity(intent)
    }

    private fun searchMovies(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                val url = "https://api.themoviedb.org/3/search/movie?api_key=$apiKey&query=$encodedQuery"
                val response = URL(url).readText()
                val resultsArray = JSONObject(response).getJSONArray("results")

                val movieList = mutableListOf<Result>()

                for (i in 0 until resultsArray.length()) {
                    val obj = resultsArray.getJSONObject(i)
                    val genreIds = mutableListOf<Int>()
                    val genreArray = obj.optJSONArray("genre_ids")
                    if (genreArray != null) {
                        for (j in 0 until genreArray.length()) {
                            genreIds.add(genreArray.getInt(j))
                        }
                    }

                    val movie = Result(
                        adult = obj.optBoolean("adult", false),
                        backdrop_path = obj.optString("backdrop_path", null),
                        genre_ids = genreIds,
                        id = obj.optInt("id", -1),
                        original_language = obj.optString("original_language", "en"),
                        original_title = obj.optString("original_title", ""),
                        overview = obj.optString("overview", ""),
                        popularity = obj.optDouble("popularity", 0.0),
                        poster_path = obj.optString("poster_path", null),
                        release_date = obj.optString("release_date", null),
                        title = obj.optString("title", ""),
                        video = obj.optBoolean("video", false),
                        vote_average = obj.optDouble("vote_average", 0.0),
                        vote_count = obj.optInt("vote_count", 0)
                    )
                    movieList.add(movie)
                }

                withContext(Dispatchers.Main) {
                    searchAdapter.setMovies(movieList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
