package com.example.kotlinflow.repository

import androidx.annotation.AnyThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.example.kotlinflow.app.CacheOnSuccess
import com.example.kotlinflow.app.ComparablePair
import com.example.kotlinflow.data.database.EpisodeDao
import com.example.kotlinflow.data.model.Episode
import com.example.kotlinflow.data.model.Trilogy
import com.example.kotlinflow.data.network.EpisodeRemoteDataSource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber

/**
 * Repository module for handling data operations.
 *
 * This EpisodeRepository exposes two UI-observable database queries [episodes] and
 * [getEpisodesWithTrilogy].
 *
 * To update the plants cache, call [tryUpdateRecentEpisodesCache] or
 * [tryUpdateRecentEpisodesForTrilogyCache].
 */
@FlowPreview
class EpisodeRepository(
    private val episodeDao: EpisodeDao,
    private val remoteDataSource: EpisodeRemoteDataSource,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    /**
     * Cache for storing the custom sort order.
     */
    private var episodesListSortOrderCache =
        CacheOnSuccess(onErrorFallback = { listOf<String>() }) {
            Timber.d("fetch custom sort order")
            remoteDataSource.customEpisodeSortOrder()
        }

    /**
     * Create a flow that calls getOrAwait and emits the result as its first and only value.
     */
    private val customSortOrderFlow = episodesListSortOrderCache::getOrAwait.asFlow()

    /**
     * Fetch a list of [Episode]s from the database and apply a custom sort order to the list.
     *
     * Returns a LiveData-wrapped List of Episodes.
     */
    val episodes: LiveData<List<Episode>> = liveData {
        // Observe episodes from the database
        val episodesLiveData = episodeDao.getEpisodes()

        // Fetch our custom sort from the network in a main-safe suspending call (cached)
        val customSortOrder = episodesListSortOrderCache.getOrAwait()

        // Map the LiveData, applying the sort criteria
        emitSource(episodesLiveData.map { episodeList ->
            episodeList.applySort(customSortOrder)
        })
    }

    /**
     * This is a version of [episodes], but using [Flow].
     */
    val episodesFlow: Flow<List<Episode>>
        get() = episodeDao.getEpisodesFlow()
            // When the result of customSortOrderFlow is available, this will combine it with the
            // latest value from the flow above
            .combine(customSortOrderFlow) { episodes, sortOrder ->
                episodes.applySort(sortOrder)
            }
            // Switches the dispatcher the previous transforms run on
            .flowOn(defaultDispatcher)
            // Removes the buffer from flowOn and only shares the last value
            .conflate()

    /**
     * Fetch a list of [Episode]s from the database that matches a given [Trilogy] and apply a
     * custom sort order to the list.
     *
     * Returns a LiveData-wrapped List of Episodes.
     *
     * This is similar to [episodes], but uses main-safe transforms to avoid blocking the main
     * thread.
     */
    fun getEpisodesWithTrilogy(trilogy: Trilogy): LiveData<List<Episode>> =
        episodeDao.getEpisodesWithTrilogyNumber(trilogy.number)
            // "Switches" to a new LiveData every time a new value is received
            .switchMap { episodeList ->
                // Use the liveData builder to construct a new LiveData
                liveData {
                    val customSortOrder = episodesListSortOrderCache.getOrAwait()

                    // The sorted list will be the new value sent to getEpisodesWithTrilogyNumber
                    emit(episodeList.applyMainSafeSort(customSortOrder))
                }
            }

    /**
     * This is a version of [getEpisodesWithTrilogy], but using [Flow].
     */
    fun getEpisodesWithTrilogyFlow(trilogy: Trilogy): Flow<List<Episode>> {
        return episodeDao.getEpisodesWithTrilogyNumberFlow(trilogy.number)
    }

    /**
     * Update the episodes cache.
     *
     * This function may decide to avoid making a network requests on every call based on a
     * cache-invalidation policy.
     */
    suspend fun tryUpdateRecentEpisodesCache() {
        if (shouldUpdateEpisodesCache()) fetchRecentEpisodes()
    }

    /**
     * Update the episodes cache for a specific trilogy.
     *
     * This function may decide to avoid making a network requests on every call based on a
     * cache-invalidation policy.
     */
    suspend fun tryUpdateRecentEpisodesForTrilogyCache(trilogy: Trilogy) {
        if (shouldUpdateEpisodesCache()) fetchEpisodesForTrilogy(trilogy)
    }

    /**
     * Returns true if we should make a network request.
     */
    private fun shouldUpdateEpisodesCache(): Boolean {
        return true
    }

    /**
     * Fetch a new list of episodes from the network, and append them to [episodeDao].
     */
    private suspend fun fetchRecentEpisodes() {
        val episodes = remoteDataSource.allEpisodes()
        episodeDao.insertAll(episodes)
    }

    /**
     * Fetch a list of episodes for a trilogy from the network, and append them to [episodeDao].
     */
    private suspend fun fetchEpisodesForTrilogy(trilogy: Trilogy) {
        val episodes = remoteDataSource.episodesByTrilogy(trilogy)
        episodeDao.insertAll(episodes)
    }

    /**
     * A function that sorts the list of episodes in a given custom order.
     */
    private fun List<Episode>.applySort(customSortOrder: List<String>): List<Episode> {
        return sortedBy { episode ->
            val positionForItem = customSortOrder.indexOf(episode.episodeId).let { index ->
                if (index > -1) index else Int.MAX_VALUE
            }
            ComparablePair(positionForItem, episode.number)
        }
    }

    /**
     * The same sorting function as [applySort], but as a suspend function that can run on any
     * thread (main-safe).
     */
    @AnyThread
    private suspend fun List<Episode>.applyMainSafeSort(customSortOrder: List<String>): List<Episode> =
        withContext(defaultDispatcher) {
            this@applyMainSafeSort.applySort(customSortOrder)
        }
}