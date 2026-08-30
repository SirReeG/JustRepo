package ru.reeg.moexmonitor.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val publishedAt: String,
    val tag: String?,
    /** HTML-текст. Догружается отдельным запросом при открытии новости. */
    val body: String?,
)
