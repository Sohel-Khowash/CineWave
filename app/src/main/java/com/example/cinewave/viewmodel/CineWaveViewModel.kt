package com.example.cinewave.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinewave.models.Result
import com.example.cinewave.repository.CineWaveRepository
import com.example.cinewave.room.MovieDatabase
import com.example.cinewave.room.MovieEntity
import kotlinx.coroutines.launch

class CineWaveViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CineWaveRepository()

    // Popular Movies
    private val _popularMovies = MutableLiveData<List<Result>>()
    val popularMovies: LiveData<List<Result>> = _popularMovies
    private var popularPage = 1
    private var popularTotalPages = Int.MAX_VALUE
    var isLoadingPopular = false

    fun fetchPopularMovies() {
        if (isLoadingPopular) return
        if (popularPage > popularTotalPages) return
        isLoadingPopular = true

        viewModelScope.launch {
            try {
                val response = repository.getPopularMovies(popularPage)
                if (response.isSuccessful) {
                    response.body()?.let { movieList ->
                        val currentList = _popularMovies.value ?: emptyList()
                        val existingIds = currentList.mapNotNull { it.id }.toSet()

                        val newMovies = movieList.results.filter { movie ->
                            movie.id != null && !existingIds.contains(movie.id)
                        }

                        if (newMovies.isNotEmpty()) {
                            _popularMovies.postValue(currentList + newMovies)
                        }

                        Log.d("CineWaveViewModel", "Popular - Page: $popularPage, New movies added: ${newMovies.size}")

                        popularPage++
                        popularTotalPages = movieList.total_pages
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingPopular = false
            }
        }
    }

    // Top Rated Movies
    private val _topRatedMovies = MutableLiveData<List<Result>>()
    val topRatedMovies: LiveData<List<Result>> = _topRatedMovies
    private var topRatedPage = 1
    private var topRatedTotalPages = Int.MAX_VALUE
    var isLoadingTopRated = false

    fun fetchTopRatedMovies() {
        if (isLoadingTopRated) return
        if (topRatedPage > topRatedTotalPages) return
        isLoadingTopRated = true

        viewModelScope.launch {
            try {
                val response = repository.getTopratedmovies(topRatedPage)
                if (response.isSuccessful) {
                    response.body()?.let { movieList ->
                        val currentList = _topRatedMovies.value ?: emptyList()
                        val existingIds = currentList.mapNotNull { it.id }.toSet()

                        val newMovies = movieList.results.filter { movie ->
                            movie.id != null && !existingIds.contains(movie.id)
                        }

                        if (newMovies.isNotEmpty()) {
                            _topRatedMovies.postValue(currentList + newMovies)
                        }

                        Log.d("CineWaveViewModel", "Top Rated - Page: $topRatedPage, New movies added: ${newMovies.size}")

                        topRatedPage++
                        topRatedTotalPages = movieList.total_pages
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingTopRated = false
            }
        }
    }

    // Upcoming Movies
    private val _upcomingMovies = MutableLiveData<List<Result>>()
    val upcomingMovies: LiveData<List<Result>> = _upcomingMovies
    private var upcomingPage = 1
    private var upcomingTotalPages = Int.MAX_VALUE
    var isLoadingUpcoming = false

    fun fetchUpcomingMovies() {
        if (isLoadingUpcoming) return
        if (upcomingPage > upcomingTotalPages) return
        isLoadingUpcoming = true

        viewModelScope.launch {
            try {
                val response = repository.getUpcomingMovies(upcomingPage)
                if (response.isSuccessful) {
                    response.body()?.let { movieList ->
                        val currentList = _upcomingMovies.value ?: emptyList()
                        val existingIds = currentList.mapNotNull { it.id }.toSet()

                        val newMovies = movieList.results.filter { movie ->
                            movie.id != null && !existingIds.contains(movie.id)
                        }

                        if (newMovies.isNotEmpty()) {
                            _upcomingMovies.postValue(currentList + newMovies)
                        }

                        Log.d("CineWaveViewModel", "Upcoming - Page: $upcomingPage, New movies added: ${newMovies.size}")

                        upcomingPage++
                        upcomingTotalPages = movieList.total_pages
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingUpcoming = false
            }
        }
    }

    // Search Movies
    private val _searchMovies = MutableLiveData<List<Result>>()
    val searchMovies: LiveData<List<Result>> = _searchMovies

    private var currentSearchQuery: String? = null
    private var currentSearchPage = 1
    private var searchTotalPages = Int.MAX_VALUE
    var isLoadingSearch = false

    fun searchMovies(query: String, page: Int = 1) {
        if (query != currentSearchQuery) {
            currentSearchQuery = query
            currentSearchPage = 1
            searchTotalPages = Int.MAX_VALUE
            _searchMovies.value = emptyList()
        }

        if (page > searchTotalPages) return
        if (isLoadingSearch) return

        isLoadingSearch = true

        viewModelScope.launch {
            try {
                val response = repository.searchMovies(query, page)
                if (response.isSuccessful) {
                    response.body()?.let { movieList ->
                        val currentList = if (page == 1) emptyList() else _searchMovies.value ?: emptyList()
                        val existingIds = currentList.mapNotNull { it.id }.toSet()

                        val newMovies = movieList.results.filter { movie ->
                            movie.id != null && !existingIds.contains(movie.id)
                        }

                        if (newMovies.isNotEmpty()) {
                            _searchMovies.postValue(currentList + newMovies)
                        }

                        Log.d("CineWaveViewModel", "Search - Page: $page, New movies added: ${newMovies.size}")

                        currentSearchPage = page + 1
                        searchTotalPages = movieList.total_pages
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingSearch = false
            }
        }
    }

    private val movieDao = MovieDatabase.getDatabase(application).movieDao()

    val watchlist: LiveData<List<MovieEntity>> = movieDao.getWatchlist()

    fun addToWatchlist(result: Result) = viewModelScope.launch {
        val entity = MovieEntity(
            id = result.id ?: 0,
            title = result.title ?: "Untitled",
            overview = result.overview,
            posterPath = result.poster_path,
            backdropPath = result.backdrop_path,
            releaseDate = result.release_date,
            voteAverage = result.vote_average,
            isInWatchlist = true,
            isWatched = false
        )
        movieDao.insertMovie(entity)
    }

    fun removeFromWatchlist(movieId: Int) = viewModelScope.launch {
        movieDao.getMovieById(movieId)?.let {
            val updated = it.copy(isInWatchlist = false)
            movieDao.insertMovie(updated)
        }
    }

    fun toggleWatched(movieId: Int) = viewModelScope.launch {
        movieDao.getMovieById(movieId)?.let {
            val updated = it.copy(isWatched = !it.isWatched)
            movieDao.insertMovie(updated)
        }
    }

    suspend fun isWatched(movieId: Int): Boolean {
        return movieDao.getMovieById(movieId)?.isWatched ?: false
    }

    suspend fun isInWatchlist(movieId: Int): Boolean {
        return movieDao.getMovieById(movieId)?.isInWatchlist ?: false
    }

    fun loadMoreSearchResults() {
        currentSearchQuery?.let { query ->
            searchMovies(query, currentSearchPage)
        }
    }
}
