package nl.koenhabets.yahtzeescore.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import nl.koenhabets.yahtzeescore.model.PlayerItem;

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
            jsonArray = new JSONArray(sharedPref.getString("playersIdv8", "[]"));
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
        Log.i("PlayerDao", "Adding player: " + item.getName());
        List<PlayerItem> playerItems = getAll();
        boolean exists = false;
        for (int i = 0; i < playerItems.size(); i++) {
            if (playerItems.get(i).getId() != null) {
                if (playerItems.get(i).getId().equals(item.getId())) {
                    playerItems.set(i, item);
                    exists = true;
                }
            }
            if (playerItems.get(i).getName() != null) {
                if (playerItems.get(i).getName().equals(item.getName())) {
                    playerItems.set(i, item);
                    exists = true;
                }
            }
        }

        if (!exists) {
            if (item.getName() != null || item.getId() != null) {
                playerItems.add(item);
            }
        } else {
            Log.i("PlayerDao", "Exists updated");
        }

        Gson gson = new Gson();
        SharedPreferences sharedPref = context.getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        sharedPref.edit().putString("playersIdv8", gson.toJson(playerItems)).apply();
    }
}
