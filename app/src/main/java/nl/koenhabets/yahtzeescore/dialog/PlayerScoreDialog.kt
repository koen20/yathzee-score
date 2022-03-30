package nl.koenhabets.yahtzeescore.dialog

import android.content.Context
import nl.koenhabets.yahtzeescore.multiplayer.PlayerItem
import android.view.LayoutInflater
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import nl.koenhabets.yahtzeescore.data.Game
import nl.koenhabets.yahtzeescore.databinding.ScorePopupBinding
import nl.koenhabets.yahtzeescore.view.ScoreView
import nl.koenhabets.yahtzeescore.view.YahtzeeView
import nl.koenhabets.yahtzeescore.view.YatzyView

class PlayerScoreDialog(private val context: Context) {
    var playerShown: String? = null
    private lateinit var scoreView: ScoreView
    private lateinit var binding: ScorePopupBinding


    fun showDialog(context: Context?, players2: List<PlayerItem>, position: Int, game: Game) {
        val builder = AlertDialog.Builder(
            context!!
        )
        binding = ScorePopupBinding.inflate(LayoutInflater.from(context));
        val view2 = binding.root

        builder.setView(view2)
        setScoreView(game)
        scoreView.setScores(players2[position].fullScore)
        builder.setTitle(players2[position].name)
        builder.setNegativeButton("Close") { _: DialogInterface?, _: Int -> }
        builder.show()
        playerShown = players2[position].name
        builder.setOnDismissListener { playerShown = "" }
    }

    private fun setScoreView(game: Game) {
        when (game) {
            Game.Yahtzee -> {
                scoreView = YahtzeeView(context, null)
                scoreView.setSpecialFieldVis(false)
            }
            Game.YahtzeeBonus -> {
                scoreView = YahtzeeView(context, null)
                scoreView.setSpecialFieldVis(true)
            }
            Game.Yatzy -> {
                scoreView = YatzyView(context, null)
            }
        }

        scoreView.id = View.generateViewId()
        scoreView.layoutParams = ViewGroup.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        binding.scorePopupConstraint.addView(scoreView)
        val set = ConstraintSet()
        set.clone(binding.scorePopupConstraint)
        set.connect(scoreView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(scoreView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        set.applyTo(binding.scorePopupConstraint)
        scoreView.disableEdit()
    }

    fun updateScore(players: List<PlayerItem>) {
        for (k in players.indices) {
            if (players[k].name == playerShown) {
                scoreView.setScores(players[k].fullScore)
                break
            }
        }
    }
}