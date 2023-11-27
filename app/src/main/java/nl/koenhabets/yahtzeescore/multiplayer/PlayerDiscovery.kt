package nl.koenhabets.yahtzeescore.multiplayer

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.koenhabets.yahtzeescore.model.NearbyMessage
import java.util.Date

class PlayerDiscovery(private val context: Context, private val userId: String) {
    private var listener: PlayerDiscoveryListener? = null
    private val json = Json {
        ignoreUnknownKeys = true
    }

    interface PlayerDiscoveryListener {
        fun onMessageReceived(message: NearbyMessage)
    }

    fun setPlayerDiscoveryListener(listener: PlayerDiscoveryListener) {
        this.listener = listener
    }

    private fun startPublishing(username: String? = null, score: Int? = null) {
        val nearbyMessage = NearbyMessage(userId, username, score, Date().time)
        var nearbyMessageJson = json.encodeToString(nearbyMessage)
        if (nearbyMessageJson.length > 130) {
            nearbyMessage.u = null
            nearbyMessageJson = json.encodeToString(nearbyMessage)
        }
        if (nearbyMessageJson.length > 130) {
            nearbyMessage.s = null
            nearbyMessageJson = json.encodeToString(nearbyMessage)
        }

        // Using the default advertising option is enough since  connecting is not required.
        val advertisingOptions = AdvertisingOptions.Builder().build()
        Nearby.getConnectionsClient(context)
            .startAdvertising(
                nearbyMessageJson,
                "nl.koenhabets.yahtzeescore",
                connectionLifecycleCallback,
                advertisingOptions
            )
            .addOnSuccessListener { _: Void? -> }
            .addOnFailureListener { e: Exception? -> e?.printStackTrace() }
    }

    private fun startSubscription() {
        val discoveryOptions = DiscoveryOptions.Builder().build()
        Nearby.getConnectionsClient(context)
            .startDiscovery(
                "nl.koenhabets.yahtzeescore",
                endpointDiscoveryCallback,
                discoveryOptions
            )
            .addOnSuccessListener { Log.i("PlayerDiscovery", "Started discovery") }
            .addOnFailureListener { e: java.lang.Exception? -> e?.printStackTrace() }
    }

    fun startDiscovery() {
        startPublishing()
        startSubscription()
    }

    fun updatePlayer(username: String? = null, score: Int? = null) {
        Nearby.getConnectionsClient(context).stopAdvertising()
        startPublishing(username, score)
    }

    fun stopDiscovery() {
        Nearby.getConnectionsClient(context).stopAdvertising()
        Nearby.getConnectionsClient(context).stopDiscovery()
        Nearby.getConnectionsClient(context).stopAllEndpoints()
    }

    private val endpointDiscoveryCallback: EndpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                try {
                    val message = info.endpointInfo.toString(Charsets.UTF_8)
                    Log.i("PlayerDiscovery", "message: $message")
                    listener?.onMessageReceived(json.decodeFromString(message))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onEndpointLost(endpointId: String) {}
        }

    private val connectionLifecycleCallback: ConnectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(
                endpointId: String,
                connectionInfo: ConnectionInfo
            ) {
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {}
            override fun onDisconnected(endpointId: String) {}
        }


}