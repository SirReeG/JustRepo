package ru.reeg.moexmonitor.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.reeg.moexmonitor.core.database.dao.CandleDao
import ru.reeg.moexmonitor.core.database.dao.NewsDao
import ru.reeg.moexmonitor.core.database.dao.SecurityDao
import ru.reeg.moexmonitor.core.database.dao.WatchlistDao
import ru.reeg.moexmonitor.core.database.entity.CandleEntity
import ru.reeg.moexmonitor.core.database.entity.NewsEntity
import ru.reeg.moexmonitor.core.database.entity.SecurityEntity
import ru.reeg.moexmonitor.core.database.entity.WatchlistEntity

@Database(
    entities = [
        SecurityEntity::class,
        CandleEntity::class,
        NewsEntity::class,
        WatchlistEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(MoexTypeConverters::class)
abstract class MoexDatabase : RoomDatabase() {
    abstract fun securityDao(): SecurityDao
    abstract fun candleDao(): CandleDao
    abstract fun newsDao(): NewsDao
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        const val NAME = "moex-monitor.db"
    }
}
