package com.example.kotlinflow.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kotlinflow.data.model.Episode

@Database(entities = [Episode::class], version = 1, exportSchema = false)
abstract class EpisodeDatabase : RoomDatabase() {

    abstract fun episodeDao(): EpisodeDao
}