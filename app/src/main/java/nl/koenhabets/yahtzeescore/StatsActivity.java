package nl.koenhabets.yahtzeescore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.appbar.AppBarLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.matomo.sdk.Tracker;
import org.matomo.sdk.extra.TrackHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatsActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        setTitle(getString(R.string.stats));
        try {
            Tracker tracker = MainActivity.getTracker2();
            TrackHelper.track().screen("/stats").title("Stats").with(tracker);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);

        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = new JSONArray(sharedPref.getString("scoresSaved", ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        TextView textViewGraph = findViewById(R.id.textViewGraph);

        LineChart lineChart = findViewById(R.id.chart);
        List<Entry> entries = new ArrayList<Entry>();

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        appBarLayout.setVisibility(View.GONE);

        JSONObject jsonObject = processScores(jsonArray);
        readScores(jsonObject);

        List<ScoreItem> scoreItemsDate = DataManager.loadScores(this);
        ;
        Collections.sort(scoreItemsDate, (o1, o2) -> o1.getDate().compareTo(o2.getDate()));
        float sum = 0;
        for (int d = 0; d < scoreItemsDate.size(); d++) {
            sum = sum + scoreItemsDate.get(d).getScore();
            float value = sum / (d + 1);
            if (scoreItemsDate.size() > 100) {
                if (d > 9) {
                    entries.add(new Entry(d + 10, value));
                }
            }
        }
        if (scoreItemsDate.size() > 100) {
            textViewGraph.setVisibility(View.VISIBLE);
        }
        Log.i("entries", entries.size() + "size");
        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.average_score));
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.YELLOW);
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setText("Average score of last " + scoreItemsDate.size() + " games");
        int nightModeFlags =
                this.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            lineChart.getXAxis().setTextColor(Color.WHITE);
            lineChart.getAxisLeft().setTextColor(Color.WHITE);
            lineChart.getLegend().setTextColor(Color.WHITE);
            lineChart.getDescription().setTextColor(Color.WHITE);
        }

        lineChart.invalidate();

        disableEdit();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private JSONObject processScores(JSONArray jsonArray) {
        JSONObject jsonObject = new JSONObject();
        for (int k = 1; k < 7; k++) {
            try {
                jsonObject.put(k + "", proccessField(jsonArray, k));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        for (int k = 21; k < 29; k++) {
            try {
                jsonObject.put(k + "", proccessField(jsonArray, k));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("atadf", jsonObject.toString());
        return jsonObject;
    }

    private String proccessField(JSONArray jsonArray, int d) {
        double totalScore = 0;
        double scoreCount = 0;
        double scoreCountMax = 0;
        for (int i = 0; i < jsonArray.length(); i++) {

            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i).getJSONObject("allScores");
                String val = jsonObject.getString(d + "");
                if (!val.equals("") && !val.equals("0")) {
                    int valInt = Integer.parseInt(val);
                    totalScore = totalScore + valInt;
                    scoreCount = scoreCount + 1;
                }
                scoreCountMax = scoreCountMax + 1;
            } catch (JSONException ignored) {
            }

        }
        int chance = (int) Math.round((scoreCount / scoreCountMax) * 100.0);
        double average = (totalScore / scoreCountMax);
        return round(average, 1) + "(" + chance + ")";
    }

    public static double round(double value, int places) {
        double res = 0;
        if (value != 0.0 && !Double.isNaN(value)) {
            if (places < 0) throw new IllegalArgumentException();

            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            res = bd.doubleValue();
        }
        return res;
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

    private void disableEdit() {
        editText1.setEnabled(false);
        editText2.setEnabled(false);
        editText3.setEnabled(false);
        editText4.setEnabled(false);
        editText5.setEnabled(false);
        editText6.setEnabled(false);
        editText21.setEnabled(false);
        editText22.setEnabled(false);
        editText23.setEnabled(false);
        editText24.setEnabled(false);
        editText25.setEnabled(false);
        editText26.setEnabled(false);
        editText27.setEnabled(false);
        editText28.setEnabled(false);

    }
}
