package nl.koenhabets.yahtzeescore.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import nl.koenhabets.yahtzeescore.MovingAverage
import nl.koenhabets.yahtzeescore.R
import nl.koenhabets.yahtzeescore.ScoreComparatorDate
import nl.koenhabets.yahtzeescore.ScoreItem
import nl.koenhabets.yahtzeescore.data.DataManager
import nl.koenhabets.yahtzeescore.data.Game
import nl.koenhabets.yahtzeescore.databinding.ActivityStatsBinding
import nl.koenhabets.yahtzeescore.view.ScoreView
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.roundToInt

class StatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatsBinding
    private lateinit var scoreView: ScoreView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        title = getString(R.string.stats)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
        val game = Game.valueOf(sharedPref.getString("game", "Yahtzee")!!)
        setScoreView(game)
        scoreView.setTotalVisibility(false)
        scoreView.disableEdit()

        var scoreItemsDate = DataManager().loadScores(this, game)

        val entries: MutableList<Entry> = ArrayList()
        val entriesMa: MutableList<Entry> = ArrayList()
        binding.appBarLayout.visibility = View.GONE

        val jsonObject = processScores(scoreItemsDate)
        scoreView.setScores(jsonObject)
        Collections.sort(scoreItemsDate, ScoreComparatorDate())
        scoreItemsDate = scoreItemsDate.reversed()
        var sum = 0f
        val gamesHidden: Int
        if (scoreItemsDate.size > 200) {
            gamesHidden = 30
            binding.textViewStatGraph.text = getString(R.string.games_hidden, gamesHidden)
        } else if (scoreItemsDate.size > 100) {
            gamesHidden = 10
            binding.textViewStatGraph.text = getString(R.string.games_hidden, gamesHidden)
        } else if (scoreItemsDate.size > 50) {
            gamesHidden = 5
            binding.textViewStatGraph.text = getString(R.string.games_hidden, gamesHidden)
        } else {
            gamesHidden = 0
        }
        for (d in scoreItemsDate.indices) {
            Log.i("score", Date(scoreItemsDate[d].date).toString())
            sum += scoreItemsDate[d].score
            val value = sum / (d + 1)
            if (d > gamesHidden - 1) {
                entries.add(Entry(d.toFloat(), value))
            }
        }
        val size = 15
        val movingAverage = MovingAverage(size)
        for (d in scoreItemsDate.indices) {
            movingAverage.addData(scoreItemsDate[d].score.toDouble())
            if (d > 14) {
                entriesMa.add(
                    Entry(
                        d.toFloat(), movingAverage.mean.toFloat()
                    )
                )
            }
        }
        Log.i("entries", entries.size.toString() + "size")
        val dataSet = LineDataSet(entries, getString(R.string.average_score))
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.YELLOW
        val lineData = LineData(dataSet)
        binding.statChart1.data = lineData
        binding.statChart1.description.text =
            getString(R.string.average_score_of_last, scoreItemsDate.size)
        lineChartSetFlags(binding.statChart1)
        val dataSetMa = LineDataSet(entriesMa, getString(R.string.average_score))
        dataSetMa.color = Color.BLUE
        dataSetMa.valueTextColor = Color.YELLOW
        val lineDataMa = LineData(dataSetMa)
        binding.chartMa.data = lineDataMa
        binding.chartMa.description.text = getString(R.string.average_score_of_last_ma)
        lineChartSetFlags(binding.chartMa)
        if (sharedPref.getBoolean("statsInfoDialog", true)) {
            infoDialog(false)
        }
    }

    private fun setScoreView(game: Game) {
        scoreView = ScoreView.getView(game, this)
        binding.activityStatsConstraint.addView(scoreView)
        val set = ConstraintSet()
        set.clone(binding.activityStatsConstraint)
        set.connect(scoreView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(scoreView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        set.connect(binding.statChart1.id, ConstraintSet.TOP, scoreView.id, ConstraintSet.BOTTOM)
        set.applyTo(binding.activityStatsConstraint)
    }

    private fun lineChartSetFlags(lineChart: LineChart) {
        val nightModeFlags = this.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            lineChart.xAxis.textColor = Color.WHITE
            lineChart.axisLeft.textColor = Color.WHITE
            lineChart.legend.textColor = Color.WHITE
            lineChart.description.textColor = Color.WHITE
        }
        lineChart.invalidate()
    }

    private fun infoDialog(checkboxChecked: Boolean) {
        val inflater = this.layoutInflater
        val builder = AlertDialog.Builder(this)
        val view = inflater.inflate(R.layout.dailog_stats, null)
        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        checkBox.isChecked = checkboxChecked
        builder.setView(view)
        builder.setPositiveButton("Ok") { _: DialogInterface?, _: Int -> }
        checkBox.setOnClickListener {
            val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
            sharedPref.edit().putBoolean("statsInfoDialog", !checkBox.isChecked).apply()
        }
        builder.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.help) {
            val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
            infoDialog(!sharedPref.getBoolean("statsInfoDialog", true))
        } else {
            finish()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_stats, menu)
        return true
    }

    private fun processScores(scoreItems: List<ScoreItem>): JSONObject {
        val jsonObject = JSONObject()
        for (k in 1..6) {
            try {
                jsonObject.put(k.toString() + "", processField(scoreItems, k))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        for (k in 21..29) {
            try {
                jsonObject.put(k.toString() + "", processField(scoreItems, k))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return jsonObject
    }

    private fun processField(scoreItems: List<ScoreItem>, d: Int): String {
        var totalScore = 0.0
        var scoreCount = 0.0
        var scoreCountMax = 0.0
        for (it in scoreItems) {
            try {
                val item = it.allScores
                val score = item.getString(d.toString() + "")
                if (score != "" && score != "0") {
                    val valInt = score.toInt()
                    totalScore += valInt
                    scoreCount += 1
                }
                scoreCountMax += 1
            } catch (ignored: JSONException) {
            } catch (ignored: NumberFormatException) {
            }
        }
        if (scoreCount == 0.0) {
            return "0"
        }
        val chance = (scoreCount / scoreCountMax * 100.0).roundToInt()
        val average = totalScore / scoreCountMax
        return round(average, 1).toString() + "(" + chance + ")"
    }

    companion object {
        fun round(value: Double, places: Int): Double {
            var res = 0.0
            if (value != 0.0 && !java.lang.Double.isNaN(value)) {
                require(places >= 0)
                var bd = BigDecimal(value)
                bd = bd.setScale(places, RoundingMode.HALF_UP)
                res = bd.toDouble()
            }
            return res
        }
    }
}