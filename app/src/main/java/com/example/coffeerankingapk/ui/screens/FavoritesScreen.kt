package com.example.coffeerankingapk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.data.model.Favorite
import com.example.coffeerankingapk.data.repository.FavoritesRepository
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.RatingStars
import com.example.coffeerankingapk.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCafe: (String) -> Unit
) {
    val repository = remember { FavoritesRepository() }
    val scope = rememberCoroutineScope()
    
    var favorites by remember { mutableStateOf<List<Favorite>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        repository.getUserFavorites().collect { result ->
            result.onSuccess { list ->
                favorites = list
                isLoading = false
            }.onFailure {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        containerColor = BgCream,
        topBar = {
            TopAppBar(
                title = { Text("My Favorites") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgCream
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBrown)
                    }
                }
                favorites.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.FavoriteBorder,
                            contentDescription = "No favorites",
                            modifier = Modifier.size(80.dp),
                            tint = TextMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Favorites Yet",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBrown
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start exploring and add your favorite coffee shops!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(favorites) { favorite ->
                            FavoriteItem(
                                favorite = favorite,
                                onClick = { onNavigateToCafe(favorite.shopId) },
                                onRemove = {
                                    scope.launch {
                                        repository.removeFavorite(favorite.shopId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteItem(
    favorite: Favorite,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = favorite.shopName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = favorite.shopAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RatingStars(
                        rating = favorite.averageRating.toFloat(),
                        showNumeric = false
                    )
                    Text(
                        text = "%.1f".format(favorite.averageRating),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Remove from favorites",
                    tint = Color.Red
                )
            }
        }
    }
}
