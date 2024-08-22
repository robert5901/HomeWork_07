package otus.homework.customview

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.animation.addListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {
    private lateinit var pieChart: PieChartView
    private lateinit var detailCategoryChart: DetailCategoryChartView
    private lateinit var switchChartButton: Button
    private lateinit var motionLayout: MotionLayout

    private var isPieChartVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pieChart = findViewById(R.id.pie_chart)
        switchChartButton = findViewById(R.id.switch_chart_btn)
        detailCategoryChart = findViewById(R.id.detail_category_chart)
        motionLayout = findViewById(R.id.motion_layout)

        motionLayout.post {
            motionLayout.transitionToEnd()
        }

        switchChartButton.setOnClickListener {
            if (isPieChartVisible) {
                flipAnimation(pieChart, detailCategoryChart)
                isPieChartVisible = false
            } else {
                flipAnimation(detailCategoryChart, pieChart)
                isPieChartVisible = true
            }
        }

        configurePieChart()
    }

    private fun flipAnimation(fromView: View, toView: View) {
        val fromViewAnimatorScale = ValueAnimator.ofFloat(1f, 0.5f).apply {
            duration = 500L
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                fromView.scaleX = it.animatedValue as Float
                fromView.scaleY = it.animatedValue as Float
            }
        }

        val fromViewAnimatorRotationY =
            ObjectAnimator.ofFloat(fromView, "rotationY", 0f, 180f).apply {
                duration = 1000L
            }

        val fromViewAnimatorAlpha = ObjectAnimator.ofFloat(fromView, "alpha", 1f, 0f).apply {
            startDelay = 500L
            duration = 1L
        }

        val toViewAnimatorScale = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1200L
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                toView.scaleX = it.animatedValue as Float
                toView.scaleY = it.animatedValue as Float
            }
        }

        val toViewAnimatorRotationY =
            ObjectAnimator.ofFloat(toView, "rotationY", -180f, 0f).apply {
                duration = 1000L
                addListener(
                    onStart = {
                        toView.visibility = View.VISIBLE
                        toView.alpha = 0F
                    },
                    onEnd = {
                        fromView.visibility = View.INVISIBLE
                    }
                )
            }

        val toViewAnimatorAlpha = ObjectAnimator.ofFloat(toView, "alpha", 0f, 1f).apply {
            startDelay = 500L
            duration = 1L
        }

        val animatorSet = AnimatorSet().apply {
            playTogether(
                fromViewAnimatorScale,
                fromViewAnimatorRotationY,
                fromViewAnimatorAlpha,
                toViewAnimatorScale,
                toViewAnimatorRotationY,
                toViewAnimatorAlpha
            )
        }

        animatorSet.start()
    }

    private fun configurePieChart() {
        val jsonString = loadJSONFromAsset(this, R.raw.payload)
        val expenseEntries = parseJSONToExpenseEntries(jsonString)

        pieChart.setPieData(expenseEntries)

        pieChart.onSliceClickListener = { category ->
            detailCategoryChart.setExpenses(expenseEntries, category)

            flipAnimation(pieChart, detailCategoryChart)
            isPieChartVisible = false
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