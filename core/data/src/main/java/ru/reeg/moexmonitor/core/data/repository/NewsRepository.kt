package ru.reeg.moexmonitor.core.data.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import java.util.Optional
import ru.reeg.moexmonitor.core.model.NewsItem

interface NewsRepository {
    fun observeNews(): Flowable<List<NewsItem>>
    fun observeNewsItem(id: Long): Flowable<Optional<NewsItem>>
    fun refreshNews(): Completable
    fun refreshNewsBody(id: Long): Completable
}
