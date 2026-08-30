package ru.reeg.moexmonitor.core.network.model

/** «Сырой» инструмент из сети: ещё не доменная модель, но уже не JSON. */
data class NetworkSecurity(
    val secId: String,
    val shortName: String,
    /** Тип бумаги из ISS: 1/2 — акции, J/A/B/9 — паи фондов. */
    val secType: String?,
    val lotSize: Int?,
    val prevPrice: Double?,
    val last: Double?,
    val changePercent: Double?,
    val open: Double?,
    val high: Double?,
    val low: Double?,
    val valueToday: Double?,
)

data class NetworkCandle(
    val begin: String,
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val volume: Double,
)

data class NetworkNews(
    val id: Long,
    val title: String,
    val publishedAt: String,
    val tag: String?,
    val body: String?,
)
