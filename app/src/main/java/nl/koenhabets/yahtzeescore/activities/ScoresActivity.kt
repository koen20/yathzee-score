package nl.koenhabets.yahtzeescore.activities

import android.app.backup.BackupManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.koenhabets.yahtzeescore.*
import nl.koenhabets.yahtzeescore.data.DataManager
import nl.koenhabets.yahtzeescore.data.Game
import nl.koenhabets.yahtzeescore.databinding.ActivityScoresBinding
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class ScoresActivity : AppCompatActivity() {
    private val scoreItems: MutableList<ScoreItem> = ArrayList()
    var scoreAdapter: ScoreAdapter? = null
    var sort = 1
    var exportResult: ActivityResultLauncher<Intent>? = null
    var importResult: ActivityResultLauncher<Intent>? = null
    private lateinit var binding: ActivityScoresBinding
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoresBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        setTitle(R.string.saved_scores)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        scoreAdapter = ScoreAdapter(this, scoreItems)
        binding.listViewScore.adapter = scoreAdapter
        val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
        scoreItems.addAll(
            DataManager().loadScores(
                this,
                Game.valueOf(sharedPref.getString("game", "YahtzeeBonus")!!)
            )
        )
        scoreAdapter!!.notifyDataSetChanged()
        val context: Context = this
        binding.listViewScore.onItemLongClickListener =
            OnItemLongClickListener { adapterView: AdapterView<*>?, view: View?, i: Int, l: Long ->
                val item = scoreItems[i]
                val builder = AlertDialog.Builder(context)
                builder.setTitle(getString(R.string.remove_score))
                builder.setPositiveButton(getString(R.string.remove)) { dialog: DialogInterface?, id: Int ->
                    for (i1 in scoreItems.indices) {
                        val scoreItem = scoreItems[i1]
                        if (item.id == scoreItem.id) {
                            scoreItems.removeAt(i1)
                            break
                        }
                    }
                    val sharedPref1 = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
                    var jsonArray1 = JSONArray()
                    try {
                        jsonArray1 = JSONArray(sharedPref1.getString("scoresSaved", ""))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    for (i1 in 0 until jsonArray1.length()) {
                        try {
                            val jsonObject = jsonArray1.getJSONObject(i1)
                            if (jsonObject.getString("id") == item.id) {
                                jsonArray1.remove(i1)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    sharedPref1.edit().putString("scoresSaved", jsonArray1.toString()).apply()
                    scoreAdapter!!.notifyDataSetChanged()
                    updateAverageScore()
                    val backupManager = BackupManager(context)
                    backupManager.dataChanged()
                }
                builder.setNegativeButton("Cancel") { dialog: DialogInterface?, id: Int -> }
                builder.show()
                true
            }
        binding.listViewScore.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            val item = scoreItems[position]
            if (item.allScores.toString() != "{}") {
                val intent = Intent(applicationContext, ScoreActivity::class.java)
                intent.putExtra("data", item.allScores.toString())
                intent.putExtra("game", item.game.toString())
                startActivity(intent)
            }
        }
        updateAverageScore()
        exportResult = registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                scope.launch {
                    exportScoresMem(data)
                }
            }
        }
        importResult = registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                scope.launch {
                    importScoresMem(data)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.export_scores) {
            exportScores()
        } else if (item.itemId == R.id.import_scores) {
            importScores()
        } else if (item.itemId == R.id.change_sort) {
            if (sort == 1) {
                Collections.sort(scoreItems, ScoreComparatorDate())
                scoreAdapter!!.notifyDataSetChanged()
                sort = 2 // date
                item.setTitle(R.string.sort_by_score)
            } else if (sort == 2) {
                Collections.sort(scoreItems, ScoreComparator())
                scoreAdapter!!.notifyDataSetChanged()
                sort = 1 // highest score
                item.setTitle(R.string.sort_by_date)
            }
        } else {
            finish()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_scores, menu)
        return true
    }

    private fun exportScores() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TITLE, "yahtzee-scores.txt")
        exportResult!!.launch(intent)
    }

    private fun importScores() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        importResult!!.launch(intent)
    }

    private fun exportScoresMem(resultData: Intent?) {
        //save scores to json file on phone storage
        if (resultData != null) {
            val uri = resultData.data
            try {
                val pfd = this.contentResolver.openFileDescriptor(uri!!, "w")
                val fileOutputStream = FileOutputStream(pfd!!.fileDescriptor)
                val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
                val textContent = sharedPref.getString("scoresSaved", "[]")
                fileOutputStream.write(textContent!!.toByteArray())
                fileOutputStream.close()
                pfd.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun importScoresMem(resultData: Intent?) {
        if (resultData != null) {
            try {
                val read = readFileContent(resultData.data)
                Log.i("readFromFile", read)
                val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
                val jsonArray = JSONArray(read)
                var jsonArrayExisting = JSONArray()
                try {
                    jsonArrayExisting = JSONArray(sharedPref.getString("scoresSaved", ""))
                    Log.i("read", jsonArrayExisting.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                for (k in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(k)
                    var exists = false
                    for (i in 0 until jsonArrayExisting.length()) {
                        val jsonObjectExist = jsonArrayExisting.getJSONObject(i)
                        if (jsonObject.getString("id") == jsonObjectExist.getString("id")) {
                            exists = true
                        }
                    }
                    if (!exists) {
                        jsonArrayExisting.put(jsonObject)
                    }
                }
                sharedPref.edit().putString("scoresSaved", jsonArrayExisting.toString()).apply()
                scoreItems.clear()
                scoreItems.addAll(DataManager().loadScores(this, null))
                withContext(Dispatchers.Main) {
                    scoreAdapter!!.notifyDataSetChanged()
                }
                updateAverageScore()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    private fun readFileContent(uri: Uri?): String {
        val inputStream = contentResolver.openInputStream(uri!!)
        val reader = BufferedReader(
            InputStreamReader(
                inputStream
            )
        )
        val stringBuilder = StringBuilder()
        var currentline: String?
        while (reader.readLine().also { currentline = it } != null) {
            stringBuilder.append(currentline).append("\n")
        }
        inputStream!!.close()
        return stringBuilder.toString()
    }

    private fun updateAverageScore() {
        var total = 0.0
        var count = 0.0
        for (i in scoreItems.indices) {
            total += scoreItems[i].score
            count++
        }
        try {
            binding.textViewAverage.text = getString(
                R.string.average_d, Math.round(total / count * 10)
                    .toDouble() / 10.0
            )
            binding.textViewAmount.text = getString(R.string.total_games_played, scoreItems.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}