package ru.reeg.moexmonitor.core.network

import io.reactivex.Maybe
import io.reactivex.Single
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import ru.reeg.moexmonitor.core.common.rx.SchedulerProvider
import ru.reeg.moexmonitor.core.model.CandleRange
import ru.reeg.moexmonitor.core.model.MarketSegment
import ru.reeg.moexmonitor.core.network.model.NetworkCandle
import ru.reeg.moexmonitor.core.network.model.NetworkNews
import ru.reeg.moexmonitor.core.network.model.NetworkSecurity

/**
 * Единственная точка входа в сеть для остальных модулей.
 * Здесь ISS-«таблицы» превращаются в нормальные Kotlin-объекты,
 * поэтому :core:data ничего не знает про формат columns/data.
 *
 * subscribeOn(io) стоит здесь, а не у вызывающего: так невозможно
 * случайно дернуть сеть в главном потоке.
 */
@Singleton
class MoexRemoteDataSource @Inject constructor(
    private val api: MoexApi,
    private val schedulers: SchedulerProvider,
) {

    fun securities(segment: MarketSegment): Single<List<NetworkSecurity>> =
        api.securities(market = segment.market, board = segment.board)
            .map { response ->
                // marketdata и securities — два независимых блока, склеиваем их по SECID.
                val marketData = response.marketdata.rows()
                    .mapNotNull { row -> row.string("SECID")?.let { it to row } }
                    .toMap()

                response.securities.rows().mapNotNull { securityRow ->
                    val secId = securityRow.string("SECID") ?: return@mapNotNull null
                    val quoteRow = marketData[secId]
                    NetworkSecurity(
                        secId = secId,
                        shortName = securityRow.string("SHORTNAME") ?: secId,
                        secType = securityRow.string("SECTYPE"),
                        lotSize = securityRow.int("LOTSIZE"),
                        prevPrice = securityRow.double("PREVPRICE")
                            ?: quoteRow?.double("LASTVALUE"),
                        last = quoteRow?.double("LAST", "CURRENTVALUE"),
                        changePercent = quoteRow?.double("LASTTOPREVPRICE", "LASTCHANGEPRC"),
                        open = quoteRow?.double("OPEN", "OPENVALUE"),
                        high = quoteRow?.double("HIGH"),
                        low = quoteRow?.double("LOW"),
                        valueToday = quoteRow?.double("VALTODAY"),
                    )
                }
            }
            .subscribeOn(schedulers.io())

    fun candles(
        secId: String,
        segment: MarketSegment,
        range: CandleRange,
        today: LocalDate = LocalDate.now(),
    ): Single<List<NetworkCandle>> = api.candles(
        market = segment.market,
        board = segment.board,
        secId = secId,
        from = today.minusDays(range.days.toLong()).toString(),
        interval = range.issInterval,
    ).map { response ->
        response.candles.rows().mapNotNull { row ->
            NetworkCandle(
                begin = row.string("begin") ?: return@mapNotNull null,
                open = row.double("open") ?: return@mapNotNull null,
                close = row.double("close") ?: return@mapNotNull null,
                high = row.double("high") ?: return@mapNotNull null,
                low = row.double("low") ?: return@mapNotNull null,
                volume = row.double("volume") ?: 0.0,
            )
        }
    }.subscribeOn(schedulers.io())

    fun news(limit: Int = 50): Single<List<NetworkNews>> = api.news(limit = limit)
        .map { response ->
            response.sitenews.rows().mapNotNull { row ->
                NetworkNews(
                    id = row.long("id") ?: return@mapNotNull null,
                    title = row.string("title") ?: return@mapNotNull null,
                    publishedAt = row.string("published_at") ?: "",
                    tag = row.string("tag"),
                    body = null,
                )
            }
        }
        .subscribeOn(schedulers.io())

    /**
     * Maybe, а не Single: MOEX может вернуть блок content без текста,
     * и «ничего не пришло» — это нормальный исход, а не ошибка.
     */
    fun newsBody(id: Long): Maybe<NetworkNews> = api.newsBody(id)
        .flatMapMaybe { response ->
            val news = response.content.rows().firstNotNullOfOrNull { row ->
                NetworkNews(
                    id = row.long("id") ?: id,
                    title = row.string("title") ?: return@firstNotNullOfOrNull null,
                    publishedAt = row.string("published_at") ?: "",
                    tag = row.string("tag"),
                    body = row.string("body"),
                )
            }
            if (news == null) Maybe.empty() else Maybe.just(news)
        }
        .subscribeOn(schedulers.io())
}
