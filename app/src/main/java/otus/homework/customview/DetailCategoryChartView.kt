package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetailCategoryChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint()

    private var expenses: List<ExpenseEntry> = emptyList()
    private var selectedCategory: String = ""

    private val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    private val amountFormat = DecimalFormat("#,###")

    fun setExpenses(expenses: List<ExpenseEntry>, category: String) {
        this.expenses = expenses.filter { it.category == category }
        selectedCategory = category
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()
        val padding = 50f
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding

        // Отрисовка осей
        paint.color = Color.BLACK
        paint.strokeWidth = 5f
        canvas.drawLine(padding, padding, padding, height - padding, paint) // ось Y
        canvas.drawLine(padding, height - padding, width - padding, height - padding, paint) // ось X

        val maxAmount = expenses.maxOfOrNull { it.amount } ?: 0f
        if (maxAmount == 0f) return

        // Paint для точек на графике и соеденяющей линии
        paint.strokeWidth = 3f
        paint.color = Color.BLUE
        paint.style = Paint.Style.STROKE

        val groupedByDate = expenses.groupBy {
            val calendar = Calendar.getInstance().apply { timeInMillis = it.time * 1000 }
            dateFormat.format(calendar.time)
        }

        val sortedDates = groupedByDate.keys.sorted()
        if (sortedDates.isEmpty()) return

        val dateToIndex = sortedDates.withIndex().associate { it.value to it.index }

        val pointWidth = chartWidth / sortedDates.size
        val pointHeightScale = chartHeight / maxAmount

        var prevX: Float? = null
        var prevY: Float? = null

        for ((date, entries) in groupedByDate) {
            val dateIndex = dateToIndex[date] ?: continue
            val x = padding + dateIndex * pointWidth + pointWidth / 2
            val totalAmount = entries.sumOf { it.amount.toDouble() }.toFloat()
            val y = height - padding - (totalAmount * pointHeightScale)

            // Отрисовка линии до предыдущей точки
            if (prevX != null && prevY != null) {
                canvas.drawLine(prevX, prevY, x, y, paint)
            }

            // Отрисовка текущей точки
            canvas.drawCircle(x, y, 5f, paint)

            prevX = x
            prevY = y
        }

        // Отрисовка дат по оси Х
        paint.textSize = 30f
        paint.color = Color.BLACK
        val labelPadding = 40f
        sortedDates.forEachIndexed { index, date ->
            val x = padding + index * pointWidth + pointWidth / 2
            canvas.drawText(date, x - (paint.measureText(date) / 2), height - padding + labelPadding, paint)
        }

        // отрисовка сумм по оси Y
        val yAxisStep = maxAmount / 5 // Кол-во делений по оси Y
        val yStepHeight = chartHeight / 5 // Разделение высоты графика на 5 шагов

        (0..5).forEach { i ->
            val amountLabel = (yAxisStep * i).toInt()
            val y = height - padding - (i * yStepHeight)
            canvas.drawLine(padding - 10f, y, padding, y, paint)
            canvas.drawText(amountFormat.format(amountLabel), padding - 50f, y + 10f, paint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val desiredWidth = 800
        val desiredHeight = 800

        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(desiredWidth, widthSize)
            MeasureSpec.UNSPECIFIED -> desiredWidth
            else -> desiredWidth
        }

        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            MeasureSpec.UNSPECIFIED -> desiredHeight
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
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