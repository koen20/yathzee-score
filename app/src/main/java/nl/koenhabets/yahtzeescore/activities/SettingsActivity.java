package nl.koenhabets.yahtzeescore.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.matomo.sdk.Tracker;
import org.matomo.sdk.extra.TrackHelper;

import nl.koenhabets.yahtzeescore.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            Tracker tracker = MainActivity.getTracker2();
            TrackHelper.track().screen("/settings").title("Settings").with(tracker);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button buttonName = findViewById(R.id.buttonName);
        Button buttonTheme = findViewById(R.id.buttonTheme);
        Button buttonLicense = findViewById(R.id.buttonLicense);
        Switch switch1 = findViewById(R.id.switch1);
        Switch switchMultiplayer = findViewById(R.id.switch2);

        SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        boolean yahtzeeBonus = sharedPref.getBoolean("yahtzeeBonus", false);
        switch1.setChecked(yahtzeeBonus);
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        switch1.setOnClickListener(view -> {
            sharedPref.edit().putBoolean("yahtzeeBonus", switch1.isChecked()).apply();
            mFirebaseAnalytics.setUserProperty("yahtzeeBonus", switch1.isChecked() + "");
        });
        switchMultiplayer.setChecked(sharedPref.getBoolean("multiplayer", true));
        switchMultiplayer.setOnClickListener(view -> {
            if (switchMultiplayer.isChecked()) {
                sharedPref.edit().putBoolean("multiplayerAsked", false).apply();
            } else {
                sharedPref.edit().putBoolean("multiplayer", false).apply();
            }
        });

        buttonName.setOnClickListener(view -> {
            final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            LayoutInflater inflater = this.getLayoutInflater();

            View view2 = inflater.inflate(R.layout.dialog_name, null);
            final EditText editTextName = view2.findViewById(R.id.editText2);
            SharedPreferences sharedPref2 = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
            editTextName.setText(sharedPref2.getString("name", ""));
            builder.setView(view2);
            builder.setMessage(getString(R.string.name_message));
            builder.setPositiveButton("Ok", (dialog, id) -> {
                SharedPreferences sharedPref3 = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
                sharedPref3.edit().putString("name", editTextName.getText().toString()).apply();
                MainActivity.name = editTextName.getText().toString();
                Tracker tracker = MainActivity.getTracker2();
                TrackHelper.track().event("category", "action").name("name changed").with(tracker);
            });
            builder.show();
        });

        buttonLicense.setOnClickListener(view -> {
            startActivity(new Intent(this, OssLicensesMenuActivity.class));
        });

        buttonTheme.setOnClickListener(view -> darkModeDialog());
    }

    private void darkModeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Theme");
        SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        builder.setNegativeButton(getString(R.string.dark), (dialog, id) -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            sharedPref.edit().putInt("theme", AppCompatDelegate.MODE_NIGHT_YES).apply();
        });
        builder.setPositiveButton(getString(R.string.light), (dialog, id) -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            sharedPref.edit().putInt("theme", AppCompatDelegate.MODE_NIGHT_NO).apply();

        });
        builder.setNeutralButton(getString(R.string.system_default), (dialogInterface, i) -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            sharedPref.edit().putInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM).apply();
        });
        builder.show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
