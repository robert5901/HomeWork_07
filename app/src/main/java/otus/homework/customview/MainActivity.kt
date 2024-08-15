package otus.homework.customview

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChart = findViewById<PieChartView>(R.id.pie_chart)

        val jsonString = loadJSONFromAsset(this, R.raw.payload)
        val expenseEntries = parseJSONToExpenseEntries(jsonString)

        pieChart.setPieData(expenseEntries)

        pieChart.onSliceClickListener = { category ->
            Toast.makeText(this, "Clicked on: $category", Toast.LENGTH_SHORT).show()
        }
    }

    // Чтение файла из ресурсов
    fun loadJSONFromAsset(context: Context, resourceId: Int): String {
        return context.resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
    }

    // Преобразование JSON в список объектов ExpenseEntry
    fun parseJSONToExpenseEntries(jsonString: String): List<ExpenseEntry> {
        val gson = Gson()
        val listType = object : TypeToken<List<ExpenseEntry>>() {}.type
        return gson.fromJson(jsonString, listType)
    }
}