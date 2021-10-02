package nl.koenhabets.yahtzeescore.dialog;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import nl.koenhabets.yahtzeescore.R;
import nl.koenhabets.yahtzeescore.multiplayer.PlayerItem;

public class PlayerScoreDialog {
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
    private TextView textViewLeft;
    private TextView textViewRight;
    private String playerShown;
    private final Context context;

    public PlayerScoreDialog(Context context){
        this.context = context;
    }

    public void showDialog(Context context, List<PlayerItem> players2, int position){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);

        View view2 = inflater.inflate(R.layout.score_popup, null);
        editText1 = view2.findViewById(R.id.editText);
        editText2 = view2.findViewById(R.id.editText3);
        editText3 = view2.findViewById(R.id.editText4);
        editText4 = view2.findViewById(R.id.editText5);
        editText5 = view2.findViewById(R.id.editText6);
        editText6 = view2.findViewById(R.id.editText7);

        editText21 = view2.findViewById(R.id.editText9);
        editText22 = view2.findViewById(R.id.editText10);
        editText23 = view2.findViewById(R.id.editText8);
        editText24 = view2.findViewById(R.id.editText11);
        editText25 = view2.findViewById(R.id.editText12);
        editText26 = view2.findViewById(R.id.editText13);
        editText27 = view2.findViewById(R.id.editText14);
        editText28 = view2.findViewById(R.id.editText16);
        textViewLeft = view2.findViewById(R.id.textViewDiaLeft);
        textViewRight = view2.findViewById(R.id.textViewDiaRight);


        builder.setView(view2);
        setScores(players2.get(position).getFullScore());
        builder.setTitle(players2.get(position).getName());
        builder.setNegativeButton("Close", (dialog, id) -> {
        });
        builder.show();
        playerShown = players2.get(position).getName();
        builder.setOnDismissListener(dialogInterface -> playerShown = "");
        calculateScores();
        disableEdit();
    }

    public void updateScore(List<PlayerItem> players) {
        for (int k = 0; k < players.size(); k++) {
            if (players.get(k).getName().equals(playerShown)){
                setScores(players.get(k).getFullScore());
                calculateScores();
                break;
            }
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

    public void calculateScores(){
        int totalLeft = getTextInt(editText1) + getTextInt(editText2) + getTextInt(editText3) + getTextInt(editText4) + getTextInt(editText5) + getTextInt(editText6);
        int totalRight = getTextInt(editText21) + getTextInt(editText22) + getTextInt(editText23)
                + getTextInt(editText24) + getTextInt(editText25) + getTextInt(editText26) + getTextInt(editText27) + getTextInt(editText28);
        if (totalLeft >= 63){
            totalLeft += 35;
        }
        textViewLeft.setText(context.getString(R.string.left, totalLeft));
        textViewRight.setText(context.getString(R.string.right, totalRight));
    }

    private void setScores(JSONObject jsonObject) {
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

    public String getPlayerShown() {
        return playerShown;
    }

    private void disableEdit(){
        editText1.setInputType(InputType.TYPE_NULL);
        editText2.setInputType(InputType.TYPE_NULL);
        editText3.setInputType(InputType.TYPE_NULL);
        editText4.setInputType(InputType.TYPE_NULL);
        editText5.setInputType(InputType.TYPE_NULL);
        editText6.setInputType(InputType.TYPE_NULL);
        editText21.setInputType(InputType.TYPE_NULL);
        editText22.setInputType(InputType.TYPE_NULL);
        editText23.setInputType(InputType.TYPE_NULL);
        editText24.setInputType(InputType.TYPE_NULL);
        editText25.setInputType(InputType.TYPE_NULL);
        editText26.setInputType(InputType.TYPE_NULL);
        editText27.setInputType(InputType.TYPE_NULL);
        editText28.setInputType(InputType.TYPE_NULL);
    }
}
