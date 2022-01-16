package nl.koenhabets.yahtzeescore.activities

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import nl.koenhabets.yahtzeescore.R
import org.matomo.sdk.extra.TrackHelper
import org.json.JSONArray
import org.json.JSONException
import com.github.mikephil.charting.charts.LineChart
import java.util.ArrayList
import org.json.JSONObject
import nl.koenhabets.yahtzeescore.ScoreItem
import nl.koenhabets.yahtzeescore.data.DataManager
import nl.koenhabets.yahtzeescore.MovingAverage
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.LineData
import android.widget.CheckBox
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.util.Log
import android.view.*
import com.github.mikephil.charting.data.Entry
import java.lang.NumberFormatException
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.android.synthetic.main.activity_stats.*

class StatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        title = getString(R.string.stats)
        try {
            val tracker = MainActivity.getTracker2()
            TrackHelper.track().screen("/stats").title("Stats").with(tracker)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
        var jsonArray = JSONArray()
        try {
            jsonArray = JSONArray(sharedPref.getString("scoresSaved", ""))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val entries: MutableList<Entry> = ArrayList()
        val entriesMa: MutableList<Entry> = ArrayList()
        appBarLayout.visibility = View.GONE
        if (!sharedPref.getBoolean("yahtzeeBonus", false)) {
            textViewStatBonus.visibility = View.GONE
            editTextStat28.visibility = View.GONE
        }
        val jsonObject = processScores(jsonArray)
        readScores(jsonObject)
        val scoreItemsDate = DataManager.loadScores(this)
        scoreItemsDate.sortWith { o1: ScoreItem, o2: ScoreItem -> o1.date.compareTo(o2.date) }
        var sum = 0f
        val gamesHidden: Int
        if (scoreItemsDate.size > 200) {
            gamesHidden = 30
            textViewStatGraph.text = getString(R.string.games_hidden, gamesHidden)
        } else if (scoreItemsDate.size > 100) {
            gamesHidden = 10
            textViewStatGraph.text = getString(R.string.games_hidden, gamesHidden)
        } else if (scoreItemsDate.size > 50) {
            gamesHidden = 5
            textViewStatGraph.text = getString(R.string.games_hidden, gamesHidden)
        } else {
            gamesHidden = 0
        }
        for (d in scoreItemsDate.indices) {
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
        statChart1.data = lineData
        statChart1.description.text = getString(R.string.average_score_of_last, scoreItemsDate.size)
        lineChartSetFlags(statChart1)
        val dataSetMa = LineDataSet(entriesMa, getString(R.string.average_score))
        dataSetMa.color = Color.BLUE
        dataSetMa.valueTextColor = Color.YELLOW
        val lineDataMa = LineData(dataSetMa)
        chartMa.data = lineDataMa
        chartMa.description.text = getString(R.string.average_score_of_last_ma)
        lineChartSetFlags(chartMa)
        disableEdit()
        if (sharedPref.getBoolean("statsInfoDialog", true)) {
            infoDialog(false)
        }
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
        builder.setPositiveButton("Ok") { dialog: DialogInterface?, id: Int -> }
        checkBox.setOnClickListener { view2: View? ->
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

    private fun processScores(jsonArray: JSONArray): JSONObject {
        val jsonObject = JSONObject()
        for (k in 1..6) {
            try {
                jsonObject.put(k.toString() + "", proccessField(jsonArray, k))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        for (k in 21..28) {
            try {
                jsonObject.put(k.toString() + "", proccessField(jsonArray, k))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return jsonObject
    }

    private fun proccessField(jsonArray: JSONArray, d: Int): String {
        var totalScore = 0.0
        var scoreCount = 0.0
        var scoreCountMax = 0.0
        for (i in 0 until jsonArray.length()) {
            try {
                val jsonObject = jsonArray.getJSONObject(i).getJSONObject("allScores")
                val score = jsonObject.getString(d.toString() + "")
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
        val chance = Math.round(scoreCount / scoreCountMax * 100.0).toInt()
        val average = totalScore / scoreCountMax
        return round(average, 1).toString() + "(" + chance + ")"
    }

    private fun readScores(jsonObject: JSONObject) {
        Log.i("score", "read$jsonObject")
        try {
            editTextStat1.setText(jsonObject.getString("1"))
            editTextStat2.setText(jsonObject.getString("2"))
            editTextStat3.setText(jsonObject.getString("3"))
            editTextStat4.setText(jsonObject.getString("4"))
            editTextStat5.setText(jsonObject.getString("5"))
            editTextStat6.setText(jsonObject.getString("6"))
            editTextStat21.setText(jsonObject.getString("21"))
            editTextStat22.setText(jsonObject.getString("22"))
            editTextStat23.setText(jsonObject.getString("23"))
            editTextStat24.setText(jsonObject.getString("24"))
            editTextStat25.setText(jsonObject.getString("25"))
            editTextStat26.setText(jsonObject.getString("26"))
            editTextStat27.setText(jsonObject.getString("27"))
            editTextStat28.setText(jsonObject.getString("28"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun disableEdit() {
        editTextStat1.isEnabled = false
        editTextStat2.isEnabled = false
        editTextStat3.isEnabled = false
        editTextStat4.isEnabled = false
        editTextStat5.isEnabled = false
        editTextStat6.isEnabled = false
        editTextStat21.isEnabled = false
        editTextStat22.isEnabled = false
        editTextStat23.isEnabled = false
        editTextStat24.isEnabled = false
        editTextStat25.isEnabled = false
        editTextStat26.isEnabled = false
        editTextStat27.isEnabled = false
        editTextStat28.isEnabled = false
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