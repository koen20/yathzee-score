package nl.koenhabets.yahtzeescore.activities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessageListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import nl.koenhabets.yahtzeescore.AppUpdates
import nl.koenhabets.yahtzeescore.PlayerAdapter
import nl.koenhabets.yahtzeescore.R
import nl.koenhabets.yahtzeescore.data.DataManager
import nl.koenhabets.yahtzeescore.data.Game
import nl.koenhabets.yahtzeescore.data.MigrateData
import nl.koenhabets.yahtzeescore.databinding.ActivityMainBinding
import nl.koenhabets.yahtzeescore.dialog.GameEndDialog
import nl.koenhabets.yahtzeescore.dialog.PlayerScoreDialog
import nl.koenhabets.yahtzeescore.multiplayer.Multiplayer
import nl.koenhabets.yahtzeescore.multiplayer.Multiplayer.MultiplayerListener
import nl.koenhabets.yahtzeescore.multiplayer.PlayerItem
import nl.koenhabets.yahtzeescore.view.ScoreView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity(), OnFailureListener {
    var multiplayer: Multiplayer? = null
    var multiplayerEnabled = false
    private var firebaseUser: FirebaseUser? = null
    private lateinit var mAuth: FirebaseAuth
    private var playerAdapter: PlayerAdapter? = null
    private val players2: MutableList<PlayerItem> = ArrayList()
    var playerScoreDialog: PlayerScoreDialog? = null
    var mMessageListener: MessageListener? = null
    private var mMessage: Message? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var scoreView: ScoreView
    private var lastInitGame: Game? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        DynamicColors.applyToActivitiesIfAvailable(application)

        // Set the score to 0 to prevent showing the default score
        binding.textViewTotal.text = getString(R.string.Total, 0)
        mAuth = FirebaseAuth.getInstance()
        val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
        Log.i("multiplayer main", sharedPref.getBoolean("multiplayer", false).toString() + "d")
        if (!sharedPref.contains("version") && !sharedPref.contains("multiplayer") && !sharedPref.contains(
                "multiplayerAsked"
            )
        ) {
            sharedPref.edit().putBoolean("welcomeShown", false).apply()
            val myIntent = Intent(this, WelcomeActivity::class.java)
            this.startActivity(myIntent)
            finish()
        } else if (!sharedPref.getBoolean("welcomeShown", true)) {
            val myIntent = Intent(this, WelcomeActivity::class.java)
            this.startActivity(myIntent)
            finish()
        }
        AppCompatDelegate.setDefaultNightMode(
            sharedPref.getInt(
                "theme",
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        )

        playerScoreDialog = PlayerScoreDialog(this)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding.recyclerViewMultiplayer.layoutManager = layoutManager
        playerAdapter = PlayerAdapter(this, players2)
        binding.recyclerViewMultiplayer.adapter = playerAdapter

        playerAdapter!!.setClickListener(object : PlayerAdapter.ItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                if (position >= 0 && position < players2.size) {
                    if (players2[position].name != name) {
                        if (players2[position].fullScore.toString() != "{}") {
                            playerScoreDialog!!.showDialog(this@MainActivity, players2, position, lastInitGame?: Game.YahtzeeBonus)
                        } else {
                            Toast.makeText(
                                this@MainActivity, R.string.score_nearby_unavailable,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        })

        val context: Context = this
        binding.button.setOnClickListener { saveScoreDialog(context) }

        // Check for updates after 3 seconds
        Timer().schedule(
            object : TimerTask() {
                override fun run() {
                    val versionText = AppUpdates(applicationContext).getVersionText()
                    if (versionText !== null) {
                        showUpdateToast(versionText)
                    }
                }
            },
            3000
        )
    }

    private fun initScoreView() {
        val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
        val game = Game.valueOf(sharedPref.getString("game", Game.Yahtzee.toString())!!)

        scoreView.setScoreListener(object : ScoreView.ScoreListener {
            override fun onScoreJson(scores: JSONObject) {
                DataManager().saveScores(scores, applicationContext, game)
                if (multiplayerEnabled && multiplayer != null) {
                    multiplayer!!.setFullScore(scores)
                    if (multiplayer!!.playerAmount == 0) {
                        binding.textViewOp.setText(R.string.No_players_nearby)
                        binding.recyclerViewMultiplayer.visibility = View.GONE
                    }
                    setMultiplayerScore(score)
                }
            }

            override fun onScore(score: Int) {
                Companion.score = score
                binding.textViewTotal.text = getString(R.string.Total, score)
            }
        })

        try {
            scoreView.setScores(JSONObject(sharedPref.getString("scores-$game", "")!!))
        } catch (ignored: Exception) {
        }
    }

    private fun setCurrentScoreView(game: Game) {
        if (this::scoreView.isInitialized) {
            binding.constraintScores.removeView(scoreView)
        }
        lastInitGame = game
        scoreView = ScoreView.getView(game, this)
        binding.constraintScores.addView(scoreView)
        val set = ConstraintSet()
        set.clone(binding.constraintScores)
        set.connect(scoreView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(scoreView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        set.connect(binding.textViewTotal.id, ConstraintSet.TOP, scoreView.id, ConstraintSet.BOTTOM)
        set.connect(binding.textViewOp.id, ConstraintSet.TOP, scoreView.id, ConstraintSet.BOTTOM)
        set.applyTo(binding.constraintScores)
    }

    private fun showUpdateToast(finalUpdateText: String) {
        runOnUiThread {
            Toast.makeText(
                this@MainActivity,
                getString(R.string.update_available) + finalUpdateText,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveScoreDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.save_score)
        builder.setNegativeButton(R.string.no) { _: DialogInterface, _: Int ->
            val builder2 = AlertDialog.Builder(context)
            builder2.setTitle(R.string.score_not_save_conf)
            builder2.setNegativeButton(R.string.no) { _: DialogInterface, _: Int -> }
            builder2.setPositiveButton(R.string.yes) { _: DialogInterface, _: Int ->
                scoreView.clearScores()
                if (multiplayerEnabled) {
                    multiplayer!!.updateNearbyScore()
                }
            }
            builder2.show()
        }
        builder.setPositiveButton(R.string.yes) { _: DialogInterface, _: Int ->
            if (score < 5) {
                val toast = Toast.makeText(this, R.string.score_too_low_save, Toast.LENGTH_SHORT)
                toast.show()
            } else {
                val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
                if (sharedPref.getBoolean("endDialog", true)) {
                    val gameEndDialog = GameEndDialog(this)
                    gameEndDialog.showDialog(score, lastInitGame!!)
                }
                DataManager().saveScore(
                    score,
                    scoreView.createJsonScores(),
                    applicationContext,
                    lastInitGame!!
                )
            }
            scoreView.clearScores()
            if (multiplayerEnabled) {
                multiplayer!!.updateNearbyScore()
            }
        }
        builder.setNeutralButton(R.string.cancel) { _: DialogInterface, _: Int -> }
        builder.show()
    }

    private fun initMultiplayer() {
        val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
        Log.i("name", sharedPref.getString("name", "")!!)
        if (sharedPref.getString("name", "") == "") {
            nameDialog(this)
        } else {
            name = sharedPref.getString("name", "")
        }
        firebaseUser = mAuth.currentUser
        if (firebaseUser == null) {
            mAuth.signInAnonymously()
                .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                    if (task.isSuccessful) {
                        Log.d("MainActivity", "signInAnonymously:success")
                        firebaseUser = mAuth.currentUser
                        initMultiplayerObj(firebaseUser!!)
                    } else {
                        Log.w("MainActivity", "signInAnonymously:failure", task.exception)
                        Toast.makeText(
                            this@MainActivity, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            initMultiplayerObj(firebaseUser!!)
        }

        binding.textViewOp.setOnClickListener { addPlayerDialog() }
    }

    private fun initMultiplayerObj(firebaseUser: FirebaseUser) {
        multiplayer = Multiplayer(this, name, score, firebaseUser.uid)
        initNearby()
        multiplayer!!.setMultiplayerListener(object : MultiplayerListener {
            override fun onChange(players: MutableList<PlayerItem>) {
                if (multiplayerEnabled) {
                    // add the local player to the players list and update it on screen
                    if (name != "" && multiplayer!!.playerAmount != 0) {
                        // remove player if name already exists
                        for (i in players.indices) {
                            val playerItem = players[i]
                            if (playerItem.name == name) {
                                players.removeAt(i)
                                break
                            }
                        }
                        val item = PlayerItem(name, score, Date().time, true, true)
                        players.add(item)
                        updateMultiplayerText(players)
                    }
                }
            }

            override fun onChangeFullScore(players: List<PlayerItem>) {
                if (playerScoreDialog!!.playerShown != null) {
                    if (playerScoreDialog!!.playerShown != "") {
                        playerScoreDialog!!.updateScore(players)
                    }
                }
            }
        })
        setMultiplayerScore(score)
        multiplayer!!.setFullScore(scoreView.createJsonScores())
    }

    private fun setMultiplayerScore(score: Int) {
        Nearby.getMessagesClient(this).unpublish(mMessage!!)
        val date = Date()
        if (name != "") {
            val text = name + ";" + score + ";" + date.time + ";" + firebaseUser!!.uid
            mMessage = Message(text.toByteArray())
            Nearby.getMessagesClient(this).publish(mMessage!!).addOnFailureListener(this)
        }
        multiplayer!!.setScore(score)
    }

    private fun initNearby() {
        mMessageListener = object : MessageListener() {
            override fun onFound(message: Message) {
                Log.d("t", "Found message: " + String(message.content))
                multiplayer!!.proccessMessage(String(message.content), false, "")
            }

            override fun onLost(message: Message) {
                Log.d("d", "Lost sight of message: " + String(message.content))
            }
        }
        mMessage = Message("new player".toByteArray())
        Nearby.getMessagesClient(this).publish(mMessage!!).addOnFailureListener(this)
        Nearby.getMessagesClient(this).subscribe(mMessageListener!!)
    }

    private fun addPlayerDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.dialog_name, null)
        val editTextName = view.findViewById<EditText>(R.id.editText2)
        builder.setView(view)
        builder.setMessage(R.string.add_player)
        builder.setPositiveButton("Ok") { _: DialogInterface, _: Int ->
            if (editTextName.text.toString() != "") {
                val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
                var playersM = JSONArray()
                try {
                    playersM = JSONArray(sharedPref.getString("players", "[]"))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                playersM.put(editTextName.text.toString())
                sharedPref.edit().putString("players", playersM.toString()).apply()
                val playerItem = PlayerItem(editTextName.text.toString(), 0, 0, true, false)
                multiplayer!!.addPlayer(playerItem)
                updateMultiplayerText(multiplayer!!.players)
            }
        }
        builder.setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int -> }
        builder.show()
    }

    fun updateMultiplayerText(players: List<PlayerItem>) {
        binding.recyclerViewMultiplayer.visibility = View.VISIBLE
        binding.textViewOp.setText(R.string.nearby)
        Collections.sort(players)
        players2.clear()
        for (i in players.indices) {
            val playerItem = players[i]
            if (playerItem.isVisible) {
                players2.add(playerItem)
            }
        }
        playerAdapter!!.notifyDataSetChanged()
    }

    override fun onFailure(e: Exception) {
        e.printStackTrace()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        menu.findItem(R.id.add_player).isVisible = multiplayerEnabled
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.privacy_policy) {
            openUrl("https://koenhabets.nl/privacy_policy.html")
            return true
        } else if (itemId == R.id.scores2) {
            val myIntent = Intent(this, ScoresActivity::class.java)
            this.startActivity(myIntent)
            return true
        } else if (itemId == R.id.settings2) {
            val myIntent2 = Intent(this, SettingsActivity::class.java)
            this.startActivity(myIntent2)
            return true
        } else if (itemId == R.id.stats) {
            val myIntent3 = Intent(this, StatsActivity::class.java)
            this.startActivity(myIntent3)
            return true
        } else if (itemId == R.id.rules) {
            when (Locale.getDefault().language) {
                "nl" -> openUrl("https://nl.wikipedia.org/wiki/Yahtzee#Spelverloop")
                "fr" -> openUrl("https://fr.wikipedia.org/wiki/Yahtzee#R%C3%A8gles")
                "de" -> openUrl("https://de.wikipedia.org/wiki/Kniffel#Spielregeln")
                "pl" -> openUrl("https://pl.wikipedia.org/wiki/Ko%C5%9Bci_(gra)#Klasyczne_zasady_gry_(Yahtzee)")
                "it" -> openUrl("https://it.wikipedia.org/wiki/Yahtzee")
                else -> openUrl("https://en.wikipedia.org/wiki/Yahtzee#Rules")
            }
            return true
        } else if (itemId == R.id.add_player) {
            if (multiplayerEnabled) {
                addPlayerDialog()
                return true
            }
            return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openUrl(url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        } catch (exception: ActivityNotFoundException) {
            val toast = Toast.makeText(this, R.string.browser_fail, Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    private fun nameDialog(context: Context) {
        val builder = MaterialAlertDialogBuilder(context)
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.dialog_name, null)
        val editTextName = view.findViewById<EditText>(R.id.editText2)
        val sharedPref2 = context.getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
        editTextName.setText(sharedPref2.getString("name", ""))
        builder.setView(view)
        builder.setMessage(context.getString(R.string.name_message))
        builder.setPositiveButton("Ok") { dialog: DialogInterface?, id: Int ->
            val sharedPref =
                context.getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
            sharedPref.edit().putString("name", editTextName.text.toString()).apply()
            name = editTextName.text.toString()
            multiplayer!!.setName(name)
        }
        builder.show()
    }

    public override fun onStart() {
        super.onStart()
        Log.i("onStart", "start")
        val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
        MigrateData(this)
        if (Game.valueOf(sharedPref.getString("game", Game.Yahtzee.toString())!!) !== lastInitGame) {
            setCurrentScoreView(Game.valueOf(sharedPref.getString("game", Game.Yahtzee.toString())!!))
            initScoreView()
        }
        if (sharedPref.getBoolean("multiplayer", false)) {
            initMultiplayer()
            multiplayerEnabled = true
            binding.recyclerViewMultiplayer.visibility = View.GONE
            binding.textViewOp.visibility = View.VISIBLE
            binding.textViewOp.setText(R.string.No_players_nearby)
        } else {
            multiplayerEnabled = false
            binding.recyclerViewMultiplayer.visibility = View.GONE
            binding.recyclerViewMultiplayer.visibility = View.GONE
        }
    }

    public override fun onStop() {
        if (multiplayer != null) {
            Log.i("onStop", "disconnecting")
            try {
                Nearby.getMessagesClient(this).unpublish(mMessage!!)
                Nearby.getMessagesClient(this).unsubscribe(
                    mMessageListener!!
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            multiplayer!!.stopMultiplayer()
        }
        super.onStop()
    }

    public override fun onResume() {
        val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
        Log.i("onResume", "start")
        if (Game.valueOf(sharedPref.getString("game", Game.Yahtzee.toString())!!) !== lastInitGame) {
            setCurrentScoreView(Game.valueOf(sharedPref.getString("game", Game.Yahtzee.toString())!!))
            initScoreView()
        }
        super.onResume()
    }

    companion object {
        var name: String? = ""
        var score = 0
    }
}