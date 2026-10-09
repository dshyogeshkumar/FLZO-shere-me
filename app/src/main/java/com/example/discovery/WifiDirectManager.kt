package com.example.discovery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Looper
import com.example.model.PeerDevice
import com.example.util.AppUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WifiDirectManager(private val context: Context) : WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

    private var wifiP2pManager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null
    private var isWifiP2pEnabled = false

    private val _p2pPeers = MutableStateFlow<List<PeerDevice>>(emptyList())
    val p2pPeers: StateFlow<List<PeerDevice>> = _p2pPeers.asStateFlow()

    private val _isP2pConnected = MutableStateFlow(false)
    val isP2pConnected: StateFlow<Boolean> = _isP2pConnected.asStateFlow()

    private val _groupOwnerAddress = MutableStateFlow<String?>(null)
    val groupOwnerAddress: StateFlow<String?> = _groupOwnerAddress.asStateFlow()

    private val _isGroupOwner = MutableStateFlow(false)
    val isGroupOwner: StateFlow<Boolean> = _isGroupOwner.asStateFlow()

    private val _p2pStatusMessage = MutableStateFlow("Wi-Fi Direct P2P Ready")
    val p2pStatusMessage: StateFlow<String> = _p2pStatusMessage.asStateFlow()

    private var receiver: BroadcastReceiver? = null

    init {
        try {
            wifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
            channel = wifiP2pManager?.initialize(context, Looper.getMainLooper()) {
                _p2pStatusMessage.value = "Wi-Fi Direct channel disconnected"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _p2pStatusMessage.value = "Wi-Fi Direct not supported on this device"
        }
    }

    fun registerReceiver() {
        if (receiver != null || wifiP2pManager == null || channel == null) return

        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                        isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                        _p2pStatusMessage.value = if (isWifiP2pEnabled) "Wi-Fi Direct active" else "Wi-Fi Direct disabled"
                    }

                    WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                        wifiP2pManager?.requestPeers(channel, this@WifiDirectManager)
                    }

                    WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                        val networkInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO, NetworkInfo::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
                        }

                        if (networkInfo?.isConnected == true) {
                            wifiP2pManager?.requestConnectionInfo(channel, this@WifiDirectManager)
                        } else {
                            _isP2pConnected.value = false
                            _groupOwnerAddress.value = null
                            _isGroupOwner.value = false
                            _p2pStatusMessage.value = "Wi-Fi Direct disconnected"
                        }
                    }

                    WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                        // Current device status changed
                    }
                }
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(receiver, intentFilter)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unregisterReceiver() {
        receiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            receiver = null
        }
    }

    fun discoverPeers(onStarted: (() -> Unit)? = null, onFailure: ((String) -> Unit)? = null) {
        registerReceiver()
        wifiP2pManager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                _p2pStatusMessage.value = "Scanning for Wi-Fi Direct & Nearby peers..."
                onStarted?.invoke()
            }

            override fun onFailure(reasonCode: Int) {
                val reason = when (reasonCode) {
                    WifiP2pManager.P2P_UNSUPPORTED -> "Wi-Fi Direct unsupported"
                    WifiP2pManager.BUSY -> "Framework busy, retrying..."
                    WifiP2pManager.ERROR -> "Internal P2P error"
                    else -> "Discovery error ($reasonCode)"
                }
                _p2pStatusMessage.value = reason
                onFailure?.invoke(reason)
            }
        })
    }

    fun createGroup(onSuccess: (() -> Unit)? = null, onFailure: ((String) -> Unit)? = null) {
        registerReceiver()
        wifiP2pManager?.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                _p2pStatusMessage.value = "Direct autonomous group created. Ready for receiver socket."
                _isGroupOwner.value = true
                onSuccess?.invoke()
            }

            override fun onFailure(reason: Int) {
                _p2pStatusMessage.value = "Failed to create P2P group: $reason"
                onFailure?.invoke("Failed code $reason")
            }
        })
    }

    fun connectToPeer(deviceAddress: String, onSuccess: (() -> Unit)? = null, onFailure: ((String) -> Unit)? = null) {
        val config = WifiP2pConfig().apply {
            this.deviceAddress = deviceAddress
        }

        wifiP2pManager?.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                _p2pStatusMessage.value = "Connecting to P2P peer $deviceAddress..."
                onSuccess?.invoke()
            }

            override fun onFailure(reason: Int) {
                _p2pStatusMessage.value = "Wi-Fi Direct connection failed: $reason"
                onFailure?.invoke("Failed code $reason")
            }
        })
    }

    fun removeGroup() {
        wifiP2pManager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                _isP2pConnected.value = false
                _groupOwnerAddress.value = null
                _isGroupOwner.value = false
                _p2pStatusMessage.value = "P2P group dissolved"
            }

            override fun onFailure(reason: Int) {
                // Ignored
            }
        })
    }

    override fun onPeersAvailable(peers: WifiP2pDeviceList?) {
        val list = mutableListOf<PeerDevice>()
        peers?.deviceList?.forEach { p2pDevice ->
            list.add(
                PeerDevice(
                    id = "p2p_${p2pDevice.deviceAddress}",
                    name = if (p2pDevice.deviceName.isNullOrBlank()) "Nearby Device (${p2pDevice.deviceAddress.takeLast(5)})" else p2pDevice.deviceName,
                    address = p2pDevice.deviceAddress,
                    port = AppUtils.DEFAULT_TRANSFER_PORT,
                    connectionType = "Wi-Fi Direct (P2P)",
                    signalStrength = getP2pStatusName(p2pDevice.status)
                )
            )
        }
        _p2pPeers.value = list
    }

    override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
        if (info == null) return
        _isP2pConnected.value = info.groupFormed
        _isGroupOwner.value = info.isGroupOwner

        val ownerAddr = info.groupOwnerAddress?.hostAddress
        _groupOwnerAddress.value = ownerAddr

        if (info.groupFormed) {
            _p2pStatusMessage.value = if (info.isGroupOwner) {
                "P2P Group Owner established. Local IP: $ownerAddr"
            } else {
                "P2P Client connected. Group Host: $ownerAddr"
            }
        }
    }

    private fun getP2pStatusName(status: Int): String {
        return when (status) {
            WifiP2pDevice.CONNECTED -> "Connected"
            WifiP2pDevice.INVITED -> "Pairing"
            WifiP2pDevice.FAILED -> "Failed"
            WifiP2pDevice.AVAILABLE -> "Available (Excellent)"
            WifiP2pDevice.UNAVAILABLE -> "Unavailable"
            else -> "Nearby"
        }
    }
}
