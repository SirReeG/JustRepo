package ru.reeg.moexmonitor.core.model

/** Новость с сайта MOEX. [body] приходит только в детальном запросе, поэтому nullable. */
data class NewsItem(
    val id: Long,
    val title: String,
    val publishedAt: String,
    val tag: String?,
    val body: String?,
)
