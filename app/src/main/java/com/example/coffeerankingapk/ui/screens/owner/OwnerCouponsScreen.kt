package com.example.coffeerankingapk.ui.screens.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeerankingapk.data.model.Coupon
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.CouponCard
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.Danger
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
import com.example.coffeerankingapk.ui.theme.Success
import com.example.coffeerankingapk.ui.theme.TextMuted
import com.example.coffeerankingapk.viewmodel.CoffeeShopViewModel
import com.example.coffeerankingapk.viewmodel.CouponViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerCouponsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddCoupon: () -> Unit = {},
    onNavigateToEditCoupon: (String) -> Unit = {},
    coffeeShopViewModel: CoffeeShopViewModel = viewModel(),
    couponViewModel: CouponViewModel = viewModel()
) {
    val coupons by couponViewModel.coupons.collectAsState()
    val isLoading by couponViewModel.isLoading.collectAsState()
    val error by couponViewModel.error.collectAsState()
    val successMessage by couponViewModel.operationSuccess.collectAsState()
    val shops by coffeeShopViewModel.coffeeShops.collectAsState()
    val allShops by coffeeShopViewModel.allCoffeeShops.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val ownerShops by coffeeShopViewModel.ownerShops.collectAsState()
    
    // Get owner's shop regardless of location filtering
    val ownerShop = ownerShops.firstOrNull { it.ownerId == currentUserId }
        ?: shops.firstOrNull { it.ownerId == currentUserId }
        ?: ownerShops.firstOrNull()
    
    // Load coupons when shop is available
    LaunchedEffect(ownerShop?.id) {
        ownerShop?.id?.let { shopId ->
            couponViewModel.loadCouponsForShop(shopId)
        }
    }
    
    // Show error/success messages
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            couponViewModel.clearError()
        }
    }
    
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            couponViewModel.clearSuccess()
        }
    }
    
    Scaffold(
        containerColor = BgCream,
        topBar = {
            TopAppBar(
                title = { Text("Manage Coupons") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgCream
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (ownerShop != null) {
                        onNavigateToAddCoupon()
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Add your coffee shop first")
                        }
                    }
                },
                containerColor = if (ownerShop != null) PrimaryBrown else TextMuted,
                contentColor = if (ownerShop != null) Color.White else Color.DarkGray
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create coupon")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && coupons.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                ownerShop == null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Coffee Shop",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please add a coffee shop first to create coupons",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "📍 Go to 'Shop' tab to add your coffee shop details",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
                coupons.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No coupons yet",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create your first coupon to attract customers",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToAddCoupon,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBrown
                            )
                        ) {
                            Text("Create Coupon")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
                    ) {
                        items(coupons) { coupon ->
                            OwnerCouponItem(
                                coupon = coupon,
                                onToggleActive = { isActive ->
                                    couponViewModel.toggleCouponStatus(coupon.id, isActive)
                                },
                                onEdit = { onNavigateToEditCoupon(coupon.id) },
                                onDelete = { couponViewModel.deleteCoupon(coupon.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnerCouponItem(
    coupon: Coupon,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val isExpired = coupon.isExpired()
    val isMaxedOut = coupon.isMaxedOut()
    
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with title and discount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = coupon.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = if (coupon.discountPercent > 0) {
                        "${coupon.discountPercent}% OFF"
                    } else {
                        "$${coupon.discountAmount} OFF"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = when {
                        isExpired || !coupon.isActive -> TextMuted
                        else -> Success
                    }
                )
            }
            
            // Description
            if (coupon.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = coupon.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date range
            Text(
                text = "Valid: ${dateFormat.format(coupon.startDate)} - ${dateFormat.format(coupon.expiryDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isExpired) Danger else TextMuted
            )
            
            // Redemptions
            if (coupon.maxRedemptions > 0) {
                Text(
                    text = "Redeemed: ${coupon.currentRedemptions}/${coupon.maxRedemptions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isMaxedOut) Danger else TextMuted
                )
            } else {
                Text(
                    text = "Redeemed: ${coupon.currentRedemptions} times",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            
            // Status badges
            if (isExpired) {
                Text(
                    text = "EXPIRED",
                    style = MaterialTheme.typography.labelSmall,
                    color = Danger,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else if (isMaxedOut) {
                Text(
                    text = "MAX REDEMPTIONS REACHED",
                    style = MaterialTheme.typography.labelSmall,
                    color = Danger,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (coupon.isActive) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = coupon.isActive,
                        onCheckedChange = onToggleActive,
                        enabled = !isExpired
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = PrimaryBrown
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Danger
                        )
                    }
                }
            }
        }
    }
}