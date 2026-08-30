package ru.reeg.moexmonitor.core.ui.ext

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Делегат, который сам обнуляет ViewBinding в onDestroyView.
 * Так исчезает классическая утечка `_binding = null` руками в каждом фрагменте.
 *
 * Использование:
 * ```kotlin
 * private val binding by viewBinding(FragmentMarketBinding::bind)
 * ```
 */
class ViewBindingProperty<T : ViewBinding>(
    private val bind: (View) -> T,
) : ReadOnlyProperty<Fragment, T>, DefaultLifecycleObserver {

    private var binding: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        binding?.let { return it }

        val lifecycle = thisRef.viewLifecycleOwner.lifecycle
        val created = bind(thisRef.requireView())
        if (lifecycle.currentState != Lifecycle.State.DESTROYED) {
            lifecycle.addObserver(this)
            binding = created
        }
        return created
    }

    override fun onDestroy(owner: LifecycleOwner) {
        binding = null
    }
}

fun <T : ViewBinding> Fragment.viewBinding(bind: (View) -> T): ReadOnlyProperty<Fragment, T> =
    ViewBindingProperty(bind)
