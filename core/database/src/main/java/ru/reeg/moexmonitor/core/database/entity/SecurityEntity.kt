package ru.reeg.moexmonitor.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.reeg.moexmonitor.core.model.MarketSegment

/**
 * Денормализованная таблица: статические поля инструмента и котировка лежат вместе.
 * Так проще читать и сортировать одним запросом.
 *
 * TODO (упражнение): вынести котировки в отдельную таблицу quotes и связать
 *  через @Relation / JOIN — заодно потренируешь миграции Room.
 */
@Entity(
    tableName = "securities",
    indices = [Index("segment"), Index("changePercent")],
)
data class SecurityEntity(
    @PrimaryKey
    @ColumnInfo(name = "secId")
    val secId: String,
    val shortName: String,
    val segment: MarketSegment,
    val board: String,
    val lotSize: Int,
    val prevPrice: Double?,
    val last: Double?,
    val changePercent: Double?,
    val openPrice: Double?,
    val highPrice: Double?,
    val lowPrice: Double?,
    val valueToday: Double?,
    val updatedAtMillis: Long,
)
