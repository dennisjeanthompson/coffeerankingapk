package com.example.coffeerankingapk.ui.screens.lover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.InteractiveRatingStars
import com.example.coffeerankingapk.ui.components.OutlineButton
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.components.RatingStars
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CafeDetailScreen(
    cafeId: String,
    onNavigateBack: () -> Unit,
    onNavigateToRate: () -> Unit = {}
) {
    var showRatingDialog by remember { mutableStateOf(false) }
    
    // Mock cafe data based on ID
    val cafe = remember(cafeId) {
        when (cafeId) {
            "1" -> CafeDetailData(
                id = "1",
                name = "The Roasted Bean",
                rating = 4.6f,
                distance = "0.3 miles",
                imageUrl = "https://via.placeholder.com/400x300/8B4513/FFFFFF?text=Cafe+1",
                reviews = listOf(
                    ReviewData("Alice", 5, "Amazing coffee!", "2 days ago"),
                    ReviewData("Bob", 4, "Great atmosphere", "3 days ago"),
                    ReviewData("Carol", 5, "Best latte ever!", "1 week ago")
                )
            )
            else -> CafeDetailData(
                id = cafeId,
                name = "Sample Cafe",
                rating = 4.5f,
                distance = "0.5 miles",
                imageUrl = "https://via.placeholder.com/400x300/6B3E2A/FFFFFF?text=Cafe",
                reviews = emptyList()
            )
        }
    }
    
    Scaffold(
        containerColor = BgCream,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Share functionality */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
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
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero image
            item {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(cafe.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Cafe image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
                )
            }
            
            // Title row
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = cafe.name,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RatingStars(rating = cafe.rating)
                        Text(
                            text = cafe.distance,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            }
            
            // Action row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrimaryButton(
                        text = "Rate",
                        onClick = onNavigateToRate,
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlineButton(
                        text = "Claim reward",
                        onClick = { /* TODO: Claim reward */ },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlineButton(
                        text = "Share cafe",
                        onClick = { /* TODO: Share */ },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlineButton(
                        text = "Save",
                        onClick = { /* TODO: Save to favorites */ },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Reviews section
            if (cafe.reviews.isNotEmpty()) {
                item {
                    Text(
                        text = "Reviews",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                items(cafe.reviews) { review ->
                    ReviewCard(
                        review = review,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
    
    if (showRatingDialog) {
        RatingDialog(
            onDismiss = { showRatingDialog = false },
            onSubmit = { rating, comment ->
                // TODO: Submit rating
                showRatingDialog = false
            }
        )
    }
}

@Composable
private fun ReviewCard(
    review: ReviewData,
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
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RatingDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var selectedRating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate this cafe") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    InteractiveRatingStars(
                        currentRating = selectedRating,
                        onRatingChanged = { selectedRating = it }
                    )
                }
                
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Submit",
                onClick = { onSubmit(selectedRating, comment) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private data class CafeDetailData(
    val id: String,
    val name: String,
    val rating: Float,
    val distance: String,
    val imageUrl: String,
    val reviews: List<ReviewData>
)

private data class ReviewData(
    val userName: String,
    val rating: Int,
    val comment: String,
    val date: String
)