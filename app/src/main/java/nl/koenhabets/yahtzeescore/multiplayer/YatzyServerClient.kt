package nl.koenhabets.yahtzeescore.multiplayer

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import nl.koenhabets.yahtzeescore.model.ActionType
import nl.koenhabets.yahtzeescore.model.Message
import nl.koenhabets.yahtzeescore.model.Response
import nl.koenhabets.yahtzeescore.model.ResponseType
import org.json.JSONObject
import java.util.*

class YatzyServerClient(private val userId: String, private val userKey: String, private val clientVersion: Int) {
    private val tag = "YatzyServerClient"
    private var listener: YatzyClientListener? = null
    val client: HttpClient
    private val scope: CoroutineScope
    var webSocketSession: DefaultClientWebSocketSession? = null
    var loggedIn = false
    val subscriptions = ArrayList<String>()
    private var lastScore: Message.Score? = null
    var username: String? = null
    var game: String? = null
    var connecting = false
    var reconnectTimer: Timer? = null

    init {
        client = HttpClient(CIO) {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter((Json {
                    ignoreUnknownKeys = true
                }))
            }
        }
        scope = CoroutineScope(Dispatchers.IO)
        connect()
    }

    interface YatzyClientListener {
        fun onScore(score: Response.ScoreResponse)
    }

    fun setYatzyClientListener(listener: YatzyClientListener) {
        this.listener = listener
    }

    fun connect() {
        scope.launch {
            startWebsocket()
        }

        reconnectTimer = Timer()
        reconnectTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                scope.launch {
                    if ((webSocketSession == null || !loggedIn) && !connecting) {
                        webSocketSession?.close()
                        startWebsocket()
                    }
                }
            }
        }, 5000, 5000)
    }

    fun disconnect() {
        Log.i(tag, "Disconnecting")
        scope.launch {
            webSocketSession?.close()
        }
        reconnectTimer?.cancel()
    }

    private suspend fun startWebsocket() {
        try {
            connecting = true
            client.wss(
                method = HttpMethod.Get,
                host = "yahtzee.koenhabets.nl",
                port = 443,
                path = "/api/v1/ws"
            ) {
                webSocketSession = this
                login()
                connecting = false
                while (true) {
                    val message = incoming.receive() as? Frame.Text
                    if (message !== null) {
                        try {
                            val res = Json.decodeFromString<Response>(message.readText())
                            Log.i(tag, res.toString())
                            processResponse(res)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            Log.i(tag, "Disconnected")
            webSocketSession = null
            loggedIn = false
            connecting = false
        }
    }

    fun endGame(game: String, versionString: String, versionCode: Int) {
        scope.launch {
            sendGameEnd(game, versionString, versionCode)
        }
    }

    private suspend fun sendGameEnd(game: String, versionString: String, versionCode: Int) {
        Log.i(tag, "Sending game end")
        val endAction = Message.EndGame(game, versionString, versionCode)
        val message =
            Message(ActionType.endGame, Json.encodeToJsonElement(endAction).jsonObject)
        webSocketSession?.sendSerialized(message)
    }

    fun subscribe(userId: String) {
        if (!subscriptions.contains(userId)) {
            subscriptions.add(userId)
        }
        if (loggedIn) {
            val list = ArrayList<String>()
            list.add(userId)
            scope.launch {
                sendSubscribe(list)
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
        webSocketSession?.sendSerialized(message)
    }

    private suspend fun sendSubscribe(userIds: ArrayList<String>) {
        userIds.forEach {
            Log.i(tag, "Subscribing to $it")
            val subscribeAction = Message.Subscribe(it)
            val message =
                Message(
                    ActionType.subscribe,
                    Json.encodeToJsonElement(subscribeAction).jsonObject
                )
            webSocketSession?.sendSerialized(message)
        }

    }

    private suspend fun processResponse(response: Response) {
        if (response.response === ResponseType.loginResponse) {
            val resData = Json.decodeFromJsonElement<Response.LoginResponse>(response.data)
            if (resData.success) {
                Log.i(tag, "Logged in")
                loggedIn = true
                scope.launch {
                    sendSubscribe(subscriptions)
                    lastScore?.let {
                        sendScore(it)
                    }
                }
            } else {
                Log.e(tag, "Login error")
            }
        } else if (response.response === ResponseType.scoreResponse) {
            val resData = Json.decodeFromJsonElement<Response.ScoreResponse>(response.data)
            withContext(Dispatchers.Main) {
                listener?.onScore(resData)
            }
        } else if (response.response === ResponseType.errorResponse) {
            val resData = Json.decodeFromJsonElement<Response.ErrorResponse>(response.data)
            Log.e(tag, resData.message)
        }
    }

    private suspend fun login() {
        Log.i(tag, "Start login $userId")
        val loginAction = Message.Login(userId, userKey, clientVersion)
        val message =
            Message(ActionType.login, Json.encodeToJsonElement(loginAction).jsonObject)
        webSocketSession?.sendSerialized(message)
    }

    companion object { // todo this will be moved when Multiplayer.java is rewritten
        fun getRandomString(length: Int): String {
            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
        }
    }
}