package com.example.coffeerankingapk.ui.screens.lover

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.coffeerankingapk.data.firebase.ServiceLocator
import com.example.coffeerankingapk.data.model.Cafe
import com.example.coffeerankingapk.data.model.Review
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.OutlineButton
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.components.RatingStars
import com.example.coffeerankingapk.ui.navigation.TurnByTurnNavigationActivity
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.TextMuted
import com.example.coffeerankingapk.ui.viewmodel.CafeDetailViewModel
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CafeDetailScreen(
    cafeId: String,
    onNavigateBack: () -> Unit,
    onNavigateToRate: () -> Unit = {},
    viewModel: CafeDetailViewModel = viewModel(factory = ServiceLocator.provideCafeDetailViewModelFactory(cafeId))
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val cafe = uiState.cafe
    
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
                            cafe?.let { shareCafe(context = context, cafe = it) }
                        },
                        enabled = cafe != null
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
        when {
            uiState.isLoading && cafe == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            cafe == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Cafe not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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

                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = cafe.name,
                                style = MaterialTheme.typography.headlineLarge
                            )
                            if (cafe.address.isNotBlank()) {
                                Text(
                                    text = cafe.address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                            if (cafe.description.isNotBlank()) {
                                Text(
                                    text = cafe.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RatingStars(rating = cafe.rating.toFloat())
                                if (cafe.latitude != 0.0 || cafe.longitude != 0.0) {
                                    Text(
                                        text = "Lat: ${String.format("%.4f", cafe.latitude)}, Lng: ${String.format("%.4f", cafe.longitude)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }

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
                                    onClick = { startTurnByTurnNavigation(context = context, cafe = cafe) },
                                    modifier = Modifier.weight(1f),
                                    enabled = cafe.latitude != 0.0 || cafe.longitude != 0.0
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlineButton(
                                    text = if (uiState.rewardClaimed) "Reward claimed" else "Claim reward",
                                    onClick = {
                                        if (!uiState.rewardClaimed) {
                                            viewModel.claimReward()
                                            Toast.makeText(
                                                context,
                                                "Reward claimed! Check your rewards wallet for details.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !uiState.rewardClaimed
                                )
                                OutlineButton(
                                    text = "Share cafe",
                                    onClick = { shareCafe(context = context, cafe = cafe) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlineButton(
                                text = if (uiState.isFavorite) "Saved to favorites" else "Save to favorites",
                                onClick = {
                                    viewModel.toggleFavorite()
                                    val message = if (uiState.isFavorite) {
                                        "Removed ${cafe.name} from favorites."
                                    } else {
                                        "Added ${cafe.name} to your favorites."
                                    }
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            uiState.errorMessage?.let { error ->
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    if (uiState.reviews.isNotEmpty()) {
                        item {
                            Text(
                                text = "Reviews",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        items(uiState.reviews) { review ->
                            ReviewCard(
                                review = review,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(
    review: Review,
    modifier: Modifier = Modifier
) {
    val formattedDate = if (review.createdAt > 0) {
        DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(review.createdAt))
    } else {
        "Just now"
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
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            RatingStars(
                rating = review.rating.toFloat(),
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
