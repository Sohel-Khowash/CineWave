package com.example.cinewave.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CineWaveInstance {

    private const val BASE_URL = "https://api.themoviedb.org/3/"

    val api:CineWaveAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CineWaveAPI::class.java)
    }
}