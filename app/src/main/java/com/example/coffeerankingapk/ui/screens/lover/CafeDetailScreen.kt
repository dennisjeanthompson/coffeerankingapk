package com.example.coffeerankingapk.ui.screens.lover

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.coffeerankingapk.data.Cafe
import com.example.coffeerankingapk.data.MockData
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.OutlineButton
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.components.RatingStars
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.TextMuted
import com.example.coffeerankingapk.ui.navigation.TurnByTurnNavigationActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CafeDetailScreen(
    cafeId: String,
    onNavigateBack: () -> Unit,
    onNavigateToRate: () -> Unit = {}
) {
    val context = LocalContext.current
    var isSaved by remember { mutableStateOf(false) }

    val cafe = remember(cafeId) {
        MockData.cafes.find { it.id == cafeId } ?: Cafe(
            id = cafeId,
            name = "Sample Cafe",
            description = "Handcrafted beverages and cozy seating",
            address = "Location coming soon",
            rating = 4.5f,
            imageUrl = "https://via.placeholder.com/400x300/6B3E2A/FFFFFF?text=Cafe",
            latitude = 0.0,
            longitude = 0.0
        )
    }
    val reviews = remember(cafeId) {
        when (cafeId) {
            "1" -> listOf(
                ReviewData("Alice", 5, "Amazing coffee!", "2 days ago"),
                ReviewData("Bob", 4, "Great atmosphere", "3 days ago"),
                ReviewData("Carol", 5, "Best latte ever!", "1 week ago")
            )
            "2" -> listOf(
                ReviewData("Devon", 5, "Single-origin beans were incredible.", "4 days ago"),
                ReviewData("Priya", 4, "Loved the pour-over recommendations!", "6 days ago")
            )
            else -> emptyList()
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
                    IconButton(
                        onClick = {
                            shareCafe(context = context, cafe = cafe)
                        }
                    ) {
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
                    Text(
                        text = cafe.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                    Text(
                        text = cafe.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RatingStars(rating = cafe.rating)
                        Text(
                            text = "Lat: ${String.format("%.4f", cafe.latitude)}, Lng: ${String.format("%.4f", cafe.longitude)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            }
            
            // Action row
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PrimaryButton(
                            text = "Rate",
                            onClick = onNavigateToRate,
                            modifier = Modifier.weight(1f)
                        )
                        OutlineButton(
                            text = "Start navigation",
                            onClick = {
                                startTurnByTurnNavigation(context = context, cafe = cafe)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlineButton(
                            text = "Claim reward",
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Reward claimed! Check your rewards wallet for details.",
                                    Toast.LENGTH_LONG
                                ).show() // Toast shown while CafeDetailScreen is hosted by MainActivity.
                            },
                            modifier = Modifier.weight(1f)
                        )
                        OutlineButton(
                            text = "Share cafe",
                            onClick = {
                                shareCafe(context = context, cafe = cafe)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlineButton(
                        text = if (isSaved) "Saved to favorites" else "Save to favorites",
                        onClick = {
                            isSaved = !isSaved
                            val message = if (isSaved) {
                                "Added ${cafe.name} to your favorites."
                            } else {
                                "Removed ${cafe.name} from favorites."
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Reviews section
            if (reviews.isNotEmpty()) {
                item {
                    Text(
                        text = "Reviews",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                items(reviews) { review ->
                    ReviewCard(
                        review = review,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
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

private fun shareCafe(context: android.content.Context, cafe: Cafe) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, cafe.name)
        putExtra(
            Intent.EXTRA_TEXT,
            "Let's grab a drink at ${cafe.name}! ${cafe.address}."
        )
    }
    val chooser = Intent.createChooser(shareIntent, "Share cafe")
    runCatching {
        context.startActivity(chooser)
    }.onFailure {
        Toast.makeText(context, "No app available to share this cafe.", Toast.LENGTH_SHORT).show()
    }
}

private fun startTurnByTurnNavigation(context: android.content.Context, cafe: Cafe) {
    if (cafe.latitude == 0.0 && cafe.longitude == 0.0) {
        Toast.makeText(context, "Navigation is not available for this cafe yet.", Toast.LENGTH_SHORT).show()
        return
    }
    val intent = TurnByTurnNavigationActivity.createIntent(
        context,
        cafe.latitude,
        cafe.longitude
    )
    try {
        context.startActivity(intent)
    } catch (error: ActivityNotFoundException) {
        Toast.makeText(context, "Navigation feature is currently unavailable.", Toast.LENGTH_SHORT).show()
    }
}

private data class ReviewData(
    val userName: String,
    val rating: Int,
    val comment: String,
    val date: String
)