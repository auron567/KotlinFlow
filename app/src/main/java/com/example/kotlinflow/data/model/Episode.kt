package com.example.kotlinflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episodes")
data class Episode(
    @PrimaryKey val episodeId: String,
    val name: String,
    val director: String,
    val number: Int,
    val posterPath: String,
    val trilogyNumber: Int
)

inline class Trilogy(val number: Int)

val noTrilogy = Trilogy(-1)