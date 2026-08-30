package ru.reeg.moexmonitor.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import ru.reeg.moexmonitor.core.common.di.ApplicationContext
import ru.reeg.moexmonitor.core.database.MoexDatabase
import ru.reeg.moexmonitor.core.database.dao.CandleDao
import ru.reeg.moexmonitor.core.database.dao.NewsDao
import ru.reeg.moexmonitor.core.database.dao.SecurityDao
import ru.reeg.moexmonitor.core.database.dao.WatchlistDao

@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): MoexDatabase =
        Room.databaseBuilder(context, MoexDatabase::class.java, MoexDatabase.NAME)
            // Кэш можно спокойно пересобрать из сети, поэтому на этапе учёбы
            // проще пересоздавать базу, чем писать миграции.
            // TODO (упражнение): заменить на реальную Migration при version = 2.
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun securityDao(db: MoexDatabase): SecurityDao = db.securityDao()

    @Provides fun candleDao(db: MoexDatabase): CandleDao = db.candleDao()

    @Provides fun newsDao(db: MoexDatabase): NewsDao = db.newsDao()

    @Provides fun watchlistDao(db: MoexDatabase): WatchlistDao = db.watchlistDao()
}
