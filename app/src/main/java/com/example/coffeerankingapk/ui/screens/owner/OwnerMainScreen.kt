package com.example.coffeerankingapk.ui.screens.owner

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun OwnerMainScreen(
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToCoupons: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "Analytics") },
                    label = { Text("Analytics") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Coupons") },
                    label = { Text("Coupons") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> OwnerDashboardScreen(
                onNavigateToAnalytics = { selectedTab = 1 },
                onNavigateToCoupons = { selectedTab = 2 }
            )
            1 -> OwnerAnalyticsScreen(
                onNavigateBack = { selectedTab = 0 }
            )
            2 -> OwnerCouponsScreen(
                onNavigateBack = { selectedTab = 0 }
            )
            3 -> OwnerProfileScreen(
                onLogout = onLogout
            )
        }
    }
}