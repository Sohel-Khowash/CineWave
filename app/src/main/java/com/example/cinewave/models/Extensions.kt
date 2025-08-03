// utils/Extensions.kt or anywhere common
package com.example.cinewave.utils

import com.example.cinewave.models.Result
import com.example.cinewave.room.MovieEntity

fun MovieEntity.toResult(): Result {
    return Result(
        id = id,
        title = title,
        overview = overview ?: "",
        poster_path = posterPath,
        backdrop_path = backdropPath,
        release_date = releaseDate,
        vote_average = voteAverage ?: 0.0,
        vote_count = 0,
        genre_ids = listOf(),
        original_language = "en",
        original_title = title,
        popularity = 0.0,
        video = false,
        adult = false
    )
}
