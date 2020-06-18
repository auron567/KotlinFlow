package com.example.kotlinflow.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.example.kotlinflow.app.CacheOnSuccess
import com.example.kotlinflow.app.ComparablePair
import com.example.kotlinflow.data.database.EpisodeDao
import com.example.kotlinflow.data.model.Episode
import com.example.kotlinflow.data.model.Trilogy
import com.example.kotlinflow.data.network.EpisodeRemoteDataSource
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
class EpisodeRepository(
    private val episodeDao: EpisodeDao,
    private val remoteDataSource: EpisodeRemoteDataSource
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
     * Fetch a list of [Episode]s from the database that matches a given [Trilogy] and apply a
     * custom sort order to the list.
     *
     * Returns a LiveData-wrapped List of Episodes.
     */
    fun getEpisodesWithTrilogy(trilogy: Trilogy): LiveData<List<Episode>> = liveData {
        // Observe episodes from the database
        val episodesTrilogyLiveData = episodeDao.getEpisodesWithTrilogyNumber(trilogy.number)

        // Fetch our custom sort from the network in a main-safe suspending call (cached)
        val customSortOrder = episodesListSortOrderCache.getOrAwait()

        // Map the LiveData, applying the sort criteria
        emitSource(episodesTrilogyLiveData.map { episodeList ->
            episodeList.applySort(customSortOrder)
        })
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
}