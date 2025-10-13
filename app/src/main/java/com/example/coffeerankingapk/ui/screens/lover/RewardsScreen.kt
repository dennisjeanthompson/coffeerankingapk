package com.example.coffeerankingapk.ui.screens.lover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.CouponCard
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.Success
import com.example.coffeerankingapk.ui.theme.TextMuted
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    onNavigateBack: () -> Unit
) {
    // Mock data
    val loyaltyPoints = 850
    val nextRewardThreshold = 1000
    val progress = loyaltyPoints.toFloat() / nextRewardThreshold.toFloat()
    
    val earnedCoupons = listOf(
        RewardCouponData(
            id = "1",
            title = "Free Coffee",
            discountPercent = 100,
            expiryDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 10) }.time
        ),
        RewardCouponData(
            id = "2",
            title = "20% Off Pastries",
            discountPercent = 20,
            expiryDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 5) }.time
        ),
        RewardCouponData(
            id = "3",
            title = "Buy 2 Get 1 Free",
            discountPercent = 33,
            expiryDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.time
        )
    )
    
    Scaffold(
        containerColor = BgCream,
        topBar = {
            TopAppBar(
                title = { Text("Rewards") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgCream
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Loyalty progress card
            item {
                LoyaltyProgressCard(
                    currentPoints = loyaltyPoints,
                    nextThreshold = nextRewardThreshold,
                    progress = progress
                )
            }
            
            // Earned coupons section
            item {
                Text(
                    text = "Your Rewards",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            items(earnedCoupons) { coupon ->
                val isExpired = coupon.expiryDate.before(Date())
                CouponCard(
                    title = coupon.title,
                    discountPercent = coupon.discountPercent,
                    expiryDate = coupon.expiryDate,
                    isExpired = isExpired,
                    onRedeemClick = if (!isExpired) {
                        { /* TODO: Redeem coupon logic */ }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun LoyaltyProgressCard(
    currentPoints: Int,
    nextThreshold: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Loyalty Progress",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$currentPoints points",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Success
                )
                
                Text(
                    text = "${nextThreshold - currentPoints} to next reward",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = Success
            )
            
            Text(
                text = "Earn points by visiting cafes and leaving reviews!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
    }
}

private data class RewardCouponData(
    val id: String,
    val title: String,
    val discountPercent: Int,
    val expiryDate: Date
)