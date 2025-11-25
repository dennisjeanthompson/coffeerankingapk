package com.example.coffeerankingapk.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class NotificationSettings(
    val pushNotifications: Boolean = true,
    val emailNotifications: Boolean = false,
    val newRatingsNotification: Boolean = true,
    val newReviewsNotification: Boolean = true,
    val promotionsNotification: Boolean = false,
    val weeklyDigest: Boolean = false,
    val nearbyShopsAlert: Boolean = true,
    val achievementsNotification: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    
    var settings by remember { mutableStateOf(NotificationSettings()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Load settings from Firestore
    LaunchedEffect(Unit) {
        try {
            val user = auth.currentUser
            if (user != null) {
                val doc = firestore.collection("users").document(user.uid)
                    .collection("settings").document("notifications")
                    .get()
                    .await()
                
                if (doc.exists()) {
                    settings = NotificationSettings(
                        pushNotifications = doc.getBoolean("pushNotifications") ?: true,
                        emailNotifications = doc.getBoolean("emailNotifications") ?: false,
                        newRatingsNotification = doc.getBoolean("newRatingsNotification") ?: true,
                        newReviewsNotification = doc.getBoolean("newReviewsNotification") ?: true,
                        promotionsNotification = doc.getBoolean("promotionsNotification") ?: false,
                        weeklyDigest = doc.getBoolean("weeklyDigest") ?: false,
                        nearbyShopsAlert = doc.getBoolean("nearbyShopsAlert") ?: true,
                        achievementsNotification = doc.getBoolean("achievementsNotification") ?: true
                    )
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading settings", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }
    
    fun saveSettings() {
        scope.launch {
            isSaving = true
            try {
                val user = auth.currentUser
                if (user != null) {
                    val settingsMap = mapOf(
                        "pushNotifications" to settings.pushNotifications,
                        "emailNotifications" to settings.emailNotifications,
                        "newRatingsNotification" to settings.newRatingsNotification,
                        "newReviewsNotification" to settings.newReviewsNotification,
                        "promotionsNotification" to settings.promotionsNotification,
                        "weeklyDigest" to settings.weeklyDigest,
                        "nearbyShopsAlert" to settings.nearbyShopsAlert,
                        "achievementsNotification" to settings.achievementsNotification,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    
                    firestore.collection("users").document(user.uid)
                        .collection("settings").document("notifications")
                        .set(settingsMap)
                        .await()
                    
                    Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving settings: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isSaving = false
            }
        }
    }
    
    Scaffold(
        containerColor = BgCream,
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings") },
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBrown)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // General Section
                item {
                    Text(
                        text = "General",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown
                    )
                }
                
                item {
                    AppCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            NotificationToggle(
                                title = "Push Notifications",
                                subtitle = "Receive notifications on your device",
                                checked = settings.pushNotifications,
                                onCheckedChange = {
                                    settings = settings.copy(pushNotifications = it)
                                    saveSettings()
                                }
                            )
                            
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            
                            NotificationToggle(
                                title = "Email Notifications",
                                subtitle = "Receive updates via email",
                                checked = settings.emailNotifications,
                                onCheckedChange = {
                                    settings = settings.copy(emailNotifications = it)
                                    saveSettings()
                                }
                            )
                        }
                    }
                }
                
                // Activity Section
                item {
                    Text(
                        text = "Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                item {
                    AppCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            NotificationToggle(
                                title = "New Ratings",
                                subtitle = "When someone rates your favorite cafe",
                                checked = settings.newRatingsNotification,
                                onCheckedChange = {
                                    settings = settings.copy(newRatingsNotification = it)
                                    saveSettings()
                                }
                            )
                            
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            
                            NotificationToggle(
                                title = "New Reviews",
                                subtitle = "When new reviews are posted",
                                checked = settings.newReviewsNotification,
                                onCheckedChange = {
                                    settings = settings.copy(newReviewsNotification = it)
                                    saveSettings()
                                }
                            )
                            
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            
                            NotificationToggle(
                                title = "Achievements",
                                subtitle = "When you earn badges or rewards",
                                checked = settings.achievementsNotification,
                                onCheckedChange = {
                                    settings = settings.copy(achievementsNotification = it)
                                    saveSettings()
                                }
                            )
                        }
                    }
                }
                
                // Location & Promotions
                item {
                    Text(
                        text = "Location & Promotions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                item {
                    AppCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            NotificationToggle(
                                title = "Nearby Coffee Shops",
                                subtitle = "Alert when near a highly rated cafe",
                                checked = settings.nearbyShopsAlert,
                                onCheckedChange = {
                                    settings = settings.copy(nearbyShopsAlert = it)
                                    saveSettings()
                                }
                            )
                            
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            
                            NotificationToggle(
                                title = "Promotions & Offers",
                                subtitle = "Special deals and discounts",
                                checked = settings.promotionsNotification,
                                onCheckedChange = {
                                    settings = settings.copy(promotionsNotification = it)
                                    saveSettings()
                                }
                            )
                        }
                    }
                }
                
                // Digests
                item {
                    Text(
                        text = "Digests",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                item {
                    AppCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            NotificationToggle(
                                title = "Weekly Digest",
                                subtitle = "Summary of your activity and new cafes",
                                checked = settings.weeklyDigest,
                                onCheckedChange = {
                                    settings = settings.copy(weeklyDigest = it)
                                    saveSettings()
                                }
                            )
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        
        if (isSaving) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBrown)
            }
        }
    }
}

@Composable
private fun NotificationToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = PrimaryBrown
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = PrimaryBrown.copy(alpha = 0.6f)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryBrown,
                checkedTrackColor = PrimaryBrown.copy(alpha = 0.5f)
            )
        )
    }
}
