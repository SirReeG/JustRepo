package ru.reeg.moexmonitor.core.common.format

import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

/**
 * Форматирование чисел и дат под русскую локаль.
 * Вынесено в отдельный object без Android-зависимостей, чтобы можно было
 * покрыть обычными JVM-тестами.
 */
object MoexFormat {

    private val ru = Locale("ru", "RU")

    private val issDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", ru)
    private val prettyDateTime = SimpleDateFormat("d MMM, HH:mm", ru)
    private val prettyDate = SimpleDateFormat("d MMM yyyy", ru)

    /** Цена: 4 знака для «копеечных» бумаг, 2 знака для остальных. */
    fun price(value: Double?): String {
        if (value == null) return "—"
        val digits = if (abs(value) < 10.0) 4 else 2
        return String.format(ru, "%,.${digits}f \u20BD", value)
    }

    /** Значение индекса — без символа валюты. */
    fun indexValue(value: Double?): String =
        if (value == null) "—" else String.format(ru, "%,.2f", value)

    /** Процент изменения со знаком: +1,25 % */
    fun percent(value: Double?): String {
        if (value == null) return "—"
        val sign = if (value > 0) "+" else ""
        return String.format(ru, "%s%.2f %%", sign, value)
    }

    /** Объём торгов в компактном виде: 1,2 млрд ₽ */
    fun compactMoney(value: Double?): String {
        if (value == null) return "—"
        val abs = abs(value)
        return when {
            abs >= 1_000_000_000 -> String.format(ru, "%.1f млрд \u20BD", value / 1_000_000_000)
            abs >= 1_000_000 -> String.format(ru, "%.1f млн \u20BD", value / 1_000_000)
            abs >= 1_000 -> String.format(ru, "%.1f тыс. \u20BD", value / 1_000)
            else -> String.format(ru, "%.0f \u20BD", value)
        }
    }

    /** "2026-08-28 21:14:31" -> "28 авг., 21:14" */
    fun newsDateTime(issValue: String?): String = reformat(issValue, prettyDateTime)

    /** "2026-08-28 00:00:00" -> "28 авг. 2026" */
    fun candleDate(issValue: String?): String = reformat(issValue, prettyDate)

    private fun reformat(issValue: String?, target: SimpleDateFormat): String {
        if (issValue.isNullOrBlank()) return "—"
        return runCatching {
            val date = synchronized(issDateTime) { issDateTime.parse(issValue) }
            if (date == null) issValue else synchronized(target) { target.format(date) }
        }.getOrDefault(issValue)
    }
}
