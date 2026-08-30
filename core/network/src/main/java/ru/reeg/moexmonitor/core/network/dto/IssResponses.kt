package ru.reeg.moexmonitor.core.network.dto

import kotlinx.serialization.Serializable
import ru.reeg.moexmonitor.core.network.iss.IssBlock

@Serializable
data class SecuritiesResponse(
    val securities: IssBlock = IssBlock(),
    val marketdata: IssBlock = IssBlock(),
)

@Serializable
data class CandlesResponse(
    val candles: IssBlock = IssBlock(),
)

@Serializable
data class SiteNewsResponse(
    val sitenews: IssBlock = IssBlock(),
)

@Serializable
data class NewsContentResponse(
    val content: IssBlock = IssBlock(),
)
