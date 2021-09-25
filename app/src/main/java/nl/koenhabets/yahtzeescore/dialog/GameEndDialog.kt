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
        val tvAvgDifToday = view2.findViewById<TextView>(R.id.textViewDifToday)

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
        var gamesToday = 0
        for (i in scoreItems.indices) {
            val scoreItem = scoreItems[i]
            total += scoreItem.score
            count++
            if (scoreItem.date in startTime until endTime) {
                dailyTotal += scoreItem.score
                gamesToday += 1
            }
        }
        val average1 = Math.round(total / count * 100.0) / 100.0
        val average2 = Math.round((total + score) / (count + 1) * 100.0) / 100.0
        val dif = Math.round((average2 - average1) * 100) / 100.0
        val difToday = Math.round((average2 - ((total - dailyTotal + score) / (count - gamesToday))) * 100) / 100.0

        tVGames.text = (scoreItems.size + 1).toString()
        tVAverageChange.text = "$average2 ($dif)"
        tvTotalToday.text = dailyTotal.toString()
        tvAvgDifToday.text = difToday.toString()

        builder.setView(view2)
        builder.setTitle(context.getString(R.string.game_stats))

        builder.setNegativeButton(context.getString(R.string.close)) { dialog: DialogInterface?, id: Int -> }
        builder.show()
    }
}