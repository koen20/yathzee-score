package nl.koenhabets.yahtzeescore.adapters

import android.content.Context
import android.icu.text.DateFormat.getDateTimeInstance
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import nl.koenhabets.yahtzeescore.R
import nl.koenhabets.yahtzeescore.ScoreItem
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date


class ScoreAdapter(context: Context, scoreItems: List<ScoreItem>) : ArrayAdapter<ScoreItem>(
    context, 0, scoreItems
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val scoreItem = getItem(position)
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.score_item, parent, false)
        }
        val textViewScore = convertView!!.findViewById<TextView>(R.id.textViewScore)
        val textViewDate = convertView.findViewById<TextView>(R.id.textViewDate)
        if (scoreItem !== null) {
            textViewScore.text = scoreItem.score.toString() + ""
            val date = Date(scoreItem.date)
            val dateFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            } else {
                SimpleDateFormat("dd/MM/yy HH:mm");
            }
            val dateText = dateFormat.format(date)
            textViewDate.text = dateText
        }
        return convertView
    }
}