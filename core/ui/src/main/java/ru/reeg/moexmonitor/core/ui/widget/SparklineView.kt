package ru.reeg.moexmonitor.core.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.ColorUtils
import ru.reeg.moexmonitor.core.ui.R

/**
 * Простейший график цены: ломаная + заливка под ней.
 * Специально написан «руками» — это хорошая площадка, чтобы разобраться
 * с onMeasure / onDraw / Path и потом добавить свои фичи.
 *
 * TODO (упражнение): добавить касание с показом значения в точке
 *  и анимацию появления линии через ValueAnimator + PathMeasure.
 */
class SparklineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var lineWidthPx: Float = resources.displayMetrics.density * 2f
    private var upColor: Int = context.getColor(R.color.market_up)
    private var downColor: Int = context.getColor(R.color.market_down)
    private var fillAlpha: Float = 0.18f
    private var showBaseline: Boolean = true

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val baselinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density
        color = context.getColor(R.color.market_neutral)
        alpha = 80
    }

    private val linePath = Path()
    private val fillPath = Path()

    private var values: List<Float> = emptyList()

    init {
        context.withStyledAttributes(attrs, R.styleable.SparklineView, defStyleAttr, 0) {
            lineWidthPx = getDimension(R.styleable.SparklineView_sparkLineWidth, lineWidthPx)
            upColor = getColor(R.styleable.SparklineView_sparkUpColor, upColor)
            downColor = getColor(R.styleable.SparklineView_sparkDownColor, downColor)
            fillAlpha = getFloat(R.styleable.SparklineView_sparkFillAlpha, fillAlpha)
            showBaseline = getBoolean(R.styleable.SparklineView_sparkShowBaseline, showBaseline)
        }
        linePaint.strokeWidth = lineWidthPx
        if (isInEditMode) {
            setValues(listOf(10f, 12f, 11f, 14f, 13f, 17f, 16f, 19f))
        }
    }

    /** [points] — цены закрытия в хронологическом порядке. */
    fun setValues(points: List<Float>) {
        values = points
        rebuildPaths()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildPaths()
    }

    private fun rebuildPaths() {
        linePath.reset()
        fillPath.reset()
        if (values.size < 2 || width == 0 || height == 0) return

        val padding = lineWidthPx
        val usableWidth = width - padding * 2
        val usableHeight = height - padding * 2

        val min = values.min()
        val max = values.max()
        val span = (max - min).takeIf { it > 0f } ?: 1f
        val stepX = usableWidth / (values.size - 1)

        values.forEachIndexed { index, value ->
            val x = padding + stepX * index
            val y = padding + usableHeight * (1f - (value - min) / span)
            if (index == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }

        fillPath.set(linePath)
        fillPath.lineTo(padding + usableWidth, height - padding)
        fillPath.lineTo(padding, height - padding)
        fillPath.close()

        val rising = values.last() >= values.first()
        val color = if (rising) upColor else downColor
        linePaint.color = color
        fillPaint.color = ColorUtils.setAlphaComponent(color, (fillAlpha * 255).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (values.size < 2) return
        if (showBaseline) {
            val y = height / 2f
            canvas.drawLine(0f, y, width.toFloat(), y, baselinePaint)
        }
        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(linePath, linePaint)
    }
}
