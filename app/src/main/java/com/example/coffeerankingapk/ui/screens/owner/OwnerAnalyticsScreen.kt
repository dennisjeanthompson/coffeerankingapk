package com.example.coffeerankingapk.ui.screens.owner

import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeerankingapk.data.firebase.ServiceLocator
import com.example.coffeerankingapk.data.model.OwnerReview
import com.example.coffeerankingapk.ui.components.AnalyticsChart
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.ChartType
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.components.RatingStars
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
import com.example.coffeerankingapk.ui.theme.TextMuted
import com.example.coffeerankingapk.ui.viewmodel.OwnerAnalyticsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerAnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: OwnerAnalyticsViewModel = viewModel(factory = ServiceLocator.provideDefaultViewModelFactory())
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val showToast = remember(context) {
        { message: String -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        containerColor = BgCream,
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showToast("Refreshing analytics soon") }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { showToast("Sharing analytics coming soon") }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCream)
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.reviewsOverTime.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(color = PrimaryBrown)
                }
            }
            else -> {
                AnalyticsContent(
                    state = uiState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onInviteReviews = {
                        showToast("Invites will be sent soon")
                    }
                )
            }
        }
    }
}

@Composable
private fun AnalyticsContent(
    state: com.example.coffeerankingapk.ui.viewmodel.OwnerAnalyticsState,
    modifier: Modifier = Modifier,
    onInviteReviews: () -> Unit
) {
    val chartPoints = state.reviewsOverTime.ifEmpty { listOf(0.0) }.map { it.toFloat() }
    val overview = state.overview

    LazyColumn(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnalyticsChart(
                dataPoints = chartPoints,
                chartType = ChartType.LINE,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KPICompactCard(
                    title = "Avg Rating",
                    value = String.format(Locale.getDefault(), "%.1f", overview.averageRating),
                    modifier = Modifier.weight(1f)
                )
                KPICompactCard(
                    title = "Reviews",
                    value = String.format(Locale.getDefault(), "%,d", overview.totalReviews),
                    modifier = Modifier.weight(1f)
                )
                KPICompactCard(
                    title = "Monthly Visits",
                    value = String.format(Locale.getDefault(), "%,d", overview.monthlyVisits),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            AppCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Boost your reviews",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryBrown
                    )
                    Text(
                        text = "Send invites to loyal customers to gather fresh feedback and climb the rankings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    PrimaryButton(
                        text = "Invite recent visitors",
                        onClick = onInviteReviews,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Text(
                text = "Recent Reviews",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (state.recentReviews.isEmpty()) {
            item {
                AppCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No reviews yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextMuted
                        )
                        Text(
                            text = "Encourage customers to leave feedback to see insights here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(state.recentReviews) { review ->
                ReviewCard(review = review)
            }
        }
    }
}

@Composable
private fun KPICompactCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun ReviewCard(
    review: OwnerReview,
    modifier: Modifier = Modifier
) {
    val timestampDisplay = remember(review.createdAt) {
        if (review.createdAt == 0L) "Just now" else DateUtils.getRelativeTimeSpanString(
            review.createdAt,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    AppCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.userName.ifBlank { "Coffee Lover" },
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = timestampDisplay,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            RatingStars(
                rating = review.rating.toFloat().coerceAtLeast(0f),
                showNumeric = false
            )

            if (review.comment.isNotBlank()) {
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}