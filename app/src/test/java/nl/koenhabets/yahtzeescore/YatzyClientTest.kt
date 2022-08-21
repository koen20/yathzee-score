package nl.koenhabets.yahtzeescore

import nl.koenhabets.yahtzeescore.multiplayer.YatzyServerClient
import org.json.JSONObject
import org.junit.Test

class YatzyClientTest {
    private val client = YatzyServerClient("test", "test")

    @Test
    fun login() {
        Thread.sleep(4000)
        assert(client.loggedIn)
    }

    @Test
    fun setScore() {
        Thread.sleep(4000)
        client.setScore(555, JSONObject())
    }

    @Test
    fun subscribe() {
        Thread.sleep(4000)
        client.subscribe("")
    }

    @Test
    fun endGame() {
        Thread.sleep(4000)
        client.endGame("test", "test", 0)
    }
}