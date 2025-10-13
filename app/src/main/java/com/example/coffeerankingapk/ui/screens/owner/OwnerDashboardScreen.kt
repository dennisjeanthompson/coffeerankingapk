package com.example.coffeerankingapk.ui.screens.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.theme.BgCream

@Composable
fun OwnerDashboardScreen(
    onNavigateToAnalytics: () -> Unit,
    onNavigateToMapPlace: () -> Unit,
    onNavigateToCoupons: () -> Unit
) {
    Scaffold(
        containerColor = BgCream
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Text(
                text = "Owner Dashboard",
                style = MaterialTheme.typography.headlineLarge
            )
            
            // KPI Cards - horizontally scrollable
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    KPICard(
                        title = "Total Reviews",
                        value = "248",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                item {
                    KPICard(
                        title = "Avg Rating",
                        value = "4.6",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                item {
                    KPICard(
                        title = "Monthly Visits",
                        value = "1,245"
                    )
                }
            }
            
            // Quick Actions
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryButton(
                    text = "View Analytics",
                    onClick = onNavigateToAnalytics,
                    modifier = Modifier.fillMaxWidth()
                )
                
                PrimaryButton(
                    text = "Place Cafe on Map",
                    onClick = onNavigateToMapPlace,
                    modifier = Modifier.fillMaxWidth()
                )
                
                PrimaryButton(
                    text = "Manage Coupons",
                    onClick = onNavigateToCoupons,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun KPICard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}