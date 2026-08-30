package ru.reeg.moexmonitor.core.ui.base

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * База для всех экранов. Единственная её задача — не дать утечь подпискам:
 * всё, что связано с View, кладём в viewDisposables, и в onDestroyView
 * они очищаются (clear, а не dispose — фрагмент может пережить свою View).
 *
 * Аналог lifecycleScope из корутин, только руками.
 */
abstract class RxFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    protected val viewDisposables = CompositeDisposable()

    /** Сахар: viewDisposables += someObservable.subscribe(...) */
    protected operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
        add(disposable)
    }

    override fun onDestroyView() {
        viewDisposables.clear()
        super.onDestroyView()
    }
}
