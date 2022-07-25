package nl.koenhabets.yahtzeescore.multiplayer

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*

class YatzyServer {
    init {
        val client = HttpClient(CIO) {
            install(WebSockets) {
                // Configure WebSockets
            }
        }
    }
}