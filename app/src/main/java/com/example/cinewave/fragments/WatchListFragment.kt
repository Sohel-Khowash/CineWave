package com.example.cinewave.ui.watchList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cinewave.activities.MovieDetailsActivity
import com.example.cinewave.adapter.MovieListAdapter
import com.example.cinewave.databinding.FragmentWatchListBinding
import com.example.cinewave.models.Result
import com.example.cinewave.room.MovieEntity
import com.example.cinewave.utils.toResult
import com.example.cinewave.viewmodel.CineWaveViewModel

class WatchListFragment : Fragment() {

    private var _binding: FragmentWatchListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CineWaveViewModel by viewModels()

    private lateinit var adapter: MovieListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWatchListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MovieListAdapter { movie ->
            val intent = Intent(requireContext(), MovieDetailsActivity::class.java)
            intent.putExtra(MovieDetailsActivity.EXTRA_MOVIE, movie)
            startActivity(intent)
        }

        binding.WLRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.WLRecyclerView.adapter = adapter

        viewModel.watchlist.observe(viewLifecycleOwner) { movieEntities ->
            val results = movieEntities
                .filter { it.isInWatchlist }
                .map { it.toResult() }
            adapter.setMovies(results)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
