package ru.reeg.moexmonitor.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.reeg.moexmonitor.core.database.entity.SecurityEntity
import ru.reeg.moexmonitor.core.model.MarketSegment

/**
 * Пример instrumented-теста для Room: база создаётся в памяти,
 * поэтому тесты быстрые и не мешают друг другу.
 * Запуск: ./gradlew :core:database:connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class WatchlistDaoTest {

    private lateinit var database: MoexDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MoexDatabase::class.java,
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun toggleAddsAndRemovesSecurity() {
        val dao = database.watchlistDao()
        database.securityDao().upsertAll(listOf(security("SBER")))

        dao.toggle("SBER", nowMillis = 1_000L)
        assertTrue(dao.favoriteCount("SBER") > 0)
        // blockingFirst берёт первое значение из Flowable синхронно —
        // для теста это проще и надёжнее, чем подписка с ожиданием.
        assertEquals(listOf("SBER"), dao.observeSecurities().blockingFirst().map { it.secId })

        dao.toggle("SBER", nowMillis = 2_000L)
        assertFalse(dao.favoriteCount("SBER") > 0)
        assertTrue(dao.observeSecurities().blockingFirst().isEmpty())
    }

    private fun security(secId: String) = SecurityEntity(
        secId = secId,
        shortName = secId,
        segment = MarketSegment.SHARES,
        board = MarketSegment.SHARES.board,
        lotSize = 10,
        prevPrice = 100.0,
        last = 101.0,
        changePercent = 1.0,
        openPrice = 100.0,
        highPrice = 102.0,
        lowPrice = 99.0,
        valueToday = 1_000.0,
        updatedAtMillis = 0L,
    )
}
