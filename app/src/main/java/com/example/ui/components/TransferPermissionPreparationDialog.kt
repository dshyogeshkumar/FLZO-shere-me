package com.example.ui.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.SuccessGreen
import com.example.util.PermissionHelper
import com.example.util.PermissionRequirement

@Composable
fun TransferPermissionPreparationDialog(
    actionType: String, // "SEND" or "RECEIVE"
    onAllGranted: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var checklist by remember { mutableStateOf(PermissionHelper.getPermissionChecklist(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Refresh checklist immediately after user interaction
        checklist = PermissionHelper.getPermissionChecklist(context)
        val allOk = checklist.all { it.isGranted }
        if (allOk) {
            onAllGranted()
        }
    }

    val allGranted = checklist.all { it.isGranted }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .testTag("transfer_permission_dialog"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D121D))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Banner
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(ElectricCyan, NeonBlue)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Permissions",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (actionType == "SEND") "Send Preparation Checklist" else "Receive Preparation Checklist",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enable required services for high-speed direct P2P",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Checklist Items
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(checklist) { req ->
                        PermissionItemRow(
                            requirement = req,
                            onFixSetting = {
                                if (req.isSystemSetting && req.settingsAction != null) {
                                    try {
                                        context.startActivity(Intent(req.settingsAction))
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    val needed = PermissionHelper.getRequiredRuntimePermissions().toTypedArray()
                                    permissionLauncher.launch(needed)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val ungrantedSystem = checklist.filter { it.isSystemSetting && !it.isGranted }
                            val ungrantedRuntime = checklist.filter { !it.isSystemSetting && !it.isGranted }

                            if (ungrantedRuntime.isNotEmpty()) {
                                val permissionsToRequest = PermissionHelper.getRequiredRuntimePermissions().toTypedArray()
                                permissionLauncher.launch(permissionsToRequest)
                            } else if (ungrantedSystem.isNotEmpty()) {
                                // Direct to first system settings page
                                val first = ungrantedSystem.first()
                                if (first.settingsAction != null) {
                                    try {
                                        context.startActivity(Intent(first.settingsAction))
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            } else {
                                onAllGranted()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (allGranted) SuccessGreen else ElectricCyan
                        ),
                        modifier = Modifier.weight(1.5f).testTag("permission_grant_all_button")
                    ) {
                        Text(
                            text = if (allGranted) "CONTINUE TRANSFER" else "GRANT / ENABLE ALL",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionItemRow(
    requirement: PermissionRequirement,
    onFixSetting: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF131A28),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (requirement.isGranted) SuccessGreen.copy(alpha = 0.4f) else Color(0xFF26354D)
        ),
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
                    .background(
                        if (requirement.isGranted) SuccessGreen.copy(0.15f) else Color(0xFF1E293B)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (requirement.iconName) {
                        "wifi" -> Icons.Default.Wifi
                        "storage" -> Icons.Default.Folder
                        "location" -> Icons.Default.LocationOn
                        "bluetooth" -> Icons.Default.Bluetooth
                        "notification" -> Icons.Default.Notifications
                        else -> Icons.Default.Security
                    },
                    contentDescription = requirement.title,
                    tint = if (requirement.isGranted) SuccessGreen else ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = requirement.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = requirement.description,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (requirement.isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = SuccessGreen,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElectricCyan.copy(alpha = 0.15f),
                    modifier = Modifier.clickable(onClick = onFixSetting)
                ) {
                    Text(
                        text = if (requirement.isSystemSetting) "ENABLE" else "ALLOW",
                        color = ElectricCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
