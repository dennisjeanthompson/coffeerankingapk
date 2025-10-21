package com.example.coffeerankingapk.ui.screens.owner

import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeerankingapk.data.firebase.ServiceLocator
import com.example.coffeerankingapk.data.model.Coupon
import com.example.coffeerankingapk.data.model.OwnerDashboard
import com.example.coffeerankingapk.data.model.OwnerActivity
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.CouponCard
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
import com.example.coffeerankingapk.ui.theme.Success
import com.example.coffeerankingapk.ui.theme.TextMuted
import com.example.coffeerankingapk.ui.viewmodel.OwnerDashboardViewModel
import java.util.Locale

@Composable
fun OwnerDashboardScreen(
    onNavigateToAnalytics: () -> Unit,
    onNavigateToCoupons: () -> Unit,
    viewModel: OwnerDashboardViewModel = viewModel(factory = ServiceLocator.provideDefaultViewModelFactory())
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val showToast = remember(context) {
        { message: String -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            showToast(message)
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            showToast(message)
            viewModel.clearSuccessMessage()
        }
    }

    val dashboard = uiState.dashboard

    when {
        uiState.isLoading && dashboard == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgCream),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(color = PrimaryBrown)
            }
        }
        dashboard == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgCream),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "We couldn't load your dashboard",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PrimaryButton(
                        text = "Try again",
                        onClick = { showToast("Refreshing dashboard...") }
                    )
                }
            }
        }
        else -> {
            OwnerDashboardContent(
                dashboard = dashboard,
                activeCoupons = uiState.activeCoupons,
                onNavigateToAnalytics = {
                    showToast("Opening analytics dashboard")
                    onNavigateToAnalytics()
                },
                onNavigateToCoupons = {
                    showToast("Managing cafe coupons")
                    onNavigateToCoupons()
                },
                onCreateCoupon = {
                    showCreateDialog = true
                }
            )
        }
    }

    if (showCreateDialog) {
        CreateCouponDialog(
            onDismiss = { showCreateDialog = false },
            onCreateCoupon = { title, description, discount, durationDays ->
                viewModel.createCoupon(title, description, discount, durationDays)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun OwnerDashboardContent(
    dashboard: OwnerDashboard,
    activeCoupons: List<Coupon>,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToCoupons: () -> Unit,
    onCreateCoupon: () -> Unit
) {
    val overview = dashboard.overview

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
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
                            text = dashboard.cafeName.ifBlank { "Your cafe" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBrown
                        )
                        Text(
                            text = "You're trending up today",
                            style = MaterialTheme.typography.bodySmall,
                            color = Success
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

        item {
            Column {
                Text(
                    text = "Today's Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )
                Spacer(modifier = Modifier.height(12.dp))

                val kpiItems = listOf(
                    KpiItem(
                        title = "Total Reviews",
                        value = overview.totalReviews.formatInt(),
                        change = overview.reviewsChange
                    ),
                    KpiItem(
                        title = "Avg Rating",
                        value = String.format(Locale.getDefault(), "%.1f", overview.averageRating),
                        change = overview.ratingChange
                    ),
                    KpiItem(
                        title = "Monthly Visits",
                        value = overview.monthlyVisits.formatInt(),
                        change = overview.visitsChange
                    ),
                    KpiItem(
                        title = "Revenue",
                        value = String.format(Locale.getDefault(), "$%,.0f", overview.revenue),
                        change = overview.revenueChange
                    )
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(kpiItems.size) { index ->
                        val item = kpiItems[index]
                        KPICard(
                            title = item.title,
                            value = item.value,
                            change = item.change,
                            changePositive = item.change.isPositive()
                        )
                    }
                }
            }
        }

        if (dashboard.recentActivity.isNotEmpty()) {
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
                            dashboard.recentActivity.forEachIndexed { index, activity ->
                                OwnerActivityRow(activity = activity)
                                if (index != dashboard.recentActivity.lastIndex) {
                                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (activeCoupons.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Promotions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBrown
                        )
                        TextButton(onClick = onNavigateToCoupons) {
                            Text("View all")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        activeCoupons.forEach { coupon ->
                            CouponPreviewCard(coupon = coupon)
                        }
                    }
                }
            }
        }

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
                        icon = Icons.Default.LocalOffer,
                        onClick = onNavigateToCoupons,
                        modifier = Modifier.weight(1f)
                    )
                }

                PrimaryButton(
                    text = "Create Coupon",
                    onClick = onCreateCoupon,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun KPICard(
    title: String,
    value: String,
    change: String,
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

            if (change.isNotBlank()) {
                Text(
                    text = change,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (changePositive) Success else Color.Red
                )
            }
        }
    }
}

@Composable
private fun OwnerActivityRow(activity: OwnerActivity) {
    val timeText = remember(activity.timestamp) {
        DateUtils.getRelativeTimeSpanString(
            activity.timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(PrimaryBrown.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = activity.icon.ifBlank { "☕" },
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = PrimaryBrown
            )
            if (activity.subtitle.isNotBlank()) {
                Text(
                    text = activity.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        Text(
            text = timeText,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
    }
}

@Composable
private fun CouponPreviewCard(coupon: Coupon) {
    val isExpired = remember(coupon.id, coupon.expiryDate, coupon.isActive) {
        val expiryMillis = coupon.expiryDate?.time
        expiryMillis != null && expiryMillis < System.currentTimeMillis()
    }

    CouponCard(
        title = coupon.title,
        description = coupon.description,
        discountPercent = coupon.discountPercent,
        expiryDate = coupon.expiryDate,
        isExpired = isExpired,
        isActive = coupon.isActive
    )
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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

private data class KpiItem(
    val title: String,
    val value: String,
    val change: String
)

private fun String.isPositive(): Boolean {
    val trimmed = trim()
    return trimmed.isNotEmpty() && trimmed.first() != '-'
}

private fun Int.formatInt(): String = String.format(Locale.getDefault(), "%,d", this)