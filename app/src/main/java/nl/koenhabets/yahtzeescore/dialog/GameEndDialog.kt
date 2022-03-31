package nl.koenhabets.yahtzeescore.dialog

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import nl.koenhabets.yahtzeescore.R
import nl.koenhabets.yahtzeescore.ScoreItem
import nl.koenhabets.yahtzeescore.data.DataManager
import nl.koenhabets.yahtzeescore.data.Game
import nl.koenhabets.yahtzeescore.databinding.EndGameDialogBinding
import java.util.*

class GameEndDialog(private var context: Context) {
    private var scoreItems: List<ScoreItem> = ArrayList()
    private lateinit var binding: EndGameDialogBinding

    fun showDialog(score: Int, game: Game) {
        val builder = AlertDialog.Builder(context)

        binding = EndGameDialogBinding.inflate(LayoutInflater.from(context))
        val view = binding.root

        scoreItems = DataManager().loadScores(context, game)

        var total = 0.0
        var count = 0.0
        val calendar = Calendar.getInstance()
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
        val difToday =
            Math.round((average2 - ((total - dailyTotal + score) / (count - gamesToday))) * 100) / 100.0

        binding.tVEndGamesPlayed.text = (scoreItems.size + 1).toString()
        binding.tVEndAverageChange.text = "$average2 ($dif)"
        binding.tVEndTotalToday.text = dailyTotal.toString()
        binding.tVEndDifToday.text = difToday.toString()

        builder.setView(view)
        builder.setTitle(context.getString(R.string.game_stats))

        builder.setNegativeButton(context.getString(R.string.close)) { dialog: DialogInterface?, id: Int -> }
        builder.show()
    }
}