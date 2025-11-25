package com.example.coffeerankingapk.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeerankingapk.data.model.Rating
import com.example.coffeerankingapk.data.repository.CoffeeShopRepository
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.theme.*
import com.example.coffeerankingapk.viewmodel.CoffeeShopViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun OwnerDashboardScreen(
    onNavigateToAnalytics: () -> Unit,
    onNavigateToCoupons: () -> Unit,
    onNavigateToAddShop: () -> Unit = {}
) {
    val coffeeShopViewModel: CoffeeShopViewModel = viewModel()
    val ownerShops by coffeeShopViewModel.ownerShops.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val scope = rememberCoroutineScope()
    val repository = remember { CoffeeShopRepository() }
    
    // State for recent ratings
    var recentRatings by remember { mutableStateOf<List<Pair<Rating, String>>>(emptyList()) }
    var isLoadingRatings by remember { mutableStateOf(false) }
    
    // Filter shops to only those owned by current user
    val myShops = remember(ownerShops, currentUserId) {
        ownerShops.filter { it.ownerId == currentUserId }
    }
    
    val myShop = myShops.firstOrNull()
    
    // Load recent ratings when shop is available
    LaunchedEffect(myShop?.id) {
        myShop?.let { shop ->
            isLoadingRatings = true
            scope.launch {
                repository.getShopRatings(shop.id).onSuccess { ratings ->
                    // Get the 5 most recent ratings
                    recentRatings = ratings
                        .sortedByDescending { it.timestamp }
                        .take(5)
                        .map { it to shop.name }
                    isLoadingRatings = false
                }.onFailure {
                    isLoadingRatings = false
                }
            }
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Show empty state if owner has no shops
        if (myShop == null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "No Shop",
                            modifier = Modifier.size(80.dp),
                            tint = TextMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Coffee Shop Yet",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBrown
                        )
                        Text(
                            text = "Create your first coffee shop to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        PrimaryButton(
                            text = "Add Your Coffee Shop",
                            onClick = onNavigateToAddShop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            return@LazyColumn
        }
        
        // Header with welcome message (show shop name)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome back!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                        Text(
                            text = myShop.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBrown
                        )
                        Text(
                            text = myShop.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Cafe",
                        modifier = Modifier.size(48.dp),
                        tint = PrimaryBrown
                    )
                }
            }
        }
        
        // KPI Cards
        item {
            Column {
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        KPICard(
                            title = "Total Reviews",
                            value = myShop.totalRatings.toString(),
                            change = if (myShop.totalRatings > 0) "${myShop.totalRatings} reviews" else "No reviews yet",
                            icon = Icons.Default.Star,
                            changePositive = true
                        )
                    }
                    item {
                        KPICard(
                            title = "Avg Rating",
                            value = if (myShop.averageRating > 0) "%.1f".format(myShop.averageRating) else "N/A",
                            change = if (myShop.averageRating > 0) "⭐ ${myShop.averageRating.roundToInt()} stars" else "No ratings",
                            icon = Icons.Default.Star,
                            changePositive = true
                        )
                    }
                    item {
                        KPICard(
                            title = "Shop Status",
                            value = if (myShop.totalRatings > 0) "Active" else "New",
                            change = if (myShop.averageRating >= 4.0) "Excellent!" else "Keep growing",
                            icon = Icons.Default.Person,
                            changePositive = myShop.averageRating >= 4.0
                        )
                    }
                }
            }
        }
        
        // Recent Activity
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
                    } else if (recentRatings.isEmpty()) {
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
                                    text = "No recent activity",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                                Text(
                                    text = "Ratings and reviews will appear here",
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
                            recentRatings.forEachIndexed { index, (rating, shopName) ->
                                RatingActivityItem(
                                    rating = rating,
                                    shopName = shopName
                                )
                                
                                if (index < recentRatings.size - 1) {
                                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                                }
                            }
                        }
                    }
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
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title = "Analytics",
                        subtitle = "View detailed insights",
                        icon = Icons.Default.Star,
                        onClick = onNavigateToAnalytics,
                        modifier = Modifier.weight(1f)
                    )
                    
                    ActionCard(
                        title = "Manage Coupons",
                        subtitle = "Create promotional offers",
                        icon = Icons.Default.Settings,
                        onClick = onNavigateToCoupons,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun KPICard(
    title: String,
    value: String,
    change: String,
    icon: ImageVector,
    changePositive: Boolean,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.width(180.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = PrimaryBrown,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
            
            Text(
                text = change,
                style = MaterialTheme.typography.bodySmall,
                color = if (changePositive) Success else Color.Red
            )
        }
    }
}

@Composable
private fun RatingActivityItem(
    rating: Rating,
    shopName: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Star,
            contentDescription = "Rating",
            tint = if (rating.rating >= 4.0) Success else PrimaryBrown,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "New ${rating.rating}-star review",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = PrimaryBrown
            )
            if (rating.comment.isNotBlank()) {
                Text(
                    text = "\"${rating.comment.take(50)}${if (rating.comment.length > 50) "..." else ""}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            } else {
                Text(
                    text = "No comment provided",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
        
        Text(
            text = formatTimestamp(rating.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
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
    icon: ImageVector,
    title: String,
    subtitle: String,
    time: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = PrimaryBrown,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
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
        
        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = PrimaryBrown,
                modifier = Modifier.size(32.dp)
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
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