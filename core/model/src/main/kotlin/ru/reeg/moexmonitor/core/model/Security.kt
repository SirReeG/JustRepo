package ru.reeg.moexmonitor.core.model

/** Статическая карточка инструмента (то, что почти не меняется в течение дня). */
data class Security(
    val secId: String,
    val shortName: String,
    val segment: MarketSegment,
    val board: String,
    val lotSize: Int,
    val prevPrice: Double?,
)

/** Текущие торговые данные по инструменту. */
data class Quote(
    val last: Double?,
    val changePercent: Double?,
    val open: Double?,
    val high: Double?,
    val low: Double?,
    val valueToday: Double?,
    val updatedAtMillis: Long,
)

/**
 * Инструмент вместе с котировкой — основная модель, которую рисует UI.
 * [quote] может быть null, если торги ещё не начались или данных нет.
 */
data class SecurityQuote(
    val security: Security,
    val quote: Quote?,
    val isFavorite: Boolean = false,
) {
    val secId: String get() = security.secId

    /** Цена, которую имеет смысл показать: последняя, иначе цена закрытия. */
    val displayPrice: Double? get() = quote?.last ?: security.prevPrice

    val changePercent: Double? get() = quote?.changePercent

    val isUp: Boolean get() = (changePercent ?: 0.0) > 0.0
}
