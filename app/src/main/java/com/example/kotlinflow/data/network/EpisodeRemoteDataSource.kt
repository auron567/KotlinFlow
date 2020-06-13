package com.example.kotlinflow.data.network

import com.example.kotlinflow.data.model.Episode
import com.example.kotlinflow.data.model.Trilogy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EpisodeRemoteDataSource(private val service: EpisodeService) {

    suspend fun allEpisodes(): List<Episode> = withContext(Dispatchers.IO) {
        val result = service.getAllEpisodes()
        result.shuffled()
    }

    suspend fun episodesByTrilogy(trilogy: Trilogy): List<Episode> = withContext(Dispatchers.IO) {
        val result = service.getAllEpisodes()
        result.filter { it.trilogyNumber == trilogy.number }.shuffled()
    }

    suspend fun customEpisodeSortOrder(): List<String> = withContext(Dispatchers.IO) {
        val result = service.getCustomEpisodeSortOrder()
        result.map { it.episodeId }
    }
}