package com.example.coffeerankingapk.ui.screens.lover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.theme.*

@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header
        item {
            AppCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture Placeholder
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(SecondaryBrown),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Coffee Lover",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown
                    )
                    
                    Text(
                        text = "coffee.lover@email.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStat(
                            title = "Reviews",
                            value = "24"
                        )
                        ProfileStat(
                            title = "Favorites",
                            value = "12"
                        )
                        ProfileStat(
                            title = "Points",
                            value = "1,250"
                        )
                    }
                }
            }
        }
        
        // Account Settings
        item {
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown
            )
        }
        
        item {
            AppCard {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Default.Person,
                        title = "Edit Profile",
                        subtitle = "Update your personal information"
                    )
                    
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    
                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Manage notification preferences"
                    )
                    
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    
                    ProfileMenuItem(
                        icon = Icons.Default.LocationOn,
                        title = "Location Settings",
                        subtitle = "Manage location preferences"
                    )
                }
            }
        }
        
        // Preferences
        item {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown
            )
        }
        
        item {
            AppCard {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Default.Star,
                        title = "My Reviews",
                        subtitle = "View and manage your reviews"
                    )
                    
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    
                    ProfileMenuItem(
                        icon = Icons.Default.FavoriteBorder,
                        title = "Favorite Cafes",
                        subtitle = "Your saved cafes"
                    )
                    
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    
                    ProfileMenuItem(
                        icon = Icons.Default.Settings,
                        title = "App Settings",
                        subtitle = "Theme, language, and more"
                    )
                }
            }
        }
        
        // Support
        item {
            Text(
                text = "Support",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown
            )
        }
        
        item {
            AppCard {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Default.HelpOutline,
                        title = "Help & Support",
                        subtitle = "Get help with the app"
                    )
                    
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    
                    ProfileMenuItem(
                        icon = Icons.Default.Info,
                        title = "About",
                        subtitle = "App version and information"
                    )
                }
            }
        }
        
        // Logout Button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Composable
private fun ProfileStat(
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PrimaryBrown
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = PrimaryBrown,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryBrown
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Go",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}