package com.example.kotlinflow.repository

import androidx.annotation.AnyThread
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
 * This EpisodeRepository exposes two UI-observable database queries [episodesFlow] and
 * [getEpisodesWithTrilogyFlow].
 *
 * To update the episodes cache, call [tryUpdateRecentEpisodesCache] or
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
     * Returns a Flow-wrapped List of Episodes.
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
     * Returns a Flow-wrapped List of Episodes.
     *
     * It differs from [episodesFlow] in that it only calls main-safe suspend functions, so it does
     * not need to use [flowOn].
     */
    fun getEpisodesWithTrilogyFlow(trilogy: Trilogy): Flow<List<Episode>> {
        return episodeDao.getEpisodesWithTrilogyNumberFlow(trilogy.number)
            // When a new value is sent from the database, we can transform it using a suspending
            // map function
            .map { episodeList ->
                // This may trigger a network request if it's not yet cached, but since the network
                // call is main safe, we won't block the main thread
                val customSortOrder = episodesListSortOrderCache.getOrAwait()

                // This call is also main-safe due to using applyMainSafeSort
                episodeList.applyMainSafeSort(customSortOrder)
            }
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