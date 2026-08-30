package ru.reeg.moexmonitor.core.di

import androidx.fragment.app.Fragment

/**
 * Реализуется классом Application. Через него любой фрагмент достаёт
 * корневой компонент, не зная про класс приложения.
 */
interface CoreComponentProvider {
    val coreComponent: CoreComponent
}

/**
 * Хелпер для фрагментов: DaggerXComponent.factory().create(coreComponent()).inject(this).
 * Вызывать нужно в onAttach — до того, как отработает super.onAttach и
 * восстановится состояние, иначе by viewModels может сработать раньше инъекции.
 */
fun Fragment.coreComponent(): CoreComponent =
    (requireActivity().application as CoreComponentProvider).coreComponent
