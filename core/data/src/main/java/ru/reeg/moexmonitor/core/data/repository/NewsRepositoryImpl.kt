package ru.reeg.moexmonitor.core.data.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton
import ru.reeg.moexmonitor.core.common.rx.SchedulerProvider
import ru.reeg.moexmonitor.core.common.rx.toOptional
import ru.reeg.moexmonitor.core.data.mapper.toDomain
import ru.reeg.moexmonitor.core.data.mapper.toEntity
import ru.reeg.moexmonitor.core.database.dao.NewsDao
import ru.reeg.moexmonitor.core.model.NewsItem
import ru.reeg.moexmonitor.core.network.MoexRemoteDataSource

@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val remote: MoexRemoteDataSource,
    private val newsDao: NewsDao,
    private val schedulers: SchedulerProvider,
) : NewsRepository {

    override fun observeNews(): Flowable<List<NewsItem>> =
        newsDao.observeLatest(NEWS_LIMIT)
            .map { items -> items.map { it.toDomain() } }
            .subscribeOn(schedulers.io())

    override fun observeNewsItem(id: Long): Flowable<Optional<NewsItem>> =
        newsDao.observeById(id)
            .map { items -> items.firstOrNull()?.toDomain().toOptional() }
            .subscribeOn(schedulers.io())

    override fun refreshNews(): Completable = remote.news().flatMapCompletable { items ->
        // upsert перезаписал бы уже загруженный body значением null,
        // поэтому список пишем как есть, а тексты обновляем точечно.
        Completable.fromAction { newsDao.upsertAll(items.map { it.toEntity() }) }
            .subscribeOn(schedulers.io())
    }

    override fun refreshNewsBody(id: Long): Completable = remote.newsBody(id)
        .flatMapCompletable { news ->
            val body = news.body
            if (body == null) {
                Completable.complete()
            } else {
                Completable.fromAction { newsDao.updateBody(id, body) }
                    .subscribeOn(schedulers.io())
            }
        }

    private companion object {
        const val NEWS_LIMIT = 100
    }
}
