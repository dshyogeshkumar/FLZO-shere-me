package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.viewmodel.AppScreen

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val screen: AppScreen,
    val testTag: String
)

@Composable
fun AppBottomNavigationBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem("HOME", Icons.Default.Home, AppScreen.Home, "bottom_nav_home"),
        NavItem("FILES", Icons.Default.Description, AppScreen.SendSelect, "bottom_nav_files"),
        NavItem("TRANSFERS", Icons.Default.SyncAlt, AppScreen.TransferActive, "bottom_nav_transfers"),
        NavItem("HISTORY", Icons.Default.History, AppScreen.History, "bottom_nav_history"),
        NavItem("SETTINGS", Icons.Default.Settings, AppScreen.Settings, "bottom_nav_settings")
    )

    NavigationBar(
        modifier = modifier
            .background(Color(0xFF090C10))
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = Color(0xFF0D111A),
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 10.sp,
                        color = if (isSelected) ElectricCyan else Color(0xFF64748B)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ElectricCyan,
                    unselectedIconColor = Color(0xFF64748B),
                    indicatorColor = Color(0xFF132238)
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
