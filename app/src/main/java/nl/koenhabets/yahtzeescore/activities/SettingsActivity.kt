package nl.koenhabets.yahtzeescore.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import nl.koenhabets.yahtzeescore.R
import nl.koenhabets.yahtzeescore.data.SubscriptionRepository
import nl.koenhabets.yahtzeescore.dialog.SubscriptionsDialog


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
            val subscriptionRepository = SubscriptionRepository(requireContext())
            preferenceManager.sharedPreferencesName = "nl.koenhabets.yahtzeescore"
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val listPreference: ListPreference? = findPreference("themePref")
            val settingsLicenses: Preference? = findPreference("settingsLicenses")
            val subscriptionsPreference: Preference? = findPreference("subscriptions")
            val sharedPref: SharedPreferences =
                requireContext().getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
            if (listPreference != null) {
                listPreference.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { preference, newValue ->
                        val index: Int = listPreference.findIndexOfValue(newValue.toString())

                        if (index == 0) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            sharedPref.edit()
                                .putInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM).apply()
                        } else if (index == 1) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            sharedPref.edit().putInt("theme", AppCompatDelegate.MODE_NIGHT_YES)
                                .apply()
                        } else if (index == 2) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            sharedPref.edit().putInt("theme", AppCompatDelegate.MODE_NIGHT_NO)
                                .apply()
                        }

                        true
                    }
            }

            settingsLicenses?.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { //code for what you want it to do
                    startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                    true
                }

            subscriptionsPreference?.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { //code for what you want it to do
                    context?.let { it1 ->
                        SubscriptionsDialog(
                            it1,
                            subscriptionRepository
                        ).showDialog()
                    }
                    true
                }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}