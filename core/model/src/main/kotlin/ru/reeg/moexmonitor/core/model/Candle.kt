package ru.reeg.moexmonitor.core.model

/** Одна свеча из истории цен. [begin] — начало интервала в формате `yyyy-MM-dd HH:mm:ss`. */
data class Candle(
    val secId: String,
    val begin: String,
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val volume: Double,
)

/** Периоды, для которых грузим историю. [issInterval] — параметр `interval` в ISS API. */
enum class CandleRange(val days: Int, val issInterval: Int) {
    WEEK(7, 60),
    MONTH(30, 24),
    YEAR(365, 24),
}
