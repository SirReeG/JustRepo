package ru.reeg.moexmonitor.core.network

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.reeg.moexmonitor.core.network.dto.SecuritiesResponse

/**
 * Проверяем самое хрупкое место интеграции: разбор ISS-формата columns/data.
 * JSON ниже — реальный урезанный ответ MOEX.
 */
class IssBlockTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val sample = """
        {
          "securities": {
            "columns": ["SECID", "SHORTNAME", "SECTYPE", "LOTSIZE", "PREVPRICE"],
            "data": [
              ["SBER", "Сбербанк", "1", 10, 281.39],
              ["AKAI", "AKAI ETF", "J", 1, null]
            ]
          },
          "marketdata": {
            "columns": ["SECID", "LAST", "LASTTOPREVPRICE", "VALTODAY"],
            "data": [
              ["SBER", 284.6, 1.14, 14967277065.88]
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `значения читаются по имени колонки`() {
        val response = json.decodeFromString<SecuritiesResponse>(sample)
        val rows = response.securities.rows()

        assertEquals(2, rows.size)
        assertEquals("SBER", rows[0].string("SECID"))
        assertEquals(10, rows[0].int("LOTSIZE"))
        assertEquals(281.39, rows[0].double("PREVPRICE")!!, 0.0001)
    }

    @Test
    fun `null и отсутствующие колонки не ломают разбор`() {
        val rows = json.decodeFromString<SecuritiesResponse>(sample).securities.rows()

        assertNull(rows[1].double("PREVPRICE"))
        assertNull(rows[1].double("КОЛОНКИ_НЕТ"))
    }

    @Test
    fun `перебор нескольких имён колонок находит первое непустое`() {
        val row = json.decodeFromString<SecuritiesResponse>(sample).marketdata.rows().first()

        // Для акций приходит LAST, для индексов — CURRENTVALUE.
        assertEquals(284.6, row.double("CURRENTVALUE", "LAST")!!, 0.0001)
    }

    @Test
    fun `пустой блок отдаёт пустой список`() {
        val empty = json.decodeFromString<SecuritiesResponse>("""{"securities":{}}""")
        assertEquals(emptyList<Any>(), empty.securities.rows())
    }
}
