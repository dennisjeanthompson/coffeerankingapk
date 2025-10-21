package com.example.coffeerankingapk.ui.screens.owner

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeerankingapk.data.firebase.ServiceLocator
import com.example.coffeerankingapk.data.model.Coupon
import com.example.coffeerankingapk.ui.components.CouponCard
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
import com.example.coffeerankingapk.ui.theme.TextMuted
import com.example.coffeerankingapk.ui.viewmodel.OwnerCouponsViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerCouponsScreen(
    onNavigateBack: () -> Unit,
    viewModel: OwnerCouponsViewModel = viewModel(factory = ServiceLocator.provideDefaultViewModelFactory())
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val showToast = remember(context) {
        { message: String -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { showToast(it) }
    }

    LaunchedEffect(uiState.creationSuccess) {
        if (uiState.creationSuccess) {
            showToast("Coupon created successfully")
            viewModel.dismissCreationSuccess()
        }
    }

    Scaffold(
        containerColor = BgCream,
        topBar = {
            TopAppBar(
                title = { Text("Coupons") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCream)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = PrimaryBrown,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create coupon")
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(color = PrimaryBrown)
                }
            }
            uiState.coupons.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No coupons yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextMuted
                    )
                    Text(
                        text = "Create your first reward to delight regulars",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.coupons) { coupon ->
                        CouponListItem(
                            coupon = coupon,
                            onDeactivate = {
                                viewModel.deactivateCoupon(coupon.id)
                                showToast("Coupon deactivated")
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCouponDialog(
            onDismiss = { showCreateDialog = false },
            onCreateCoupon = { title, description, discount, durationDays ->
                val expiryTimestamp = durationDays?.let { days ->
                    System.currentTimeMillis() + days * 24L * 60L * 60L * 1000L
                }
                viewModel.createCoupon(title, description, discount, expiryTimestamp)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CouponListItem(
    coupon: Coupon,
    onDeactivate: () -> Unit
) {
    val expiryDate = coupon.expiryDate
    val isExpired = expiryDate?.before(Date()) == true

    CouponCard(
        title = coupon.title,
        description = coupon.description,
        discountPercent = coupon.discountPercent,
        expiryDate = expiryDate,
        isExpired = isExpired,
        isActive = coupon.isActive,
        actionText = if (!isExpired && coupon.isActive) "Deactivate" else null,
        onActionClick = if (!isExpired && coupon.isActive) {
            onDeactivate
        } else null
    )
}