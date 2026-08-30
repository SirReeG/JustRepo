package ru.reeg.moexmonitor.core.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.reeg.moexmonitor.core.common.format.MoexFormat
import ru.reeg.moexmonitor.core.model.MarketSegment
import ru.reeg.moexmonitor.core.model.SecurityQuote
import ru.reeg.moexmonitor.core.ui.R
import ru.reeg.moexmonitor.core.ui.databinding.ItemSecurityBinding

/**
 * Один адаптер на все списки бумаг: «Рынок», «Топ роста», «Фонды», «Избранное».
 * Живёт в :core:ui, поэтому его переиспользуют сразу несколько feature-модулей.
 */
class SecurityQuoteAdapter(
    private val onClick: (SecurityQuote) -> Unit,
    private val onFavoriteClick: (SecurityQuote) -> Unit,
) : ListAdapter<SecurityQuote, SecurityQuoteAdapter.ViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSecurityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemSecurityBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SecurityQuote) = with(binding) {
            textTicker.text = item.secId
            textName.text = item.security.shortName
            textPrice.text = if (item.security.segment == MarketSegment.INDEX) {
                MoexFormat.indexValue(item.displayPrice)
            } else {
                MoexFormat.price(item.displayPrice)
            }
            viewDelta.setDelta(item.changePercent)

            val starColor = if (item.isFavorite) R.color.star_active else R.color.star_inactive
            buttonFavorite.imageTintList =
                ContextCompat.getColorStateList(root.context, starColor)
            buttonFavorite.contentDescription = root.context.getString(
                if (item.isFavorite) R.string.ui_favorite_remove else R.string.ui_favorite_add,
            )

            root.setOnClickListener { onClick(item) }
            buttonFavorite.setOnClickListener { onFavoriteClick(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<SecurityQuote>() {
        override fun areItemsTheSame(oldItem: SecurityQuote, newItem: SecurityQuote): Boolean =
            oldItem.secId == newItem.secId

        override fun areContentsTheSame(oldItem: SecurityQuote, newItem: SecurityQuote): Boolean =
            oldItem == newItem
    }
}
