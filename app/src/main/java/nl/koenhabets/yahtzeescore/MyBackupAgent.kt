package nl.koenhabets.yahtzeescore

import android.app.backup.BackupAgentHelper
import android.app.backup.SharedPreferencesBackupHelper

class MyBackupAgent : BackupAgentHelper() {
    override fun onCreate() {
        val helper = SharedPreferencesBackupHelper(this, PREFS)
        addHelper("scoresSaved", helper)
    }

    companion object {
        // The name of the SharedPreferences file
        const val PREFS = "nl.koenhabets.yahtzeescore"
    }
}