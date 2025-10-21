package com.example.coffeerankingapk.ui.screens.lover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.ui.components.CafeListItem
import com.example.coffeerankingapk.ui.theme.*
import com.example.coffeerankingapk.data.MockData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoverDiscoverScreen(
    onCafeClick: (String) -> Unit,
    onNavigateToRewards: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var favoriteCafes by remember { mutableStateOf(setOf<String>()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
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
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBrown,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            IconButton(
                onClick = { /* Filter action */ }
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Filter")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Cafe list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(MockData.cafes.filter { 
                searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)
            }) { cafe ->
                CafeListItem(
                    cafe = cafe,
                    isFavorite = cafe.id in favoriteCafes,
                    onClick = { onCafeClick(cafe.id) },
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