package nl.koenhabets.yahtzeescore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ScoreAdapter extends ArrayAdapter<ScoreItem> {
    public ScoreAdapter(Context context, List<ScoreItem> scoreItems) {
        super(context, 0, scoreItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ScoreItem scoreItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.score_item, parent, false);
        }

        TextView textViewScore = convertView.findViewById(R.id.textViewScore);
        TextView textViewDate = convertView.findViewById(R.id.textViewDate);


        textViewScore.setText(scoreItem.getScore() + "");
        Date date = new Date(scoreItem.getDate());
        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy HH:mm");
        String dateText = df2.format(date);
        textViewDate.setText(dateText);


        return convertView;
    }
}
