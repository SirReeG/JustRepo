package ru.reeg.moexmonitor.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.MapKey
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

/**
 * Ключ для multibinding-карты ViewModel.
 * В фиче это выглядит так:
 *
 * @Binds @IntoMap @ViewModelKey(MarketViewModel::class)
 * fun bind(vm: MarketViewModel): ViewModel
 */
@MapKey
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewModelKey(val value: KClass<out ViewModel>)

/**
 * Замена @HiltViewModel: Dagger собирает Map<Class<ViewModel>, Provider<ViewModel>>,
 * а фабрика по классу достаёт нужный провайдер. @JvmSuppressWildcards обязателен,
 * иначе Kotlin сгенерирует Provider<? extends ViewModel> и Dagger не найдёт биндинг.
 */
class ViewModelFactory @Inject constructor(
    private val providers: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val provider = providers[modelClass]
            // Подстраховка на случай, если в карте лежит подкласс запрошенного типа.
            ?: providers.entries.firstOrNull { modelClass.isAssignableFrom(it.key) }?.value
            ?: error(
                "ViewModel ${modelClass.name} не зарегистрирована. " +
                    "Добавьте @Binds @IntoMap @ViewModelKey в модуль фичи.",
            )

        @Suppress("UNCHECKED_CAST")
        return provider.get() as T
    }
}

/**
 * Для ViewModel с параметром (secId, newsId) multibinding не подходит:
 * аргумент известен только во время выполнения. Пишем крошечную фабрику вручную.
 */
inline fun <reified VM : ViewModel> simpleViewModelFactory(
    crossinline create: () -> VM,
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(VM::class.java)) {
            "Ожидалась ${VM::class.java.name}, запрошена ${modelClass.name}"
        }
        @Suppress("UNCHECKED_CAST")
        return create() as T
    }
}
