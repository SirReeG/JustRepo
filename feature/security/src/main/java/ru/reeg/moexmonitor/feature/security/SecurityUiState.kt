package ru.reeg.moexmonitor.feature.security

import ru.reeg.moexmonitor.core.model.Candle
import ru.reeg.moexmonitor.core.model.CandleRange
import ru.reeg.moexmonitor.core.model.SecurityQuote

data class SecurityUiState(
    val secId: String,
    val quote: SecurityQuote? = null,
    val candles: List<Candle> = emptyList(),
    val range: CandleRange = CandleRange.MONTH,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
