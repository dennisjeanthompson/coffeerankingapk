package com.example.coffeerankingapk.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.data.repository.UserRepository
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val userRepository = remember { UserRepository() }
    
    var displayName by remember { mutableStateOf(auth.currentUser?.displayName ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Load current user data
    LaunchedEffect(Unit) {
        isLoading = true
        userRepository.getCurrentUser().onSuccess { user ->
            displayName = user?.displayName ?: auth.currentUser?.displayName ?: ""
        }
        isLoading = false
    }
    
    Scaffold(
        containerColor = BgCream,
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgCream
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Personal Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBrown
                        )
                        
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = auth.currentUser?.email ?: "",
                            onValueChange = {},
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            singleLine = true
                        )
                        
                        Text(
                            text = "Email cannot be changed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                PrimaryButton(
                    text = if (isSaving) "Saving..." else "Save Changes",
                    onClick = {
                        if (displayName.isBlank()) {
                            Toast.makeText(context, "Display name cannot be empty", Toast.LENGTH_SHORT).show()
                            return@PrimaryButton
                        }
                        
                        isSaving = true
                        scope.launch {
                            try {
                                // Update Firebase Auth profile
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .build()
                                
                                auth.currentUser?.updateProfile(profileUpdates)?.await()
                                
                                // Update Firestore user document
                                userRepository.getCurrentUser().onSuccess { user ->
                                    if (user != null) {
                                        val updatedUser = user.copy(displayName = displayName)
                                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                            .collection("users")
                                            .document(user.uid)
                                            .update("displayName", displayName, "updatedAt", System.currentTimeMillis())
                                            .await()
                                    }
                                }
                                
                                isSaving = false
                                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            } catch (e: Exception) {
                                isSaving = false
                                Toast.makeText(context, "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isSaving && displayName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
