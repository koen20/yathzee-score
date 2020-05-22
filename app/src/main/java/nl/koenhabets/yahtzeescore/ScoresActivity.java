package nl.koenhabets.yahtzeescore;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.matomo.sdk.Tracker;
import org.matomo.sdk.extra.TrackHelper;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoresActivity extends AppCompatActivity {
    private List<ScoreItem> scoreItems = new ArrayList<>();
    ListView listView;
    ScoreAdapter scoreAdapter;
    TextView textViewAverage;
    TextView textViewAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        setTitle(R.string.saved_scores);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            Tracker tracker = MainActivity.getTracker2();
            TrackHelper.track().screen("/saved_scores").title("Saved scores").with(tracker);
        } catch (Exception e) {
            e.printStackTrace();
        }
        listView = findViewById(R.id.listViewScore);
        textViewAverage = findViewById(R.id.textViewAverage);
        textViewAmount = findViewById(R.id.textViewAmount);


        scoreAdapter = new ScoreAdapter(this, scoreItems);
        listView.setAdapter(scoreAdapter);
        loadScores();
        final Context context = this;
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final ScoreItem item = scoreItems.get(i);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(getString(R.string.remove_score));
                builder.setPositiveButton(getString(R.string.remove), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        for (int i = 0; i < scoreItems.size(); i++) {
                            ScoreItem scoreItem = scoreItems.get(i);
                            if (item.getId().equals(scoreItem.getId())) {
                                scoreItems.remove(i);
                                break;
                            }
                        }
                        SharedPreferences sharedPref1 = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
                        JSONArray jsonArray1 = new JSONArray();
                        try {
                            jsonArray1 = new JSONArray(sharedPref1.getString("scoresSaved", ""));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        for (int i = 0; i < jsonArray1.length(); i++) {
                            try {
                                JSONObject jsonObject = jsonArray1.getJSONObject(i);
                                if (jsonObject.getString("id").equals(item.getId())) {
                                    jsonArray1.remove(i);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        sharedPref1.edit().putString("scoresSaved", jsonArray1.toString()).apply();
                        scoreAdapter.notifyDataSetChanged();
                        updateAverageScore();
                        BackupManager backupManager = new BackupManager(context);
                        backupManager.dataChanged();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                builder.show();
                return true;
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            final ScoreItem item = scoreItems.get(position);
            if (!item.getAllScores().toString().equals("{}")) {
                Intent myIntent = new Intent(getApplicationContext(), ScoreActivity.class);
                myIntent.putExtra("data", item.getAllScores().toString());
                startActivity(myIntent);
            }
        });
        updateAverageScore();
    }

    //load all scores from sharedprefrences, sort them and display the scores.
    private void loadScores(){
        scoreItems.clear();
        SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = new JSONArray(sharedPref.getString("scoresSaved", ""));
            Log.i("read", jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = jsonArray.length(); i >= 0; i--) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject allScores = new JSONObject();
                try {
                    allScores = jsonObject.getJSONObject("allScores");
                } catch (JSONException ignored) {
                }

                ScoreItem scoreItem = new ScoreItem(jsonObject.getInt("score"), jsonObject.getLong("date"), jsonObject.getString("id"), allScores);
                scoreItems.add(scoreItem);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        Collections.sort(scoreItems, new ScoreComparator());
        scoreAdapter.notifyDataSetChanged();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.export_scores){
            exportScores(findViewById(android.R.id.content).getRootView());
        } else if (item.getItemId() == R.id.import_scores) {
            importScores(findViewById(android.R.id.content).getRootView());
        } else {
            finish();
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_scores, menu);
        return true;
    }

    public void exportScores(View view) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "yahtzee-scores.txt");

        startActivityForResult(intent, 4);
    }

    public void importScores(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, 6);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == 4
                && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                    ParcelFileDescriptor pfd =
                            this.getContentResolver().
                                    openFileDescriptor(uri, "w");

                    FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                    SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
                    String textContent = sharedPref.getString("scoresSaved", "[]");
                    fileOutputStream.write(textContent.getBytes());

                    fileOutputStream.close();
                    pfd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == 6 && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri currentUri = resultData.getData();

                try {
                    String read = readFileContent(currentUri);
                    Log.i("readFromFile", read);
                    SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
                    try {
                        JSONArray jsonArray = new JSONArray(read);
                        sharedPref.edit().putString("scoresSaved", jsonArray.toString()).apply();
                        loadScores();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private String readFileContent(Uri uri) throws IOException {

        InputStream inputStream =
                getContentResolver().openInputStream(uri);
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(
                        inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String currentline;
        while ((currentline = reader.readLine()) != null) {
            stringBuilder.append(currentline + "\n");
        }
        inputStream.close();
        return stringBuilder.toString();
    }

    private void updateAverageScore() {
        int total = 0;
        int count = 0;
        for (int i = 0; i < scoreItems.size(); i++) {
            total = total + scoreItems.get(i).getScore();
            count++;
        }
        try {
            textViewAverage.setText(getString(R.string.average) + (total / count));
            textViewAmount.setText(getString(R.string.total_games_played) + scoreItems.size());
        } catch (Exception ignored) {

        }
    }
}
