package com.example.coffeerankingapk.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.ui.components.RoleSelectorCard
import com.example.coffeerankingapk.ui.theme.BgCream

@Composable
fun RoleSelectScreen(
    onOwnerSelected: () -> Unit,
    onLoverSelected: () -> Unit
) {
    val context = LocalContext.current
    val showToast = remember(context) {
        { message: String -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        containerColor = BgCream
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "Choose Your Role",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Select how you'd like to use Coffee Ranking",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            
            // Owner role card
            RoleSelectorCard(
                title = "Cafe Owner",
                description = "Manage your cafe, track analytics, create coupons, and engage with customers.",
                isOwnerRole = true,
                onClick = {
                    showToast("Welcome to your owner dashboard!")
                    onOwnerSelected()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // Lover role card
            RoleSelectorCard(
                title = "Cafe Lover",
                description = "Discover amazing cafes, leave reviews, earn rewards, and share your experiences.",
                isOwnerRole = false,
                onClick = {
                    showToast("Welcome, coffee lover!")
                    onLoverSelected()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}