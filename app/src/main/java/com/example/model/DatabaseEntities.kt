package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfer_history")
data class TransferHistoryEntity(
    @PrimaryKey val id: String,
    val direction: String, // "SENT" or "RECEIVED"
    val peerName: String,
    val fileCount: Int,
    val totalBytes: Long,
    val averageSpeed: Double,
    val peakSpeed: Double,
    val timestamp: Long,
    val status: String, // "COMPLETED", "FAILED", "CANCELLED", "INTERRUPTED"
    val isVerified: Boolean = true
)

@Entity(tableName = "trusted_devices")
data class TrustedDeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val lastConnected: Long,
    val isTrusted: Boolean = true
)
