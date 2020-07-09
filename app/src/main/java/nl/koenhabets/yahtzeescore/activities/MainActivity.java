package nl.koenhabets.yahtzeescore.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.matomo.sdk.Matomo;
import org.matomo.sdk.Tracker;
import org.matomo.sdk.TrackerBuilder;
import org.matomo.sdk.extra.TrackHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import nl.koenhabets.yahtzeescore.DataManager;
import nl.koenhabets.yahtzeescore.Mqtt;
import nl.koenhabets.yahtzeescore.PlayerItem;
import nl.koenhabets.yahtzeescore.R;

public class MainActivity extends AppCompatActivity implements TextWatcher, GoogleApiClient.OnConnectionFailedListener, OnFailureListener {
    private EditText editText1;
    private EditText editText2;
    private EditText editText3;
    private EditText editText4;
    private EditText editText5;
    private EditText editText6;
    private EditText editText21;
    private EditText editText22;
    private EditText editText23;
    private EditText editText24;
    private EditText editText25;
    private EditText editText26;
    private EditText editText27;
    private EditText editText28;
    private TextView tvTotalLeft;
    private TextView tvTotalRight;
    private TextView tvTotal;
    private TextView tvBonus;
    private static TextView tvOp;
    private TextView tvYahtzeeBonus;
    private Button button;

    private MessageListener mMessageListener;
    private Message mMessage;
    public static String name = "";

    private int totalLeft = 0;
    private int totalRight = 0;
    private static List<PlayerItem> players = new ArrayList<>();
    Timer updateTimer;
    private int updateInterval = 10000;
    private JSONArray playersM = new JSONArray();

    static boolean multiplayer;
    static boolean playersNearby = false;
    private FirebaseUser firebaseUser;
    private DatabaseReference database;

    private static Tracker mMatomoTracker;
    private FirebaseAuth mAuth;
    private Boolean realtimeDatabaseEnabled = true;

    public synchronized Tracker getTracker() {
        if (mMatomoTracker != null) return mMatomoTracker;
        mMatomoTracker = TrackerBuilder.createDefault("https://analytics.koenhabets.nl/matomo.php", 6).build(Matomo.getInstance(this));
        return mMatomoTracker;
    }

    public static Tracker getTracker2() {
        return mMatomoTracker;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        AppCompatDelegate.setDefaultNightMode(sharedPref.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));

        int nightModeFlags =
                this.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#121212")));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = this.getWindow();
                window.setStatusBarColor(Color.parseColor("#121212"));
            }
        }

        try {
            FirebaseAnalytics mFirebaseAnalytics;
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

            String testLabSetting = Settings.System.getString(getContentResolver(), "firebase.test.lab");
            if ("true".equals(testLabSetting)) {
                mFirebaseAnalytics.setAnalyticsCollectionEnabled(false);  //Disable Analytics Collection
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getTracker();
        Tracker tracker = mMatomoTracker;
        TrackHelper.track().screen("/").title("Main screen").with(tracker);
        TrackHelper.track().download().with(tracker);


        editText1 = findViewById(R.id.editText);
        editText2 = findViewById(R.id.editText3);
        editText3 = findViewById(R.id.editText4);
        editText4 = findViewById(R.id.editText5);
        editText5 = findViewById(R.id.editText6);
        editText6 = findViewById(R.id.editText7);

        editText21 = findViewById(R.id.editText9);
        editText22 = findViewById(R.id.editText10);
        editText23 = findViewById(R.id.editText8);
        editText24 = findViewById(R.id.editText11);
        editText25 = findViewById(R.id.editText12);
        editText26 = findViewById(R.id.editText13);
        editText27 = findViewById(R.id.editText14);
        editText28 = findViewById(R.id.editText16);

        button = findViewById(R.id.button);

        tvTotalLeft = findViewById(R.id.textViewTotalLeft);
        tvTotalRight = findViewById(R.id.textViewTotalRight);
        tvTotal = findViewById(R.id.textViewTotal);
        tvBonus = findViewById(R.id.textViewBonus);
        tvOp = findViewById(R.id.textViewOp);
        tvYahtzeeBonus = findViewById(R.id.textView7);

        try {
            readScores(new JSONObject(sharedPref.getString("scores", "")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        editText1.addTextChangedListener(this);
        editText2.addTextChangedListener(this);
        editText3.addTextChangedListener(this);
        editText4.addTextChangedListener(this);
        editText5.addTextChangedListener(this);
        editText6.addTextChangedListener(this);
        editText21.addTextChangedListener(this);
        editText22.addTextChangedListener(this);
        editText23.addTextChangedListener(this);
        editText24.addTextChangedListener(this);
        editText25.addTextChangedListener(this);
        editText26.addTextChangedListener(this);
        editText27.addTextChangedListener(this);
        editText28.addTextChangedListener(this);

        calculateTotal();

        editText23.setOnClickListener(view -> {
            setDefaultValue(editText23, 25);

        });
        editText24.setOnClickListener(view -> {
            setDefaultValue(editText24, 30);

        });
        editText25.setOnClickListener(view -> {
            setDefaultValue(editText25, 40);
        });
        editText26.setOnClickListener(view -> setDefaultValue(editText26, 50));

        final Context context = this;
        button.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = this.getLayoutInflater();
            View view2 = inflater.inflate(R.layout.dialog_clear, null);
            builder.setView(view2);
            CheckBox checkBoxSave = view2.findViewById(R.id.checkBoxSave);
            builder.setTitle("Clear all");
            builder.setNegativeButton("Cancel", (dialog, id) -> {
            });
            builder.setPositiveButton("Clear", (dialog, id) -> {
                if (checkBoxSave.isChecked()) {
                    if((totalLeft + totalRight) < 5){
                        Toast toast = Toast.makeText(this, R.string.score_too_low_save, Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        DataManager.saveScore(totalLeft + totalRight, createJsonScores(), getApplicationContext());
                        TrackHelper.track().event("category", "action").name("clear and save").with(mMatomoTracker);
                    }
                } else {
                    TrackHelper.track().event("category", "action").name("clear").with(mMatomoTracker);
                }
                clearText();
            });
            builder.show();
        });
    }

    private void setDefaultValue(EditText editTextD, int value) {
        int number = -1;
        try {
            number = Integer.parseInt(editTextD.getText().toString());
        } catch (NumberFormatException ignored) {
        }
        if (number == value) {
            editTextD.setText(0 + "");
        } else if (number == 0) {
            editTextD.setText("");
        } else {
            editTextD.setText(value + "");
        }

        SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        if (!sharedPref.getBoolean("fieldHintShown", false)) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.press_again), Snackbar.LENGTH_LONG).show();
            sharedPref.edit().putBoolean("fieldHintShown", true).apply();
        }
    }

    private void permissionDialog() {
        Context context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Multiplayer");
        builder.setMessage(getString(R.string.multiplayer_permission_dialog));
        builder.setNegativeButton("No", (dialog, id) -> {
            TrackHelper.track().event("multiplayer", "disable").name("disable").with(mMatomoTracker);
            SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
            sharedPref.edit().putBoolean("multiplayer", false).apply();
            sharedPref.edit().putBoolean("multiplayerAsked", true).apply();
        });
        builder.setPositiveButton("Yes", (dialog, id) -> {
            tvOp.setText(R.string.No_players_nearby);
            TrackHelper.track().event("multiplayer", "enable").name("enable").with(mMatomoTracker);
            SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
            sharedPref.edit().putBoolean("multiplayer", true).apply();
            sharedPref.edit().putBoolean("multiplayerAsked", true).apply();
            initMultiplayer();
        });
        builder.show();
    }

    private void initMultiplayer() {
        multiplayer = true;
        mMessage = new Message(("new player").getBytes());
        if (realtimeDatabaseEnabled) {
            firebaseUser = mAuth.getCurrentUser();
            if (firebaseUser == null) {
                mAuth.signInAnonymously()
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                Log.d("MainActivity", "signInAnonymously:success");
                                firebaseUser = mAuth.getCurrentUser();
                            } else {
                                Log.w("MainActivity", "signInAnonymously:failure", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                Log.d("t", "Found message: " + new String(message.getContent()));
                proccessMessage(new String(message.getContent()), false);
            }

            @Override
            public void onLost(Message message) {
                Log.d("d", "Lost sight of message: " + new String(message.getContent()));
            }
        };

        Nearby.getMessagesClient(this).publish(mMessage).addOnFailureListener(this);
        Nearby.getMessagesClient(this).subscribe(mMessageListener);
        try {
            Mqtt.connectMqtt(name, this);
            updateTimer = new Timer();
            updateTimer.scheduleAtFixedRate(new updateTask(), 6000, updateInterval);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);

        Log.i("name", sharedPref.getString("name", ""));
        if (sharedPref.getString("name", "").equals("")) {
            nameDialog(this);
        } else {
            name = sharedPref.getString("name", "");
        }

        try {
            playersM = new JSONArray(sharedPref.getString("players", ""));
            for (int i = 0; i < playersM.length(); i++) {
                boolean exists = false;
                for (int k = 0; k < players.size(); k++) {
                    PlayerItem item = players.get(k);
                    if (item.getName().equals(playersM.getString(i))) {
                        exists = true;
                    }
                }
                if (!exists) {
                    PlayerItem playerItem = new PlayerItem(playersM.getString(i), 0, 0, false);
                    players.add(playerItem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvOp.setMovementMethod(new ScrollingMovementMethod());
        tvOp.setOnClickListener(view -> addPlayerDialog());
        Log.i("players", playersM.toString() + "");
        calculateTotal();

        if (realtimeDatabaseEnabled) {
            database.child("score").addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.i("Firebase Received", dataSnapshot.getValue().toString());
                    try {
                        proccessMessage(dataSnapshot.getValue().toString(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("EditTagsActivity", "Failed to read scores.", error.toException());
                }
            });
        }
    }


    private void addPlayerDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_name, null);
        final EditText editTextName = view.findViewById(R.id.editText2);
        builder.setView(view);
        builder.setMessage("Add player");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                playersM.put(editTextName.getText().toString());
                SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
                sharedPref.edit().putString("players", playersM.toString()).apply();
                PlayerItem playerItem = new PlayerItem(editTextName.getText().toString(), 0, 0, true);
                players.add(playerItem);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, id) -> {
        });
        builder.show();
    }

    public static void proccessMessage(String message, boolean mqtt) {
        try {
            if (!message.equals("new player")) {
                String[] messageSplit = message.split(";");
                boolean exists = false;
                if (!messageSplit[0].equals(name) && !messageSplit[0].equals("")) {
                    for (int i = 0; i < players.size(); i++) {
                        PlayerItem playerItem = players.get(i);
                        if (playerItem.getName().equals(messageSplit[0])) {
                            exists = true;
                            if (playerItem.getLastUpdate() < Long.parseLong(messageSplit[2]) && mqtt) {
                                Log.i("message", "newer message");
                                players.remove(i);
                                PlayerItem item = new PlayerItem(messageSplit[0], Integer.parseInt(messageSplit[1]), Long.parseLong(messageSplit[2]), true);
                                players.add(item);
                                updateMultiplayerText();
                                break;
                            }
                        }
                    }
                    if (!exists && !mqtt) {
                        Log.i("New player", messageSplit[0]);
                        PlayerItem item = new PlayerItem(messageSplit[0], Integer.parseInt(messageSplit[1]), Long.parseLong(messageSplit[2]), true);
                        players.add(item);
                        updateMultiplayerText();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateMultiplayerText() {
        String text = "Nearby: " + "<br>";
        Collections.sort(players);
        for (int i = 0; i < players.size(); i++) {
            PlayerItem playerItem = players.get(i);
            if (playerItem.isVisible()) {
                if (playerItem.getName().equals(name)) {
                    text = text + "<b>" + playerItem.getName() + ": " + playerItem.getScore() + "</b><br>";
                } else {
                    text = text + playerItem.getName() + ": " + playerItem.getScore() + "<br>";
                }
            }
        }
        if (players.size() != 0) {
            text = text.substring(0, (text.length() - 1));
            tvOp.setText(Html.fromHtml(text));
            playersNearby = true;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("error", connectionResult.getErrorMessage());
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        e.printStackTrace();
    }

    private class updateTask extends TimerTask {
        @Override
        public void run() {
            updateNearbyScore();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.privacy_policy:
                openUrl("https://koenhabets.nl/privacy_policy.html");
                return true;
            case R.id.scores2:
                Intent myIntent = new Intent(this, ScoresActivity.class);
                this.startActivity(myIntent);
                return true;
            case R.id.settings2:
                Intent myIntent2 = new Intent(this, SettingsActivity.class);
                this.startActivity(myIntent2);
                return true;
            case R.id.stats:
                Intent myIntent3 = new Intent(this, StatsActivity.class);
                this.startActivity(myIntent3);
                return true;
            case R.id.rules:
                String language = Locale.getDefault().getLanguage();
                if (language.equals("nl")) {
                    openUrl("https://nl.wikipedia.org/wiki/Yahtzee#Spelverloop");
                } else if (language.equals("fr")) {
                    openUrl("https://fr.wikipedia.org/wiki/Yahtzee");
                } else {
                    openUrl("https://en.wikipedia.org/wiki/Yahtzee#Rules");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void openUrl(String url) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        } catch (ActivityNotFoundException exception) {
            Toast toast = Toast.makeText(this, R.string.browser_fail, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void updateNearbyScore() {
        if (multiplayer) {
            Nearby.getMessagesClient(this).unpublish(mMessage);
            Date date = new Date();
            if (!name.equals("")) {
                String text = name + ";" + (totalLeft + totalRight) + ";" + date.getTime();
                mMessage = new Message((text).getBytes());
                Log.i("tada", "score sent");
                try {
                    if (!Mqtt.mqttAndroidClient.isConnected()) {
                        Mqtt.connectMqtt(name, this);
                    }
                    Mqtt.publish("score", text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Nearby.getMessagesClient(this).publish(mMessage).addOnFailureListener(this);
                if (realtimeDatabaseEnabled) {
                    try {
                        database.child("score").child(firebaseUser.getUid()).setValue(text);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void nameDialog(Context context) {
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        LayoutInflater inflater = this.getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_name, null);
        final EditText editTextName = view.findViewById(R.id.editText2);
        SharedPreferences sharedPref2 = context.getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        editTextName.setText(sharedPref2.getString("name", ""));
        builder.setView(view);
        builder.setMessage(context.getString(R.string.name_message));
        builder.setPositiveButton("Ok", (dialog, id) -> {
            SharedPreferences sharedPref = context.getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
            sharedPref.edit().putString("name", editTextName.getText().toString()).apply();
            name = editTextName.getText().toString();
            try {
                Mqtt.disconnectMqtt();
                Mqtt.connectMqtt(name, context);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            TrackHelper.track().event("category", "action").name("name changed").with(mMatomoTracker);
        });
        builder.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        if (sharedPref.contains("scores") || sharedPref.contains("name")) {
            if (!sharedPref.contains("version")) {
                sharedPref.edit().putInt("version", 1).apply();
                sharedPref.edit().putBoolean("multiplayer", true).apply();
                sharedPref.edit().putBoolean("multiplayerAsked", true).apply();
            }
        } else {
            sharedPref.edit().putInt("version", 1).apply();
        }

        if (sharedPref.getBoolean("multiplayer", false)) {
            initMultiplayer();
            multiplayer = true;
            tvOp.setText(R.string.No_players_nearby);
        } else {
            multiplayer = false;
            tvOp.setText("");
        }

        if (!sharedPref.getBoolean("multiplayerAsked", false)) {
            permissionDialog();
        }
        if (multiplayer) {
            try {
                updateTimer.cancel();
                updateTimer.purge();
            } catch (Exception ignored) {
            }
            updateTimer = new Timer();
            updateTimer.scheduleAtFixedRate(new updateTask(), 3000, updateInterval);
        }
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setUserProperty("multiplayer", multiplayer + "");
        if (!sharedPref.getBoolean("yahtzeeBonus", false)) {
            tvYahtzeeBonus.setVisibility(View.GONE);
            editText28.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        if (multiplayer) {
            Log.i("onStop", "disconnecting");
            try {
                Nearby.getMessagesClient(this).unpublish(mMessage);
                Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Mqtt.disconnectMqtt();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                updateTimer.cancel();
                updateTimer.purge();
                mMatomoTracker.dispatch();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (realtimeDatabaseEnabled) {
                try {
                    database.child("score").child(firebaseUser.getUid()).removeValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        Log.i("onResume", "start");
        if (!sharedPref.getBoolean("yahtzeeBonus", false)) {
            tvYahtzeeBonus.setVisibility(View.GONE);
            editText28.setVisibility(View.GONE);
        } else {
            tvYahtzeeBonus.setVisibility(View.VISIBLE);
            editText28.setVisibility(View.VISIBLE);
        }
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setUserProperty("yahtzeeBonus", sharedPref.getBoolean("yahtzeeBonus", false) + "");
        super.onResume();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        calculateTotal();
        DataManager.saveScores(createJsonScores(), getApplicationContext());
        updateNearbyScore();
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    private void calculateTotal() {
        totalLeft = getTextInt(editText1) + getTextInt(editText2) + getTextInt(editText3) + getTextInt(editText4) + getTextInt(editText5) + getTextInt(editText6);
        totalRight = getTextInt(editText21) + getTextInt(editText22) + getTextInt(editText23)
                + getTextInt(editText24) + getTextInt(editText25) + getTextInt(editText26) + getTextInt(editText27) + getTextInt(editText28);
        if (totalLeft >= 63) {
            tvBonus.setText(getString(R.string.bonus) + 35);
            totalLeft = totalLeft + 35;
        } else {
            tvBonus.setText(getString(R.string.bonus) + 0);
        }
        tvTotalLeft.setText(getString(R.string.left) + " " + totalLeft);
        tvTotalRight.setText(getString(R.string.right) + " " + totalRight);
        tvTotal.setText(getString(R.string.Total) + " " + (totalLeft + totalRight));
        if (players.size() == 0 && multiplayer) {
            tvOp.setText(R.string.No_players_nearby);
        }

        // add the player to the players list and update it on screen
        if (!name.equals("") && playersNearby) {
            // remove player if name already exists
            for (int i = 0; i < players.size(); i++) {
                PlayerItem playerItem = players.get(i);
                if (playerItem.getName().equals(name)) {
                    players.remove(i);
                    break;
                }
            }
            PlayerItem item = new PlayerItem(name, (totalLeft + totalRight), new Date().getTime(), true);
            players.add(item);
            updateMultiplayerText();
        }


        int color = Color.BLACK;
        // change editText color to white if there is a black theme
        int nightModeFlags =
                this.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            color = Color.WHITE;
        }
        if (getTextInt(editText1) > 5) {
            editText1.setTextColor(Color.RED);
        } else {
            editText1.setTextColor(color);
        }
        if (getTextInt(editText2) > 10 || !(getTextInt(editText2) % 2 == 0)) {
            editText2.setTextColor(Color.RED);
        } else {
            editText2.setTextColor(color);
        }
        if (getTextInt(editText3) > 15 || !(getTextInt(editText3) % 3 == 0)) {
            editText3.setTextColor(Color.RED);
        } else {
            editText3.setTextColor(color);
        }
        if (getTextInt(editText4) > 20 || !(getTextInt(editText4) % 4 == 0)) {
            editText4.setTextColor(Color.RED);
        } else {
            editText4.setTextColor(color);
        }
        if (getTextInt(editText5) > 25 || !(getTextInt(editText5) % 5 == 0)) {
            editText5.setTextColor(Color.RED);
        } else {
            editText5.setTextColor(color);
        }
        if (getTextInt(editText6) > 30 || !(getTextInt(editText6) % 6 == 0)) {
            editText6.setTextColor(Color.RED);
        } else {
            editText6.setTextColor(color);
        }

        if (getTextInt(editText21) > 30) {
            editText21.setTextColor(Color.RED);
        } else {
            editText21.setTextColor(color);
        }

        if (getTextInt(editText22) > 30) {
            editText22.setTextColor(Color.RED);
        } else {
            editText22.setTextColor(color);
        }
        if (getTextInt(editText27) > 30) {
            editText27.setTextColor(Color.RED);
        } else {
            editText27.setTextColor(color);
        }
    }

    private int getTextInt(EditText editText) {
        int d = 0;
        try {
            d = Integer.parseInt(editText.getText().toString());
        } catch (Exception ignored) {
        }
        return d;
    }

    private void clearText() {
        Log.i("clear", "tada");
        editText1.setText("");
        editText2.setText("");
        editText3.setText("");
        editText4.setText("");
        editText5.setText("");
        editText6.setText("");
        editText21.setText("");
        editText22.setText("");
        editText23.setText("");
        editText24.setText("");
        editText25.setText("");
        editText26.setText("");
        editText27.setText("");
        editText28.setText("");
        updateNearbyScore();
    }

    private JSONObject createJsonScores() {
        JSONObject jsonObject = new JSONObject();
        Log.i("score", "saving");
        try {
            jsonObject.put("1", editText1.getText().toString());
            jsonObject.put("2", editText2.getText().toString());
            jsonObject.put("3", editText3.getText().toString());
            jsonObject.put("4", editText4.getText().toString());
            jsonObject.put("5", editText5.getText().toString());
            jsonObject.put("6", editText6.getText().toString());
            jsonObject.put("21", editText21.getText().toString());
            jsonObject.put("22", editText22.getText().toString());
            jsonObject.put("23", editText23.getText().toString());
            jsonObject.put("24", editText24.getText().toString());
            jsonObject.put("25", editText25.getText().toString());
            jsonObject.put("26", editText26.getText().toString());
            jsonObject.put("27", editText27.getText().toString());
            jsonObject.put("28", editText28.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    private void readScores(JSONObject jsonObject) {
        Log.i("score", "read" + jsonObject.toString());
        try {
            editText1.setText(jsonObject.getString("1"));
            editText2.setText(jsonObject.getString("2"));
            editText3.setText(jsonObject.getString("3"));
            editText4.setText(jsonObject.getString("4"));
            editText5.setText(jsonObject.getString("5"));
            editText6.setText(jsonObject.getString("6"));
            editText21.setText(jsonObject.getString("21"));
            editText22.setText(jsonObject.getString("22"));
            editText23.setText(jsonObject.getString("23"));
            editText24.setText(jsonObject.getString("24"));
            editText25.setText(jsonObject.getString("25"));
            editText26.setText(jsonObject.getString("26"));
            editText27.setText(jsonObject.getString("27"));
            editText28.setText(jsonObject.getString("28"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
