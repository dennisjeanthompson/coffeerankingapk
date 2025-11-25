package com.example.coffeerankingapk.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePictureScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val storage = FirebaseStorage.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var currentPhotoUrl by remember { mutableStateOf(auth.currentUser?.photoUrl?.toString()) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    Scaffold(
        containerColor = BgCream,
        topBar = {
            TopAppBar(
                title = { Text("Profile Picture") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgCream,
                    titleContentColor = PrimaryBrown
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Profile Picture Preview
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(PrimaryBrown.copy(alpha = 0.1f))
                        .border(4.dp, PrimaryBrown, CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        selectedImageUri != null -> {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        currentPhotoUrl != null -> {
                            AsyncImage(
                                model = currentPhotoUrl,
                                contentDescription = "Current Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Default Avatar",
                                modifier = Modifier.size(80.dp),
                                tint = PrimaryBrown.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                
                // Camera icon overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(PrimaryBrown)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Change Photo",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Tap to select a photo",
                style = MaterialTheme.typography.bodyLarge,
                color = PrimaryBrown.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Recommended: Square image, at least 400x400px",
                style = MaterialTheme.typography.bodySmall,
                color = PrimaryBrown.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Upload Button
            if (selectedImageUri != null) {
                PrimaryButton(
                    text = if (isUploading) "Uploading..." else "Upload Photo",
                    onClick = {
                        scope.launch {
                            isUploading = true
                            try {
                                val user = auth.currentUser
                                if (user != null && selectedImageUri != null) {
                                    // Upload to Firebase Storage
                                    val storageRef = storage.reference
                                        .child("profile_pictures/${user.uid}.jpg")
                                    
                                    val uploadTask = storageRef.putFile(selectedImageUri!!)
                                    uploadTask.await()
                                    
                                    // Get download URL
                                    val downloadUrl = storageRef.downloadUrl.await()
                                    
                                    // Update Firebase Auth profile
                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setPhotoUri(downloadUrl)
                                        .build()
                                    user.updateProfile(profileUpdates).await()
                                    
                                    // Update Firestore user document
                                    firestore.collection("users").document(user.uid)
                                        .update("photoUrl", downloadUrl.toString())
                                        .await()
                                    
                                    currentPhotoUrl = downloadUrl.toString()
                                    selectedImageUri = null
                                    
                                    Toast.makeText(
                                        context,
                                        "Profile picture updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Error uploading photo: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            } finally {
                                isUploading = false
                            }
                        }
                    },
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { selectedImageUri = null },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                ) {
                    Text("Cancel")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBrown)
            }
        }
    }
}
