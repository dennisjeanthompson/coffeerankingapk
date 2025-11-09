package com.example.coffeerankingapk.ui.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeerankingapk.ui.screens.lover.CoffeeShopMapScreen
import com.example.coffeerankingapk.viewmodel.CoffeeShopViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerMapScreen(
    onNavigateToAddShop: () -> Unit,
    onNavigateToRating: (String) -> Unit,
    viewModel: CoffeeShopViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val coffeeShops by viewModel.coffeeShops.collectAsState()
    
    // Filter to show only owner's shops
    val ownShops = if (currentUser != null) {
        coffeeShops.filter { it.ownerId == currentUser.uid }
    } else {
        emptyList()
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Use the same map view as lovers, but filtered to owner's shops
        CoffeeShopMapScreen(
            onNavigateToRating = onNavigateToRating
        )
        
        // Stats card at top
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Coffee Shops",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${ownShops.size} shop${if (ownShops.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (currentUser != null) {
                    FilledTonalButton(
                        onClick = onNavigateToAddShop
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Shop")
                    }
                }
            }
        }
        
        // FAB for adding shop
        if (currentUser != null) {
            FloatingActionButton(
                onClick = onNavigateToAddShop,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Coffee Shop")
            }
        } else {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            ) {
                Text(
                    text = "Please log in to add coffee shops",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
