package ru.reeg.moexmonitor.feature.security

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import javax.inject.Inject
import ru.reeg.moexmonitor.core.common.format.MoexFormat
import ru.reeg.moexmonitor.core.di.coreComponent
import ru.reeg.moexmonitor.core.model.CandleRange
import ru.reeg.moexmonitor.core.model.MarketSegment
import ru.reeg.moexmonitor.core.ui.base.RxFragment
import ru.reeg.moexmonitor.core.ui.ext.viewBinding
import ru.reeg.moexmonitor.feature.security.databinding.FragmentSecurityBinding
import ru.reeg.moexmonitor.feature.security.databinding.ItemStatRowBinding
import ru.reeg.moexmonitor.feature.security.di.DaggerSecurityComponent
import ru.reeg.moexmonitor.core.ui.R as UiR

class SecurityFragment : RxFragment(R.layout.fragment_security) {

    @Inject
    lateinit var viewModelFactory: SecurityViewModelFactory

    private val binding by viewBinding(FragmentSecurityBinding::bind)

    /** Аргумент навигации читаем сами — SavedStateHandle здесь не используется. */
    private val secId: String by lazy {
        requireNotNull(requireArguments().getString(SecurityViewModel.ARG_SEC_ID)) {
            "SecurityFragment требует аргумент ${SecurityViewModel.ARG_SEC_ID}"
        }
    }

    private val viewModel: SecurityViewModel by viewModels { viewModelFactory.create(secId) }

    private var favoriteItem: MenuItem? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DaggerSecurityComponent.factory().create(coreComponent()).inject(this)
        setupToolbar()
        setupRangeTabs()
        observeState()
    }

    private fun setupToolbar() = with(binding.toolbar) {
        title = viewModel.secId
        setNavigationOnClickListener { findNavController().navigateUp() }
        menu.clear()
        favoriteItem = menu.add(getString(UiR.string.ui_favorite_add)).apply {
            setIcon(UiR.drawable.ic_star)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                viewModel.toggleFavorite()
                true
            }
        }
    }

    private fun setupRangeTabs() = with(binding.tabsRange) {
        RANGES.forEach { (range, titleRes) ->
            addTab(newTab().setText(getString(titleRes)).setTag(range))
        }
        addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    (tab.tag as? CandleRange)?.let(viewModel::selectRange)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) = Unit
                override fun onTabReselected(tab: TabLayout.Tab) = Unit
            },
        )
        getTabAt(RANGES.indexOfFirst { it.first == CandleRange.MONTH })?.select()
    }

    private fun observeState() {
        viewDisposables += viewModel.uiState.subscribe { state ->
            val quote = state.quote
            val isIndex = quote?.security?.segment == MarketSegment.INDEX

            binding.textName.text = quote?.security?.shortName.orEmpty()
            binding.textPrice.text = if (isIndex) {
                MoexFormat.indexValue(quote?.displayPrice)
            } else {
                MoexFormat.price(quote?.displayPrice)
            }
            binding.viewDelta.setDelta(quote?.changePercent)

            val closes = state.candles.map { it.close.toFloat() }
            binding.sparkline.setValues(closes)
            val hasChart = closes.size >= 2
            binding.sparkline.visibility = if (hasChart) View.VISIBLE else View.GONE
            binding.textChartEmpty.visibility = if (hasChart) View.GONE else View.VISIBLE

            renderStats(state, isIndex)
            renderFavorite(quote?.isFavorite == true)

            state.errorMessage?.let { message ->
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.consumeError()
            }
        }
    }

    private fun renderFavorite(isFavorite: Boolean) {
        val colorRes = if (isFavorite) UiR.color.star_active else UiR.color.star_inactive
        favoriteItem?.icon?.setTint(ContextCompat.getColor(requireContext(), colorRes))
        favoriteItem?.title = getString(
            if (isFavorite) UiR.string.ui_favorite_remove else UiR.string.ui_favorite_add,
        )
    }

    private fun renderStats(state: SecurityUiState, isIndex: Boolean) {
        val table = binding.tableStats
        table.removeAllViews()
        val quote = state.quote ?: return
        val money: (Double?) -> String = { value ->
            if (isIndex) MoexFormat.indexValue(value) else MoexFormat.price(value)
        }

        val rows = listOf(
            getString(R.string.security_stat_open) to money(quote.quote?.open),
            getString(R.string.security_stat_high) to money(quote.quote?.high),
            getString(R.string.security_stat_low) to money(quote.quote?.low),
            getString(R.string.security_stat_volume) to
                MoexFormat.compactMoney(quote.quote?.valueToday),
            getString(R.string.security_stat_lot) to quote.security.lotSize.toString(),
            getString(R.string.security_stat_board) to quote.security.board,
        )

        val inflater = LayoutInflater.from(requireContext())
        rows.forEach { (label, value) ->
            val rowBinding = ItemStatRowBinding.inflate(inflater, table, false)
            rowBinding.textStatLabel.text = label
            rowBinding.textStatValue.text = value
            table.addView(rowBinding.root)
        }
    }

    private companion object {
        val RANGES = listOf(
            CandleRange.WEEK to R.string.security_range_week,
            CandleRange.MONTH to R.string.security_range_month,
            CandleRange.YEAR to R.string.security_range_year,
        )
    }
}
