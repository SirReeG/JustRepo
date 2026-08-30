package ru.reeg.moexmonitor.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import io.reactivex.Flowable
import ru.reeg.moexmonitor.core.database.entity.CandleEntity

@Dao
interface CandleDao {

    @Query("SELECT * FROM candles WHERE secId = :secId AND range = :range ORDER BY begin ASC")
    fun observe(secId: String, range: String): Flowable<List<CandleEntity>>

    @Upsert
    fun upsertAll(items: List<CandleEntity>)

    @Query("DELETE FROM candles WHERE secId = :secId AND range = :range")
    fun clear(secId: String, range: String)

    /** Заменяем кэш периода целиком — так не остаётся «дыр» в истории. */
    @Transaction
    fun replace(secId: String, range: String, items: List<CandleEntity>) {
        clear(secId, range)
        upsertAll(items)
    }
}
