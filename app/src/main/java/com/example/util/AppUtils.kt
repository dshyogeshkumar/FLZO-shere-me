package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.security.MessageDigest
import java.util.Locale

object AppUtils {

    const val DEFAULT_TRANSFER_PORT = 8988
    const val DISCOVERY_PORT = 8989
    const val BUFFER_SIZE_TURBO = 512 * 1024 // 512 KB high-speed buffered streaming
    const val BUFFER_SIZE_BALANCED = 64 * 1024 // 64 KB battery-friendly streaming
    const val CHUNK_SIZE = 4 * 1024 * 1024L // 4MB chunks for resilient resumes

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, index.toDouble())
        return String.format(Locale.US, "%.2f %s", value, units[index])
    }

    fun formatSpeed(bytesPerSec: Double): String {
        if (bytesPerSec <= 0) return "0 MB/s"
        val mbps = bytesPerSec / (1024.0 * 1024.0)
        return if (mbps >= 1000) {
            String.format(Locale.US, "%.2f GB/s", mbps / 1024.0)
        } else {
            String.format(Locale.US, "%.1f MB/s", mbps)
        }
    }

    fun formatEta(seconds: Long): String {
        if (seconds <= 0) return "00:00"
        if (seconds > 86400) return ">24h"
        val mins = seconds / 60
        val secs = seconds % 60
        return if (mins >= 60) {
            val hrs = mins / 60
            val remMins = mins % 60
            String.format(Locale.US, "%02d:%02d:%02d", hrs, remMins, secs)
        } else {
            String.format(Locale.US, "%02d:%02d", mins, secs)
        }
    }

    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val host = addr.hostAddress ?: ""
                        if (!host.startsWith("127.")) {
                            return host
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "127.0.0.1"
    }

    fun getAvailableStorageBytes(context: Context): Long {
        return try {
            val path = context.getExternalFilesDir(null) ?: Environment.getDataDirectory()
            val stat = StatFs(path.path)
            stat.availableBytes
        } catch (e: Exception) {
            10L * 1024 * 1024 * 1024 // Fallback 10GB
        }
    }

    fun getTotalStorageBytes(context: Context): Long {
        return try {
            val path = context.getExternalFilesDir(null) ?: Environment.getDataDirectory()
            val stat = StatFs(path.path)
            stat.totalBytes
        } catch (e: Exception) {
            64L * 1024 * 1024 * 1024 // Fallback 64GB
        }
    }

    fun computeFileSha256(file: File, maxBytesToCheck: Long = 10 * 1024 * 1024): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var read: Int
                var totalRead = 0L
                while (input.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                    totalRead += read
                    if (maxBytesToCheck in 1..totalRead) break
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            file.length().toString() + "_" + file.name
        }
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
    }

    fun getThermalStatus(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            when (powerManager?.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> "Cool / Optimal"
                PowerManager.THERMAL_STATUS_LIGHT -> "Normal"
                PowerManager.THERMAL_STATUS_MODERATE -> "Warm"
                PowerManager.THERMAL_STATUS_SEVERE -> "Device Temperature High"
                PowerManager.THERMAL_STATUS_CRITICAL -> "Critical Heat - Throttling"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency Heat"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "Thermal Shutdown"
                else -> "Optimal"
            }
        } else {
            "Optimal"
        }
    }

    fun getConnectionDetails(context: Context): Pair<String, String> {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val net = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(net)

        return if (caps != null) {
            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    val linkSpeed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        caps.linkDownstreamBandwidthKbps / 1000
                    } else 0
                    val speedText = if (linkSpeed > 0) " (~$linkSpeed Mbps)" else ""
                    Pair("Wi-Fi Direct / Local LAN", "Excellent$speedText")
                }
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Pair("Cellular / Hotspot", "Good")
                }
                else -> Pair("Direct Socket P2P", "Good")
            }
        } else {
            Pair("Local Ad-hoc / Wi-Fi", "Ready")
        }
    }
}
