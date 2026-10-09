package com.example.util

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

enum class PermissionType {
    STORAGE,
    LOCATION,
    NEARBY_WIFI,
    BLUETOOTH,
    NOTIFICATIONS
}

data class PermissionRequirement(
    val title: String,
    val description: String,
    val iconName: String, // "wifi", "bluetooth", "location", "storage", "notification"
    val isGranted: Boolean,
    val isSystemSetting: Boolean = false, // True if requires enabling in system (Wi-Fi, GPS, Bluetooth)
    val settingsAction: String? = null
)

object PermissionHelper {

    fun getRequiredRuntimePermissions(): List<String> {
        val list = mutableListOf<String>()

        // 1. Storage & Media
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.READ_MEDIA_IMAGES)
            list.add(Manifest.permission.READ_MEDIA_VIDEO)
            list.add(Manifest.permission.READ_MEDIA_AUDIO)
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            list.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // 2. Wi-Fi Direct & Nearby Devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        // 3. Bluetooth for device pairing & discovery
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list.add(Manifest.permission.BLUETOOTH_SCAN)
            list.add(Manifest.permission.BLUETOOTH_CONNECT)
            list.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }

        // 4. Location (Mandatory for Wi-Fi Direct & Bluetooth scanning on Android 12 and below)
        list.add(Manifest.permission.ACCESS_FINE_LOCATION)
        list.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        return list
    }

    fun hasAllRuntimePermissions(context: Context): Boolean {
        for (permission in getRequiredRuntimePermissions()) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                // For NEARBY_WIFI_DEVICES or Notification on newer OS, check carefully
                if (permission == Manifest.permission.ACCESS_COARSE_LOCATION &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ) {
                    continue
                }
                return false
            }
        }
        return true
    }

    fun isWifiEnabled(context: Context): Boolean {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wifiManager?.isWifiEnabled == true
        } catch (e: Exception) {
            true
        }
    }

    fun isLocationEnabled(context: Context): Boolean {
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                    lm?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
        } catch (e: Exception) {
            true
        }
    }

    fun isBluetoothEnabled(): Boolean {
        return try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            adapter?.isEnabled == true
        } catch (e: Exception) {
            true
        }
    }

    fun getPermissionChecklist(context: Context): List<PermissionRequirement> {
        val list = mutableListOf<PermissionRequirement>()

        // 1. Wi-Fi Adapter Status
        val wifiOn = isWifiEnabled(context)
        list.add(
            PermissionRequirement(
                title = "Wi-Fi & Wi-Fi Direct",
                description = "Required to establish ultra-fast direct socket peer streams without internet.",
                iconName = "wifi",
                isGranted = wifiOn,
                isSystemSetting = true,
                settingsAction = Settings.ACTION_WIFI_SETTINGS
            )
        )

        // 2. Storage / Media Files
        val hasStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
        list.add(
            PermissionRequirement(
                title = "Files & Media Access",
                description = "Required to choose photos, videos, APKs, documents, and folders to send or save.",
                iconName = "storage",
                isGranted = hasStorage
            )
        )

        // 3. Nearby Wi-Fi Devices / Location
        val hasNearbyOrLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
        list.add(
            PermissionRequirement(
                title = "Nearby Wi-Fi Discovery",
                description = "Required by Android to detect peer devices within range for Wi-Fi P2P transfer.",
                iconName = "nearby",
                isGranted = hasNearbyOrLocation
            )
        )

        // 4. Location Service (GPS)
        val locationOn = isLocationEnabled(context)
        list.add(
            PermissionRequirement(
                title = "Location (System Service)",
                description = "Android network framework requires Location to be active during Wi-Fi P2P and Bluetooth beacon discovery.",
                iconName = "location",
                isGranted = locationOn,
                isSystemSetting = true,
                settingsAction = Settings.ACTION_LOCATION_SOURCE_SETTINGS
            )
        )

        // 5. Bluetooth Service
        val btOn = isBluetoothEnabled()
        list.add(
            PermissionRequirement(
                title = "Bluetooth Radio",
                description = "Enables fast zero-config beacon pairing, session handshake, and direct app sharing.",
                iconName = "bluetooth",
                isGranted = btOn,
                isSystemSetting = true,
                settingsAction = Settings.ACTION_BLUETOOTH_SETTINGS
            )
        )

        // 6. Foreground Notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotif = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            list.add(
                PermissionRequirement(
                    title = "Background Transfer Notifications",
                    description = "Displays real-time speed, live progress, and remaining ETA while app is in background.",
                    iconName = "notification",
                    isGranted = hasNotif
                )
            )
        }

        return list
    }
}
