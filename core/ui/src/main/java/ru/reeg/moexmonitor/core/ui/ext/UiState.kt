package ru.reeg.moexmonitor.core.ui.ext

/**
 * Универсальное состояние экрана со списком.
 * Один тип на все фичи — меньше дублирования и одинаковая логика показа
 * прогресса, ошибки и пустого списка.
 */
data class ListUiState<T>(
    val items: List<T> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean get() = items.isEmpty() && !isRefreshing && errorMessage == null
}
