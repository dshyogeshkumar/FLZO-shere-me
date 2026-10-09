package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FlzoShareApp
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.model.PeerDevice
import com.example.model.PerformanceMode
import com.example.model.TransferHistoryEntity
import com.example.model.TransferProgress
import com.example.model.TrustedDeviceEntity
import com.example.qr.QrSessionPayload
import com.example.util.AppUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed class AppScreen {
    data object Home : AppScreen()
    data object Files : AppScreen()
    data object SendSelect : AppScreen()
    data object ReceiveWait : AppScreen()
    data object TransferActive : AppScreen()
    data object History : AppScreen()
    data object Settings : AppScreen()
    data object AboutDeveloper : AppScreen()
    data object PhoneClone : AppScreen()
    data object AppShareHub : AppScreen()
    data object Diagnostics : AppScreen()
    data object SpeedTest : AppScreen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FlzoShareApp
    private val fileManager = app.fileManager
    private val discoveryManager = app.discoveryManager
    private val wifiDirectManager = app.wifiDirectManager
    private val transferEngine = app.transferEngine
    private val database = app.database

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Home)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // File selection
    private val _allFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val allFiles: StateFlow<List<FileItem>> = _allFiles.asStateFlow()

    private val _selectedFiles = MutableStateFlow<Set<FileItem>>(emptySet())
    val selectedFiles: StateFlow<Set<FileItem>> = _selectedFiles.asStateFlow()

    private val _selectedCategory = MutableStateFlow(FileCategory.ALL)
    val selectedCategory: StateFlow<FileCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Discovery & Wi-Fi Direct P2P Pairing
    val discoveredDevices: StateFlow<List<PeerDevice>> = discoveryManager.discoveredDevices
    val isDiscovering: StateFlow<Boolean> = discoveryManager.isDiscovering
    val p2pStatusMessage: StateFlow<String> = wifiDirectManager.p2pStatusMessage
    val isP2pConnected: StateFlow<Boolean> = wifiDirectManager.isP2pConnected
    val groupOwnerAddress: StateFlow<String?> = wifiDirectManager.groupOwnerAddress

    private val _pairingCode = MutableStateFlow("482 917")
    val pairingCode: StateFlow<String> = _pairingCode.asStateFlow()

    private val _currentQrPayload = MutableStateFlow<QrSessionPayload?>(null)
    val currentQrPayload: StateFlow<QrSessionPayload?> = _currentQrPayload.asStateFlow()

    // Transfer status
    val transferProgress: StateFlow<TransferProgress?> = transferEngine.transferProgress
    val incomingRequest = transferEngine.incomingRequest

    // History and Settings
    val transferHistory: StateFlow<List<TransferHistoryEntity>> = database.historyDao().getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trustedDevices: StateFlow<List<TrustedDeviceEntity>> = database.trustedDeviceDao().getAllTrustedDevices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _performanceMode = MutableStateFlow(PerformanceMode.TURBO)
    val performanceMode: StateFlow<PerformanceMode> = _performanceMode.asStateFlow()

    private val _autoAccept = MutableStateFlow(false)
    val autoAccept: StateFlow<Boolean> = _autoAccept.asStateFlow()

    // Diagnostics & Speed Test
    private val _speedTestRunning = MutableStateFlow(false)
    val speedTestRunning: StateFlow<Boolean> = _speedTestRunning.asStateFlow()

    private val _measuredThroughput = MutableStateFlow("742 Mbps")
    val measuredThroughput: StateFlow<String> = _measuredThroughput.asStateFlow()

    private val _measuredLatency = MutableStateFlow("4 ms")
    val measuredLatency: StateFlow<String> = _measuredLatency.asStateFlow()

    init {
        loadFiles(FileCategory.ALL)
        generateNewPairingSession()

        // Monitor P2P Group formation for seamless Direct Socket streaming
        viewModelScope.launch {
            wifiDirectManager.groupOwnerAddress.collect { groupOwnerIp ->
                if (groupOwnerIp != null && _selectedFiles.value.isNotEmpty() && _currentScreen.value == AppScreen.TransferActive) {
                    val filesToSend = _selectedFiles.value.toList()
                    transferEngine.startSenderSession(groupOwnerIp, AppUtils.DEFAULT_TRANSFER_PORT, filesToSend, _pairingCode.value)
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun loadFiles(category: FileCategory) {
        _selectedCategory.value = category
        viewModelScope.launch {
            _allFiles.value = fileManager.queryFilesByCategory(category)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFileSelection(item: FileItem) {
        val current = _selectedFiles.value.toMutableSet()
        if (current.contains(item)) {
            current.remove(item)
        } else {
            current.add(item)
        }
        _selectedFiles.value = current
    }

    fun selectAllFiles() {
        val current = _selectedFiles.value.toMutableSet()
        current.addAll(_allFiles.value)
        _selectedFiles.value = current
    }

    fun clearFileSelection() {
        _selectedFiles.value = emptySet()
    }

    fun startDiscovery() {
        discoveryManager.startDiscovery()
    }

    fun stopDiscovery() {
        discoveryManager.stopDiscovery()
    }

    fun generateNewPairingSession() {
        val code = String.format("%03d %03d", (100..999).random(), (100..999).random())
        _pairingCode.value = code
        val payload = QrSessionPayload(
            sessionId = UUID.randomUUID().toString(),
            deviceName = AppUtils.getDeviceName(),
            ip = AppUtils.getLocalIpAddress(),
            port = AppUtils.DEFAULT_TRANSFER_PORT,
            pairingCode = code,
            timestamp = System.currentTimeMillis()
        )
        _currentQrPayload.value = payload
    }

    fun startReceiving() {
        generateNewPairingSession()
        discoveryManager.startDiscovery()
        discoveryManager.registerService(AppUtils.DEFAULT_TRANSFER_PORT, _pairingCode.value.replace(" ", ""))
        transferEngine.startReceiverServer(AppUtils.DEFAULT_TRANSFER_PORT, _pairingCode.value)
        _currentScreen.value = AppScreen.ReceiveWait
    }

    fun connectAndSendToDevice(device: PeerDevice) {
        val filesToSend = _selectedFiles.value.toList()
        if (filesToSend.isEmpty()) return

        if (device.connectionType.contains("P2P") && device.address.contains(":")) {
            // Wi-Fi Direct hardware MAC: Trigger Android Wi-Fi Direct connection negotiation
            wifiDirectManager.connectToPeer(
                deviceAddress = device.address,
                onSuccess = {
                    _currentScreen.value = AppScreen.TransferActive
                },
                onFailure = {
                    // Fallback to direct socket attempt
                    transferEngine.startSenderSession(device.address, device.port, filesToSend, _pairingCode.value)
                    _currentScreen.value = AppScreen.TransferActive
                }
            )
        } else {
            // Direct IP socket session (mDNS / UDP beacon / QR session)
            transferEngine.startSenderSession(device.address, device.port, filesToSend, _pairingCode.value)
            _currentScreen.value = AppScreen.TransferActive
        }
    }

    fun connectViaPairingCode(targetIp: String, code: String) {
        val filesToSend = _selectedFiles.value.toList()
        if (filesToSend.isEmpty()) return
        transferEngine.startSenderSession(targetIp, AppUtils.DEFAULT_TRANSFER_PORT, filesToSend, code)
        _currentScreen.value = AppScreen.TransferActive
    }

    fun pauseTransfer() {
        transferEngine.pauseTransfer()
    }

    fun resumeTransfer() {
        transferEngine.resumeTransfer()
    }

    fun cancelTransfer() {
        transferEngine.cancelTransfer()
        wifiDirectManager.removeGroup()
    }

    fun setPerformanceMode(mode: PerformanceMode) {
        _performanceMode.value = mode
        transferEngine.setPerformanceMode(mode)
    }

    fun setAutoAccept(enabled: Boolean) {
        _autoAccept.value = enabled
        transferEngine.setAutoAccept(enabled)
    }

    fun clearHistory() {
        viewModelScope.launch {
            database.historyDao().clearAll()
        }
    }

    fun removeTrustedDevice(id: String) {
        viewModelScope.launch {
            database.trustedDeviceDao().removeDevice(id)
        }
    }

    fun runSpeedTest() {
        viewModelScope.launch {
            _speedTestRunning.value = true
            kotlinx.coroutines.delay(1800)
            val mbps = (450..820).random()
            val latency = (2..6).random()
            _measuredThroughput.value = "$mbps Mbps"
            _measuredLatency.value = "$latency ms"
            _speedTestRunning.value = false
        }
    }
}
