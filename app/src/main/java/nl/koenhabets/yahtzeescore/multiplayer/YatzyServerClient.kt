package nl.koenhabets.yahtzeescore.multiplayer

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import nl.koenhabets.yahtzeescore.model.ActionType
import nl.koenhabets.yahtzeescore.model.Message
import nl.koenhabets.yahtzeescore.model.Response
import nl.koenhabets.yahtzeescore.model.ResponseType
import org.json.JSONObject
import java.util.Date

class YatzyServerClient(
    private val userId: String,
    private val userKey: String,
    private val clientVersion: Int
) {
    private val tag = "YatzyServerClient"
    private val host = "yahtzee.koenhabets.nl"
    private val hostBackup = "yatzy-backup.koenhabets.nl"
    private var yatzyServerWs: YatzyServerWs? = null
    private var yatzyServerWsBackup: YatzyServerWs? = null
    private var listener: YatzyClientListener? = null
    val subscriptions = ArrayList<String>()
    private var lastScore: Message.Score? = null
    var username: String? = null
    var game: String? = null
    private var lastGameEnd = 0L
    var wsFailCount = 0
    var loggedIn = false
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    var yatzyServerWsListener: YatzyServerWs.YatzyServerWsListener? = null

    init {
        yatzyServerWsListener = (object : YatzyServerWs.YatzyServerWsListener {
            override fun onLoggedIn() {
                loggedIn = true
                scope.launch {
                    sendSubscribe(subscriptions)
                    lastScore?.let {
                        sendScore(it)
                    }
                }
            }

            override fun onError() {
                wsFailCount++
                if (wsFailCount > 2 && yatzyServerWsBackup == null) {
                    yatzyServerWsBackup =
                        YatzyServerWs(hostBackup, userId, userKey, clientVersion, scope)
                    yatzyServerWsBackup?.setYatzyServerWsListener(yatzyServerWsListener!!)
                }
            }

            override fun onMessage(response: Response) {
                scope.launch {
                    processResponse(response)
                }
            }
        })
        yatzyServerWs = YatzyServerWs(host, userId, userKey, clientVersion, scope)
        yatzyServerWs?.setYatzyServerWsListener(yatzyServerWsListener!!)
    }

    interface YatzyClientListener {
        fun onScore(score: Response.ScoreResponse)
        fun onPair(pairResponse: Response.PairResponse)
    }

    fun setYatzyClientListener(listener: YatzyClientListener) {
        this.listener = listener
    }

    fun endGame(game: String, versionString: String, versionCode: Int) {
        // limit sending game end to once every 2.5 minutes
        if (Date().time - lastGameEnd > 150000) {
            scope.launch {
                sendGameEnd(game, versionString, versionCode)
            }
            lastGameEnd = Date().time
        }
    }

    private suspend fun sendGameEnd(game: String, versionString: String, versionCode: Int) {
        Log.i(tag, "Sending game end")
        val endAction = Message.EndGame(game, versionString, versionCode)
        val message =
            Message(ActionType.endGame, Json.encodeToJsonElement(endAction).jsonObject)
        sendMessage(message)
    }

    fun subscribe(userId: String, scannedPairCode: String? = null) {
        if (!subscriptions.contains(userId)) {
            subscriptions.add(userId)
        }
        if (loggedIn) {
            val list = ArrayList<String>()
            list.add(userId)
            scope.launch {
                sendSubscribe(list, scannedPairCode)
            }
        }
    }

    fun setScore(score: Int, fullScore: JSONObject) {
        username?.let { username ->
            val scoreObj =
                Message.Score(
                    username,
                    game ?: "",
                    score,
                    Json.decodeFromString(fullScore.toString()),
                    Date().time
                )
            lastScore = scoreObj
            if (loggedIn) {
                scope.launch {
                    sendScore(scoreObj)
                }
            }
        }
    }

    private suspend fun sendScore(score: Message.Score) {
        Log.i(tag, "Sending score ${score.score}")
        val message =
            Message(ActionType.score, Json.encodeToJsonElement(score).jsonObject)
        sendMessage(message)
    }

    private suspend fun sendSubscribe(userIds: ArrayList<String>, scannedPairCode: String? = null) {
        userIds.forEach {
            Log.i(tag, "Subscribing to $it")
            val subscribeAction = Message.Subscribe(it, scannedPairCode)
            val message =
                Message(
                    ActionType.subscribe,
                    Json.encodeToJsonElement(subscribeAction).jsonObject
                )
            sendMessage(message)
        }

    }

    private suspend fun sendMessage(message: Message) {
        yatzyServerWs?.sendMessage(message)
        yatzyServerWsBackup?.sendMessage(message)
    }

    private suspend fun processResponse(response: Response) {
        if (response.response === ResponseType.scoreResponse) {
            val resData = Json.decodeFromJsonElement<Response.ScoreResponse>(response.data)
            withContext(Dispatchers.Main) {
                listener?.onScore(resData)
            }
        } else if (response.response === ResponseType.errorResponse) {
            val resData = Json.decodeFromJsonElement<Response.ErrorResponse>(response.data)
            Log.e(tag, resData.message)
        } else if (response.response === ResponseType.pairResponse) {
            val resData = Json.decodeFromJsonElement<Response.PairResponse>(response.data)
            withContext(Dispatchers.Main) {
                listener?.onPair(resData)
            }
        }
    }

    fun disconnect() {
        yatzyServerWs?.disconnect()
        yatzyServerWsBackup?.disconnect()
    }
}