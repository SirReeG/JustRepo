package ru.reeg.moexmonitor.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.reactivex.Flowable
import ru.reeg.moexmonitor.core.database.entity.SecurityEntity
import ru.reeg.moexmonitor.core.model.MarketSegment

/**
 * Чтение отдаём как Flowable — Room сам присылает новый список при каждом
 * изменении таблицы, это и есть основа offline-first.
 *
 * Запись оставлена синхронной специально: репозиторий оборачивает её в
 * Completable.fromAction(...).subscribeOn(io()), поэтому поток выбирается
 * в одном месте и его видно в коде.
 */
@Dao
interface SecurityDao {

    @Upsert
    fun upsertAll(items: List<SecurityEntity>)

    @Query(
        """
        SELECT * FROM securities
        WHERE segment = :segment
        ORDER BY valueToday DESC, secId ASC
        LIMIT :limit
        """,
    )
    fun observeBySegment(segment: MarketSegment, limit: Int): Flowable<List<SecurityEntity>>

    @Query(
        """
        SELECT * FROM securities
        WHERE segment = :segment AND changePercent IS NOT NULL AND last IS NOT NULL
        ORDER BY changePercent DESC
        LIMIT :limit
        """,
    )
    fun observeGainers(segment: MarketSegment, limit: Int): Flowable<List<SecurityEntity>>

    @Query(
        """
        SELECT * FROM securities
        WHERE segment = :segment AND changePercent IS NOT NULL AND last IS NOT NULL
        ORDER BY changePercent ASC
        LIMIT :limit
        """,
    )
    fun observeLosers(segment: MarketSegment, limit: Int): Flowable<List<SecurityEntity>>

    @Query(
        """
        SELECT * FROM securities
        WHERE secId LIKE '%' || :query || '%'
           OR shortName LIKE '%' || :query || '%'
        ORDER BY valueToday DESC
        LIMIT 50
        """,
    )
    fun search(query: String): Flowable<List<SecurityEntity>>

    /**
     * Возвращаем список, а не одну сущность: Flowable не умеет передавать null,
     * а Room для пустого результата просто не присылает элемент —
     * экран тогда навсегда останется пустым и без состояния «не найдено».
     */
    @Query("SELECT * FROM securities WHERE secId = :secId LIMIT 1")
    fun observeBySecId(secId: String): Flowable<List<SecurityEntity>>

    @Query("SELECT segment FROM securities WHERE secId = :secId LIMIT 1")
    fun segmentOf(secId: String): MarketSegment?

    @Query("SELECT COUNT(*) FROM securities WHERE segment = :segment")
    fun countBySegment(segment: MarketSegment): Int
}
