package nl.koenhabets.yahtzeescore.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import nl.koenhabets.yahtzeescore.data.Game
import nl.koenhabets.yahtzeescore.databinding.ScoreActivityBinding
import nl.koenhabets.yahtzeescore.view.ScoreView
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
        scoreView = ScoreView.getView(game, this)
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