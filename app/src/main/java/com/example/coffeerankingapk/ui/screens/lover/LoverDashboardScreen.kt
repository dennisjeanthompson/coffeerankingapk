package com.example.coffeerankingapk.ui.screens.lover

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeerankingapk.data.firebase.ServiceLocator
import com.example.coffeerankingapk.data.model.Cafe
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.CafeListItem
import com.example.coffeerankingapk.ui.components.RatingStars
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.Danger
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
import com.example.coffeerankingapk.ui.theme.Success
import com.example.coffeerankingapk.ui.theme.TextMuted
import com.example.coffeerankingapk.ui.viewmodel.LoverDashboardViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoverDashboardScreen(
    onCafeClick: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    viewModel: LoverDashboardViewModel = viewModel(factory = ServiceLocator.provideDefaultViewModelFactory())
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val showToast: (String) -> Unit = remember(context) {
        { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { showToast(it) }
    }

    val recommendedCafes = remember(uiState.cafes) { uiState.cafes.take(3) }
    val topRatedCafes = remember(uiState.cafes) { uiState.cafes.sortedByDescending { it.rating }.take(5) }
    val favoriteCafes = remember(uiState.cafes, uiState.favorites) {
        uiState.cafes.filter { uiState.favorites.contains(it.id) }.take(3)
    }

    val rankDisplay = remember(uiState.rank) { uiState.rank.ifBlank { "#—" } }
    val pointsDisplay = remember(uiState.points) {
        if (uiState.points > 0) String.format(Locale.getDefault(), "%,d", uiState.points) else "--"
    }
    val reviewsDisplay = remember(uiState.reviewsCount) {
        if (uiState.reviewsCount > 0) uiState.reviewsCount.toString() else "--"
    }
    val favoritesDisplay = remember(uiState.favoritesCount, uiState.favorites) {
        when {
            uiState.favoritesCount > 0 -> uiState.favoritesCount.toString()
            uiState.favorites.isNotEmpty() -> uiState.favorites.size.toString()
            else -> "--"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = PrimaryBrown,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }

                        Column {
                            Text(
                                text = uiState.userName.ifBlank { "Coffee Lover" },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Coffee Enthusiast",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = {
                                viewModel.refresh()
                                showToast("Refreshing your feed")
                            }
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                showToast("Notifications are coming soon")
                                onNotificationClick()
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AppCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            title = "Rank",
                            value = rankDisplay,
                            color = Color.White
                        )
                        Divider(
                            modifier = Modifier
                                .height(40.dp)
                                .width(1.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        )
                        StatItem(
                            title = "Points",
                            value = pointsDisplay,
                            color = Success
                        )
                        Divider(
                            modifier = Modifier
                                .height(40.dp)
                                .width(1.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        )
                        StatItem(
                            title = "Reviews",
                            value = reviewsDisplay,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Discovering new cafes near you",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    placeholder = { Text("Search for cafes...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showToast("Launching search soon")
                            onSearchClick()
                        },
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                        disabledBorderColor = Color.White.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        if (uiState.isLoading && uiState.cafes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(color = PrimaryBrown)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    SectionHeader(
                        title = "Recent Activity",
                        actionLabel = "View All",
                        onActionClick = { showToast("Activity timeline coming soon") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ActivityItem(
                                icon = Icons.Default.Star,
                                title = "You have $reviewsDisplay reviews",
                                subtitle = "Share more ratings to climb the leaderboard",
                                backgroundColor = Success.copy(alpha = 0.1f)
                            )
                            Divider(color = Color.Gray.copy(alpha = 0.2f))
                            ActivityItem(
                                icon = Icons.Default.Favorite,
                                title = "${favoritesDisplay.takeIf { it != "--" } ?: "No"} favorite cafes",
                                subtitle = "Tap the heart icon to curate your list",
                                backgroundColor = Danger.copy(alpha = 0.1f)
                            )
                            Divider(color = Color.Gray.copy(alpha = 0.2f))
                            ActivityItem(
                                icon = Icons.Default.AutoAwesome,
                                title = "${pointsDisplay.takeIf { it != "--" } ?: "Earn"} coffee points",
                                subtitle = "Redeem rewards after every visit",
                                backgroundColor = PrimaryBrown.copy(alpha = 0.1f)
                            )
                        }
                    }
                }

                if (recommendedCafes.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Recommended For You",
                            actionLabel = "View All",
                            onActionClick = { showToast("Full cafe list coming soon") }
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            recommendedCafes.forEach { cafe ->
                                key(cafe.id) {
                                    CafeListItem(
                                        cafe = cafe,
                                        onClick = { onCafeClick(cafe.id) },
                                        isFavorite = uiState.favorites.contains(cafe.id),
                                        onFavoriteClick = {
                                            val isFavorite = uiState.favorites.contains(cafe.id)
                                            viewModel.toggleFavorite(cafe.id)
                                            val message = if (isFavorite) {
                                                "Removed ${cafe.name} from favorites"
                                            } else {
                                                "Added ${cafe.name} to favorites"
                                            }
                                            showToast(message)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (topRatedCafes.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Top Rated Sips",
                            actionLabel = "See All",
                            onActionClick = { showToast("Top rated list coming soon") }
                        )
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(topRatedCafes, key = { it.id }) { cafe ->
                                TopRatedCafeCard(
                                    cafe = cafe,
                                    isFavorite = uiState.favorites.contains(cafe.id),
                                    onClick = { onCafeClick(cafe.id) },
                                    onFavoriteClick = {
                                        val isFavorite = uiState.favorites.contains(cafe.id)
                                        viewModel.toggleFavorite(cafe.id)
                                        val message = if (isFavorite) {
                                            "Removed ${cafe.name} from favorites"
                                        } else {
                                            "Added ${cafe.name} to favorites"
                                        }
                                        showToast(message)
                                    }
                                )
                            }
                        }
                    }
                }

                if (favoriteCafes.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Saved Cafes",
                            actionLabel = "Manage",
                            onActionClick = { showToast("Favorites management coming soon") }
                        )
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            favoriteCafes.forEach { cafe ->
                                key("favorite-${cafe.id}") {
                                    CafeListItem(
                                        cafe = cafe,
                                        onClick = { onCafeClick(cafe.id) },
                                        isFavorite = true,
                                        onFavoriteClick = {
                                            viewModel.toggleFavorite(cafe.id)
                                            showToast("Removed ${cafe.name} from favorites")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    SectionHeader(title = "Quick Actions")
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                showToast("Review history coming soon")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBrown)
                        ) {
                            Text("My Reviews")
                        }
                        OutlinedButton(
                            onClick = {
                                showToast("Rewards wallet coming soon")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBrown)
                        ) {
                            Text("Rewards Wallet")
                        }
                        OutlinedButton(
                            onClick = {
                                showToast("New cafe suggestions coming soon")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBrown)
                        ) {
                            Text("Discover New Cafes")
                        }
                    }
                }

                item {
                    SectionHeader(title = "Recent Achievements")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AchievementBadge(
                            title = "Coffee Master",
                            icon = "☕",
                            modifier = Modifier.weight(1f)
                        )
                        AchievementBadge(
                            title = "Savings Star",
                            icon = "💰",
                            modifier = Modifier.weight(1f)
                        )
                        AchievementBadge(
                            title = "Loyalty Streak",
                            icon = "🔥",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                uiState.errorMessage?.let { error ->
                    item {
                        AppCard {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PrimaryBrown
        )
        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(actionLabel, color = PrimaryBrown)
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ActivityItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(backgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = PrimaryBrown,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
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

@Composable
private fun TopRatedCafeCard(
    cafe: Cafe,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier
            .width(180.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(PrimaryBrown.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "☕",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Text(
                text = cafe.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = PrimaryBrown,
                maxLines = 2
            )

            RatingStars(rating = cafe.rating.toFloat(), showNumeric = true)

            Text(
                text = "${cafe.reviewCount} reviews",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )

            OutlinedButton(
                onClick = onFavoriteClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBrown)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isFavorite) "Saved" else "Save")
            }
        }
    }
}

@Composable
private fun AchievementBadge(
    title: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(PrimaryBrown, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = PrimaryBrown,
                fontWeight = FontWeight.Medium
            )
        }
    }
}