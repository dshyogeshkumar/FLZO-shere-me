package com.example.protocol

import org.json.JSONArray
import org.json.JSONObject

sealed class ProtocolMessage {
    abstract val type: String
    abstract fun toJson(): String

    data class HandshakeRequest(
        val sessionId: String,
        val senderName: String,
        val pairingCode: String,
        val appVersion: String = "1.0",
        val performanceMode: String = "TURBO"
    ) : ProtocolMessage() {
        override val type: String = "HANDSHAKE_REQ"
        override fun toJson(): String = JSONObject().apply {
            put("type", type)
            put("sessionId", sessionId)
            put("senderName", senderName)
            put("code", pairingCode)
            put("v", appVersion)
            put("mode", performanceMode)
        }.toString()
    }

    data class HandshakeResponse(
        val accepted: Boolean,
        val receiverName: String,
        val availableStorage: Long,
        val message: String = ""
    ) : ProtocolMessage() {
        override val type: String = "HANDSHAKE_RES"
        override fun toJson(): String = JSONObject().apply {
            put("type", type)
            put("accepted", accepted)
            put("receiverName", receiverName)
            put("storage", availableStorage)
            put("msg", message)
        }.toString()
    }

    data class MetadataHeader(
        val transferId: String,
        val totalFiles: Int,
        val totalBytes: Long,
        val files: List<MetadataFileItem>
    ) : ProtocolMessage() {
        override val type: String = "METADATA_HEADER"
        override fun toJson(): String = JSONObject().apply {
            put("type", type)
            put("transferId", transferId)
            put("totalFiles", totalFiles)
            put("totalBytes", totalBytes)
            val arr = JSONArray()
            files.forEach { f ->
                arr.put(JSONObject().apply {
                    put("id", f.id)
                    put("name", f.name)
                    put("size", f.size)
                    put("mime", f.mime)
                    put("cat", f.category)
                    put("relPath", f.relativePath)
                    put("isFolder", f.isFolder)
                    put("checksum", f.checksum)
                })
            }
            put("files", arr)
        }.toString()
    }

    data class MetadataDecision(
        val accepted: Boolean,
        val reason: String = ""
    ) : ProtocolMessage() {
        override val type: String = "METADATA_DECISION"
        override fun toJson(): String = JSONObject().apply {
            put("type", type)
            put("accepted", accepted)
            put("reason", reason)
        }.toString()
    }

    data class ChunkHeader(
        val fileIndex: Int,
        val chunkIndex: Long,
        val chunkOffset: Long,
        val chunkLength: Int,
        val isLastChunkOfFile: Boolean
    ) : ProtocolMessage() {
        override val type: String = "CHUNK_HEADER"
        override fun toJson(): String = JSONObject().apply {
            put("type", type)
            put("fileIdx", fileIndex)
            put("chunkIdx", chunkIndex)
            put("offset", chunkOffset)
            put("len", chunkLength)
            put("isLast", isLastChunkOfFile)
        }.toString()
    }

    data class ChunkAck(
        val chunkIndex: Long,
        val success: Boolean
    ) : ProtocolMessage() {
        override val type: String = "CHUNK_ACK"
        override fun toJson(): String = JSONObject().apply {
            put("type", type)
            put("chunkIdx", chunkIndex)
            put("success", success)
        }.toString()
    }

    data class FileCompleteNotification(
        val fileIndex: Int,
        val checksum: String
    ) : ProtocolMessage() {
        override val type: String = "FILE_COMPLETE"
        override fun toJson(): String = JSONObject().apply {
            put("type", type)
            put("fileIdx", fileIndex)
            put("checksum", checksum)
        }.toString()
    }

    data class SessionFinish(
        val success: Boolean,
        val totalBytes: Long
    ) : ProtocolMessage() {
        override val type: String = "SESSION_FINISH"
        override fun toJson(): String = JSONObject().apply {
            put("type", type)
            put("success", success)
            put("totalBytes", totalBytes)
        }.toString()
    }

    companion object {
        fun parse(line: String): ProtocolMessage? {
            return try {
                val obj = JSONObject(line)
                when (obj.getString("type")) {
                    "HANDSHAKE_REQ" -> HandshakeRequest(
                        sessionId = obj.getString("sessionId"),
                        senderName = obj.getString("senderName"),
                        pairingCode = obj.getString("code"),
                        appVersion = obj.optString("v", "1.0"),
                        performanceMode = obj.optString("mode", "TURBO")
                    )
                    "HANDSHAKE_RES" -> HandshakeResponse(
                        accepted = obj.getBoolean("accepted"),
                        receiverName = obj.getString("receiverName"),
                        availableStorage = obj.optLong("storage", 0L),
                        message = obj.optString("msg", "")
                    )
                    "METADATA_HEADER" -> {
                        val filesArr = obj.getJSONArray("files")
                        val fileList = mutableListOf<MetadataFileItem>()
                        for (i in 0 until filesArr.length()) {
                            val f = filesArr.getJSONObject(i)
                            fileList.add(
                                MetadataFileItem(
                                    id = f.getString("id"),
                                    name = f.getString("name"),
                                    size = f.getLong("size"),
                                    mime = f.getString("mime"),
                                    category = f.optString("cat", "OTHER"),
                                    relativePath = f.optString("relPath", ""),
                                    isFolder = f.optBoolean("isFolder", false),
                                    checksum = f.optString("checksum", "")
                                )
                            )
                        }
                        MetadataHeader(
                            transferId = obj.getString("transferId"),
                            totalFiles = obj.getInt("totalFiles"),
                            totalBytes = obj.getLong("totalBytes"),
                            files = fileList
                        )
                    }
                    "METADATA_DECISION" -> MetadataDecision(
                        accepted = obj.getBoolean("accepted"),
                        reason = obj.optString("reason", "")
                    )
                    "CHUNK_HEADER" -> ChunkHeader(
                        fileIndex = obj.getInt("fileIdx"),
                        chunkIndex = obj.getLong("chunkIdx"),
                        chunkOffset = obj.getLong("offset"),
                        chunkLength = obj.getInt("len"),
                        isLastChunkOfFile = obj.getBoolean("isLast")
                    )
                    "CHUNK_ACK" -> ChunkAck(
                        chunkIndex = obj.getLong("chunkIdx"),
                        success = obj.getBoolean("success")
                    )
                    "FILE_COMPLETE" -> FileCompleteNotification(
                        fileIndex = obj.getInt("fileIdx"),
                        checksum = obj.getString("checksum")
                    )
                    "SESSION_FINISH" -> SessionFinish(
                        success = obj.getBoolean("success"),
                        totalBytes = obj.getLong("totalBytes")
                    )
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class MetadataFileItem(
    val id: String,
    val name: String,
    val size: Long,
    val mime: String,
    val category: String,
    val relativePath: String,
    val isFolder: Boolean,
    val checksum: String
)
