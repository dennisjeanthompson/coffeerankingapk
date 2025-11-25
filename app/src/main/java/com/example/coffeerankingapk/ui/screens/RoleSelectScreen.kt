package com.example.coffeerankingapk.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.data.model.UserRole
import com.example.coffeerankingapk.data.repository.UserRepository
import com.example.coffeerankingapk.ui.components.RoleSelectorCard
import com.example.coffeerankingapk.ui.theme.BgCream
import kotlinx.coroutines.launch

@Composable
fun RoleSelectScreen(
    onOwnerSelected: () -> Unit,
    onLoverSelected: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    var isLoading by remember { mutableStateOf(false) }
    
    val handleRoleSelection: (UserRole, String, () -> Unit) -> Unit = handleRole@{ role, message, onSuccess ->
        if (isLoading) return@handleRole
        
        isLoading = true
        scope.launch {
            try {
                val result = userRepository.saveUserProfile(role)
                isLoading = false
                
                result.onSuccess {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    onSuccess()
                }.onFailure { error ->
                    Toast.makeText(
                        context,
                        "Failed to save role: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(
                    context,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    Scaffold(
        containerColor = BgCream
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                        handleRoleSelection(
                            UserRole.OWNER,
                            "Welcome, Cafe Owner! 🏪",
                            onOwnerSelected
                        )
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
                        handleRoleSelection(
                            UserRole.LOVER,
                            "Welcome, Coffee Lover! ☕",
                            onLoverSelected
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Loading overlay
            if (isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}