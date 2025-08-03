package com.example.cinewave.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.cinewave.BuildConfig
import com.example.cinewave.R
import com.example.cinewave.adapter.CastAdapter
import com.example.cinewave.adapter.MovieListAdapter
import com.example.cinewave.models.CastMember
import com.example.cinewave.models.Result
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class TrailerActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var titleView: TextView
    private lateinit var ratingView: TextView
    private lateinit var overviewView: TextView
    private lateinit var castRecyclerView: RecyclerView
    private lateinit var similarMoviesRecyclerView: RecyclerView
    private lateinit var lottieNoData: LottieAnimationView

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private lateinit var fullScreenContainer: ViewGroup

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trailer_activty)

        webView = findViewById(R.id.trailerWebView)
        titleView = findViewById(R.id.trailerTitle)
        ratingView = findViewById(R.id.trailerRating)
        overviewView = findViewById(R.id.trailerOverview)
        castRecyclerView = findViewById(R.id.castRecyclerView)
        similarMoviesRecyclerView = findViewById(R.id.similarMoviesRecyclerView)
        lottieNoData = findViewById(R.id.lottieNoData)

        val movie = intent.getParcelableExtra<Result>("movie")
        val movieId = movie?.id ?: -1

        titleView.text = movie?.title ?: "No Title"
        overviewView.text = movie?.overview ?: "No Overview"
        ratingView.text = "Rating: ${movie?.vote_average ?: "N/A"}"

        setupWebView()
        fetchTrailerUrl(movieId)
        setupCastRecycler()
        fetchCast(movieId)
        setupSimilarMoviesRecycler()
        fetchSimilarMovies(movieId)
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            mediaPlaybackRequiresUserGesture = false
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customViewCallback = callback
                fullScreenContainer = window.decorView as ViewGroup
                fullScreenContainer.addView(
                    customView,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webView.visibility = View.GONE
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }

            override fun onHideCustomView() {
                fullScreenContainer.removeView(customView)
                customView = null
                webView.visibility = View.VISIBLE
                customViewCallback?.onCustomViewHidden()
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (url.contains("youtube.com")) {
                    val intent = Intent(Intent.ACTION_VIEW, request?.url)
                    startActivity(intent)
                    return true
                }
                return false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBackPressed() {
        if (customView != null) {
            webView.webChromeClient?.onHideCustomView()
        } else {
            super.onBackPressed()
        }
    }

    private fun fetchTrailerUrl(movieId: Int) {
        val apiKey = BuildConfig.API_KEY.toString()
        val url = "https://api.themoviedb.org/3/movie/$movieId/videos?api_key=$apiKey"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = URL(url).readText()
                val json = JSONObject(response)
                val results = json.getJSONArray("results")

                var trailerKey: String? = null
                for (i in 0 until results.length()) {
                    val video = results.getJSONObject(i)
                    if (video.getString("site") == "YouTube" && video.getString("type") == "Trailer") {
                        trailerKey = video.getString("key")
                        break
                    }
                }

                withContext(Dispatchers.Main) {
                    if (trailerKey != null) {
                        lottieNoData.visibility = View.GONE
                        webView.visibility = View.VISIBLE

                        val html = """
                            <html>
                                <body style="margin:0;background-color:black;">
                                    <iframe 
                                        width="100%" 
                                        height="100%" 
                                        src="https://www.youtube.com/embed/$trailerKey?autoplay=1&modestbranding=1&rel=0&showinfo=0&fs=1" 
                                        frameborder="0" 
                                        allow="autoplay; encrypted-media; fullscreen"
                                        allowfullscreen>
                                    </iframe>
                                </body>
                            </html>
                        """.trimIndent()

                        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                    } else {
                        webView.visibility = View.GONE
                        lottieNoData.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    webView.visibility = View.GONE
                    lottieNoData.visibility = View.VISIBLE
                }
                e.printStackTrace()
            }
        }
    }

    private fun setupCastRecycler() {
        castRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun fetchCast(movieId: Int) {
        val apiKey = BuildConfig.API_KEY.toString()
        val url = "https://api.themoviedb.org/3/movie/$movieId/credits?api_key=$apiKey"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = URL(url).readText()
                val json = JSONObject(response)
                val castArray = json.getJSONArray("cast")

                val castList = mutableListOf<CastMember>()
                for (i in 0 until castArray.length().coerceAtMost(15)) {
                    val obj = castArray.getJSONObject(i)
                    castList.add(
                        CastMember(
                            name = obj.getString("name"),
                            character = obj.getString("character"),
                            profilePath = obj.optString("profile_path", null),
                            id = obj.getInt("id")
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    castRecyclerView.adapter = CastAdapter(castList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupSimilarMoviesRecycler() {
        similarMoviesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun fetchSimilarMovies(movieId: Int) {
        val apiKey = BuildConfig.API_KEY.toString()
        val url = "https://api.themoviedb.org/3/movie/$movieId/similar?api_key=$apiKey"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = URL(url).readText()
                val json = JSONObject(response)
                val results = json.getJSONArray("results")

                val similarList = mutableListOf<Result>()
                for (i in 0 until results.length()) {
                    val obj = results.getJSONObject(i)
                    similarList.add(
                        Result(
                            adult = obj.optBoolean("adult", false),
                            backdrop_path = obj.optString("backdrop_path", null),
                            genre_ids = null, // or parse if needed
                            id = obj.getInt("id"),
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
                    )

                }

                withContext(Dispatchers.Main) {
                    val adapter = MovieListAdapter { movie ->
                        val intent = Intent(this@TrailerActivity, MovieDetailsActivity::class.java)
                        intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE, movie)
                        startActivity(intent)
                    }
                    adapter.setMovies(similarList)
                    similarMoviesRecyclerView.adapter = adapter

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
