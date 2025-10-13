package com.example.coffeerankingapk.ui.screens.owner

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.ui.components.AnalyticsChart
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.ChartType
import com.example.coffeerankingapk.ui.components.RatingStars
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerAnalyticsScreen(
    onNavigateBack: () -> Unit
) {
    // Mock data for charts and reviews
    val reviewsOverTime = listOf(12f, 18f, 15f, 22f, 28f, 24f, 30f)
    val mockReviews = listOf(
        ReviewItem("Alice Johnson", 5, "Amazing coffee and atmosphere!", "2 days ago"),
        ReviewItem("Bob Smith", 4, "Great service, will come back!", "3 days ago"),
        ReviewItem("Carol Davis", 5, "Best latte in town!", "1 week ago"),
        ReviewItem("David Wilson", 4, "Cozy place for work", "1 week ago")
    )
    
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
            // Reviews over time chart
            item {
                AnalyticsChart(
                    dataPoints = reviewsOverTime,
                    chartType = ChartType.LINE,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // KPI Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KPICompactCard(
                        title = "Avg Rating",
                        value = "4.6",
                        modifier = Modifier.weight(1f)
                    )
                    KPICompactCard(
                        title = "Reviews",
                        value = "248",
                        modifier = Modifier.weight(1f)
                    )
                    KPICompactCard(
                        title = "Daily Visits",
                        value = "42",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Top Reviews section
            item {
                Text(
                    text = "Recent Reviews",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            items(mockReviews) { review ->
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
    review: ReviewItem,
    modifier: Modifier = Modifier
) {
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
                    text = review.userName,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = review.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            
            RatingStars(
                rating = review.rating.toFloat(),
                showNumeric = false
            )
            
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
        }
    }
}

private data class ReviewItem(
    val userName: String,
    val rating: Int,
    val comment: String,
    val date: String
)