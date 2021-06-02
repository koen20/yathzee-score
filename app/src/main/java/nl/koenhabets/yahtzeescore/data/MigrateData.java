package nl.koenhabets.yahtzeescore.data;

import android.content.Context;
import android.content.SharedPreferences;

public class MigrateData {
    //run in onStart in main activity
    public MigrateData(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        if (sharedPref.contains("scores") || sharedPref.contains("name")) {
            if (!sharedPref.contains("version")) {
                sharedPref.edit().putInt("version", 1).apply();
                sharedPref.edit().putBoolean("multiplayer", true).apply();
                sharedPref.edit().putBoolean("multiplayerAsked", true).apply();
            }
        } else {
            sharedPref.edit().putInt("version", 1).apply();
        }
    }
}
