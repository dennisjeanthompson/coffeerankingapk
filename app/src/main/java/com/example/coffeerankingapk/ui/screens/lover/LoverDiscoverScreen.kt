package com.example.coffeerankingapk.ui.screens.lover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.ui.components.CafeListItem
import com.example.coffeerankingapk.ui.theme.BgCream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoverDiscoverScreen(
    onCafeClick: (String) -> Unit,
    onNavigateToRewards: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedBottomNavItem by remember { mutableStateOf(0) }
    
    // Mock cafe data
    val cafes = remember {
        listOf(
            CafeData(
                id = "1",
                name = "The Roasted Bean",
                description = "Cozy coffee shop with artisan pastries and excellent WiFi for remote work.",
                rating = 4.6f,
                imageUrl = "https://via.placeholder.com/300x200/8B4513/FFFFFF?text=Cafe+1"
            ),
            CafeData(
                id = "2",
                name = "Brew & Grind",
                description = "Specialty coffee roasters serving single-origin beans from around the world.",
                rating = 4.8f,
                imageUrl = "https://via.placeholder.com/300x200/6B3E2A/FFFFFF?text=Cafe+2"
            ),
            CafeData(
                id = "3",
                name = "Morning Glory Cafe",
                description = "Fresh breakfast options and the best espresso in town. Pet-friendly atmosphere.",
                rating = 4.4f,
                imageUrl = "https://via.placeholder.com/300x200/8B4513/FFFFFF?text=Cafe+3"
            ),
            CafeData(
                id = "4",
                name = "Sunset Coffee Co",
                description = "Evening coffee experience with live acoustic music and local art displays.",
                rating = 4.7f,
                imageUrl = "https://via.placeholder.com/300x200/6B3E2A/FFFFFF?text=Cafe+4"
            )
        )
    }
    
    var favoriteCafes by remember { mutableStateOf(setOf<String>()) }
    
    Scaffold(
        containerColor = BgCream,
        bottomBar = {
            BottomAppBar(
                containerColor = androidx.compose.ui.graphics.Color.White
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedBottomNavItem == 0,
                    onClick = { selectedBottomNavItem = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
                    label = { Text("Map") },
                    selected = selectedBottomNavItem == 1,
                    onClick = { selectedBottomNavItem = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Rewards") },
                    label = { Text("Rewards") },
                    selected = selectedBottomNavItem == 2,
                    onClick = { 
                        selectedBottomNavItem = 2
                        onNavigateToRewards()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = selectedBottomNavItem == 3,
                    onClick = { selectedBottomNavItem = 3 }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Discover Cafes",
                style = MaterialTheme.typography.headlineLarge
            )
            
            // Search bar with filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search cafes...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = { /* TODO: Implement filter */ }
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Filter")
                }
            }
            
            // Cafe list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cafes.filter { 
                    searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)
                }) { cafe ->
                    CafeListItem(
                        name = cafe.name,
                        description = cafe.description,
                        rating = cafe.rating,
                        imageUrl = cafe.imageUrl,
                        isFavorite = cafe.id in favoriteCafes,
                        onItemClick = { onCafeClick(cafe.id) },
                        onFavoriteClick = {
                            favoriteCafes = if (cafe.id in favoriteCafes) {
                                favoriteCafes - cafe.id
                            } else {
                                favoriteCafes + cafe.id
                            }
                        }
                    )
                }
            }
        }
    }
}

private data class CafeData(
    val id: String,
    val name: String,
    val description: String,
    val rating: Float,
    val imageUrl: String
)