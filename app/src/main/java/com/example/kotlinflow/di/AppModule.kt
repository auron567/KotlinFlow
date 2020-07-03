@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.example.kotlinflow.di

import com.example.kotlinflow.data.database.EpisodeDao
import com.example.kotlinflow.data.network.EpisodeRemoteDataSource
import com.example.kotlinflow.repository.EpisodeRepository
import com.example.kotlinflow.viewmodel.EpisodeListViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // EpisodeRepository instance
    single { provideEpisodeRepository(get(), get()) }
    // EpisodeListViewModel instance
    viewModel { provideEpisodeListViewModel(get()) }
}

private fun provideEpisodeRepository(
    episodeDao: EpisodeDao,
    remoteDataSource: EpisodeRemoteDataSource
) : EpisodeRepository {
    return EpisodeRepository(episodeDao, remoteDataSource)
}

private fun provideEpisodeListViewModel(repository: EpisodeRepository): EpisodeListViewModel {
    return EpisodeListViewModel(repository)
}