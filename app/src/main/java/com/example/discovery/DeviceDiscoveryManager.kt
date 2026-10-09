package com.example.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.example.model.PeerDevice
import com.example.util.AppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class DeviceDiscoveryManager(
    private val context: Context,
    val wifiDirectManager: WifiDirectManager
) {

    private val _discoveredDevices = MutableStateFlow<List<PeerDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<PeerDevice>> = _discoveredDevices.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private var nsdManager: NsdManager? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    private var broadcastSocket: DatagramSocket? = null
    private var isBroadcasting = false
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager

        // Collect Wi-Fi Direct peers and merge with discovered nearby devices
        scope.launch {
            wifiDirectManager.p2pPeers.collect { p2pList ->
                p2pList.forEach { addOrUpdateDevice(it) }
            }
        }
    }

    fun startDiscovery() {
        if (_isDiscovering.value) return
        _isDiscovering.value = true

        // 1. Wi-Fi Direct Peer Discovery
        wifiDirectManager.discoverPeers()

        // 2. Local Network mDNS / NSD Discovery
        startNsdDiscovery()

        // 3. UDP Broadcast Beaconing (Local Subnet Nearby Share)
        startUdpBroadcastListener()
        broadcastPresence()
    }

    fun stopDiscovery() {
        _isDiscovering.value = false
        stopNsdDiscovery()
        isBroadcasting = false
        try {
            broadcastSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun registerService(port: Int, sessionCode: String) {
        // Create autonomous Wi-Fi Direct group for receiver if supported
        wifiDirectManager.createGroup()

        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "FLZO_${AppUtils.getDeviceName()}_$sessionCode"
            serviceType = "_flzo._tcp."
            setPort(port)
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {}
            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
            override fun onServiceUnregistered(arg0: NsdServiceInfo) {}
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
        }

        try {
            nsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unregisterService() {
        try {
            registrationListener?.let { nsdManager?.unregisterService(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        wifiDirectManager.removeGroup()
    }

    private fun startNsdDiscovery() {
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {}

            override fun onServiceFound(service: NsdServiceInfo) {
                if (service.serviceType.contains("_flzo._tcp")) {
                    resolveService(service)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                val current = _discoveredDevices.value.toMutableList()
                current.removeAll { it.name == service.serviceName }
                _discoveredDevices.value = current
            }

            override fun onDiscoveryStopped(serviceType: String) {}
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {}
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
        }

        try {
            nsdManager?.discoverServices("_flzo._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resolveService(serviceInfo: NsdServiceInfo) {
        nsdManager?.resolveService(serviceInfo, object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                val host = serviceInfo.host?.hostAddress ?: return
                val port = serviceInfo.port
                val rawName = serviceInfo.serviceName
                val cleanName = rawName.removePrefix("FLZO_").substringBeforeLast("_")

                val device = PeerDevice(
                    id = "nsd_${host}_$port",
                    name = if (cleanName.isNotBlank()) cleanName else "Nearby Device",
                    address = host,
                    port = port,
                    connectionType = "Nearby Share (Wi-Fi P2P / LAN)",
                    signalStrength = "Excellent"
                )
                addOrUpdateDevice(device)
            }
        })
    }

    private fun stopNsdDiscovery() {
        try {
            discoveryListener?.let { nsdManager?.stopServiceDiscovery(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun broadcastPresence() {
        scope.launch {
            isBroadcasting = true
            while (isBroadcasting && _isDiscovering.value) {
                try {
                    val socket = DatagramSocket()
                    socket.broadcast = true
                    val myName = AppUtils.getDeviceName()
                    val myIp = AppUtils.getLocalIpAddress()
                    val message = "FLZO_BEACON:$myName:$myIp:${AppUtils.DEFAULT_TRANSFER_PORT}"
                    val data = message.toByteArray()
                    val packet = DatagramPacket(
                        data,
                        data.size,
                        InetAddress.getByName("255.255.255.255"),
                        AppUtils.DISCOVERY_PORT
                    )
                    socket.send(packet)
                    socket.close()
                } catch (e: Exception) {
                    // Ignored
                }
                kotlinx.coroutines.delay(3000)
            }
        }
    }

    private fun startUdpBroadcastListener() {
        scope.launch {
            try {
                broadcastSocket = DatagramSocket(AppUtils.DISCOVERY_PORT)
                val buffer = ByteArray(1024)
                while (_isDiscovering.value) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    broadcastSocket?.receive(packet)
                    val msg = String(packet.data, 0, packet.length)
                    if (msg.startsWith("FLZO_BEACON:")) {
                        val parts = msg.split(":")
                        if (parts.size >= 4) {
                            val name = parts[1]
                            val ip = parts[2]
                            val port = parts[3].toIntOrNull() ?: AppUtils.DEFAULT_TRANSFER_PORT
                            val myIp = AppUtils.getLocalIpAddress()

                            if (ip != myIp && ip != "127.0.0.1") {
                                val dev = PeerDevice(
                                    id = "udp_${ip}_$port",
                                    name = name,
                                    address = ip,
                                    port = port,
                                    connectionType = "Nearby Share (Wi-Fi P2P)",
                                    signalStrength = "Excellent"
                                )
                                addOrUpdateDevice(dev)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Socket closed or error
            }
        }
    }

    private fun addOrUpdateDevice(device: PeerDevice) {
        val current = _discoveredDevices.value.toMutableList()
        val index = current.indexOfFirst { it.address == device.address || it.id == device.id }
        if (index != -1) {
            current[index] = device
        } else {
            current.add(device)
        }
        _discoveredDevices.value = current
    }

    fun addManualPeer(address: String, port: Int, name: String) {
        val dev = PeerDevice(
            id = "manual_${address}_$port",
            name = name,
            address = address,
            port = port,
            connectionType = "Direct Pairing",
            signalStrength = "Excellent"
        )
        addOrUpdateDevice(dev)
    }
}
