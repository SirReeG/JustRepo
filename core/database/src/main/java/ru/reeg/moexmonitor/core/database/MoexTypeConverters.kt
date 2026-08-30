package ru.reeg.moexmonitor.core.database

import androidx.room.TypeConverter
import ru.reeg.moexmonitor.core.model.MarketSegment

/** Room не умеет хранить enum сам — учим его через конвертеры. */
class MoexTypeConverters {

    @TypeConverter
    fun fromSegment(segment: MarketSegment): String = segment.name

    @TypeConverter
    fun toSegment(raw: String): MarketSegment = MarketSegment.fromName(raw)
}
