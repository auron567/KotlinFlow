package com.example.kotlinflow.view.main.episodelist

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kotlinflow.R
import com.example.kotlinflow.databinding.FragmentEpisodeListBinding
import com.example.kotlinflow.viewmodel.EpisodeListViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class EpisodeListFragment : Fragment() {

    private lateinit var binding: FragmentEpisodeListBinding

    private val viewModel: EpisodeListViewModel by viewModel()
    private val episodeAdapter = EpisodeAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEpisodeListBinding.inflate(inflater, container, false)
        context ?: binding.root

        setupObservers()
        setupRecyclerView()

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_episode_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filter_prequels -> {
                viewModel.setTrilogyNumber(1)
                true
            }
            R.id.filter_original -> {
                viewModel.setTrilogyNumber(2)
                true
            }
            R.id.filter_sequels -> {
                viewModel.setTrilogyNumber(3)
                true
            }
            R.id.filter_spinoffs -> {
                viewModel.setTrilogyNumber(4)
                true
            }
            R.id.filter_all -> {
                viewModel.clearTrilogyNumber()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupObservers() {
        viewModel.progressBar.observe(viewLifecycleOwner) { show ->
            Timber.d("progress bar: $show")
            binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        }

        viewModel.snackbar.observe(viewLifecycleOwner) { text ->
            Timber.d("snackbar: $text")
            text?.let {
                Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
                viewModel.onSnackbarShown()
            }
        }

        viewModel.episodes.observe(viewLifecycleOwner) { episodes ->
            Timber.d("episodes: $episodes")
            episodeAdapter.submitList(episodes)
        }
    }

    private fun setupRecyclerView() {
        binding.episodesRecyclerView.apply {
            layoutManager = GridLayoutManager(activity, 2)
            adapter = episodeAdapter
        }
    }
}