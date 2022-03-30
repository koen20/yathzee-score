package nl.koenhabets.yahtzeescore.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import nl.koenhabets.yahtzeescore.data.Game
import nl.koenhabets.yahtzeescore.databinding.ScoreActivityBinding
import nl.koenhabets.yahtzeescore.view.ScoreView
import nl.koenhabets.yahtzeescore.view.YahtzeeView
import nl.koenhabets.yahtzeescore.view.YatzyView
import org.json.JSONException
import org.json.JSONObject

class ScoreActivity : AppCompatActivity() {
    private lateinit var binding: ScoreActivityBinding
    private lateinit var scoreView: ScoreView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ScoreActivityBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val data = intent.getStringExtra("data")
        if (intent.getStringExtra("game") !== null) {
            setScoreView(Game.valueOf(intent.getStringExtra("game")!!))
        } else {
            setScoreView(Game.Yatzy)
        }

        try {
            if (data != null) {
                val allScores = JSONObject(data)
                scoreView.setScores(allScores)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        scoreView.disableEdit()
    }

    private fun setScoreView(game: Game) {
        when (game) {
            Game.Yahtzee -> {
                scoreView = YahtzeeView(this, null)
                scoreView.setSpecialFieldVis(false)
            }
            Game.YahtzeeBonus -> {
                scoreView = YahtzeeView(this, null)
                scoreView.setSpecialFieldVis(true)
            }
            Game.Yatzy -> {
                scoreView = YatzyView(this, null)
            }
        }

        scoreView.id = View.generateViewId()
        scoreView.layoutParams = ViewGroup.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        binding.scoreActivityLayout.addView(scoreView)
        val set = ConstraintSet()
        set.clone(binding.scoreActivityLayout)
        set.connect(scoreView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(scoreView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        set.applyTo(binding.scoreActivityLayout)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}