package nl.koenhabets.yahtzeescore.multiplayer

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import java.util.*

class YatzyServerClient {
    val client: HttpClient
    private val scope: CoroutineScope
    var webSocketSession: DefaultClientWebSocketSession? = null

    init {
        client = HttpClient(CIO) {
            install(WebSockets) {
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
                host = "yahtzee.koenhabets.nl",
                port = 443,
                path = "/api/v1/ws"
            ) {
                webSocketSession = this
                while (true) {
                    if (incoming.receive() is Frame.Text) {

                        val message = incoming.receive() as? Frame.Text
                        val messageString = message?.readText()
                        println(messageString)
                        val myMessage = Scanner(System.`in`).next()
                        if (myMessage != null) {
                            send(myMessage)
                        }
                    }
                }
            }
        } finally {

        }
    }
}