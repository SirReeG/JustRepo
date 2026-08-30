package ru.reeg.moexmonitor.core.data.mapper

import ru.reeg.moexmonitor.core.database.entity.CandleEntity
import ru.reeg.moexmonitor.core.database.entity.NewsEntity
import ru.reeg.moexmonitor.core.database.entity.SecurityEntity
import ru.reeg.moexmonitor.core.model.Candle
import ru.reeg.moexmonitor.core.model.MarketSegment
import ru.reeg.moexmonitor.core.model.NewsItem
import ru.reeg.moexmonitor.core.model.Quote
import ru.reeg.moexmonitor.core.model.Security
import ru.reeg.moexmonitor.core.model.SecurityQuote
import ru.reeg.moexmonitor.core.network.model.NetworkCandle
import ru.reeg.moexmonitor.core.network.model.NetworkNews
import ru.reeg.moexmonitor.core.network.model.NetworkSecurity

/**
 * Мапперы «сеть -> база» и «база -> домен».
 * Держим их в одном месте и покрываем unit-тестами: это самое дешёвое место,
 * где ловятся ошибки вроде перепутанных local/high или потерянных null.
 */

fun NetworkSecurity.toEntity(
    requested: MarketSegment,
    nowMillis: Long,
): SecurityEntity {
    val segment = MarketSegment.classify(secType, requested)
    return SecurityEntity(
        secId = secId,
        shortName = shortName,
        segment = segment,
        board = segment.board,
        lotSize = lotSize ?: 1,
        prevPrice = prevPrice,
        last = last,
        changePercent = changePercent,
        openPrice = open,
        highPrice = high,
        lowPrice = low,
        valueToday = valueToday,
        updatedAtMillis = nowMillis,
    )
}

fun SecurityEntity.toDomain(isFavorite: Boolean = false): SecurityQuote = SecurityQuote(
    security = Security(
        secId = secId,
        shortName = shortName,
        segment = segment,
        board = board,
        lotSize = lotSize,
        prevPrice = prevPrice,
    ),
    quote = if (last == null && changePercent == null) {
        null
    } else {
        Quote(
            last = last,
            changePercent = changePercent,
            open = openPrice,
            high = highPrice,
            low = lowPrice,
            valueToday = valueToday,
            updatedAtMillis = updatedAtMillis,
        )
    },
    isFavorite = isFavorite,
)

fun NetworkCandle.toEntity(secId: String, range: String): CandleEntity = CandleEntity(
    secId = secId,
    range = range,
    begin = begin,
    openPrice = open,
    closePrice = close,
    highPrice = high,
    lowPrice = low,
    volume = volume,
)

fun CandleEntity.toDomain(): Candle = Candle(
    secId = secId,
    begin = begin,
    open = openPrice,
    close = closePrice,
    high = highPrice,
    low = lowPrice,
    volume = volume,
)

fun NetworkNews.toEntity(): NewsEntity = NewsEntity(
    id = id,
    title = title,
    publishedAt = publishedAt,
    tag = tag,
    body = body,
)

fun NewsEntity.toDomain(): NewsItem = NewsItem(
    id = id,
    title = title,
    publishedAt = publishedAt,
    tag = tag,
    body = body,
)
