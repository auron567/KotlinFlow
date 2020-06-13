package com.example.kotlinflow.di

import android.app.Application
import androidx.room.Room
import com.example.kotlinflow.data.database.EpisodeDao
import com.example.kotlinflow.data.database.EpisodeDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {

    // EpisodeDatabase instance
    single { provideEpisodeDatabase(androidApplication()) }
    // EpisodeDao instance
    single { provideEpisodeDao(get()) }
}

private fun provideEpisodeDatabase(application: Application): EpisodeDatabase {
    return Room.databaseBuilder(application, EpisodeDatabase::class.java, "episode_database")
        .build()
}

private fun provideEpisodeDao(database: EpisodeDatabase): EpisodeDao {
    return database.episodeDao()
}