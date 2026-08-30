package ru.reeg.moexmonitor.core.network.iss

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * MOEX ISS отдаёт данные не привычным массивом объектов, а «таблицей»:
 *
 * ```json
 * { "securities": { "columns": ["SECID", "LAST"], "data": [["SBER", 281.4]] } }
 * ```
 *
 * [IssBlock] — это один такой блок. Набор колонок MOEX может менять,
 * поэтому парсим значения по имени колонки, а не по индексу.
 */
@Serializable
data class IssBlock(
    val columns: List<String> = emptyList(),
    val data: List<List<JsonElement>> = emptyList(),
) {
    /** Превращает «таблицу» в список строк с доступом по имени колонки. */
    fun rows(): List<IssRow> {
        if (columns.isEmpty()) return emptyList()
        val index = columns.withIndex().associate { (position, name) -> name to position }
        return data.map { values -> IssRow(index, values) }
    }
}

/** Одна строка ISS-таблицы. Все геттеры возвращают null, если колонки нет или значение пустое. */
class IssRow internal constructor(
    private val index: Map<String, Int>,
    private val values: List<JsonElement>,
) {
    private fun primitive(column: String): JsonPrimitive? {
        val position = index[column] ?: return null
        val element = values.getOrNull(position) ?: return null
        if (element is JsonNull) return null
        return element as? JsonPrimitive
    }

    fun string(vararg columns: String): String? =
        columns.firstNotNullOfOrNull { primitive(it)?.contentOrNull?.takeIf(String::isNotBlank) }

    fun double(vararg columns: String): Double? =
        columns.firstNotNullOfOrNull { primitive(it)?.contentOrNull?.toDoubleOrNull() }

    fun int(vararg columns: String): Int? =
        columns.firstNotNullOfOrNull { primitive(it)?.contentOrNull?.toDoubleOrNull()?.toInt() }

    fun long(vararg columns: String): Long? =
        columns.firstNotNullOfOrNull { primitive(it)?.contentOrNull?.toDoubleOrNull()?.toLong() }
}
