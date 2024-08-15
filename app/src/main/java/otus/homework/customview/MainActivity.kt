package otus.homework.customview

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.view.isVisible
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {
    private lateinit var pieChart: PieChartView
    private lateinit var detailCategoryChart: DetailCategoryChartView
    private lateinit var switchChartButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pieChart = findViewById(R.id.pie_chart)
        switchChartButton = findViewById(R.id.switch_chart_btn)
        detailCategoryChart = findViewById(R.id.detail_category_chart)

        switchChartButton.setOnClickListener {
            if (pieChart.isVisible) {
                pieChart.isVisible = false
                detailCategoryChart.isVisible = true
            } else {
                pieChart.isVisible = true
                detailCategoryChart.isVisible = false
            }
        }

        configurePieChart()
    }

    private fun configurePieChart() {
        val jsonString = loadJSONFromAsset(this, R.raw.payload)
        val expenseEntries = parseJSONToExpenseEntries(jsonString)

        pieChart.setPieData(expenseEntries)

        pieChart.onSliceClickListener = { category ->
            detailCategoryChart.setExpenses(expenseEntries, category)

            pieChart.isVisible = false
            detailCategoryChart.isVisible = true
        }
    }

    // Чтение файла из ресурсов
    private fun loadJSONFromAsset(context: Context, resourceId: Int): String {
        return context.resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
    }

    // Преобразование JSON в список объектов ExpenseEntry
    private fun parseJSONToExpenseEntries(jsonString: String): List<ExpenseEntry> {
        val gson = Gson()
        val listType = object : TypeToken<List<ExpenseEntry>>() {}.type
        return gson.fromJson(jsonString, listType)
    }
}