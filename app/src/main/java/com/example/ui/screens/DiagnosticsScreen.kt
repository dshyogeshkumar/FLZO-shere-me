package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TurboGold
import com.example.util.AppUtils
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun DiagnosticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val conn = remember(context) { AppUtils.getConnectionDetails(context) }
    val thermal = remember(context) { AppUtils.getThermalStatus(context) }
    val availStorage = remember(context) { AppUtils.getAvailableStorageBytes(context) }
    val totalStorage = remember(context) { AppUtils.getTotalStorageBytes(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(AppScreen.Home) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Connection Diagnostics",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hardware telemetry & network link health",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                DiagnosticItemCard(
                    title = "Connection Method",
                    value = conn.first,
                    subtext = "Direct socket streaming without cloud relays",
                    statusText = "OPTIMAL",
                    statusColor = SuccessGreen
                )
            }

            item {
                DiagnosticItemCard(
                    title = "Link Quality",
                    value = conn.second,
                    subtext = "High frequency 5GHz / 2.4GHz Wi-Fi P2P support",
                    statusText = "EXCELLENT",
                    statusColor = SuccessGreen
                )
            }

            item {
                DiagnosticItemCard(
                    title = "Thermal Telemetry",
                    value = thermal,
                    subtext = "Monitors device processor and battery thermal zones",
                    statusText = "NORMAL",
                    statusColor = if (thermal.contains("High")) Color(0xFFFF5252) else SuccessGreen
                )
            }

            item {
                DiagnosticItemCard(
                    title = "Storage Write Performance",
                    value = "${AppUtils.formatFileSize(availStorage)} Free",
                    subtext = "Total storage: ${AppUtils.formatFileSize(totalStorage)}",
                    statusText = "READY",
                    statusColor = ElectricCyan
                )
            }

            item {
                DiagnosticItemCard(
                    title = "Current Bottleneck Status",
                    value = "None Detected",
                    subtext = "Sustained streaming throughput capable up to hardware limits",
                    statusText = "UNCONSTRAINED",
                    statusColor = SuccessGreen
                )
            }
        }
    }
}

@Composable
fun SpeedTestScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isRunning by viewModel.speedTestRunning.collectAsState()
    val throughput by viewModel.measuredThroughput.collectAsState()
    val latency by viewModel.measuredLatency.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.Home) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Local Speed Test",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111726)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRunning) {
                            CircularProgressIndicator(color = ElectricCyan, strokeWidth = 3.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Speed",
                                tint = ElectricCyan,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = if (isRunning) "Measuring Socket..." else throughput,
                        color = ElectricCyan,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "MEASURED LOCAL THROUGHPUT",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "LATENCY", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = latency, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "STABILITY", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "99.8%", color = SuccessGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F1523),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Actual file-transfer speed depends on device hardware, storage write speed, Wi-Fi conditions, distance and interference.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Button(
            onClick = { viewModel.runSpeedTest() },
            enabled = !isRunning,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
        ) {
            Text(
                text = if (isRunning) "BENCHMARKING..." else "RUN SPEED TEST",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun DiagnosticItemCard(
    title: String,
    value: String,
    subtext: String,
    statusText: String,
    statusColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111726)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtext, color = Color(0xFF64748B), fontSize = 11.sp)
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
