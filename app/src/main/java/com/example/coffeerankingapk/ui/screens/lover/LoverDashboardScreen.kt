package com.example.coffeerankingapk.ui.screens.lover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.data.MockData
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.size
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.CafeListItem
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.components.RatingStars
import com.example.coffeerankingapk.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoverDashboardScreen(
    onCafeClick: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    val nearbycafes = MockData.cafes.take(3)
    val topRatedCafes = MockData.cafes.sortedByDescending { it.rating }.take(3)
    val recentlyVisited = MockData.cafes.take(2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
    ) {
        // Header with greeting and location
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Good morning!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                        Text(
                            text = "Coffee Lover",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBrown
                        )
                    }
                    
                    IconButton(onClick = onNotificationClick) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = PrimaryBrown
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Location row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Downtown, New York",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search bar
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    placeholder = { Text("Search for cafes...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryBrown,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    onClick = onSearchClick
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Nearby Cafes Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nearby Cafes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBrown
                        )
                        TextButton(onClick = onSearchClick) {
                            Text("See All", color = SecondaryBrown)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(nearbycafes) { cafe ->
                            AppCard(
                                modifier = Modifier.width(250.dp),
                                onClick = { onCafeClick(cafe.id) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = cafe.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryBrown
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = cafe.address,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RatingStars(
                                            rating = cafe.rating,
                                            modifier = Modifier,
    
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${cafe.rating}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "~${(cafe.rating * 2 + 3).toInt()} min",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Success
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Top Rated Section
            item {
                Column {
                    Text(
                        text = "Top Rated",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            items(topRatedCafes) { cafe ->
                CafeListItem(
                    cafe = cafe,
                    onClick = { onCafeClick(cafe.id) }
                )
            }
            
            // Recently Visited Section
            item {
                Column {
                    Text(
                        text = "Recently Visited",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            items(recentlyVisited) { cafe ->
                CafeListItem(
                    cafe = cafe,
                    onClick = { onCafeClick(cafe.id) }
                )
            }
            
            // Quick Actions
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
                        PrimaryButton(
                            text = "Find Cafes",
                            onClick = onSearchClick,
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SecondaryBrown
                            )
                        ) {
                            Text("My Reviews")
                        }
                    }
                }
            }
        }
    }
}