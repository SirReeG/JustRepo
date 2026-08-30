package ru.reeg.moexmonitor.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val secId: String,
    val addedAtMillis: Long,
)
