package ru.reeg.moexmonitor.feature.news

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject
import ru.reeg.moexmonitor.core.di.ViewModelFactory
import ru.reeg.moexmonitor.core.di.coreComponent
import ru.reeg.moexmonitor.core.ui.base.RxFragment
import ru.reeg.moexmonitor.core.ui.ext.viewBinding
import ru.reeg.moexmonitor.feature.news.databinding.FragmentNewsBinding
import ru.reeg.moexmonitor.feature.news.di.DaggerNewsComponent

class NewsFragment : RxFragment(R.layout.fragment_news) {

    @Inject
    lateinit var viewModel: NewsViewModel

    private val binding by viewBinding(FragmentNewsBinding::bind)

    private val adapter by lazy {
        NewsAdapter { item ->
            findNavController().navigate(
                R.id.action_news_to_detail,
                bundleOf(NewsDetailViewModel.ARG_NEWS_ID to item.id),
            )
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DaggerNewsComponent.factory().create(coreComponent()).inject(this)
        with(binding) {
            recyclerNews.layoutManager = LinearLayoutManager(requireContext())
            recyclerNews.adapter = adapter
            recyclerNews.addItemDecoration(
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL),
            )
            swipeRefreshNews.setOnRefreshListener { viewModel.refresh() }
        }

        viewDisposables += viewModel.uiState.subscribe { state ->
            adapter.submitList(state.items)
            binding.swipeRefreshNews.isRefreshing = state.isRefreshing
            binding.textNewsEmpty.visibility = if (state.isEmpty) View.VISIBLE else View.GONE
            state.errorMessage?.let { message ->
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.consumeError()
            }
        }
    }
}
