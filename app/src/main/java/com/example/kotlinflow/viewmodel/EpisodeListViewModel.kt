package com.example.kotlinflow.viewmodel

import androidx.lifecycle.*
import com.example.kotlinflow.data.model.Episode
import com.example.kotlinflow.data.model.Trilogy
import com.example.kotlinflow.data.model.noTrilogy
import com.example.kotlinflow.repository.EpisodeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

/**
 * The [ViewModel] for fetching a list of [Episode]s.
 */
@ExperimentalCoroutinesApi
@FlowPreview
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
     * The current trilogy selection (flow version).
     */
    private val trilogyState = MutableStateFlow(noTrilogy)

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
    val episodesWithFlow: LiveData<List<Episode>> = trilogyState.flatMapLatest { trilogy ->
        if (trilogy == noTrilogy) {
            repository.episodesFlow
        } else {
            repository.getEpisodesWithTrilogyFlow(trilogy)
        }
    }.asLiveData()

    init {
        // When creating a new ViewModel, clear the trilogy and perform the related udpates
        clearTrilogyNumber()

        // Updating the trilogy will automatically kick off a network request
        loadDataFor(trilogyState) { trilogy ->
            if (trilogy == noTrilogy) {
                repository.tryUpdateRecentEpisodesCache()
            } else {
                repository.tryUpdateRecentEpisodesForTrilogyCache(trilogy)
            }
        }
    }

    /**
     * Clear the current filter.
     */
    fun clearTrilogyNumber() {
        trilogyState.value = noTrilogy
    }

    /**
     * Filter to this trilogy.
     */
    fun setTrilogyNumber(number: Int) {
        trilogyState.value = Trilogy(number)
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
    private fun <T> loadDataFor(source: StateFlow<T>, block: suspend (T) -> Unit) {
        source
            .mapLatest {
                _progressBar.value = true
                block(it)
            }
            .onEach { _progressBar.value = false }
            .onCompletion { _progressBar.value = false }
            .catch { throwable -> _snackbar.value = throwable.message }
            .launchIn(viewModelScope)
    }
}