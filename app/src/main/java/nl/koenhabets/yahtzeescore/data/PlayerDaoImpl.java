package nl.koenhabets.yahtzeescore.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

public class PlayerDaoImpl implements PlayerDao {
    Context context;

    public PlayerDaoImpl(Context context) {
        this.context = context;
    }

    @Override
    public JSONArray getAll() {
        SharedPreferences sharedPref = context.getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = new JSONArray(sharedPref.getString("playersId", "[]"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    @Override
    public void add(String id) {
        SharedPreferences sharedPref = context.getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        JSONArray playersM = new JSONArray();
        try {
            playersM = new JSONArray(sharedPref.getString("players", "[]"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        playersM.put(id);
        sharedPref.edit().putString("players", playersM.toString()).apply();
    }
}
