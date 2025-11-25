package com.example.coffeerankingapk.ui.screens.lover

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LoverMainScreen(
    onCafeClick: (String) -> Unit = {},
    onNavigateToRewards: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToRating: (String) -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToProfilePicture: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    var requestedMapAccess by remember { mutableStateOf(false) }
    
    // Location permissions state
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    // Handle Map tab click with permission check
    val handleMapTabClick = {
        if (!locationPermissions.allPermissionsGranted) {
            // Request permissions when Map tab is clicked
            locationPermissions.launchMultiplePermissionRequest()
            requestedMapAccess = true
        }
        selectedTab = 1
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Map") },
                    label = { Text("Map") },
                    selected = selectedTab == 1,
                    onClick = handleMapTabClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Rewards") },
                    label = { Text("Rewards") },
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
            0 -> LoverDashboardScreen(
                onCafeClick = onCafeClick
            )
            1 -> {
                // Show permission explanation or map based on permission state
                if (!locationPermissions.allPermissionsGranted && requestedMapAccess) {
                    PermissionExplanationScreen(
                        locationPermissions = locationPermissions,
                        onGrantPermission = {
                            locationPermissions.launchMultiplePermissionRequest()
                        },
                        onGoBack = { 
                            selectedTab = 0
                            requestedMapAccess = false
                        }
                    )
                } else {
                    CoffeeShopMapScreen(
                        onNavigateToRating = onNavigateToRating
                    )
                }
            }
            2 -> RewardsScreen(
                onNavigateBack = { selectedTab = 0 }
            )
            3 -> ProfileScreen(
                onLogout = onLogout,
                onNavigateToEditProfile = onNavigateToEditProfile,
                onNavigateToProfilePicture = onNavigateToProfilePicture,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToFavorites = onNavigateToFavorites,
                onNavigateToLocationSettings = { /* TODO */ },
                onNavigateToMyReviews = { /* TODO */ },
                onNavigateToAppSettings = { /* TODO */ },
                onNavigateToHelp = { /* TODO */ },
                onNavigateToPrivacy = { /* TODO */ },
                onNavigateToTerms = { /* TODO */ }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionExplanationScreen(
    locationPermissions: com.google.accompanist.permissions.MultiplePermissionsState,
    onGrantPermission: () -> Unit,
    onGoBack: () -> Unit
) {
    val allPermissionsRevoked = locationPermissions.permissions.all { !it.status.isGranted }
    val shouldShowRationale = locationPermissions.permissions.any { 
        it.status.shouldShowRationale 
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Location Permission Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (shouldShowRationale) {
                "The map feature needs location permissions to:\n\n" +
                "• Show your current location\n" +
                "• Display nearby cafes\n" +
                "• Calculate distances and directions\n" +
                "• Provide accurate navigation\n\n" +
                "Your location data is only used while using the map and is never shared."
            } else {
                "To use the map feature, we need access to your location. This allows us to:\n\n" +
                "• Show your current position on the map\n" +
                "• Find cafes near you\n" +
                "• Calculate routes and distances\n\n" +
                "We respect your privacy and only use location while you're using the map."
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (allPermissionsRevoked) {
            Button(
                onClick = onGrantPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Grant Location Permission", style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = onGoBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Go Back to Home", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            // Some permissions granted, some denied
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "⚠️ Permission Denied",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You've denied location permission. To use map features, please enable location permissions in your device settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onGoBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Return to Home", style = MaterialTheme.typography.titleMedium)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "💡 Tip: You can change permissions anytime in Settings",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}