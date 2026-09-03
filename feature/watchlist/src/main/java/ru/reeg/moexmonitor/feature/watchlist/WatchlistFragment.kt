package ru.reeg.moexmonitor.feature.watchlist

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import javax.inject.Inject
import ru.reeg.moexmonitor.core.di.ViewModelFactory
import ru.reeg.moexmonitor.core.di.coreComponent
import ru.reeg.moexmonitor.core.ui.base.RxFragment
import ru.reeg.moexmonitor.core.ui.ext.viewBinding
import ru.reeg.moexmonitor.core.ui.list.SecurityQuoteAdapter
import ru.reeg.moexmonitor.feature.watchlist.databinding.FragmentWatchlistBinding
import ru.reeg.moexmonitor.feature.watchlist.di.DaggerWatchlistComponent

class WatchlistFragment : RxFragment(R.layout.fragment_watchlist) {

    @Inject
    lateinit var viewModel: WatchlistViewModel

    private val binding by viewBinding(FragmentWatchlistBinding::bind)

    private val adapter by lazy {
        SecurityQuoteAdapter(
            onClick = { item ->
                val request = NavDeepLinkRequest.Builder
                    .fromUri("moexmonitor://security/${item.secId}".toUri())
                    .build()
                findNavController().navigate(request)
            },
            onFavoriteClick = { item -> viewModel.toggleFavorite(item.secId) },
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DaggerWatchlistComponent.factory().create(coreComponent()).inject(this)
        binding.recyclerWatchlist.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerWatchlist.adapter = adapter
        binding.recyclerWatchlist.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL),
        )

        viewDisposables += viewModel.items.subscribe { items ->
            adapter.submitList(items)
            binding.textWatchlistEmpty.visibility =
                if (items.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
