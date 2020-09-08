package nl.koenhabets.yahtzeescore.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import nl.koenhabets.yahtzeescore.DataManager;
import nl.koenhabets.yahtzeescore.R;
import nl.koenhabets.yahtzeescore.ScoreAdapter;
import nl.koenhabets.yahtzeescore.ScoreComparator;
import nl.koenhabets.yahtzeescore.ScoreComparatorDate;
import nl.koenhabets.yahtzeescore.ScoreItem;

public class ScoresActivity extends AppCompatActivity {
    private List<ScoreItem> scoreItems = new ArrayList<>();
    ListView listView;
    ScoreAdapter scoreAdapter;
    TextView textViewAverage;
    TextView textViewAmount;
    int sort = 1;

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
            } else if (sort == 2){
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
                        scoreItems = DataManager.loadScores(this);
                        scoreAdapter.notifyDataSetChanged();
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
