package ru.reeg.moexmonitor.feature.news

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.reeg.moexmonitor.core.common.format.MoexFormat
import ru.reeg.moexmonitor.core.model.NewsItem
import ru.reeg.moexmonitor.feature.news.databinding.ItemNewsBinding

class NewsAdapter(
    private val onClick: (NewsItem) -> Unit,
) : ListAdapter<NewsItem, NewsAdapter.ViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(
        private val binding: ItemNewsBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NewsItem) = with(binding) {
            textNewsDate.text = MoexFormat.newsDateTime(item.publishedAt)
            textNewsTitle.text = item.title
            root.setOnClickListener { onClick(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<NewsItem>() {
        override fun areItemsTheSame(oldItem: NewsItem, newItem: NewsItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NewsItem, newItem: NewsItem) = oldItem == newItem
    }
}
