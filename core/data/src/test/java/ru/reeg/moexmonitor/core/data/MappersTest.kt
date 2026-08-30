package ru.reeg.moexmonitor.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.reeg.moexmonitor.core.data.mapper.toDomain
import ru.reeg.moexmonitor.core.data.mapper.toEntity
import ru.reeg.moexmonitor.core.model.MarketSegment
import ru.reeg.moexmonitor.core.network.model.NetworkSecurity

class MappersTest {

    private fun network(secId: String, secType: String?) = NetworkSecurity(
        secId = secId,
        shortName = "Тест",
        secType = secType,
        lotSize = null,
        prevPrice = 100.0,
        last = 101.0,
        changePercent = 1.0,
        open = 100.5,
        high = 102.0,
        low = 99.5,
        valueToday = 1_000.0,
    )

    @Test
    fun `бумаги с типом фонда попадают в сегмент FUNDS`() {
        val entity = network("AKAI", "J").toEntity(MarketSegment.SHARES, nowMillis = 1)
        assertEquals(MarketSegment.FUNDS, entity.segment)
    }

    @Test
    fun `обычные и привилегированные акции остаются в сегменте SHARES`() {
        assertEquals(
            MarketSegment.SHARES,
            network("SBER", "1").toEntity(MarketSegment.SHARES, 1).segment,
        )
        assertEquals(
            MarketSegment.SHARES,
            network("SBERP", "2").toEntity(MarketSegment.SHARES, 1).segment,
        )
    }

    @Test
    fun `запрос индексов всегда даёт сегмент INDEX`() {
        val entity = network("IMOEX", null).toEntity(MarketSegment.INDEX, 1)
        assertEquals(MarketSegment.INDEX, entity.segment)
    }

    @Test
    fun `отсутствующий размер лота заменяется единицей`() {
        assertEquals(1, network("SBER", "1").toEntity(MarketSegment.SHARES, 1).lotSize)
    }

    @Test
    fun `без торговых данных котировка равна null`() {
        val entity = network("SBER", "1")
            .copy(last = null, changePercent = null)
            .toEntity(MarketSegment.SHARES, 1)

        assertNull(entity.toDomain().quote)
        // но цена для показа всё равно есть — берём цену закрытия
        assertEquals(100.0, entity.toDomain().displayPrice!!, 0.0001)
    }

    @Test
    fun `флаг избранного переносится в доменную модель`() {
        val domain = network("SBER", "1").toEntity(MarketSegment.SHARES, 1).toDomain(isFavorite = true)
        assertTrue(domain.isFavorite)
        assertTrue(domain.isUp)
    }
}
