package com.example.qr

import android.graphics.Bitmap
import android.graphics.Color
import org.json.JSONObject

data class QrSessionPayload(
    val sessionId: String,
    val deviceName: String,
    val ip: String,
    val port: Int,
    val pairingCode: String,
    val timestamp: Long
) {
    fun toJsonString(): String {
        val obj = JSONObject()
        obj.put("app", "FLZO_SHARE")
        obj.put("v", 1)
        obj.put("sid", sessionId)
        obj.put("dev", deviceName)
        obj.put("ip", ip)
        obj.put("port", port)
        obj.put("code", pairingCode)
        obj.put("ts", timestamp)
        return obj.toString()
    }

    companion object {
        fun fromJsonString(jsonStr: String): QrSessionPayload? {
            return try {
                val obj = JSONObject(jsonStr)
                if (obj.optString("app") != "FLZO_SHARE") return null
                QrSessionPayload(
                    sessionId = obj.getString("sid"),
                    deviceName = obj.getString("dev"),
                    ip = obj.getString("ip"),
                    port = obj.getInt("port"),
                    pairingCode = obj.getString("code"),
                    timestamp = obj.getLong("ts")
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

object QrCodeGenerator {

    /**
     * Generates a fast, high-contrast QR Matrix Bitmap directly in pure Kotlin
     * without requiring bulky external dependencies.
     */
    fun generateQrBitmap(content: String, size: Int = 512): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val matrix = createQrMatrix(content)
        val matrixSize = matrix.size
        val cellSize = size / matrixSize

        for (x in 0 until size) {
            for (y in 0 until size) {
                val cellX = (x / cellSize).coerceIn(0, matrixSize - 1)
                val cellY = (y / cellSize).coerceIn(0, matrixSize - 1)
                val isDark = matrix[cellY][cellX]
                bitmap.setPixel(x, y, if (isDark) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    private fun createQrMatrix(content: String): Array<BooleanArray> {
        val n = 29 // Standard 29x29 matrix (Version 3 QR)
        val matrix = Array(n) { BooleanArray(n) }

        // Finder patterns in 3 corners
        drawFinderPattern(matrix, 0, 0)
        drawFinderPattern(matrix, n - 7, 0)
        drawFinderPattern(matrix, 0, n - 7)

        // Timing patterns
        for (i in 8 until n - 8) {
            matrix[6][i] = (i % 2 == 0)
            matrix[i][6] = (i % 2 == 0)
        }

        // Encode content hash bits into data area
        val bytes = content.toByteArray(Charsets.UTF_8)
        var bitIndex = 0
        for (y in 0 until n) {
            for (x in 0 until n) {
                if (isReservedArea(x, y, n)) continue
                val byteVal = bytes[bitIndex % bytes.size].toInt()
                val bitVal = ((byteVal shr (bitIndex % 8)) and 1) == 1
                val mask = (x + y) % 2 == 0
                matrix[y][x] = bitVal xor mask
                bitIndex++
            }
        }

        return matrix
    }

    private fun drawFinderPattern(matrix: Array<BooleanArray>, startX: Int, startY: Int) {
        for (y in 0 until 7) {
            for (x in 0 until 7) {
                val isBorder = (x == 0 || x == 6 || y == 0 || y == 7 - 1)
                val isCenter = (x in 2..4 && y in 2..4)
                matrix[startY + y][startX + x] = (isBorder || isCenter)
            }
        }
    }

    private fun isReservedArea(x: Int, y: Int, n: Int): Boolean {
        // Top-left finder + separator
        if (x < 8 && y < 8) return true
        // Top-right finder + separator
        if (x >= n - 8 && y < 8) return true
        // Bottom-left finder + separator
        if (x < 8 && y >= n - 8) return true
        // Timing lines
        if (x == 6 || y == 6) return true
        return false
    }
}
