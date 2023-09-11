package nl.koenhabets.yahtzeescore.dialog

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import nl.koenhabets.yahtzeescore.data.Game
import nl.koenhabets.yahtzeescore.databinding.ScorePopupBinding
import nl.koenhabets.yahtzeescore.model.PlayerItem
import nl.koenhabets.yahtzeescore.view.ScoreView

class PlayerScoreDialog(private val context: Context) {
    var playerShown: String? = null
    private lateinit var scoreView: ScoreView
    private lateinit var binding: ScorePopupBinding


    fun showDialog(context: Context?, player: PlayerItem, position: Int) {
        val builder = AlertDialog.Builder(
            context!!
        )
        binding = ScorePopupBinding.inflate(LayoutInflater.from(context));
        val view2 = binding.root

        builder.setView(view2)
        setScoreView(Game.valueOf(player.game))
        player.fullScore?.let { scoreView.setScores(it) }
        builder.setTitle(player.name)
        builder.setNegativeButton("Close") { _: DialogInterface?, _: Int -> }
        builder.show()
        playerShown = player.id
        builder.setOnDismissListener { playerShown = "" }
    }

    private fun setScoreView(game: Game) {
        scoreView = ScoreView.getView(game, context)
        binding.scorePopupConstraint.addView(scoreView)
        val set = ConstraintSet()
        set.clone(binding.scorePopupConstraint)
        set.connect(scoreView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(scoreView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        set.applyTo(binding.scorePopupConstraint)
        scoreView.disableEdit()
    }

    fun updateScore(player: PlayerItem) {
        if (player.id == playerShown) {
            player.fullScore?.let {
                scoreView.setScores(it)
            }
        }
    }
}