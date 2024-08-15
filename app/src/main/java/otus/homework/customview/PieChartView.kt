package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var pieData: List<PieEntry> = emptyList()
    private val colors = mutableListOf<Int>()
    private var selectedCategory: String = ""

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val rectF = RectF()
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    var onSliceClickListener: ((String) -> Unit)? = null

    init {
        colors.addAll(
            listOf(
                Color.parseColor("#FF8C00"),
                Color.parseColor("#1E90FF"),
                Color.parseColor("#32CD32"),
                Color.parseColor("#FF1493"),
                Color.parseColor("#FFD700"),
                Color.parseColor("#7B68EE"),
                Color.parseColor("#00FA9A"),
                Color.parseColor("#FF4500"),
                Color.parseColor("#ADFF2F"),
                Color.parseColor("#DC143C")
            )
        )

        textPaint.color = Color.BLACK
        textPaint.textSize = 48f
        textPaint.textAlign = Paint.Align.CENTER
    }

    fun setPieData(data: List<ExpenseEntry>) {
        // Группировка и суммирование данных по категориям
        val groupedData = data.groupBy { it.category }
            .map { (category, entries) ->
                PieEntry(
                    amount = entries.sumOf { it.amount.toDouble() }.toFloat(),
                    category = category
                )
            }

        pieData = groupedData
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = if (widthMode == MeasureSpec.EXACTLY) widthSize else min(100, widthSize)
        val height = if (heightMode == MeasureSpec.EXACTLY) heightSize else min(100, heightSize)

        val size = min(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (pieData.isEmpty()) return

        centerX = width / 2f
        centerY = height / 2f
        radius = min(centerX, centerY) * 0.8f

        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        var startAngle = 0f
        val total = pieData.sumOf { it.amount.toDouble() }.toFloat()

        // Отрисовка категорий
        pieData.forEachIndexed { index, pieEntry ->
            val sweepAngle = (pieEntry.amount / total) * 360
            paint.color = colors[index % colors.size]
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)
            startAngle += sweepAngle
        }

        // Белый круг в центре
        paint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY, radius * 0.5f, paint)

        // Текст выбранной категории под диаграммой
        canvas.drawText(selectedCategory, centerX, centerY + radius + 50, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val touchX = event.x
            val touchY = event.y

            if (isPointInsideCategoryLine(touchX, touchY, centerX, centerY, radius)) {
                val angle = getAngle(centerX, centerY, touchX, touchY)
                val selectedCategoryName = findCategoryByAngle(angle)

                if (selectedCategoryName != null) {
                    selectedCategory = selectedCategoryName
                    onSliceClickListener?.invoke(selectedCategoryName)
                    invalidate()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    // Проверка нажали ли на линию категорий, а не на пустое место
    private fun isPointInsideCategoryLine(touchX: Float, touchY: Float, centerX: Float, centerY: Float, radius: Float): Boolean {
        val dx = (touchX - centerX).toDouble().pow(2.0)
        val dy = (touchY - centerY).toDouble().pow(2.0)
        return sqrt(dx + dy) <= radius && sqrt(dx + dy) >= radius * 0.5
    }

    private fun getAngle(centerX: Float, centerY: Float, touchX: Float, touchY: Float): Float {
        val dx = touchX - centerX
        val dy = touchY - centerY
        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        angle = (angle + 360) % 360
        return angle
    }

    private fun findCategoryByAngle(angle: Float): String? {
        var startAngle = 0f
        val total = pieData.sumOf { it.amount.toDouble() }.toFloat()

        pieData.forEach { pieEntry ->
            val sweepAngle = (pieEntry.amount / total) * 360
            if (angle in startAngle..(startAngle + sweepAngle)) {
                return pieEntry.category
            }
            startAngle += sweepAngle
        }
        return null
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("superState", super.onSaveInstanceState())
            putString("selectedCategory", selectedCategory)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable("superState"))
            selectedCategory = state.getString("selectedCategory", "")
        } else {
            super.onRestoreInstanceState(state)
        }
    }
}

data class ExpenseEntry(
    val id: Int,
    val name: String,
    val amount: Float,
    val category: String,
    val time: Long
)

data class PieEntry(
    val amount: Float,
    val category: String
)