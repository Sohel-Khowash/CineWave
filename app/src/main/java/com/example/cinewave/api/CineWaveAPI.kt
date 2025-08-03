package com.example.cinewave.api

import com.example.cinewave.models.MovieList
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CineWaveAPI {
    @GET("movie/popular")
    suspend fun getPopularMovies(@Query("api_key") apiKey:String ,
        @Query("page") page:Int):Response<MovieList>

    @GET("movie/top_rated")
    suspend fun getTopratedmovies(@Query("api_key") apiKey:String,
        @Query("page") page:Int):Response<MovieList>

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(@Query("api_key") apiKey: String ,
        @Query("page") page:Int):Response<MovieList>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int
    ):Response<MovieList>
}