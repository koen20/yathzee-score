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


class PlayerDiscovery(private val context: Context, private val userId: String) {
    private fun startPublishing() {
        // Using the default advertising option is enough since  connecting is not required.
        val advertisingOptions = AdvertisingOptions.Builder().build()
        Nearby.getConnectionsClient(context)
            .startAdvertising(
                userId,
                "nl.koenhabets.yahtzeescore",
                connectionLifecycleCallback,
                advertisingOptions
            )
            .addOnSuccessListener { unused: Void? -> }
            .addOnFailureListener { e: Exception? -> e?.printStackTrace() }
    }

    private fun startSubscription() {
        // Using the default discovery option is enough since connection is not required.
        val discoveryOptions = DiscoveryOptions.Builder().build()
        Nearby.getConnectionsClient(context) // The SERVICE_ID value must uniquely identify your app.
            // As a best practice, use the package name of your app
            // (for example, com.google.example.myapp).
            .startDiscovery(
                "nl.koenhabets.yahtzeescore",
                endpointDiscoveryCallback,
                discoveryOptions
            )
            .addOnSuccessListener { unused: Void? -> }
            .addOnFailureListener { e: java.lang.Exception? -> e?.printStackTrace() }
    }

    fun startDiscovery() {
        startPublishing()
        startSubscription()
    }

    fun stopDiscovery() {
        Nearby.getConnectionsClient(context).stopAdvertising()
        Nearby.getConnectionsClient(context).stopDiscovery()
        Nearby.getConnectionsClient(context).stopAllEndpoints()
    }

    private val endpointDiscoveryCallback: EndpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                // A remote advertising endpoint is found.
                // To retrieve the published message data.
                val message = info.endpointInfo
                Log.i("Discovery", "message: $message")
            }

            override fun onEndpointLost(endpointId: String) {
                // A previously discovered endpoint has gone away.
            }
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