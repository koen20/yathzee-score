package nl.koenhabets.yahtzeescore.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessageListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.koenhabets.yahtzeescore.*
import nl.koenhabets.yahtzeescore.adapters.PlayerAdapter
import nl.koenhabets.yahtzeescore.data.AppDatabase
import nl.koenhabets.yahtzeescore.data.DataManager
import nl.koenhabets.yahtzeescore.data.Game
import nl.koenhabets.yahtzeescore.data.MigrateData
import nl.koenhabets.yahtzeescore.databinding.ActivityMainBinding
import nl.koenhabets.yahtzeescore.dialog.AddPlayerDialog
import nl.koenhabets.yahtzeescore.dialog.GameEndDialog
import nl.koenhabets.yahtzeescore.dialog.PlayerScoreDialog
import nl.koenhabets.yahtzeescore.model.PlayerItem
import nl.koenhabets.yahtzeescore.multiplayer.Multiplayer
import nl.koenhabets.yahtzeescore.multiplayer.Multiplayer.MultiplayerListener
import nl.koenhabets.yahtzeescore.view.ScoreView
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity(), OnFailureListener {
    var multiplayer: Multiplayer? = null
    var multiplayerEnabled = false
    private var playerAdapter: PlayerAdapter? = null
    private val multiplayerPlayers: MutableList<PlayerItem> = ArrayList()
    private var playerScoreDialog: PlayerScoreDialog? = null
    private var addPlayerDialog: AddPlayerDialog? = null
    private var mMessageListener: MessageListener? = null
    private var mMessage: Message? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var scoreView: ScoreView
    private var lastInitGame: Game? = null
    private lateinit var appDatabase: AppDatabase
    var score = 0
    var name: String? = null
    private var nearbyEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        DynamicColors.applyToActivitiesIfAvailable(application)

        val localPlayer = PlayerItem(id = "", null, null, null, 0, true, "")
        multiplayerPlayers.add(localPlayer)
        updateMultiplayerUI(multiplayerPlayers.indexOf(localPlayer), false)

        appDatabase = AppDatabase.getDatabase(this)

        // Set the score to 0 to prevent showing the default score
        binding.textViewTotal.text = getString(R.string.Total, 0)
        val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)

        nearbyEnabled = Date().time > sharedPref.getLong("nearbyMessages", 1717266612000)

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

        addPlayerDialog = AddPlayerDialog(this)
        playerScoreDialog = PlayerScoreDialog(this)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding.recyclerViewMultiplayer.layoutManager = layoutManager
        playerAdapter = PlayerAdapter(this, multiplayerPlayers)
        binding.recyclerViewMultiplayer.adapter = playerAdapter

        playerAdapter?.setClickListener(object : PlayerAdapter.ItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                if (position >= 0 && position < multiplayerPlayers.size) {
                    val player = multiplayerPlayers[position]
                    if (player.id != multiplayer?.userId) {
                        player.fullScore?.let {
                            playerScoreDialog?.showDialog(this@MainActivity, player, position)
                            return
                        }

                        Toast.makeText(
                            this@MainActivity, R.string.score_nearby_unavailable,
                            Toast.LENGTH_SHORT
                        ).show()
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
                    val appUpdates = AppUpdates(applicationContext)
                    val versionText = appUpdates.getVersionText()
                    if (versionText !== null) {
                        showUpdateToast(versionText)
                    }
                    appUpdates.updateConfig(context)
                }
            },
            3000
        )

        requestNearbyPermissions()
    }

    private fun initScoreView() {
        val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
        val game = Game.valueOf(sharedPref.getString("game", Game.Yahtzee.toString())!!)

        scoreView.setScoreListener(object : ScoreView.ScoreListener {
            override fun onScore(scoreReceived: Int, scores: JSONObject) {
                DataManager().saveScores(scores, applicationContext, game)
                if (multiplayerEnabled && multiplayer != null) {
                    setMultiplayerScore(scoreReceived, scores)
                }

                score = scoreReceived
                binding.textViewTotal.text = getString(R.string.Total, score)
                updateLocalPlayer()
            }
        })

        try {
            scoreView.setScores(JSONObject(sharedPref.getString("scores-$game", "")!!))
        } catch (ignored: Exception) {
        }
    }

    private fun updateLocalPlayer() {
        var playerFound: PlayerItem? = null
        multiplayerPlayers.forEach {
            if (it.isLocal) {
                it.score = score
                it.name = name
                playerFound = it
                return@forEach
            }
        }
        updateMultiplayerUI(multiplayerPlayers.indexOf(playerFound), true)
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
                if (multiplayerEnabled) {
                    if (score > 40) {
                        multiplayer?.endGame(
                            (lastInitGame ?: "").toString(),
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE
                        )
                    }
                }
                scoreView.clearScores()
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
            if (multiplayerEnabled) {
                multiplayer?.endGame(
                    (lastInitGame ?: "").toString(),
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE
                )
            }
            scoreView.clearScores()
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
        updateLocalPlayer()

        initMultiplayerObj()

        binding.textViewOp.setOnClickListener { addPlayerDialog() }
    }

    private fun initMultiplayerObj() {
        val subscriptionDao = appDatabase.subscriptionDao()
        multiplayer = Multiplayer(this, name, subscriptionDao)

        if (nearbyEnabled) {
            initNearby()
        }

        multiplayer?.setMultiplayerListener(object : MultiplayerListener {
            override fun onPlayerChanged(player: PlayerItem) {
                Log.i("main", "player changed")
                val existingPlayer = multiplayerPlayers.find { it.id == player.id }

                if (existingPlayer == null) {
                    multiplayerPlayers.add(player)
                    updateMultiplayerUI(multiplayerPlayers.indexOf(player), false)
                } else {
                    multiplayerPlayers.remove(existingPlayer)
                    val combinedPlayer = existingPlayer.copy(
                        name = player.name ?: existingPlayer.name,
                        score = player.score ?: existingPlayer.score,
                        fullScore = player.fullScore ?: existingPlayer.fullScore,
                        lastUpdate = player.lastUpdate,
                        isLocal = player.isLocal,
                        game = player.game ?: existingPlayer.game
                    )
                    multiplayerPlayers.add(combinedPlayer)
                    updateMultiplayerUI(multiplayerPlayers.indexOf(player), true)
                }

                if (playerScoreDialog?.playerShown != null) {
                    if (playerScoreDialog?.playerShown != "") {
                        playerScoreDialog?.updateScore(player)
                    }
                }

            }
        })
        lastInitGame?.let {
            multiplayer?.setGame(it.name)
        }
        setMultiplayerScore(score, scoreView.createJsonScores())
    }

    private fun setMultiplayerScore(score: Int, fullScore: JSONObject) {
        if (nearbyEnabled) {
            Nearby.getMessagesClient(this).unpublish(mMessage!!)
        }
        val date = Date()
        if (name != "" && multiplayer?.userId != null) {
            val text = name + ";" + score + ";" + date.time + ";" + multiplayer!!.userId
            Log.i("nearby", text)
            mMessage = Message(text.toByteArray())
            if (nearbyEnabled) {
                Nearby.getMessagesClient(this).publish(mMessage!!).addOnFailureListener(this)
            }
        }
        multiplayer?.setScore(score, fullScore)
    }

    private fun initNearby() {
        mMessageListener = object : MessageListener() {
            override fun onFound(message: Message) {
                Log.d("t", "Found message: " + String(message.content))
                multiplayer?.subscribeMessage(String(message.content))
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
        if (multiplayer?.userId != null && multiplayer?.pairCode != null) {
            addPlayerDialog?.showDialog(multiplayer?.userId!!, multiplayer?.pairCode!!)
        }
        addPlayerDialog?.setAddPlayerDialogListener(object :
            AddPlayerDialog.AddPlayerDialogListener {
            override fun onAddPlayer(userId: String, pairCode: String) {
                multiplayer?.subscribe(userId, pairCode)
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity, getString(R.string.player_added),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun requestPermissions() {
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf(
                        Manifest.permission.CAMERA
                    ), 23
                )
            }
        })
    }

    private fun requestNearbyPermissions() {
        val REQUIRED_PERMISSIONS: Array<String>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            REQUIRED_PERMISSIONS = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.NEARBY_WIFI_DEVICES
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            REQUIRED_PERMISSIONS = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            REQUIRED_PERMISSIONS = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            REQUIRED_PERMISSIONS = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
        ActivityCompat.requestPermissions(
            this@MainActivity, REQUIRED_PERMISSIONS, 56
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 23) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addPlayerDialog?.startCodeScanner()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Unable to add players without camera permission.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else if (requestCode == 56) {
            Log.i("Nearby", "Permissions granted")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMultiplayerUI(position: Int?, existing: Boolean?) {
        if (multiplayerPlayers.size > 1) {
            binding.recyclerViewMultiplayer.visibility = View.VISIBLE
            binding.textViewOp.setText(R.string.nearby)
        } else {
            binding.recyclerViewMultiplayer.visibility = View.GONE
            binding.textViewOp.setText(R.string.No_players_nearby)
        }

        multiplayerPlayers.sort()

        /*if (position != null && existing != null) {
            if (existing) {
                playerAdapter?.notifyItemChanged(position)
            } else {
                playerAdapter?.notifyItemInserted(position)
            }
        } else {
            playerAdapter?.notifyDataSetChanged()
        }*/
        playerAdapter?.notifyDataSetChanged()
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
            Rules.openUrl("https://koenhabets.nl/privacy_policy.html", this)
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
            if (lastInitGame !== null) {
                val url = Rules.getRules(lastInitGame!!)
                if (url !== null) {
                    Rules.openUrl(url, this)
                }
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
            val name = editTextName.text.toString()
            multiplayer?.setName(name)
        }
        builder.show()
    }

    public override fun onStart() {
        super.onStart()
        Log.i("onStart", "start")
        val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
        MigrateData(this)
        if (Game.valueOf(
                sharedPref.getString(
                    "game",
                    Game.Yahtzee.toString()
                )!!
            ) !== lastInitGame
        ) {
            setCurrentScoreView(
                Game.valueOf(
                    sharedPref.getString(
                        "game",
                        Game.Yahtzee.toString()
                    )!!
                )
            )
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
            binding.textViewOp.visibility = View.GONE
        }
    }

    public override fun onStop() {
        if (multiplayer != null) {
            Log.i("onStop", "disconnecting")
            if (nearbyEnabled) {
                try {
                    Nearby.getMessagesClient(this).unpublish(mMessage!!)
                    Nearby.getMessagesClient(this).unsubscribe(
                        mMessageListener!!
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            multiplayer?.stopMultiplayer()
        }
        super.onStop()
    }

    public override fun onResume() {
        val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", MODE_PRIVATE)
        Log.i("onResume", "start")
        scoreView.validateScores()
        if (Game.valueOf(
                sharedPref.getString(
                    "game",
                    Game.Yahtzee.toString()
                )!!
            ) !== lastInitGame
        ) {
            setCurrentScoreView(
                Game.valueOf(
                    sharedPref.getString(
                        "game",
                        Game.Yahtzee.toString()
                    )!!
                )
            )
            initScoreView()
        }
        super.onResume()
    }
}