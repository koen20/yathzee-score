package nl.koenhabets.yahtzeescore.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import nl.koenhabets.yahtzeescore.multiplayer.PlayerItem;

public class PlayerDaoImpl implements PlayerDao {
    Context context;

    public PlayerDaoImpl(Context context) {
        this.context = context;
    }

    @Override
    public List<PlayerItem> getAll() {
        SharedPreferences sharedPref = context.getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = new JSONArray(sharedPref.getString("playersIdv2", "[]"));
            Log.i("players", "Local players read: " + jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<PlayerItem> playerItems = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Gson gson = new Gson();
            try {
                playerItems.add(gson.fromJson(jsonArray.getString(i), PlayerItem.class));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return playerItems;
    }

    @Override
    public void add(PlayerItem item) {
        SharedPreferences sharedPref = context.getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        JSONArray playersM = new JSONArray();
        try {
            playersM = new JSONArray(sharedPref.getString("playersIdv2", "[]"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        playersM.put(gson.toJson(item));
        sharedPref.edit().putString("playersIdv2", playersM.toString()).apply();
    }
}
