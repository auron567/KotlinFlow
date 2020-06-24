package com.example.kotlinflow.viewmodel

import androidx.lifecycle.*
import com.example.kotlinflow.data.model.Episode
import com.example.kotlinflow.data.model.Trilogy
import com.example.kotlinflow.data.model.noTrilogy
import com.example.kotlinflow.repository.EpisodeRepository
import kotlinx.coroutines.launch

/**
 * The [ViewModel] for fetching a list of [Episode]s.
 */
class EpisodeListViewModel(private val repository: EpisodeRepository) : ViewModel() {

    /**
     * Show a loading progress bar if true.
     */
    private val _progressBar = MutableLiveData<Boolean>(false)
    val progressBar: LiveData<Boolean> get() = _progressBar

    /**
     * Request a snackbar to display a string.
     */
    private val _snackbar = MutableLiveData<String?>()
    val snackbar: LiveData<String?> get() = _snackbar

    /**
     * The current trilogy selection.
     */
    private val trilogy = MutableLiveData<Trilogy>(noTrilogy)

    /**
     * A list of episodes that updates based on the current filter.
     */
    val episodes: LiveData<List<Episode>> = trilogy.switchMap { trilogy ->
        if (trilogy == noTrilogy) {
            repository.episodes
        } else {
            repository.getEpisodesWithTrilogy(trilogy)
        }
    }

    /**
     * A list of episodes that updates based on the current filter (flow version).
     */
    val episodesWithFlow: LiveData<List<Episode>> = repository.episodesFlow.asLiveData()

    init {
        // When creating a new ViewModel, clear the trilogy and perform the related udpates
        clearTrilogyNumber()
    }

    /**
     * Clear the current filter.
     */
    fun clearTrilogyNumber() {
        trilogy.value = noTrilogy
        launchLoadData { repository.tryUpdateRecentEpisodesCache() }
    }

    /**
     * Filter to this trilogy.
     */
    fun setTrilogyNumber(number: Int) {
        trilogy.value = Trilogy(number)
        launchLoadData { repository.tryUpdateRecentEpisodesCache() }
    }

    /**
     * Called immediately after the UI shows the snackbar.
     */
    fun onSnackbarShown() {
        _snackbar.value = null
    }

    /**
     * Helper function to call a load data function with a loading spinner; errors will show a
     * snackbar.
     *
     * By marking [block] as suspend this creates a lambda which can call suspend functions.
     */
    private fun launchLoadData(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                _progressBar.value = true
                block()
            } catch (error: Throwable) {
                _snackbar.value = error.message
            } finally {
                _progressBar.value = false
            }
        }
    }
}