package ru.reeg.moexmonitor.feature.news

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import javax.inject.Inject
import ru.reeg.moexmonitor.core.common.format.MoexFormat
import ru.reeg.moexmonitor.core.common.rx.orNull
import ru.reeg.moexmonitor.core.di.coreComponent
import ru.reeg.moexmonitor.core.ui.base.RxFragment
import ru.reeg.moexmonitor.core.ui.ext.viewBinding
import ru.reeg.moexmonitor.feature.news.databinding.FragmentNewsDetailBinding
import ru.reeg.moexmonitor.feature.news.di.DaggerNewsComponent

class NewsDetailFragment : RxFragment(R.layout.fragment_news_detail) {

    @Inject
    lateinit var viewModel: NewsDetailViewModel

    private val binding by viewBinding(FragmentNewsDetailBinding::bind)

    private val newsId: Long by lazy {
        requireArguments().getLong(NewsDetailViewModel.ARG_NEWS_ID)
    }


    override fun onAttach(context: Context) {
        DaggerNewsComponent.factory().create(coreComponent()).inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarNewsDetail.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewDisposables += viewModel.newsItem.subscribe { optional ->
            val item = optional.orNull()
            binding.textDetailDate.text = MoexFormat.newsDateTime(item?.publishedAt)
            binding.textDetailTitle.text = item?.title.orEmpty()
            // MOEX отдаёт текст новости в HTML — рендерим его через HtmlCompat.
            binding.textDetailBody.text = item?.body
                ?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_COMPACT) }
                ?: getString(R.string.news_loading_body)
        }
    }
}
