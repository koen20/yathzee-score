package nl.koenhabets.yahtzeescore.multiplayer

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import nl.koenhabets.yahtzeescore.model.ActionType
import nl.koenhabets.yahtzeescore.model.Message
import nl.koenhabets.yahtzeescore.model.Response
import nl.koenhabets.yahtzeescore.model.ResponseType
import java.util.*

class YatzyServerWs(
    private val host: String,
    private val userId: String,
    private val userKey: String,
    private val clientVersion: Int,
    private val scope: CoroutineScope
) {
    private val tag = "YatzyServerWs"
    var connecting = false
    var reconnectTimer: Timer? = null
    var webSocketSession: DefaultClientWebSocketSession? = null
    var loggedIn = false
    val client: HttpClient
    private var listener: YatzyServerWsListener? = null

    init {
        client = HttpClient(OkHttp) {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter((Json {
                    ignoreUnknownKeys = true
                }))
            }
        }
        connect()
    }

    interface YatzyServerWsListener {
        fun onLoggedIn()
        fun onError()
        fun onMessage(response: Response)
    }

    fun setYatzyServerWsListener(listener: YatzyServerWsListener) {
        this.listener = listener
    }

    fun connect() {
        scope.launch {
            startWebsocket(host)
        }

        reconnectTimer = Timer()
        reconnectTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                scope.launch {
                    if ((webSocketSession == null || !loggedIn) && !connecting) {
                        webSocketSession?.close()
                        startWebsocket(host)
                    }
                }
            }
        }, 5000, 5000)
    }

    private suspend fun startWebsocket(host: String) {
        try {
            println("Connecting to $host")
            connecting = true
            client.wss(
                method = HttpMethod.Get,
                host = host,
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
                            if (res.response === ResponseType.loginResponse) {
                                val resData =
                                    Json.decodeFromJsonElement<Response.LoginResponse>(res.data)
                                if (resData.success) {
                                    Log.i(tag, "Logged in $host")
                                    loggedIn = true
                                    listener?.onLoggedIn()
                                } else {
                                    Log.e(tag, "Login error")
                                }
                            } else {
                                listener?.onMessage(res)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            Log.i(tag, "Disconnected $host")
            webSocketSession = null
            loggedIn = false
            connecting = false
            listener?.onError()
        }
    }

    private suspend fun login() {
        Log.i(tag, "Start login $userId")
        val loginAction = Message.Login(userId, userKey, clientVersion)
        val message =
            Message(ActionType.login, Json.encodeToJsonElement(loginAction).jsonObject)
        sendMessage(message)
    }

    suspend fun sendMessage(message: Message) {
        webSocketSession?.sendSerialized(message)
    }

    fun disconnect() {
        Log.i(tag, "Disconnecting")
        scope.launch {
            webSocketSession?.close()
        }
        reconnectTimer?.cancel()
    }
}