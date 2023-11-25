package nl.koenhabets.yahtzeescore

import android.Manifest
import android.os.Build

class Permissions {
    fun getNearbyPermissions(): Array<String> {
        val nearbyPermissions: Array<String>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            nearbyPermissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.NEARBY_WIFI_DEVICES
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            nearbyPermissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            nearbyPermissions = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            nearbyPermissions = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
        return nearbyPermissions
    }
}