package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel
import java.io.File

@Composable
fun AppShareHubScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Top Bar
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
                    text = "Share FLZO Share",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Share the app with another device",
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
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF111726),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Direct APK Transfer",
                            color = ElectricCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Send FLZO Share's own APK directly to nearby Android phones over local Wi-Fi or Bluetooth. Receiver keeps full control of installation.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            item {
                ShareHubOption(
                    title = "Nearby Device",
                    subtitle = "Send APK via FLZO direct peer socket",
                    icon = Icons.Default.Devices,
                    accentColor = ElectricCyan,
                    onClick = {
                        val appInfo = context.applicationInfo
                        val apkFile = File(appInfo.sourceDir)
                        if (apkFile.exists()) {
                            viewModel.loadFiles(com.example.model.FileCategory.APKS)
                            viewModel.navigateTo(AppScreen.SendSelect)
                        }
                    }
                )
            }

            item {
                ShareHubOption(
                    title = "Android Share Sheet",
                    subtitle = "Share via installed messaging apps or Bluetooth",
                    icon = Icons.Default.Share,
                    accentColor = NeonBlue,
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Download FLZO Share — Fast. Private. Direct device-to-device file transfer app by GEN-Z.")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share FLZO Share"))
                    }
                )
            }

            item {
                ShareHubOption(
                    title = "Bluetooth App Sharing",
                    subtitle = "Compatibility transfer via Android Bluetooth stack",
                    icon = Icons.Default.Bluetooth,
                    accentColor = CyberPurple,
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "FLZO Share Android APK transfer")
                            type = "text/plain"
                            setPackage("com.android.bluetooth")
                        }
                        try {
                            context.startActivity(sendIntent)
                        } catch (e: Exception) {
                            val general = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, "FLZO Share APK")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(general, "Bluetooth Transfer"))
                        }
                    }
                )
            }

            item {
                ShareHubOption(
                    title = "Copy App Info",
                    subtitle = "Copy official app description and details",
                    icon = Icons.Default.ContentCopy,
                    accentColor = SuccessGreen,
                    onClick = {
                        clipboardManager.setText(AnnotatedString("FLZO Share — Fast. Private. Direct. Developed by GEN-Z (@Codingwithflzo)"))
                    }
                )
            }

            item {
                ShareHubOption(
                    title = "QR Code Session",
                    subtitle = "Pair receiver directly via camera scan",
                    icon = Icons.Default.QrCode,
                    accentColor = Color(0xFFFFB300),
                    onClick = { viewModel.navigateTo(AppScreen.ReceiveWait) }
                )
            }
        }
    }
}

@Composable
fun ShareHubOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Action",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
