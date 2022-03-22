package nl.koenhabets.yahtzeescore.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import nl.koenhabets.yahtzeescore.databinding.ScoreActivityBinding
import org.json.JSONException
import org.json.JSONObject

class ScoreActivity : AppCompatActivity() {
    private lateinit var binding: ScoreActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ScoreActivityBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val data = intent.getStringExtra("data")
        try {
            if (data != null) {
                val allScores = JSONObject(data)
                binding.scoreScoreView.setScores(allScores)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        binding.scoreScoreView.disableEdit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}