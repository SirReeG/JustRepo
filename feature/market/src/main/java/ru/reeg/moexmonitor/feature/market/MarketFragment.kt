package ru.reeg.moexmonitor.feature.market

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import javax.inject.Inject
import ru.reeg.moexmonitor.core.common.format.MoexFormat
import ru.reeg.moexmonitor.core.common.rx.orNull
import ru.reeg.moexmonitor.core.di.ViewModelFactory
import ru.reeg.moexmonitor.core.di.coreComponent
import ru.reeg.moexmonitor.core.model.MarketList
import ru.reeg.moexmonitor.core.ui.base.RxFragment
import ru.reeg.moexmonitor.core.ui.ext.viewBinding
import ru.reeg.moexmonitor.core.ui.list.SecurityQuoteAdapter
import ru.reeg.moexmonitor.feature.market.databinding.FragmentMarketBinding
import ru.reeg.moexmonitor.feature.market.di.DaggerMarketComponent

class MarketFragment : RxFragment(R.layout.fragment_market) {

    @Inject
    lateinit var viewModel: MarketViewModel

    private val binding by viewBinding(FragmentMarketBinding::bind)

    private val adapter by lazy {
        SecurityQuoteAdapter(
            onClick = { item -> openSecurity(item.secId) },
            onFavoriteClick = { item -> viewModel.toggleFavorite(item.secId) },
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DaggerMarketComponent.factory().create().inject(this)
        setupList()
        setupTabs()
        setupSearch()
        observeState()
    }

    private fun setupList() = with(binding) {
        recyclerSecurities.layoutManager = LinearLayoutManager(requireContext())
        recyclerSecurities.adapter = adapter
        recyclerSecurities.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL),
        )
        swipeRefresh.setOnRefreshListener { viewModel.refresh() }
    }

    private fun setupTabs() = with(binding.tabs) {
        TABS.forEach { tab ->
            addTab(newTab().setText(getString(tab.titleRes)).setTag(tab.list))
        }
        addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    (tab.tag as? MarketList)?.let(viewModel::selectList)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) = Unit

                override fun onTabReselected(tab: TabLayout.Tab) = Unit
            },
        )
    }

    private fun setupSearch() {
        binding.editSearch.doAfterTextChanged { text ->
            viewModel.onSearchQueryChanged(text?.toString().orEmpty())
        }
    }

    /**
     * Подписки живут от onViewCreated до onDestroyView (см. RxFragment).
     *
     * TODO (упражнение): подписываться только в состоянии STARTED, как делал
     * repeatOnLifecycle в корутинах — например, через свой LifecycleObserver,
     * который вызывает clear() в onStop.
     */
    private fun observeState() {
        viewDisposables += viewModel.uiState.subscribe { state ->
            adapter.submitList(state.items)
            binding.swipeRefresh.isRefreshing = state.isRefreshing
            binding.textEmpty.visibility = if (state.isEmpty) View.VISIBLE else View.GONE

            state.errorMessage?.let { message ->
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                    .setAction(ru.reeg.moexmonitor.core.ui.R.string.ui_retry) {
                        viewModel.refresh()
                    }
                    .show()
                viewModel.consumeError()
            }
        }

        viewDisposables += viewModel.mainIndex.subscribe { index ->
            val quote = index.orNull()
            binding.textIndexValue.text = MoexFormat.indexValue(quote?.displayPrice)
            binding.viewIndexDelta.setDelta(quote?.changePercent)
        }
    }

    /**
     * Переход в другой feature-модуль через deep link:
     * :feature:market не знает про классы :feature:security,
     * они связаны только строкой-схемой.
     */
    private fun openSecurity(secId: String) {
        val request = NavDeepLinkRequest.Builder
            .fromUri("moexmonitor://security/$secId".toUri())
            .build()
        findNavController().navigate(request)
    }

    private data class TabSpec(val list: MarketList, val titleRes: Int)

    private companion object {
        val TABS = listOf(
            TabSpec(MarketList.ALL, R.string.market_tab_all),
            TabSpec(MarketList.GAINERS, R.string.market_tab_gainers),
            TabSpec(MarketList.LOSERS, R.string.market_tab_losers),
            TabSpec(MarketList.FUNDS, R.string.market_tab_funds),
        )
    }
}
