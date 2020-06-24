package com.example.kotlinflow.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kotlinflow.data.model.Episode
import kotlinx.coroutines.flow.Flow

/**
 * The Data Access Object for the [Episode] class.
 */
@Dao
interface EpisodeDao {

    @Query("SELECT * FROM episodes ORDER BY number")
    fun getEpisodes(): LiveData<List<Episode>>

    @Query("SELECT * FROM episodes ORDER BY number")
    fun getEpisodesFlow(): Flow<List<Episode>>

    @Query("SELECT * FROM episodes WHERE trilogyNumber = :trilogyNumber ORDER BY number")
    fun getEpisodesWithTrilogyNumber(trilogyNumber: Int): LiveData<List<Episode>>

    @Query("SELECT * FROM episodes WHERE trilogyNumber = :trilogyNumber ORDER BY number")
    fun getEpisodesWithTrilogyNumberFlow(trilogyNumber: Int): Flow<List<Episode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(episodes: List<Episode>)
}