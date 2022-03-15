package nl.koenhabets.yahtzeescore.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import nl.koenhabets.yahtzeescore.AppUpdates;
import nl.koenhabets.yahtzeescore.PlayerAdapter;
import nl.koenhabets.yahtzeescore.ScoresView;
import nl.koenhabets.yahtzeescore.dialog.GameEndDialog;
import nl.koenhabets.yahtzeescore.dialog.PlayerScoreDialog;
import nl.koenhabets.yahtzeescore.R;
import nl.koenhabets.yahtzeescore.data.DataManager;
import nl.koenhabets.yahtzeescore.data.MigrateData;
import nl.koenhabets.yahtzeescore.multiplayer.Multiplayer;
import nl.koenhabets.yahtzeescore.multiplayer.PlayerItem;

public class MainActivity extends AppCompatActivity implements OnFailureListener {
    public static String name = "";
    Multiplayer multiplayer;
    boolean multiplayerEnabled;
    private TextView tvTotal;
    private TextView tvOp;
    private ScoresView scoresView;
    private RecyclerView recyclerView;
    public static int score = 0;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;
    private PlayerAdapter playerAdapter;
    private final List<PlayerItem> players2 = new ArrayList<>();
    PlayerScoreDialog playerScoreDialog;
    MessageListener mMessageListener;
    private Message mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        mAuth = FirebaseAuth.getInstance();
        SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        Log.i("multiplayer main", sharedPref.getBoolean("multiplayer", false) + "d");
        if (!sharedPref.contains("version") && !sharedPref.contains("multiplayer") && !sharedPref.contains("multiplayerAsked")) {
            sharedPref.edit().putBoolean("welcomeShown", false).apply();
            Intent myIntent = new Intent(this, WelcomeActivity.class);
            this.startActivity(myIntent);
            finish();
        } else if (!sharedPref.getBoolean("welcomeShown", true)) {
            Intent myIntent = new Intent(this, WelcomeActivity.class);
            this.startActivity(myIntent);
            finish();
        }
        AppCompatDelegate.setDefaultNightMode(sharedPref.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));

        int nightModeFlags =
                this.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#121212")));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = this.getWindow();
                window.setStatusBarColor(Color.parseColor("#121212"));
            }
        }

        playerScoreDialog = new PlayerScoreDialog(this);

        Button button = findViewById(R.id.button);

        tvTotal = findViewById(R.id.textViewTotal);
        tvOp = findViewById(R.id.textViewOp);
        recyclerView = findViewById(R.id.reyclerViewMultiplayer);
        scoresView = findViewById(R.id.scoresView);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        playerAdapter = new PlayerAdapter(this, players2);
        recyclerView.setAdapter(playerAdapter);

        scoresView.setScoreListener(new ScoresView.ScoreListener() {
            @Override
            public void onScoreJson(@NonNull JSONObject jsonObjectScores) {
                DataManager.saveScores(jsonObjectScores, getApplicationContext());
                if (multiplayerEnabled && multiplayer != null) {
                    multiplayer.setFullScore(jsonObjectScores);
                    if (multiplayer.getPlayerAmount() == 0) {
                        tvOp.setText(R.string.No_players_nearby);
                        recyclerView.setVisibility(View.GONE);
                    }
                    setMultiplayerScore(score);
                }
            }

            @Override
            public void onScore(int score) {
                MainActivity.score = score;
                tvTotal.setText(getString(R.string.Total, score));
            }
        });

        playerAdapter.setClickListener((view, position) -> {
            if (position >= 0 && position < players2.size()) {
                if (!players2.get(position).getName().equals(name)) {
                    if (!players2.get(position).getFullScore().toString().equals("{}")) {
                        playerScoreDialog.showDialog(this, players2, position);
                    } else {
                        Toast.makeText(MainActivity.this, R.string.score_nearby_unavailable,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        try {
            scoresView.setScores(new JSONObject(sharedPref.getString("scores", "")));
        } catch (Exception ignored) {
        }

        final Context context = this;
        button.setOnClickListener(view -> saveScoreDialog(context));

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = AppUpdates.getVersionInfo();
                            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                            int verCode = pInfo.versionCode;
                            if (jsonObject.getInt("flexibleVersion") > verCode) {
                                String updateText = "";
                                if (jsonObject.has("updateText")) {
                                    updateText = jsonObject.getString("updateText");
                                }
                                String finalUpdateText = updateText;
                                showUpdateToast(finalUpdateText);
                            }
                            JSONArray versions = jsonObject.getJSONArray("versions");
                            for (int i = 0; i < versions.length(); i++) {
                                JSONObject version = versions.getJSONObject(i);
                                if (version.getInt("version") == verCode) {
                                    showUpdateToast(version.getString("updateText"));
                                }
                            }
                        } catch (PackageManager.NameNotFoundException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                3000
        );
    }

    private void showUpdateToast(String finalUpdateText) {
        MainActivity.this.runOnUiThread(() -> Toast.makeText(MainActivity.this, getString(R.string.update_available) + finalUpdateText, Toast.LENGTH_LONG).show());
    }

    private void saveScoreDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.save_score);
        builder.setNegativeButton(R.string.no, (dialog, id) -> {
            AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
            builder2.setTitle(R.string.score_not_save_conf);
            builder2.setNegativeButton(R.string.no, (dialog2, id2) -> {

            });
            builder2.setPositiveButton(R.string.yes, (dialog2, id2) -> {
                scoresView.clearScores();
                if (multiplayerEnabled) {
                    multiplayer.updateNearbyScore();
                }
            });
            builder2.show();
        });
        builder.setPositiveButton(R.string.yes, (dialog, id) -> {
            if ((score) < 5) {
                Toast toast = Toast.makeText(this, R.string.score_too_low_save, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
                if (sharedPref.getBoolean("endDialog", true)) {
                    GameEndDialog gameEndDialog = new GameEndDialog(this);
                    gameEndDialog.showDialog(score);
                }
                DataManager.saveScore(score, scoresView.createJsonScores(), getApplicationContext());
            }
            scoresView.clearScores();
            if (multiplayerEnabled) {
                multiplayer.updateNearbyScore();
            }
        });
        builder.setNeutralButton(R.string.cancel, (dialogInterface, i) -> {
        });
        builder.show();
    }

    private void initMultiplayer() {
        SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        Log.i("name", sharedPref.getString("name", ""));
        if (sharedPref.getString("name", "").equals("")) {
            nameDialog(this);
        } else {
            name = sharedPref.getString("name", "");
        }

        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d("MainActivity", "signInAnonymously:success");
                            firebaseUser = mAuth.getCurrentUser();
                            initMultiplayerObj(firebaseUser);

                        } else {
                            Log.w("MainActivity", "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            initMultiplayerObj(firebaseUser);
        }

        tvOp.setMovementMethod(new ScrollingMovementMethod());
        tvOp.setOnClickListener(view -> addPlayerDialog());
    }

    private void initMultiplayerObj(FirebaseUser firebaseUser) {
        multiplayer = new Multiplayer(this, name, score, firebaseUser.getUid());
        initNearby();
        multiplayer.setMultiplayerListener(new Multiplayer.MultiplayerListener() {
            @Override
            public void onChange(List<PlayerItem> players) {
                if (multiplayerEnabled) {
                    // add the local player to the players list and update it on screen
                    if (!name.equals("") && multiplayer.getPlayerAmount() != 0) {
                        // remove player if name already exists
                        for (int i = 0; i < players.size(); i++) {
                            PlayerItem playerItem = players.get(i);
                            if (playerItem.getName().equals(name)) {
                                players.remove(i);
                                break;
                            }
                        }
                        PlayerItem item = new PlayerItem(name, score, new Date().getTime(), true, true);
                        players.add(item);
                        updateMultiplayerText(players);
                    }
                }
            }

            @Override
            public void onChangeFullScore(List<PlayerItem> players) {
                if (playerScoreDialog.getPlayerShown() != null) {
                    if (!playerScoreDialog.getPlayerShown().equals("")) {
                        playerScoreDialog.updateScore(players);
                    }
                }
            }
        });
        setMultiplayerScore(score);
        multiplayer.setFullScore(scoresView.createJsonScores());
    }

    private void setMultiplayerScore(int score) {
        Nearby.getMessagesClient(this).unpublish(mMessage);
        Date date = new Date();
        if (!name.equals("")) {
            String text = name + ";" + (score) + ";" + date.getTime() + ";" + firebaseUser.getUid();
            mMessage = new Message((text).getBytes());
            Nearby.getMessagesClient(this).publish(mMessage).addOnFailureListener(this);
        }
        multiplayer.setScore(score);
    }

    public void initNearby() {
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                Log.d("t", "Found message: " + new String(message.getContent()));
                multiplayer.proccessMessage(new String(message.getContent()), false, "");
            }

            @Override
            public void onLost(Message message) {
                Log.d("d", "Lost sight of message: " + new String(message.getContent()));
            }
        };
        mMessage = new Message(("new player").getBytes());

        Nearby.getMessagesClient(this).publish(mMessage).addOnFailureListener(this);
        Nearby.getMessagesClient(this).subscribe(mMessageListener);
    }

    private void addPlayerDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_name, null);
        final EditText editTextName = view.findViewById(R.id.editText2);
        builder.setView(view);
        builder.setMessage(R.string.add_player);
        builder.setPositiveButton("Ok", (dialog, id) -> {
            if (!editTextName.getText().toString().equals("")) {
                SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
                JSONArray playersM = new JSONArray();
                try {
                    playersM = new JSONArray(sharedPref.getString("players", "[]"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                playersM.put(editTextName.getText().toString());
                sharedPref.edit().putString("players", playersM.toString()).apply();
                PlayerItem playerItem = new PlayerItem(editTextName.getText().toString(), 0, 0, true, false);
                multiplayer.addPlayer(playerItem);
                updateMultiplayerText(multiplayer.getPlayers());
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
        });
        builder.show();
    }

    public void updateMultiplayerText(List<PlayerItem> players) {
        recyclerView.setVisibility(View.VISIBLE);
        tvOp.setText(R.string.nearby);
        Collections.sort(players);
        players2.clear();
        for (int i = 0; i < players.size(); i++) {
            PlayerItem playerItem = players.get(i);
            if (playerItem.isVisible()) {
                players2.add(playerItem);
            }
        }
        playerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        e.printStackTrace();
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        menu.findItem(R.id.add_player).setVisible(multiplayerEnabled);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.privacy_policy) {
            openUrl("https://koenhabets.nl/privacy_policy.html");
            return true;
        } else if (itemId == R.id.scores2) {
            Intent myIntent = new Intent(this, ScoresActivity.class);
            this.startActivity(myIntent);
            return true;
        } else if (itemId == R.id.settings2) {
            Intent myIntent2 = new Intent(this, SettingsActivity.class);
            this.startActivity(myIntent2);
            return true;
        } else if (itemId == R.id.stats) {
            Intent myIntent3 = new Intent(this, StatsActivity.class);
            this.startActivity(myIntent3);
            return true;
        } else if (itemId == R.id.rules) {
            String language = Locale.getDefault().getLanguage();
            switch (language) {
                case "nl":
                    openUrl("https://nl.wikipedia.org/wiki/Yahtzee#Spelverloop");
                    break;
                case "fr":
                    openUrl("https://fr.wikipedia.org/wiki/Yahtzee#R%C3%A8gles");
                    break;
                case "de":
                    openUrl("https://de.wikipedia.org/wiki/Kniffel#Spielregeln");
                    break;
                case "pl":
                    openUrl("https://pl.wikipedia.org/wiki/Ko%C5%9Bci_(gra)#Klasyczne_zasady_gry_(Yahtzee)");
                    break;
                case "it":
                    openUrl("https://it.wikipedia.org/wiki/Yahtzee");
                    break;
                default:
                    openUrl("https://en.wikipedia.org/wiki/Yahtzee#Rules");
                    break;
            }
            return true;
        } else if (itemId == R.id.add_player) {
            if (multiplayerEnabled) {
                addPlayerDialog();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);

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
            multiplayer.setName(name);
        });
        builder.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("onStart", "start");
        SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        new MigrateData(this);

        if (sharedPref.getBoolean("multiplayer", false)) {
            initMultiplayer();
            multiplayerEnabled = true;
            recyclerView.setVisibility(View.GONE);
            tvOp.setVisibility(View.VISIBLE);
            tvOp.setText(R.string.No_players_nearby);
        } else {
            multiplayerEnabled = false;
            recyclerView.setVisibility(View.GONE);
            tvOp.setVisibility(View.GONE);
        }

        scoresView.setYahtzeeBonusVisibility(sharedPref.getBoolean("yahtzeeBonus", false));
    }

    @Override
    public void onStop() {
        if (multiplayer != null) {
            Log.i("onStop", "disconnecting");
            try {
                Nearby.getMessagesClient(this).unpublish(mMessage);
                Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
            multiplayer.stopMultiplayer();
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        Log.i("onResume", "start");
        scoresView.setYahtzeeBonusVisibility(sharedPref.getBoolean("yahtzeeBonus", false));
        super.onResume();
    }
}
