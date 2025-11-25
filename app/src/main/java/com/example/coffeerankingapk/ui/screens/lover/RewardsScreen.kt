package com.example.coffeerankingapk.ui.screens.lover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.CouponCard
import com.example.coffeerankingapk.ui.theme.*
import com.example.coffeerankingapk.viewmodel.CouponViewModel
import com.example.coffeerankingapk.viewmodel.CoffeeShopViewModel
import com.example.coffeerankingapk.viewmodel.PointsViewModel
import com.example.coffeerankingapk.data.model.UserPoints
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    onNavigateBack: () -> Unit,
    couponViewModel: CouponViewModel = viewModel(),
    coffeeShopViewModel: CoffeeShopViewModel = viewModel(),
    pointsViewModel: PointsViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""
    
    // Load real coupons from Firestore
    val allCoupons by couponViewModel.allActiveCoupons.collectAsState()
    val coffeeShops by coffeeShopViewModel.coffeeShops.collectAsState()
    
    // Load points and leaderboard
    val userPoints by pointsViewModel.userPoints.collectAsState()
    val leaderboard by pointsViewModel.leaderboard.collectAsState()
    
    LaunchedEffect(Unit) {
        couponViewModel.loadAllActiveCoupons()
        coffeeShopViewModel.loadCoffeeShops()
        pointsViewModel.loadUserPoints()
        pointsViewModel.loadLeaderboard(100)
    }
    
    // Mock data for tasks (we can implement this later)
    val loyaltyPoints = userPoints?.totalPoints ?: 0
    val currentLevel = userPoints?.currentLevel ?: 1
    val nextRewardThreshold = UserPoints.LEVEL_THRESHOLDS.getOrNull(currentLevel) ?: 100
    
    val earnedCoupons = listOf(
        RewardCouponData(
            id = "1",
            title = "20% Off Any Drink",
            description = "Valid on all beverages",
            discountPercent = 20,
            expiryDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 10) }.time
        ),
        RewardCouponData(
            id = "2",
            title = "Free Pastry",
            description = "With any coffee purchase",
            discountPercent = 100,
            expiryDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 5) }.time
        )
    )
    
    val tasks = listOf(
        TaskItem("Rate a Coffee Shop", "Earn ${UserPoints.POINTS_PER_RATING} points", "+${UserPoints.POINTS_PER_RATING} points", false),
        TaskItem("Write a Review", "Earn up to ${UserPoints.POINTS_PER_DETAILED_REVIEW} points", "+${UserPoints.POINTS_PER_DETAILED_REVIEW} points", false),
        TaskItem("Redeem a Coupon", "Earn ${UserPoints.POINTS_PER_COUPON_REDEEM} points", "+${UserPoints.POINTS_PER_COUPON_REDEEM} points", false)
    )
    
    Scaffold(
        containerColor = PrimaryBrown,
        topBar = {
            TopAppBar(
                title = { Text("Rewards", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBrown
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // User header with profile and points
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PrimaryBrown.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(24.dp),
                            tint = PrimaryBrown
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userPoints?.displayName ?: "Coffee Lover",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBrown
                        )
                        Text(
                            text = UserPoints.getLevelTitle(currentLevel),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Points",
                            tint = Success,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "$loyaltyPoints",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                    }
                }
                
                // Level progress
                LevelProgressBar(
                    currentPoints = loyaltyPoints,
                    nextThreshold = nextRewardThreshold,
                    currentLevel = currentLevel,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = PrimaryBrown,
                contentColor = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Coupons") },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f)
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Leaderboard") },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f)
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Tasks") },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f)
                )
            }
            
            // Tab content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgCream)
            ) {
                when (selectedTab) {
                    0 -> RealCouponsTab(
                        coupons = allCoupons,
                        coffeeShops = coffeeShops,
                        onRedeemCoupon = { couponViewModel.redeemCoupon(it) }
                    )
                    1 -> RealLeaderboardTab(
                        leaderboard = leaderboard,
                        currentUserId = currentUserId
                    )
                    2 -> TasksTab(tasks)
                }
            }
        }
    }
}

@Composable
private fun RealCouponsTab(
    coupons: List<com.example.coffeerankingapk.data.model.Coupon>,
    coffeeShops: List<com.example.coffeerankingapk.data.model.CoffeeShop>,
    onRedeemCoupon: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    if (coupons.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No Active Coupons",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Check back soon for new deals from coffee shops!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Available Coupons",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )
            }
            
            items(coupons) { coupon ->
                val shop = coffeeShops.find { it.id == coupon.shopId }
                val isExpiringSoon = run {
                    val daysUntilExpiry = (coupon.expiryDate.time - Date().time) / (1000 * 60 * 60 * 24)
                    daysUntilExpiry in 1..7
                }
                
                AppCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Shop name header
                        Text(
                            text = shop?.name ?: "Unknown Shop",
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryBrown,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Coupon title
                        Text(
                            text = coupon.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBrown
                        )
                        
                        // Description
                        if (coupon.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = coupon.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Discount badge
                        Box(
                            modifier = Modifier
                                .background(
                                    color = PrimaryBrown,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (coupon.discountPercent > 0) {
                                    "${coupon.discountPercent}% OFF"
                                } else {
                                    "$${coupon.discountAmount} OFF"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Details
                        if (coupon.minimumPurchase > 0) {
                            Text(
                                text = "Min. purchase: $${coupon.minimumPurchase}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        
                        Text(
                            text = if (isExpiringSoon) {
                                "⏰ Expires soon: ${dateFormat.format(coupon.expiryDate)}"
                            } else {
                                "Valid until ${dateFormat.format(coupon.expiryDate)}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isExpiringSoon) MaterialTheme.colorScheme.error else TextMuted,
                            fontWeight = if (isExpiringSoon) FontWeight.SemiBold else FontWeight.Normal
                        )
                        
                        if (coupon.maxRedemptions > 0) {
                            val remaining = coupon.maxRedemptions - coupon.currentRedemptions
                            Text(
                                text = "$remaining remaining",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (remaining < 10) MaterialTheme.colorScheme.error else TextMuted
                            )
                        }
                        
                        // Coupon code and redeem button
                        if (coupon.code.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Coupon Code",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = coupon.code,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBrown
                                    )
                                }
                                
                                Text(
                                    text = "Use Now",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier
                                        .background(Success, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
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
private fun RealLeaderboardTab(
    leaderboard: List<UserPoints>,
    currentUserId: String
) {
    if (leaderboard.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextMuted
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Rankings Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start earning points to appear on the leaderboard!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Top Coffee Lovers 🏆",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            items(leaderboard) { userPoint ->
                val isCurrentUser = userPoint.userId == currentUserId
                RealLeaderboardItemCard(
                    userPoints = userPoint,
                    isCurrentUser = isCurrentUser
                )
            }
        }
    }
}

@Composable
private fun RealLeaderboardItemCard(
    userPoints: UserPoints,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    val rankColor = when (userPoints.rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> PrimaryBrown
    }
    
    val rankEmoji = when (userPoints.rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> ""
    }
    
    AppCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isCurrentUser) PrimaryBrown.copy(alpha = 0.1f) else Color.Transparent
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Box(
                modifier = Modifier
                    .width(60.dp),
                contentAlignment = Alignment.Center
            ) {
                if (userPoints.rank <= 3) {
                    Text(
                        text = rankEmoji,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Text(
                        text = "#${userPoints.rank}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = rankColor
                    )
                }
            }
            
            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = userPoints.displayName.ifEmpty { "Anonymous" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                        color = PrimaryBrown,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isCurrentUser) {
                        Text(
                            text = "YOU",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier
                                .background(Success, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Text(
                    text = "Level ${userPoints.currentLevel} • ${UserPoints.getLevelTitle(userPoints.currentLevel)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            
            // Points
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Points",
                        tint = Success,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${userPoints.totalPoints}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Success
                    )
                }
                Text(
                    text = "${userPoints.totalRatings} ratings",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun RewardsTab(coupons: List<RewardCouponData>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "My Coupons",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Active",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryBrown,
                    modifier = Modifier
                        .background(
                            PrimaryBrown.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Text(
                    text = "Expired",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        
        items(coupons) { coupon ->
            val isExpired = coupon.expiryDate.before(Date())
            RewardCouponCard(coupon = coupon, isExpired = isExpired)
        }
        
        item {
            Text(
                text = "My Rewards",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RewardItem(
                    title = "15% Off Latte",
                    subtitle = "Expires Dec 25, 2024",
                    actionText = "Use Now",
                    modifier = Modifier.weight(1f)
                )
                RewardItem(
                    title = "Free Cookie",
                    subtitle = "Expires Dec 20, 2024",
                    actionText = "Use Now",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LeaderboardTab(leaderboard: List<LeaderboardItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Your Rank",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        items(leaderboard) { item ->
            LeaderboardItemCard(
                item = item,
                isCurrentUser = item.name == "Sarah Chen"
            )
        }
    }
}

@Composable
private fun TasksTab(tasks: List<TaskItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Tasks",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown
            )
            Text(
                text = "View All",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBrown,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        items(tasks) { task ->
            TaskItemCard(task = task)
        }
    }
}

@Composable
private fun LevelProgressBar(
    currentPoints: Int,
    nextThreshold: Int,
    currentLevel: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Level Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = PrimaryBrown
            )
            Text(
                text = "Level $currentLevel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Success
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = currentPoints.toFloat() / nextThreshold.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            color = Success,
            trackColor = Color.Gray.copy(alpha = 0.2f)
        )
        
        Text(
            text = "${nextThreshold - currentPoints} XP to next level",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun RewardCouponCard(
    coupon: RewardCouponData,
    isExpired: Boolean,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isExpired) Color.Gray.copy(alpha = 0.3f) else PrimaryBrown.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${coupon.discountPercent}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpired) TextMuted else PrimaryBrown
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = coupon.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isExpired) TextMuted else PrimaryBrown
                )
                Text(
                    text = coupon.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            
            Text(
                text = if (isExpired) "Expired" else "Apply",
                style = MaterialTheme.typography.labelMedium,
                color = if (isExpired) TextMuted else Success,
                modifier = Modifier
                    .background(
                        if (isExpired) Color.Gray.copy(alpha = 0.2f) else Success.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun LeaderboardItemCard(
    item: LeaderboardItem,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isCurrentUser) PrimaryBrown.copy(alpha = 0.1f) else Color.Transparent
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.rank,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown,
                modifier = Modifier.width(48.dp)
            )
            
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                color = PrimaryBrown,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = "${item.points}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Success
            )
        }
    }
}

@Composable
private fun TaskItemCard(
    task: TaskItem,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (task.isCompleted) Success.copy(alpha = 0.1f) else Color.White
    val iconColor = if (task.isCompleted) Success else PrimaryBrown
    
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .background(backgroundColor)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (task.isCompleted) "✓" else "○",
                    style = MaterialTheme.typography.headlineSmall,
                    color = iconColor
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryBrown
                )
                Text(
                    text = task.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            
            Text(
                text = task.reward,
                style = MaterialTheme.typography.labelMedium,
                color = Success,
                modifier = Modifier
                    .background(Success.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun RewardItem(
    title: String,
    subtitle: String,
    actionText: String,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = PrimaryBrown
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium,
                color = Success,
                modifier = Modifier
                    .background(Success.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

private data class RewardCouponData(
    val id: String,
    val title: String,
    val description: String,
    val discountPercent: Int,
    val expiryDate: Date
)

private data class LeaderboardItem(
    val name: String,
    val rank: String,
    val points: Int
)

private data class TaskItem(
    val title: String,
    val subtitle: String,
    val reward: String,
    val isCompleted: Boolean
)