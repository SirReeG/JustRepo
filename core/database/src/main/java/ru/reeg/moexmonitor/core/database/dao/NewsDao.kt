package ru.reeg.moexmonitor.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.reactivex.Flowable
import ru.reeg.moexmonitor.core.database.entity.NewsEntity

@Dao
interface NewsDao {

    @Query("SELECT * FROM news ORDER BY publishedAt DESC LIMIT :limit")
    fun observeLatest(limit: Int): Flowable<List<NewsEntity>>

    /** Список вместо одной сущности — по той же причине, что и в SecurityDao. */
    @Query("SELECT * FROM news WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flowable<List<NewsEntity>>

    @Upsert
    fun upsertAll(items: List<NewsEntity>)

    /** Текст новости приходит позже заголовка, поэтому обновляем его отдельно. */
    @Query("UPDATE news SET body = :body WHERE id = :id")
    fun updateBody(id: Long, body: String)
}
