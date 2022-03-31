package nl.koenhabets.yahtzeescore.data

import org.json.JSONObject
import org.json.JSONArray
import org.json.JSONException
import android.app.backup.BackupManager
import android.content.Context
import android.util.Log
import nl.koenhabets.yahtzeescore.ScoreItem
import nl.koenhabets.yahtzeescore.ScoreComparator
import java.util.*

class DataManager {
    // save the game to shared preferences
    fun saveScore(score: Int, jsonObjectScores: JSONObject, context: Context, game: Game) {
        val sharedPref =
            context.getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE)
        var jsonArray = JSONArray()
        try {
            jsonArray = JSONArray(sharedPref.getString("scoresSaved", ""))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val jsonObject = JSONObject()
        try {
            Log.i("scooree", score.toString() + "")
            jsonObject.put("score", score)
            jsonObject.put("date", Date().time)
            jsonObject.put("id", UUID.randomUUID().toString())
            jsonObject.put("allScores", jsonObjectScores)
            jsonObject.put("game", game)
            jsonArray.put(jsonObject)
            sharedPref.edit().putString("scoresSaved", jsonArray.toString()).apply()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val backupManager = BackupManager(context)
        backupManager.dataChanged()
    }

    //save the current score sheet to shared preferences.
    fun saveScores(jsonObject: JSONObject, context: Context, game: Game) {
        Log.i("score", "saving")
        val sharedPref =
            context.getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE)
        Log.i("saving", jsonObject.toString())
        sharedPref.edit().putString("scores-$game", jsonObject.toString()).apply()
    }

    //get all scores from sharedprefrences, and sort them descending by score
    fun loadScores(context: Context, gameFilter: Game?): List<ScoreItem> {
        val scoreItems: MutableList<ScoreItem> = ArrayList()
        val sharedPref =
            context.getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE)
        var jsonArray = JSONArray()
        try {
            jsonArray = JSONArray(sharedPref.getString("scoresSaved", ""))
            Log.i("read", jsonArray.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        for (i in jsonArray.length() - 1 downTo 0) {
            try {
                val jsonObject = jsonArray.getJSONObject(i)
                var allScores: JSONObject? = JSONObject()
                try {
                    allScores = jsonObject.getJSONObject("allScores")
                } catch (ignored: JSONException) {
                }
                var game = Game.YahtzeeBonus
                var filterEnabled = false
                if (jsonObject.has("game")) {
                    game = Game.valueOf(jsonObject.getString("game"))
                    filterEnabled = true
                } else {
                    if (jsonObject.has("yahtzeeBonus")) {
                        if (!jsonObject.getBoolean("yahtzeeBonus")) {
                            game = Game.Yahtzee
                        }
                    }
                }
                if (gameFilter == null) {
                    filterEnabled = false
                } else if (gameFilter === Game.Yatzy) {
                    filterEnabled = true
                }
                if (!filterEnabled || game === gameFilter) {
                    val scoreItem = ScoreItem(
                        jsonObject.getInt("score"), jsonObject.getLong("date"),
                        jsonObject.getString("id"), game, allScores!!
                    )
                    scoreItems.add(scoreItem)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        Collections.sort(scoreItems, ScoreComparator())
        return scoreItems
    }
}