package nl.koenhabets.yahtzeescore.activities

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import nl.koenhabets.yahtzeescore.R
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
import nl.koenhabets.yahtzeescore.databinding.ActivityStatsBinding
import java.lang.NumberFormatException
import java.math.BigDecimal
import java.math.RoundingMode

class StatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        title = getString(R.string.stats)

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
        binding.appBarLayout.visibility = View.GONE
        if (!sharedPref.getBoolean("yahtzeeBonus", false)) {
            binding.textViewStatBonus.visibility = View.GONE
            binding.editTextStat28.visibility = View.GONE
        }
        val jsonObject = processScores(jsonArray)
        readScores(jsonObject)
        val scoreItemsDate = DataManager().loadScores(this)
        scoreItemsDate.sortWith { o1: ScoreItem, o2: ScoreItem -> o1.date.compareTo(o2.date) }
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
            binding.editTextStat1.setText(jsonObject.getString("1"))
            binding.editTextStat2.setText(jsonObject.getString("2"))
            binding.editTextStat3.setText(jsonObject.getString("3"))
            binding.editTextStat4.setText(jsonObject.getString("4"))
            binding.editTextStat5.setText(jsonObject.getString("5"))
            binding.editTextStat6.setText(jsonObject.getString("6"))
            binding.editTextStat21.setText(jsonObject.getString("21"))
            binding.editTextStat22.setText(jsonObject.getString("22"))
            binding.editTextStat23.setText(jsonObject.getString("23"))
            binding.editTextStat24.setText(jsonObject.getString("24"))
            binding.editTextStat25.setText(jsonObject.getString("25"))
            binding.editTextStat26.setText(jsonObject.getString("26"))
            binding.editTextStat27.setText(jsonObject.getString("27"))
            binding.editTextStat28.setText(jsonObject.getString("28"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun disableEdit() {
        binding.editTextStat1.isEnabled = false
        binding.editTextStat2.isEnabled = false
        binding.editTextStat3.isEnabled = false
        binding.editTextStat4.isEnabled = false
        binding.editTextStat5.isEnabled = false
        binding.editTextStat6.isEnabled = false
        binding.editTextStat21.isEnabled = false
        binding.editTextStat22.isEnabled = false
        binding.editTextStat23.isEnabled = false
        binding.editTextStat24.isEnabled = false
        binding.editTextStat25.isEnabled = false
        binding.editTextStat26.isEnabled = false
        binding.editTextStat27.isEnabled = false
        binding.editTextStat28.isEnabled = false
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