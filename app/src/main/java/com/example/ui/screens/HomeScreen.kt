package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PerformanceMode
import com.example.ui.components.TransferPermissionPreparationDialog
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TurboGold
import com.example.util.AppUtils
import com.example.util.PermissionHelper
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val isDiscovering by viewModel.isDiscovering.collectAsState()
    val performanceMode by viewModel.performanceMode.collectAsState()
    val history by viewModel.transferHistory.collectAsState()

    val connectionDetails = remember(context) { AppUtils.getConnectionDetails(context) }
    val thermalStatus = remember(context) { AppUtils.getThermalStatus(context) }

    var pendingPermissionAction by remember { mutableStateOf<String?>(null) }

    if (pendingPermissionAction != null) {
        TransferPermissionPreparationDialog(
            actionType = pendingPermissionAction!!,
            onAllGranted = {
                val action = pendingPermissionAction
                pendingPermissionAction = null
                if (action == "SEND") {
                    viewModel.navigateTo(AppScreen.SendSelect)
                } else if (action == "RECEIVE") {
                    viewModel.startReceiving()
                }
            },
            onDismiss = {
                pendingPermissionAction = null
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP BRANDING AREA
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GEN-Z",
                        color = ElectricCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "FLZO Share",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "\"Fast. Private. Direct.\"",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Performance Mode Toggle Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (performanceMode == PerformanceMode.TURBO) TurboGold.copy(alpha = 0.15f) else Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (performanceMode == PerformanceMode.TURBO) TurboGold else Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .clickable {
                            viewModel.setPerformanceMode(
                                if (performanceMode == PerformanceMode.TURBO) PerformanceMode.BALANCED else PerformanceMode.TURBO
                            )
                        }
                        .testTag("performance_mode_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Turbo",
                            tint = if (performanceMode == PerformanceMode.TURBO) TurboGold else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (performanceMode == PerformanceMode.TURBO) "TURBO" else "BALANCED",
                            color = if (performanceMode == PerformanceMode.TURBO) TurboGold else Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // PRIMARY ACTION BUTTONS: SEND & RECEIVE
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SEND BUTTON
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clickable {
                            if (PermissionHelper.hasAllRuntimePermissions(context) &&
                                PermissionHelper.isWifiEnabled(context) &&
                                PermissionHelper.isLocationEnabled(context)
                            ) {
                                viewModel.navigateTo(AppScreen.SendSelect)
                            } else {
                                pendingPermissionAction = "SEND"
                            }
                        }
                        .testTag("home_send_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(ElectricCyan, NeonBlue)
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CallMade,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "SEND",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Select & transfer files",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // RECEIVE BUTTON
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clickable {
                            if (PermissionHelper.hasAllRuntimePermissions(context) &&
                                PermissionHelper.isWifiEnabled(context) &&
                                PermissionHelper.isLocationEnabled(context)
                            ) {
                                viewModel.startReceiving()
                            } else {
                                pendingPermissionAction = "RECEIVE"
                            }
                        }
                        .testTag("home_receive_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(CyberPurple, Color(0xFF4A148C))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CallReceived,
                                    contentDescription = "Receive",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "RECEIVE",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Ready to accept",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // QUICK UTILITIES: PHONE CLONE, APP SHARING, SPEED TEST, DIAGNOSTICS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickUtilityCard(
                    title = "Phone Clone",
                    icon = Icons.Default.Devices,
                    modifier = Modifier.weight(1f)
                ) { viewModel.navigateTo(AppScreen.PhoneClone) }

                QuickUtilityCard(
                    title = "Share App",
                    icon = Icons.Default.QrCode,
                    modifier = Modifier.weight(1f)
                ) { viewModel.navigateTo(AppScreen.AppShareHub) }

                QuickUtilityCard(
                    title = "Speed Test",
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                ) { viewModel.navigateTo(AppScreen.SpeedTest) }

                QuickUtilityCard(
                    title = "Diagnostics",
                    icon = Icons.Default.NetworkCheck,
                    modifier = Modifier.weight(1f)
                ) { viewModel.navigateTo(AppScreen.Diagnostics) }
            }
        }

        // CONNECTION & THERMAL STATUS BANNER
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF111726),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "Network",
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = connectionDetails.first,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Link: ${connectionDetails.second} • Temp: $thermalStatus",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SuccessGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "DIRECT",
                            color = SuccessGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // NEARBY DEVICES SECTION
        item {
            val p2pStatus by viewModel.p2pStatusMessage.collectAsState()
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Nearby Devices (P2P / Direct)",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isDiscovering) {
                            CircularProgressIndicator(
                                color = ElectricCyan,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (isDiscovering) viewModel.stopDiscovery() else viewModel.startDiscovery()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isDiscovering) "Stop Scan" else "Scan P2P",
                            color = ElectricCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = p2pStatus,
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
            }
        }

        if (discoveredDevices.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F1523),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = "Search",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isDiscovering) "Scanning for nearby devices via Wi-Fi Direct..." else "Tap 'Scan' to discover nearby devices",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(discoveredDevices) { dev ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.connectAndSendToDevice(dev) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = dev.name,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dev.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${dev.connectionType} • ${dev.signalStrength} connection",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = { viewModel.connectAndSendToDevice(dev) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "CONNECT",
                                color = Color(0xFF0F172A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // RECENT TRANSFERS
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transfers",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All",
                    color = ElectricCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.History) }
                )
            }
        }

        if (history.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F1523),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No transfers yet. Begin by sending or receiving files.",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(history.take(3)) { item ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF111726),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (item.direction == "SENT") NeonBlue.copy(0.2f) else CyberPurple.copy(0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (item.direction == "SENT") Icons.Default.CallMade else Icons.Default.CallReceived,
                                contentDescription = item.direction,
                                tint = if (item.direction == "SENT") NeonBlue else CyberPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${item.fileCount} files • ${AppUtils.formatFileSize(item.totalBytes)}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${item.peerName} • Avg ${AppUtils.formatSpeed(item.averageSpeed)}",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (item.status == "COMPLETED") SuccessGreen.copy(0.15f) else Color(0xFFFF5252).copy(0.15f)
                        ) {
                            Text(
                                text = item.status,
                                color = if (item.status == "COMPLETED") SuccessGreen else Color(0xFFFF5252),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun QuickUtilityCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(82.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111726))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = ElectricCyan,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color(0xFFE2E8F0),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
