package ru.reeg.moexmonitor.core.di

import javax.inject.Scope

/**
 * Скоуп уровня приложения.
 * Живёт столько же, сколько процесс.
 * Используется для CoreComponent.
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope

/**
 * Скоуп уровня фичи (экрана/модуля).
 * Живёт пока активна фича (например, пока пользователь находится в модуле).
 * Используется для Feature-компонентов (WatchlistComponent, FavoritesComponent, etc).
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class FeatureScope

/**
 * Скоуп уровня фрагмента.
 * Живёт пока фрагмент существует.
 * Используется для Fragment-компонентов (если они нужны).
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class FragmentScope
