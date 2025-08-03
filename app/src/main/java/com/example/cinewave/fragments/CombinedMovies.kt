package com.example.cinewave.fragments

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.cinewave.BuildConfig
import com.example.cinewave.R
import com.example.cinewave.activities.MovieDetailsActivity
import com.example.cinewave.adapter.MovieListAdapter
import com.example.cinewave.models.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*



class CombinedMoviesFragment : Fragment() {

    private lateinit var rvPopular: RecyclerView
    private lateinit var rvTopRated: RecyclerView
    private lateinit var rvUpcoming: RecyclerView
    private lateinit var genreSpinner: Spinner
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var upcomingtv: TextView
    private lateinit var populartv: TextView
    private lateinit var topratedtv: TextView

    private lateinit var popularAdapter: MovieListAdapter
    private lateinit var topRatedAdapter: MovieListAdapter
    private lateinit var upcomingAdapter: MovieListAdapter

    private val apiKey = BuildConfig.API_KEY.toString()


    private val genreNameToIdMap = mutableMapOf<String, Int>()
    private var selectedGenreId: Int? = null
    private var loadedSections = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_combined_movies, container, false)


        rvPopular = view.findViewById(R.id.rvPopular)
        rvTopRated = view.findViewById(R.id.rvTopRated)
        rvUpcoming = view.findViewById(R.id.rvUpcoming)
        genreSpinner = view.findViewById(R.id.genreSpinner)
        lottieAnimationView = view.findViewById(R.id.lottieAnimationView)
        upcomingtv = view.findViewById(R.id.upcomingtv)
        populartv = view.findViewById(R.id.populartv)
        topratedtv = view.findViewById(R.id.topratedtv)

        rvPopular.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvTopRated.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvUpcoming.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        LinearSnapHelper().attachToRecyclerView(rvPopular)
        LinearSnapHelper().attachToRecyclerView(rvTopRated)
        LinearSnapHelper().attachToRecyclerView(rvUpcoming)

        popularAdapter = MovieListAdapter { movie -> openMovieDetails(movie) }
        topRatedAdapter = MovieListAdapter { movie -> openMovieDetails(movie) }
        upcomingAdapter = MovieListAdapter { movie -> openMovieDetails(movie) }

        rvPopular.adapter = popularAdapter
        rvTopRated.adapter = topRatedAdapter
        rvUpcoming.adapter = upcomingAdapter

        hideMovieViews()
        fetchGenres()
        fetchAllMovies()

        return view
    }

    private fun openMovieDetails(movie: Result) {
        val intent = Intent(requireContext(), MovieDetailsActivity::class.java)
        intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE, movie)
        startActivity(intent)
    }

    private fun fetchGenres() {
        val url = "https://api.themoviedb.org/3/genre/movie/list?api_key=$apiKey"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = URL(url).readText()
                val genres = JSONObject(response).getJSONArray("genres")
                val genreNames = mutableListOf("All Genres")

                for (i in 0 until genres.length()) {
                    val genre = genres.getJSONObject(i)
                    val id = genre.getInt("id")
                    val name = genre.getString("name")
                    genreNameToIdMap[name] = id
                    genreNames.add(name)
                }

                withContext(Dispatchers.Main) {
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genreNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    genreSpinner.adapter = adapter

                    genreSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            val selectedGenre = genreNames[position]
                            selectedGenreId = if (selectedGenre == "All Genres") null else genreNameToIdMap[selectedGenre]
                            hideMovieViews()
                            fetchAllMovies()
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchAllMovies() {
        if (!isNetworkAvailable()) {
            showAnimation(R.raw.no_internet)
            return
        }

        loadedSections = 0
        showAnimation(R.raw.loading)

        CoroutineScope(Dispatchers.IO).launch {
            fetchPopularMovies()
            fetchTopRatedMovies()
            fetchUpcomingMovies()
        }
    }

    private fun fetchPopularMovies() {
        val url = buildDiscoverUrl(sort = "popularity.desc")
        fetchAndSetMovies(url, popularAdapter)
    }

    private fun fetchTopRatedMovies() {
        val url = buildDiscoverUrl(sort = "vote_average.desc", voteCount = 100)
        fetchAndSetMovies(url, topRatedAdapter)
    }

    private fun fetchUpcomingMovies() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val url = buildDiscoverUrl(sort = "release_date.asc", releaseDateGte = today)
        fetchAndSetMovies(url, upcomingAdapter)
    }

    private fun buildDiscoverUrl(
        sort: String,
        voteCount: Int? = null,
        releaseDateGte: String? = null
    ): String {
        val base = StringBuilder("https://api.themoviedb.org/3/discover/movie?api_key=$apiKey")
        base.append("&sort_by=${URLEncoder.encode(sort, "UTF-8")}")
        base.append("&include_adult=false") // ✅ Block adult movies

        selectedGenreId?.let {
            base.append("&with_genres=$it")
        }

        voteCount?.let {
            base.append("&vote_count.gte=$it")
        }

        releaseDateGte?.let {
            base.append("&primary_release_date.gte=$it")
        }

        return base.toString()
    }


    private fun fetchAndSetMovies(url: String, adapter: MovieListAdapter) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
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

                    // ✅ Only add non-adult movies
                    if (movie.adult == false) {
                        movieList.add(movie)
                    }
                }

                withContext(Dispatchers.Main) {
                    adapter.setMovies(movieList)
                    checkAndDisplayContent()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun checkAndDisplayContent() {
        loadedSections++
        if (loadedSections == 3) {
            hideAnimation()
            showMovieViews()
        }
    }

    private fun showAnimation(animationRes: Int) {
        lottieAnimationView.setAnimation(animationRes)
        lottieAnimationView.visibility = View.VISIBLE
        lottieAnimationView.playAnimation()
        hideMovieViews()
    }

    private fun hideAnimation() {
        lottieAnimationView.cancelAnimation()
        lottieAnimationView.visibility = View.GONE
    }

    private fun hideMovieViews() {
        rvPopular.visibility = View.GONE
        rvTopRated.visibility = View.GONE
        rvUpcoming.visibility = View.GONE

        populartv.visibility = View.GONE
        topratedtv.visibility = View.GONE
        upcomingtv.visibility = View.GONE
    }

    private fun showMovieViews() {
        rvPopular.visibility = View.VISIBLE
        rvTopRated.visibility = View.VISIBLE
        rvUpcoming.visibility = View.VISIBLE

        populartv.visibility = View.VISIBLE
        topratedtv.visibility = View.VISIBLE
        upcomingtv.visibility = View.VISIBLE
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
