package com.example.coffeerankingapk.ui.screens.lover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeerankingapk.data.model.Coupon
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
import com.example.coffeerankingapk.ui.theme.TextMuted
import com.example.coffeerankingapk.viewmodel.CouponViewModel
import com.example.coffeerankingapk.viewmodel.CoffeeShopViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoverCouponsScreen(
    couponViewModel: CouponViewModel = viewModel(),
    coffeeShopViewModel: CoffeeShopViewModel = viewModel()
) {
    val allCoupons by couponViewModel.allActiveCoupons.collectAsState()
    val coffeeShops by coffeeShopViewModel.coffeeShops.collectAsState()
    val isLoading by couponViewModel.isLoading.collectAsState()
    val error by couponViewModel.error.collectAsState()
    
    // Load all active coupons
    LaunchedEffect(Unit) {
        couponViewModel.loadAllActiveCoupons()
        coffeeShopViewModel.loadCoffeeShops()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Coupons") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBrown,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && allCoupons.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                allCoupons.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Active Coupons",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Check back soon for new deals from coffee shops!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allCoupons) { coupon ->
                            val shop = coffeeShops.find { it.id == coupon.shopId }
                            LoverCouponCard(
                                coupon = coupon,
                                shopName = shop?.name ?: "Unknown Shop",
                                onRedeem = { couponViewModel.redeemCoupon(coupon.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoverCouponCard(
    coupon: Coupon,
    shopName: String,
    onRedeem: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val isExpiringSoon = run {
        val daysUntilExpiry = (coupon.expiryDate.time - Date().time) / (1000 * 60 * 60 * 24)
        daysUntilExpiry in 1..7
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpiringSoon) Color(0xFFFFF3E0) else Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with shop name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = PrimaryBrown,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = shopName,
                    style = MaterialTheme.typography.labelLarge,
                    color = PrimaryBrown,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Coupon title
            Text(
                text = coupon.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
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
            
            // Minimum purchase
            if (coupon.minimumPurchase > 0) {
                Text(
                    text = "Min. purchase: $${coupon.minimumPurchase}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            
            // Expiry date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpiringSoon) {
                        "⏰ Expires soon: ${dateFormat.format(coupon.expiryDate)}"
                    } else {
                        "Valid until ${dateFormat.format(coupon.expiryDate)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isExpiringSoon) Color(0xFFE65100) else TextMuted,
                    fontWeight = if (isExpiringSoon) FontWeight.SemiBold else FontWeight.Normal
                )
            }
            
            // Redemption status
            if (coupon.maxRedemptions > 0) {
                val remaining = coupon.maxRedemptions - coupon.currentRedemptions
                Text(
                    text = "$remaining remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (remaining < 10) Color(0xFFE65100) else TextMuted
                )
            }
            
            // Coupon code
            if (coupon.code.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
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
                    
                    Button(
                        onClick = onRedeem,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBrown
                        )
                    ) {
                        Text("Use Now")
                    }
                }
            }
        }
    }
}
