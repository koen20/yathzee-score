package nl.koenhabets.yahtzeescore.data

import android.content.Context

class MigrateData(context: Context) {
    //run in onStart in main activity
    init {
        val sharedPref =
            context.getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE)
        if (sharedPref.contains("scores") || sharedPref.contains("name")) {
            if (!sharedPref.contains("version")) {
                sharedPref.edit().putInt("version", 1).apply()
                sharedPref.edit().putBoolean("multiplayer", true).apply()
                sharedPref.edit().putBoolean("multiplayerAsked", true).apply()
            }
        } else {
            sharedPref.edit().putInt("version", 1).apply()
        }
    }
}