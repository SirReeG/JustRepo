package ru.reeg.moexmonitor.core.model

/**
 * Сегмент рынка MOEX, который умеет показывать приложение.
 *
 * Важный нюанс реального API: акции и паи фондов торгуются в одном режиме TQBR,
 * различить их можно только по колонке SECTYPE (см. [FUND_SEC_TYPES]).
 * Отдельного борда TQTF для фондов больше нет — он отдаёт пустой список.
 *
 * @param market значение параметра `market` в ISS API
 * @param board режим торгов (board), из которого грузим данные
 */
enum class MarketSegment(val market: String, val board: String) {
    /** Акции и депозитарные расписки основного режима торгов. */
    SHARES("shares", "TQBR"),

    /** Паи биржевых и закрытых фондов (ETF, БПИФ, ЗПИФ). */
    FUNDS("shares", "TQBR"),

    /** Индексы (IMOEX, RTSI и отраслевые). */
    INDEX("index", "SNDX"),
    ;

    companion object {
        /** Тикер главного индекса — показываем его шапкой на экране «Рынок». */
        const val MAIN_INDEX = "IMOEX"

        /** SECTYPE, которые считаем паями фондов. */
        val FUND_SEC_TYPES: Set<String> = setOf("J", "A", "B", "9")

        fun fromName(name: String?): MarketSegment =
            entries.firstOrNull { it.name == name } ?: SHARES

        /** Определяет сегмент по типу бумаги из ISS. */
        fun classify(secType: String?, requested: MarketSegment): MarketSegment = when {
            requested == INDEX -> INDEX
            secType != null && secType in FUND_SEC_TYPES -> FUNDS
            else -> SHARES
        }
    }
}
