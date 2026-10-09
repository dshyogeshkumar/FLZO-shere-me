package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.SuccessGreen
import com.example.util.AppUtils
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun PhoneCloneScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val availableBytes = remember(context) { AppUtils.getAvailableStorageBytes(context) }

    // Categories to clone
    val selectedCategories = remember {
        mutableStateMapOf(
            "Photos & Gallery" to true,
            "Videos & Movies" to true,
            "Music & Audio" to true,
            "Documents & PDFs" to true,
            "Installed APK Files" to true,
            "Download Archives (ZIP)" to false
        )
    }

    val estimatedBytes = 4_850_000_000L // 4.85 GB
    val hasEnoughStorage = availableBytes >= estimatedBytes

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
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
                        text = "Phone Clone Guided Mode",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Migrate personal data between Android phones",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            // Visual Old -> New Device Flow
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111726)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DeviceBadge("Old Phone", "Sender")
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "To",
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    DeviceBadge("New Phone", AppUtils.getDeviceName())
                }
            }

            // Storage Safety Pre-Check
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1523)),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (hasEnoughStorage) SuccessGreen.copy(0.4f) else Color(0xFFFF5252).copy(0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (hasEnoughStorage) Icons.Default.Storage else Icons.Default.Warning,
                        contentDescription = "Storage",
                        tint = if (hasEnoughStorage) SuccessGreen else Color(0xFFFF5252),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasEnoughStorage) "Storage Check: Sufficient Space" else "Not enough storage space",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Required: ${AppUtils.formatFileSize(estimatedBytes)} • Available: ${AppUtils.formatFileSize(availableBytes)}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Clone Category Selection
            Text(
                text = "SELECT DATA CATEGORIES",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                selectedCategories.keys.forEach { catName ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategories[catName] = !(selectedCategories[catName] ?: false)
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF111726))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = catName,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Checkbox(
                                    checked = selectedCategories[catName] ?: false,
                                    onCheckedChange = { selectedCategories[catName] = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = ElectricCyan,
                                        checkmarkColor = Color(0xFF0F172A)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Android Security Notice
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF172033),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Notice",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Android platform policy protects private sandbox app logins. Only user-selected media and files are transferred directly.",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Action Start
        Button(
            onClick = {
                viewModel.loadFiles(com.example.model.FileCategory.ALL)
                viewModel.selectAllFiles()
                viewModel.navigateTo(AppScreen.Home)
            },
            enabled = hasEnoughStorage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricCyan,
                disabledContainerColor = Color(0xFF1E293B)
            )
        ) {
            Text(
                text = if (hasEnoughStorage) "START PHONE CLONE" else "INSUFFICIENT STORAGE",
                color = if (hasEnoughStorage) Color(0xFF0F172A) else Color(0xFF64748B),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun DeviceBadge(title: String, name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Devices,
                contentDescription = title,
                tint = ElectricCyan,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(text = name, color = Color(0xFF94A3B8), fontSize = 10.sp)
    }
}
