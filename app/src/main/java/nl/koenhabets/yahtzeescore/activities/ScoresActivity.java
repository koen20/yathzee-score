package nl.koenhabets.yahtzeescore.activities;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Objects;

import nl.koenhabets.yahtzeescore.R;
import nl.koenhabets.yahtzeescore.ScoreAdapter;
import nl.koenhabets.yahtzeescore.ScoreComparator;
import nl.koenhabets.yahtzeescore.ScoreComparatorDate;
import nl.koenhabets.yahtzeescore.ScoreItem;
import nl.koenhabets.yahtzeescore.data.DataManager;

public class ScoresActivity extends AppCompatActivity {
    private List<ScoreItem> scoreItems = new ArrayList<>();
    ListView listView;
    ScoreAdapter scoreAdapter;
    TextView textViewAverage;
    TextView textViewAmount;
    int sort = 1;
    ActivityResultLauncher<Intent> exportResult;
    ActivityResultLauncher<Intent> importResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        setTitle(R.string.saved_scores);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
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
        scoreItems.addAll(DataManager.loadScores(this));
        scoreAdapter.notifyDataSetChanged();
        final Context context = this;
        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            final ScoreItem item = scoreItems.get(i);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(getString(R.string.remove_score));
            builder.setPositiveButton(getString(R.string.remove), (dialog, id) -> {
                for (int i1 = 0; i1 < scoreItems.size(); i1++) {
                    ScoreItem scoreItem = scoreItems.get(i1);
                    if (item.getId().equals(scoreItem.getId())) {
                        scoreItems.remove(i1);
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
                for (int i1 = 0; i1 < jsonArray1.length(); i1++) {
                    try {
                        JSONObject jsonObject = jsonArray1.getJSONObject(i1);
                        if (jsonObject.getString("id").equals(item.getId())) {
                            jsonArray1.remove(i1);
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
            });
            builder.setNegativeButton("Cancel", (dialog, id) -> {
            });
            builder.show();
            return true;
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

        exportResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        exportScoresMem(data);
                    }
                });

        importResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        importScoresMem(data);
                    }
                });
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.export_scores) {
            exportScores();
        } else if (item.getItemId() == R.id.import_scores) {
            importScores(findViewById(android.R.id.content).getRootView());
        } else if (item.getItemId() == R.id.change_sort) {
            if (sort == 1) {
                Collections.sort(scoreItems, new ScoreComparatorDate());
                scoreAdapter.notifyDataSetChanged();
                sort = 2; // date
                item.setTitle(R.string.sort_by_score);
            } else if (sort == 2) {
                Collections.sort(scoreItems, new ScoreComparator());
                scoreAdapter.notifyDataSetChanged();
                sort = 1; // highest score
                item.setTitle(R.string.sort_by_date);
            }
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

    public void exportScores() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "yahtzee-scores.txt");

        exportResult.launch(intent);
    }

    public void importScores(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        importResult.launch(intent);
    }

    public void exportScoresMem(Intent resultData) {
        //save scores to json file on phone storage
        Uri uri;
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
    }

    public void importScoresMem(Intent resultData) {
        if (resultData != null) {
            Uri currentUri = resultData.getData();

            try {
                String read = readFileContent(currentUri);
                Log.i("readFromFile", read);
                SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);

                JSONArray jsonArray = new JSONArray(read);
                JSONArray jsonArrayExisting = new JSONArray();
                try {
                    jsonArrayExisting = new JSONArray(sharedPref.getString("scoresSaved", ""));
                    Log.i("read", jsonArrayExisting.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int k = 0; k < jsonArray.length(); k++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(k);
                    boolean exists = false;
                    for (int i = 0; i < jsonArrayExisting.length(); i++) {
                        JSONObject jsonObjectExist = jsonArrayExisting.getJSONObject(i);
                        if (jsonObject.getString("id").equals(jsonObjectExist.getString("id"))) {
                            exists = true;
                        }
                    }
                    if (!exists) {
                        Log.i("add", "asdds");
                        jsonArrayExisting.put(jsonObject);
                    }
                }
                sharedPref.edit().putString("scoresSaved", jsonArrayExisting.toString()).apply();
                scoreItems.clear();
                scoreItems.addAll(DataManager.loadScores(this));
                scoreAdapter.notifyDataSetChanged();
                updateAverageScore();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
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
            stringBuilder.append(currentline).append("\n");
        }
        inputStream.close();
        return stringBuilder.toString();
    }

    private void updateAverageScore() {
        double total = 0;
        double count = 0;
        for (int i = 0; i < scoreItems.size(); i++) {
            total = total + scoreItems.get(i).getScore();
            count++;
        }
        try {
            textViewAverage.setText(getString(R.string.average_d, (double) Math.round((total / count) * 10) / 10d));
            textViewAmount.setText(getString(R.string.total_games_played, scoreItems.size()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
