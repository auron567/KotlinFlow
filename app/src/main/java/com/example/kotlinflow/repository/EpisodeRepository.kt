package com.example.kotlinflow.repository

import androidx.lifecycle.LiveData
import com.example.kotlinflow.data.database.EpisodeDao
import com.example.kotlinflow.data.model.Episode
import com.example.kotlinflow.data.model.Trilogy
import com.example.kotlinflow.data.network.EpisodeRemoteDataSource

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
     * Fetch a list of [Episode]s from the database.
     *
     * Returns a LiveData-wrapped List of Episodes.
     */
    val episodes: LiveData<List<Episode>> = episodeDao.getEpisodes()

    /**
     * Fetch a list of [Episode]s from the database that matches a given [Trilogy].
     *
     * Returns a LiveData-wrapped List of Episodes.
     */
    fun getEpisodesWithTrilogy(trilogy: Trilogy): LiveData<List<Episode>> {
        return episodeDao.getEpisodesWithTrilogyNumber(trilogy.number)
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
}