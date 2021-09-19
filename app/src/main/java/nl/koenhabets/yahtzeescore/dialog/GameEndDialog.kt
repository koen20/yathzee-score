package nl.koenhabets.yahtzeescore.dialog

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import nl.koenhabets.yahtzeescore.R
import nl.koenhabets.yahtzeescore.ScoreItem
import nl.koenhabets.yahtzeescore.data.DataManager
import java.util.*

class GameEndDialog(context: Context) {
    private var context: Context = context
    private var scoreItems: List<ScoreItem> = ArrayList()


    fun showDialog(score: Int) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)

        val view2: View = inflater.inflate(R.layout.end_game_dialog, null)
        val tVGames = view2.findViewById<TextView>(R.id.textViewGamesPlayed)
        val tVAverageChange = view2.findViewById<TextView>(R.id.textViewAverageChange)
        val tvTotalToday = view2.findViewById<TextView>(R.id.textViewTotalToday)

        scoreItems = DataManager.loadScores(context)

        var total = 0.0
        var count = 0.0
        var calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        val startTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        val endTime = calendar.timeInMillis
        var dailyTotal = score
        for (i in scoreItems.indices) {
            val scoreItem = scoreItems[i]
            total += scoreItem.score
            count++
            if (scoreItem.date in startTime until endTime) {
                dailyTotal += scoreItem.score
            }
        }
        val average1 = Math.round((total / count * 100.0) / 100.0)
        val average2 = Math.round(((total + score) / (count + 1) * 100.0) / 100.0)
        val dif = average2 - average1

        tVGames.text = context.getString(R.string.total_games_played, scoreItems.size + 1)
        tVAverageChange.text = "Average (difference): $average2 ($dif)"
        tvTotalToday.text = "Total today: $dailyTotal"

        builder.setView(view2)
        builder.setTitle("Game stats")

        builder.setNegativeButton("Close") { dialog: DialogInterface?, id: Int -> }
        builder.show()
    }
}