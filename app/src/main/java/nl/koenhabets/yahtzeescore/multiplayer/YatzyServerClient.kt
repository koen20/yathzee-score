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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import nl.koenhabets.yahtzeescore.model.ActionType
import nl.koenhabets.yahtzeescore.model.Message

class YatzyServerClient {
    val client: HttpClient
    private val scope: CoroutineScope
    var webSocketSession: DefaultClientWebSocketSession? = null

    init {
        client = HttpClient(CIO) {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        }
        scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            startWebsocket()
        }
    }

    private suspend fun startWebsocket() {
        try {
            client.webSocket(
                method = HttpMethod.Get,
                host = "192.168.2.23",
                port = 8080,
                path = "/api/v1/ws"
            ) {
                webSocketSession = this
                login()
                while (true) {
                    val message = incoming.receive() as? Frame.Text
                    val messageString = message?.readText()
                    Log.i("YatzyServerClient", messageString!!)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {

        }
    }

    private suspend fun login() {
        Log.i("YatzyServerClient", "Start login")
        if (webSocketSession !== null) {
            val loginAction = Message.Login("userId", "key", 2)
            val message =
                Message(ActionType.login, Json.encodeToJsonElement(loginAction).jsonObject)
            webSocketSession!!.sendSerialized(message)
        }
    }
}