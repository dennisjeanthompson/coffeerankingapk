package com.example.coffeerankingapk.ui.screens.lover

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.data.MockData
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.RatingStars
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.Danger
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
import com.example.coffeerankingapk.ui.theme.Success
import com.example.coffeerankingapk.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoverDashboardScreen(
    onCafeClick: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    val context = LocalContext.current
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    val nearbycafes = MockData.cafes.take(3)
    val topRatedCafes = MockData.cafes.sortedByDescending { it.rating }.take(3)
    val recentlyVisited = MockData.cafes.take(2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
    ) {
        // Enhanced Header with user profile card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = PrimaryBrown,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Profile picture placeholder
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Sarah Chen",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Coffee Enthusiast",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = {
                            showToast("Notifications are coming soon")
                            onNotificationClick()
                        }
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // User stats row
                AppCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            title = "Your Rank",
                            value = "#127",
                            color = PrimaryBrown
                        )
                        Divider(
                            modifier = Modifier
                                .height(40.dp)
                                .width(1.dp),
                            color = Color.Gray.copy(alpha = 0.3f)
                        )
                        StatItem(
                            title = "Points",
                            value = "2,847",
                            color = Success
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Location row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Downtown, New York",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search bar
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    placeholder = { Text("Search for cafes...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showToast("Launching search soon")
                            onSearchClick()
                        },
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBrown,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                        disabledBorderColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Recent Activity Section (matching mockup)
            item {
                Column {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    AppCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ActivityItem(
                                icon = Icons.Default.Star,
                                title = "Rated Ethiopian Yirgacheffe",
                                subtitle = "⭐⭐⭐⭐⭐ 2h ago",
                                backgroundColor = Success.copy(alpha = 0.1f)
                            )
                            
                            Divider(color = Color.Gray.copy(alpha = 0.2f))
                            
                            ActivityItem(
                                icon = Icons.Default.Star,
                                title = "Earned 'Coffee Connoisseur' Badge",
                                subtitle = "🏆 5h ago",
                                backgroundColor = Success.copy(alpha = 0.1f)
                            )
                            
                            Divider(color = Color.Gray.copy(alpha = 0.2f))
                            
                            ActivityItem(
                                icon = Icons.Default.Favorite,
                                title = "Added Blue Mountain to Favorites",
                                subtitle = "❤️ 1d ago",
                                backgroundColor = Danger.copy(alpha = 0.1f)
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                showToast("Viewing all activity soon")
                            }
                        ) {
                            Text("View All", color = PrimaryBrown)
                        }
                    }
                }
            }
            
            // Your Top Rated Section (matching mockup)
            item {
                Column {
                    Text(
                        text = "Your Top Rated",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(listOf(
                            TopRatedItem("Ethiopian Yirgacheffe", 5.0f),
                            TopRatedItem("Blue Mountain", 4.8f), 
                            TopRatedItem("Colon Supreme", 4.5f)
                        )) { item ->
                            AppCard(
                                modifier = Modifier.width(120.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .background(
                                                PrimaryBrown.copy(alpha = 0.1f),
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "☕",
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PrimaryBrown,
                                        maxLines = 2
                                    )
                                    
                                    RatingStars(
                                        rating = item.rating,
                                        showNumeric = false
                                    )
                                }
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                showToast("See all top-rated cafes coming soon")
                            }
                        ) {
                            Text("See All", color = PrimaryBrown)
                        }
                    }
                }
            }
            
            // Recent Achievements (matching mockup)
            item {
                Column {
                    Text(
                        text = "Recent Achievements",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AchievementBadge(
                            title = "Coffee Master",
                            icon = "☕",
                            modifier = Modifier.weight(1f)
                        )
                        AchievementBadge(
                            title = "Savings Warrior", 
                            icon = "💰",
                            modifier = Modifier.weight(1f)
                        )
                        AchievementBadge(
                            title = "Streak Hero",
                            icon = "🔥",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Quick Actions
            item {
                Column {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = {
                            showToast("Opening your reviews soon")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryBrown
                        )
                    ) {
                        Text("My Reviews")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
    }
}

@Composable
private fun ActivityItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(backgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = PrimaryBrown,
                modifier = Modifier.size(20.dp)
            )
        }
        
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
                color = TextMuted
            )
        }
    }
}

@Composable
private fun AchievementBadge(
    title: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(PrimaryBrown, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = PrimaryBrown,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private data class TopRatedItem(
    val name: String,
    val rating: Float
)