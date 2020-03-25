package nl.koenhabets.yahtzeescore;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

public class DataManager {
    public static void saveScore(int score, JSONObject jsonObjectScores, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = new JSONArray(sharedPref.getString("scoresSaved", ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject();
        try {
            Log.i("scooree", score + "");
            jsonObject.put("score", score);
            jsonObject.put("date", new Date().getTime());
            jsonObject.put("id", UUID.randomUUID().toString());
            jsonObject.put("allScores", jsonObjectScores);
            jsonObject.put("yahtzeeBonus", sharedPref.getBoolean("yahtzeeBonus", false));
            jsonArray.put(jsonObject);
            sharedPref.edit().putString("scoresSaved", jsonArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        BackupManager backupManager = new BackupManager(context);
        backupManager.dataChanged();
    }

    //read the current scores from editText and save it to sharedpreferences.
    public static void saveScores(JSONObject jsonObject, Context context) {
        Log.i("score", "saving");
        SharedPreferences sharedPref = context.getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        Log.i("saving", jsonObject.toString());
        sharedPref.edit().putString("scores", jsonObject.toString()).apply();
    }
}
