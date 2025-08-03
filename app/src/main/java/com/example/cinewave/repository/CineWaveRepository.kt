package com.example.cinewave.repository

import com.example.cinewave.BuildConfig
import com.example.cinewave.api.CineWaveInstance

class CineWaveRepository {


    private val apiKey = BuildConfig.API_KEY.toString()

    //PopularMovies
    suspend fun getPopularMovies(page: Int) =
        CineWaveInstance.api.getPopularMovies(apiKey, page)

    //TopRatedMovies
    suspend fun getTopratedmovies(page: Int) =
        CineWaveInstance.api.getTopratedmovies(apiKey, page)

    //UpcomingMovies
    suspend fun getUpcomingMovies(page: Int) =
        CineWaveInstance.api.getUpcomingMovies(apiKey, page)

    //SearchMovies
    suspend fun searchMovies(query: String, page: Int = 1) =
        CineWaveInstance.api.searchMovies(apiKey, query, page)
}