package com.example.model

enum class PerformanceMode {
    TURBO,
    BALANCED
}

enum class TransferDirection {
    SEND,
    RECEIVE
}

enum class TransferState {
    IDLE,
    DISCOVERING,
    PAIRING,
    CONNECTING,
    PREPARING,
    TRANSFERRING,
    PAUSED,
    RECONNECTING,
    VERIFYING,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class FileCategory {
    ALL,
    PHOTOS,
    VIDEOS,
    MUSIC,
    DOCUMENTS,
    APKS,
    ZIPS,
    FOLDERS,
    OTHER
}

data class FileItem(
    val id: String,
    val name: String,
    val path: String,
    val size: Long,
    val mimeType: String,
    val category: FileCategory,
    val lastModified: Long,
    val isFolder: Boolean = false,
    val relativePath: String = "",
    val uriString: String? = null
)

data class PeerDevice(
    val id: String,
    val name: String,
    val address: String,
    val port: Int,
    val connectionType: String, // e.g. "Wi-Fi Direct", "Local Wi-Fi", "Hotspot P2P"
    val signalStrength: String = "Excellent", // "Excellent", "Good", "Fair"
    val isTrusted: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
)

data class TransferSessionInfo(
    val sessionId: String,
    val peerName: String,
    val totalFiles: Int,
    val totalBytes: Long,
    val pairingCode: String,
    val ipAddress: String,
    val port: Int,
    val expiresAt: Long
)

data class TransferRequest(
    val sessionId: String,
    val senderName: String,
    val totalFiles: Int,
    val totalBytes: Long,
    val files: List<FileItem>
)

data class TransferProgress(
    val transferId: String,
    val currentFileName: String,
    val currentFileIndex: Int,
    val totalFiles: Int,
    val currentFileBytesTransferred: Long,
    val currentFileSizeBytes: Long,
    val totalBytesTransferred: Long,
    val totalBytes: Long,
    val currentSpeedBytesPerSec: Double,
    val averageSpeedBytesPerSec: Double,
    val peakSpeedBytesPerSec: Double,
    val etaSeconds: Long,
    val state: TransferState,
    val statusMessage: String,
    val speedHistory: List<Float> = emptyList(),
    val isVerified: Boolean = false
)
