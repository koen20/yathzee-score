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
import android.text.Editable;
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
import com.google.android.material.snackbar.Snackbar;
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
import nl.koenhabets.yahtzeescore.dialog.GameEndDialog;
import nl.koenhabets.yahtzeescore.dialog.PlayerScoreDialog;
import nl.koenhabets.yahtzeescore.R;
import nl.koenhabets.yahtzeescore.data.DataManager;
import nl.koenhabets.yahtzeescore.data.MigrateData;
import nl.koenhabets.yahtzeescore.multiplayer.Multiplayer;
import nl.koenhabets.yahtzeescore.multiplayer.PlayerItem;

public class MainActivity extends AppCompatActivity implements TextWatcher, OnFailureListener {
    public static String name = "";
    Multiplayer multiplayer;
    boolean multiplayerEnabled;
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
    private TextView tvOp;
    private RecyclerView recyclerView;
    private TextView tvYahtzeeBonus;
    private EditText editTextBonus;
    private int totalLeft = 0;
    private int totalRight = 0;
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
        editTextBonus = findViewById(R.id.editTextBonus);

        Button button = findViewById(R.id.button);

        tvTotalLeft = findViewById(R.id.textViewTotalLeft);
        tvTotalRight = findViewById(R.id.textViewTotalRight);
        tvTotal = findViewById(R.id.textViewTotal);
        tvOp = findViewById(R.id.textViewOp);
        tvYahtzeeBonus = findViewById(R.id.textView7);
        recyclerView = findViewById(R.id.reyclerViewMultiplayer);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        playerAdapter = new PlayerAdapter(this, players2);
        recyclerView.setAdapter(playerAdapter);

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
            readScores(new JSONObject(sharedPref.getString("scores", "")));
        } catch (Exception ignored) {
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

        editText23.setOnClickListener(view -> setDefaultValue(editText23, 25));
        editText24.setOnClickListener(view -> setDefaultValue(editText24, 30));
        editText25.setOnClickListener(view -> setDefaultValue(editText25, 40));
        editText26.setOnClickListener(view -> setDefaultValue(editText26, 50));

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
                clearText();
            });
            builder2.show();
        });
        builder.setPositiveButton(R.string.yes, (dialog, id) -> {
            if ((totalLeft + totalRight) < 5) {
                Toast toast = Toast.makeText(this, R.string.score_too_low_save, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
                if (sharedPref.getBoolean("endDialog", true)) {
                    GameEndDialog gameEndDialog = new GameEndDialog(this);
                    gameEndDialog.showDialog(totalLeft + totalRight);
                }
                DataManager.saveScore(totalLeft + totalRight, createJsonScores(), getApplicationContext());
            }
            clearText();
        });
        builder.setNeutralButton(R.string.cancel, (dialogInterface, i) -> {
        });
        builder.show();
    }

    private void setDefaultValue(EditText editTextD, int value) {
        int number = -1;
        try {
            number = Integer.parseInt(editTextD.getText().toString());
        } catch (NumberFormatException ignored) {
        }
        if (number == value) {
            editTextD.setText(String.valueOf(0));
        } else if (number == 0) {
            editTextD.setText("");
        } else {
            editTextD.setText(String.valueOf(value));
        }

        SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        if (!sharedPref.getBoolean("fieldHintShown", false)) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.press_again), Snackbar.LENGTH_LONG).show();
            sharedPref.edit().putBoolean("fieldHintShown", true).apply();
        }
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
        multiplayer = new Multiplayer(this, name, (totalLeft + totalRight), firebaseUser.getUid());
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
                        PlayerItem item = new PlayerItem(name, (totalLeft + totalRight), new Date().getTime(), true, true);
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
        setMultiplayerScore(totalLeft + totalRight);
        multiplayer.setFullScore(createJsonScores());
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

        if (!sharedPref.getBoolean("yahtzeeBonus", false)) {
            tvYahtzeeBonus.setVisibility(View.GONE);
            editText28.setVisibility(View.GONE);
        }
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
        if (!sharedPref.getBoolean("yahtzeeBonus", false)) {
            tvYahtzeeBonus.setVisibility(View.GONE);
            editText28.setVisibility(View.GONE);
        } else {
            tvYahtzeeBonus.setVisibility(View.VISIBLE);
            editText28.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        calculateTotal();
        JSONObject jsonObjectScores = createJsonScores();
        DataManager.saveScores(jsonObjectScores, getApplicationContext());
        if (multiplayerEnabled && multiplayer != null) {
            multiplayer.setFullScore(jsonObjectScores);
            if (multiplayer.getPlayerAmount() == 0) {
                tvOp.setText(R.string.No_players_nearby);
                recyclerView.setVisibility(View.GONE);
            }
            setMultiplayerScore(totalLeft + totalRight);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    private void calculateTotal() {
        totalLeft = getTextInt(editText1) + getTextInt(editText2) + getTextInt(editText3) + getTextInt(editText4) + getTextInt(editText5) + getTextInt(editText6);
        totalRight = getTextInt(editText21) + getTextInt(editText22) + getTextInt(editText23)
                + getTextInt(editText24) + getTextInt(editText25) + getTextInt(editText26) + getTextInt(editText27) + getTextInt(editText28);
        if (totalLeft >= 63) {
            editTextBonus.setText(String.valueOf(35));
            totalLeft = totalLeft + 35;
        } else {
            editTextBonus.setText(getString(R.string.bonus_value, 63 - totalLeft));
        }
        tvTotalLeft.setText(getString(R.string.left, totalLeft));
        tvTotalRight.setText(getString(R.string.right, totalRight));
        tvTotal.setText(getString(R.string.Total, (totalLeft + totalRight)));

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
        if (multiplayerEnabled) {
            multiplayer.updateNearbyScore();
        }
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
