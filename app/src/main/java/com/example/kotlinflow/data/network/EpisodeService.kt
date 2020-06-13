package com.example.kotlinflow.data.network

import com.example.kotlinflow.data.model.Episode
import retrofit2.http.GET

interface EpisodeService {

    @GET("auron567/KotlinFlow/master/app/src/main/assets/episodes.json")
    suspend fun getAllEpisodes(): List<Episode>

    @GET("auron567/KotlinFlow/master/app/src/main/assets/custom_episode_sort_order.json")
    suspend fun getCustomEpisodeSortOrder(): List<Episode>
}