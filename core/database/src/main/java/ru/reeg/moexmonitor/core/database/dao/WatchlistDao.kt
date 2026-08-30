package ru.reeg.moexmonitor.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Flowable
import ru.reeg.moexmonitor.core.database.entity.SecurityEntity
import ru.reeg.moexmonitor.core.database.entity.WatchlistEntity

@Dao
interface WatchlistDao {

    @Query("SELECT secId FROM watchlist")
    fun observeIds(): Flowable<List<String>>

    @Query(
        """
        SELECT s.* FROM securities AS s
        INNER JOIN watchlist AS w ON w.secId = s.secId
        ORDER BY w.addedAtMillis DESC
        """,
    )
    fun observeSecurities(): Flowable<List<SecurityEntity>>

    /** Int, а не Boolean: так меньше сюрпризов с маппингом типов в Room. */
    @Query("SELECT COUNT(*) FROM watchlist WHERE secId = :secId")
    fun observeFavoriteCount(secId: String): Flowable<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(entity: WatchlistEntity)

    @Delete
    fun remove(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE secId = :secId")
    fun removeById(secId: String)

    @Query("SELECT COUNT(*) FROM watchlist WHERE secId = :secId")
    fun favoriteCount(secId: String): Int

    @Transaction
    fun toggle(secId: String, nowMillis: Long) {
        if (favoriteCount(secId) > 0) {
            removeById(secId)
        } else {
            add(WatchlistEntity(secId = secId, addedAtMillis = nowMillis))
        }
    }
}
