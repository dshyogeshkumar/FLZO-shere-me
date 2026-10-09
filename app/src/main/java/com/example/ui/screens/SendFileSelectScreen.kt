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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.ui.components.TransferPermissionPreparationDialog
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.util.AppUtils
import com.example.util.PermissionHelper
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun SendFileSelectScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allFiles by viewModel.allFiles.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showPermissionDialog by remember { mutableStateOf(false) }

    if (showPermissionDialog) {
        TransferPermissionPreparationDialog(
            actionType = "SEND",
            onAllGranted = {
                showPermissionDialog = false
                viewModel.startDiscovery()
                viewModel.navigateTo(AppScreen.Home)
            },
            onDismiss = {
                showPermissionDialog = false
            }
        )
    }

    val filteredFiles = allFiles.filter {
        if (searchQuery.isBlank()) true else it.name.contains(searchQuery, ignoreCase = true)
    }

    val totalSelectedBytes = selectedFiles.sumOf { it.size }

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
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.Home) },
                modifier = Modifier.testTag("send_screen_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Select Files to Send",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredFiles.size} items available",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
            if (selectedFiles.isNotEmpty()) {
                Text(
                    text = "Clear",
                    color = ElectricCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { viewModel.clearFileSelection() }
                        .padding(horizontal = 8.dp)
                )
            }
            Text(
                text = "Select All",
                color = ElectricCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { viewModel.selectAllFiles() }
                    .padding(horizontal = 8.dp)
            )
        }

        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search files, videos, documents...", color = Color(0xFF64748B), fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF64748B)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF111726),
                unfocusedContainerColor = Color(0xFF111726),
                focusedIndicatorColor = ElectricCyan,
                unfocusedIndicatorColor = Color(0xFF1E293B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Categories Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf(
                Pair(FileCategory.ALL, "All"),
                Pair(FileCategory.PHOTOS, "Photos"),
                Pair(FileCategory.VIDEOS, "Videos"),
                Pair(FileCategory.MUSIC, "Music"),
                Pair(FileCategory.DOCUMENTS, "Documents"),
                Pair(FileCategory.APKS, "APKs"),
                Pair(FileCategory.ZIPS, "ZIPs"),
                Pair(FileCategory.FOLDERS, "Folders")
            )
            items(categories) { (cat, label) ->
                val isSelected = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) ElectricCyan else Color(0xFF111726),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) ElectricCyan else Color(0xFF1E293B)
                    ),
                    modifier = Modifier.clickable { viewModel.loadFiles(cat) }
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Files List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredFiles) { file ->
                val isSelected = selectedFiles.contains(file)
                FileItemRow(
                    file = file,
                    isSelected = isSelected,
                    onToggle = { viewModel.toggleFileSelection(file) }
                )
            }
        }

        // Bottom Selection Bar & Continue Action
        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = Color(0xFF111726),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (selectedFiles.isEmpty()) "No files selected" else "${selectedFiles.size} files selected",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (selectedFiles.isEmpty()) "Select items to transfer" else "Total: ${AppUtils.formatFileSize(totalSelectedBytes)}",
                        color = ElectricCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        if (PermissionHelper.hasAllRuntimePermissions(context) &&
                            PermissionHelper.isWifiEnabled(context) &&
                            PermissionHelper.isLocationEnabled(context)
                        ) {
                            viewModel.startDiscovery()
                            viewModel.navigateTo(AppScreen.Home) // User can choose nearby device card to connect
                        } else {
                            showPermissionDialog = true
                        }
                    },
                    enabled = selectedFiles.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        disabledContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("send_continue_button")
                ) {
                    Text(
                        text = "CONTINUE",
                        color = if (selectedFiles.isNotEmpty()) Color(0xFF0F172A) else Color(0xFF64748B),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FileItemRow(
    file: FileItem,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .testTag("file_item_${file.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF14223A) else Color(0xFF0F1523)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) ElectricCyan.copy(alpha = 0.6f) else Color(0xFF1E293B)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getCategoryColor(file.category).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(file.category),
                    contentDescription = file.category.name,
                    tint = getCategoryColor(file.category),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "${AppUtils.formatFileSize(file.size)} • ${file.mimeType.substringAfter('/')}",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = if (isSelected) ElectricCyan else Color(0xFF475569),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

fun getCategoryIcon(cat: FileCategory): ImageVector {
    return when (cat) {
        FileCategory.PHOTOS -> Icons.Default.Image
        FileCategory.VIDEOS -> Icons.Default.Movie
        FileCategory.MUSIC -> Icons.Default.MusicNote
        FileCategory.DOCUMENTS -> Icons.Default.Description
        FileCategory.APKS -> Icons.Default.Widgets
        FileCategory.FOLDERS -> Icons.Default.Folder
        else -> Icons.Default.Description
    }
}

fun getCategoryColor(cat: FileCategory): Color {
    return when (cat) {
        FileCategory.PHOTOS -> ElectricCyan
        FileCategory.VIDEOS -> NeonBlue
        FileCategory.MUSIC -> Color(0xFF00E676)
        FileCategory.DOCUMENTS -> Color(0xFFFFB300)
        FileCategory.APKS -> Color(0xFF7C4DFF)
        FileCategory.FOLDERS -> Color(0xFFFF9100)
        else -> Color(0xFF94A3B8)
    }
}
