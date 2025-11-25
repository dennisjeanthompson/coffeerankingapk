package com.example.coffeerankingapk.ui.screens.lover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeerankingapk.data.model.CoffeeShop
import com.example.coffeerankingapk.data.model.Rating
import com.example.coffeerankingapk.data.repository.CoffeeShopRepository
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.RatingStars
import com.example.coffeerankingapk.ui.theme.*
import com.example.coffeerankingapk.viewmodel.CoffeeShopViewModel
import com.example.coffeerankingapk.viewmodel.PointsViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoverDashboardScreen(
    onCafeClick: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    val coffeeShopViewModel: CoffeeShopViewModel = viewModel()
    val pointsViewModel: PointsViewModel = viewModel()
    val allShops by coffeeShopViewModel.allCoffeeShops.collectAsState()
    val userPoints by pointsViewModel.userPoints.collectAsState()
    val leaderboard by pointsViewModel.leaderboard.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val scope = rememberCoroutineScope()
    val repository = remember { CoffeeShopRepository() }
    
    // State for user's ratings
    var userRatings by remember { mutableStateOf<List<Pair<Rating, CoffeeShop>>>(emptyList()) }
    var isLoadingRatings by remember { mutableStateOf(false) }
    
    // Calculate user's rank
    val userRank = remember(leaderboard, currentUser) {
        if (currentUser != null) {
            val rank = leaderboard.indexOfFirst { it.userId == currentUser.uid }
            if (rank >= 0) rank + 1 else null
        } else null
    }
    
    // Load leaderboard
    LaunchedEffect(Unit) {
        pointsViewModel.loadLeaderboard()
    }
    
    // Load user's ratings across all shops
    LaunchedEffect(allShops) {
        if (allShops.isNotEmpty() && currentUser != null) {
            isLoadingRatings = true
            val allRatings = mutableListOf<Pair<Rating, CoffeeShop>>()
            
            allShops.forEach { shop ->
                repository.getShopRatings(shop.id).onSuccess { ratings ->
                    val userShopRatings = ratings.filter { it.userId == currentUser.uid }
                    userShopRatings.forEach { rating ->
                        allRatings.add(rating to shop)
                    }
                }
            }
            
            userRatings = allRatings.sortedByDescending { it.first.timestamp }
            isLoadingRatings = false
        }
    }
    
    // Get top 3 globally rated shops (from all shops)
    val topRatedShops = remember(allShops) {
        allShops
            .filter { it.averageRating > 0 }
            .sortedByDescending { it.averageRating }
            .take(3)
    }
    
    // Get recent activity (last 5 ratings)
    val recentActivity = remember(userRatings) {
        userRatings.take(5)
    }

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
                                text = currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "Coffee Lover",
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
                    
                    IconButton(onClick = onNotificationClick) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // User stats row - LIVE DATA
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
                            value = if (userRank != null) "#$userRank" else "N/A",
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
                            value = NumberFormat.getNumberInstance(Locale.US).format(userPoints?.totalPoints ?: 0),
                            color = Success
                        )
                    }
                }
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Recent Activity Section - LIVE DATA
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
                        if (isLoadingRatings) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PrimaryBrown)
                            }
                        } else if (recentActivity.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "No Activity",
                                        modifier = Modifier.size(48.dp),
                                        tint = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No activity yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "Start rating cafes to see your activity",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                recentActivity.forEachIndexed { index, (rating, shop) ->
                                    RealActivityItem(
                                        rating = rating,
                                        shop = shop,
                                        onClick = { onCafeClick(shop.id) }
                                    )
                                    
                                    if (index < recentActivity.size - 1) {
                                        Divider(color = Color.Gray.copy(alpha = 0.2f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Top Rated Cafes Section - GLOBAL TOP RATED
            item {
                Column {
                    Text(
                        text = "Top Rated Cafes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (topRatedShops.isEmpty()) {
                        AppCard {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "☕",
                                        style = MaterialTheme.typography.displayMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No rated cafes yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(topRatedShops) { shop ->
                                TopRatedCafeCard(
                                    shop = shop,
                                    userRating = shop.averageRating.toDouble(),
                                    onClick = { onCafeClick(shop.id) }
                                )
                            }
                        }
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
private fun RealActivityItem(
    rating: Rating,
    shop: CoffeeShop,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (rating.rating >= 4.0) Success.copy(alpha = 0.1f) else PrimaryBrown.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = if (rating.rating >= 4.0) Success else PrimaryBrown,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Rated ${shop.name}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = PrimaryBrown
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(rating.rating.toInt()) {
                    Text("⭐", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = " • ${formatTimestamp(rating.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun TopRatedCafeCard(
    shop: CoffeeShop,
    userRating: Double,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
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
                text = shop.name,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBrown,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            RatingStars(
                rating = userRating.toFloat(),
                showNumeric = true
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> {
            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
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