package ru.reeg.moexmonitor.core.database.entity

import androidx.room.Entity

@Entity(tableName = "candles", primaryKeys = ["secId", "range", "begin"])
data class CandleEntity(
    val secId: String,
    /** Имя CandleRange — позволяет держать в кэше несколько периодов одновременно. */
    val range: String,
    val begin: String,
    val openPrice: Double,
    val closePrice: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val volume: Double,
)
