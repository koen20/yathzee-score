package nl.koenhabets.yahtzeescore.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import nl.koenhabets.yahtzeescore.R
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)


        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, SettingsFragment())
                    .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = "nl.koenhabets.yahtzeescore"
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val listPreference: ListPreference? = findPreference("themePref")
            val switchPreference: SwitchPreferenceCompat? = findPreference("multiplayer")
            val sharedPref: SharedPreferences = requireContext().getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
            if (listPreference != null) {
                listPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                    val index: Int = listPreference.findIndexOfValue(newValue.toString())

                    if (index == 0) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        sharedPref.edit().putInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM).apply()
                    } else if (index == 1) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        sharedPref.edit().putInt("theme", AppCompatDelegate.MODE_NIGHT_YES).apply()
                    } else if (index == 2) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        sharedPref.edit().putInt("theme", AppCompatDelegate.MODE_NIGHT_NO).apply()
                    }

                    true
                }
            }

            /*if (switchPreference != null) {
                switchPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                    val enabled = newValue as Boolean
                    if (enabled) {
                        sharedPref.edit().putBoolean("multiplayerAsked", false).apply()
                    }
                    true
                }
            }*/
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}