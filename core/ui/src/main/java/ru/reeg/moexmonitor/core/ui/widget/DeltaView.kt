package ru.reeg.moexmonitor.core.ui.widget

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.ColorUtils
import com.google.android.material.textview.MaterialTextView
import ru.reeg.moexmonitor.core.common.format.MoexFormat
import ru.reeg.moexmonitor.core.ui.R

/**
 * Показывает изменение цены в процентах: сам подбирает цвет и рисует
 * подложку-«чип» через GradientDrawable.
 *
 * Пример использования в XML:
 * ```xml
 * <ru.reeg.moexmonitor.core.ui.widget.DeltaView
 *     style="@style/Widget.Moex.Delta"
 *     app:deltaShowChip="true"
 *     app:deltaChipAlpha="0.14" />
 * ```
 */
class DeltaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : MaterialTextView(context, attrs, defStyleAttr) {

    private var positiveColor: Int = 0
    private var negativeColor: Int = 0
    private var neutralColor: Int = 0
    private var showChip: Boolean = true
    private var chipAlpha: Float = 0.14f

    private val chip = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = resources.getDimension(R.dimen.radius_chip)
    }

    private var currentValue: Double? = null

    init {
        // Значения по умолчанию берём из темы: deltaPositiveColor / deltaNegativeColor.
        positiveColor = themeColor(R.attr.deltaPositiveColor, R.color.market_up)
        negativeColor = themeColor(R.attr.deltaNegativeColor, R.color.market_down)
        neutralColor = themeColor(R.attr.deltaNeutralColor, R.color.market_neutral)

        context.withStyledAttributes(attrs, R.styleable.DeltaView, defStyleAttr, 0) {
            positiveColor = getColor(R.styleable.DeltaView_deltaPositiveColor, positiveColor)
            negativeColor = getColor(R.styleable.DeltaView_deltaNegativeColor, negativeColor)
            neutralColor = getColor(R.styleable.DeltaView_deltaNeutralColor, neutralColor)
            showChip = getBoolean(R.styleable.DeltaView_deltaShowChip, showChip)
            chipAlpha = getFloat(R.styleable.DeltaView_deltaChipAlpha, chipAlpha)
        }

        if (isInEditMode) setDelta(1.25)
    }

    /** Главный публичный метод: передаём процент изменения, null — данных нет. */
    fun setDelta(percent: Double?) {
        currentValue = percent
        val color = colorFor(percent)
        text = MoexFormat.percent(percent)
        setTextColor(color)
        background = if (showChip) {
            chip.apply {
                setColor(ColorUtils.setAlphaComponent(color, (chipAlpha * 255).toInt()))
            }
        } else {
            null
        }
    }

    /** Иногда нужно показать процент, посчитанный из двух цен. */
    fun setDelta(from: Double?, to: Double?) {
        val percent = if (from == null || to == null || from == 0.0) {
            null
        } else {
            (to - from) / from * 100.0
        }
        setDelta(percent)
    }

    fun colorFor(percent: Double?): Int = when {
        percent == null -> neutralColor
        percent > 0.0 -> positiveColor
        percent < 0.0 -> negativeColor
        else -> neutralColor
    }

    private fun themeColor(attr: Int, fallback: Int): Int {
        val typed = context.obtainStyledAttributes(intArrayOf(attr))
        try {
            val resolved = typed.getColor(0, 0)
            return if (resolved != 0) resolved else context.getColor(fallback)
        } finally {
            typed.recycle()
        }
    }
}
