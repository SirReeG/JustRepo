package ru.reeg.moexmonitor.core.network

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.reeg.moexmonitor.core.network.dto.CandlesResponse
import ru.reeg.moexmonitor.core.network.dto.NewsContentResponse
import ru.reeg.moexmonitor.core.network.dto.SecuritiesResponse
import ru.reeg.moexmonitor.core.network.dto.SiteNewsResponse

/**
 * Retrofit-описание публичного MOEX ISS API. Ключи и авторизация не нужны.
 * Все методы возвращают Single: RxJava2CallAdapterFactory подключён в NetworkModule.
 * Документация: https://iss.moex.com/iss/reference/
 */
interface MoexApi {

    /**
     * Список инструментов режима торгов вместе с текущими котировками.
     * Лишние колонки MOEX просто игнорирует, поэтому запрашиваем объединение
     * колонок для акций и для индексов одним запросом.
     */
    @GET("engines/stock/markets/{market}/boards/{board}/securities.json")
    fun securities(
        @Path("market") market: String,
        @Path("board") board: String,
        @Query("iss.meta") meta: String = "off",
        @Query("iss.only") only: String = "securities,marketdata",
        @Query("securities.columns") securitiesColumns: String = SECURITIES_COLUMNS,
        @Query("marketdata.columns") marketDataColumns: String = MARKET_DATA_COLUMNS,
    ): Single<SecuritiesResponse>

    /** История цен (свечи). [interval]: 1, 10, 60 — минуты; 24 — день; 7 — неделя; 31 — месяц. */
    @GET("engines/stock/markets/{market}/boards/{board}/securities/{secId}/candles.json")
    fun candles(
        @Path("market") market: String,
        @Path("board") board: String,
        @Path("secId") secId: String,
        @Query("from") from: String,
        @Query("interval") interval: Int,
        @Query("iss.meta") meta: String = "off",
    ): Single<CandlesResponse>

    /** Лента новостей сайта MOEX. */
    @GET("sitenews.json")
    fun news(
        @Query("limit") limit: Int = 50,
        @Query("iss.meta") meta: String = "off",
    ): Single<SiteNewsResponse>

    /** Полный текст новости (приходит как HTML). */
    @GET("sitenews/{id}.json")
    fun newsBody(
        @Path("id") id: Long,
        @Query("iss.meta") meta: String = "off",
    ): Single<NewsContentResponse>

    companion object {
        const val BASE_URL = "https://iss.moex.com/iss/"

        const val SECURITIES_COLUMNS = "SECID,SHORTNAME,SECTYPE,LOTSIZE,PREVPRICE"

        const val MARKET_DATA_COLUMNS =
            "SECID,LAST,LASTTOPREVPRICE,OPEN,HIGH,LOW,VALTODAY," +
                "CURRENTVALUE,LASTVALUE,OPENVALUE,LASTCHANGEPRC"
    }
}
