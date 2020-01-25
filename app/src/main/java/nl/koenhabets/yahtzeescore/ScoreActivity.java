package nl.koenhabets.yahtzeescore;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class ScoreActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        appBarLayout.setVisibility(View.GONE);

        button = findViewById(R.id.button);

        tvTotalLeft = findViewById(R.id.textViewTotalLeft);
        tvTotalRight = findViewById(R.id.textViewTotalRight);
        tvTotal = findViewById(R.id.textViewTotal);
        tvBonus = findViewById(R.id.textViewBonus);
        tvOp = findViewById(R.id.textViewOp);
        tvYahtzeeBonus = findViewById(R.id.textView7);

        tvTotalLeft.setVisibility(View.GONE);
        tvTotalRight.setVisibility(View.GONE);
        tvTotal.setVisibility(View.GONE);
        tvBonus.setVisibility(View.GONE);
        tvOp.setVisibility(View.GONE);
        button.setVisibility(View.GONE);

        String data = getIntent().getStringExtra("data");
        try {
            JSONObject allScores = new JSONObject(data);
            readScores(allScores);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        disableEdit();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
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

    private void disableEdit(){
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
