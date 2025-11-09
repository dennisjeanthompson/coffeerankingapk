package com.example.coffeerankingapk.ui.screens.lover

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeerankingapk.viewmodel.CoffeeShopViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(
    shopId: String,
    onNavigateBack: () -> Unit,
    viewModel: CoffeeShopViewModel = viewModel()
) {
    var rating by remember { mutableStateOf(3.0) }
    var comment by remember { mutableStateOf("") }
    val ratingSubmitted by viewModel.ratingSubmitted.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val currentShop = viewModel.coffeeShops.collectAsState().value
        .find { it.id == shopId }
    
    LaunchedEffect(shopId) {
        if (currentShop == null) {
            viewModel.loadCoffeeShops()
        }
    }
    
    LaunchedEffect(ratingSubmitted) {
        if (ratingSubmitted) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rate Coffee Shop") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentShop != null) {
                Text(
                    text = currentShop.name,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Current rating: ★ ${String.format("%.1f", currentShop.averageRating)} (${currentShop.totalRatings} ratings)",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
            
            // Star rating display
            Text(
                text = "Your Rating",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                for (i in 1..5) {
                    IconButton(
                        onClick = { rating = i.toDouble() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Star $i",
                            tint = if (i <= rating) Color(0xFFFFD700) else Color.LightGray,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            
            Text(
                text = String.format("%.1f / 5.0", rating),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Rating slider for fine control
            Text(
                text = "Fine-tune your rating:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Slider(
                value = rating.toFloat(),
                onValueChange = { rating = it.toDouble() },
                valueRange = 1f..5f,
                steps = 39, // 0.1 increments: (5.0 - 1.0) / 0.1 - 1
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )
            
            // Comment field
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Comment (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(bottom = 24.dp),
                maxLines = 4
            )
            
            // Error message
            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Submit button
            Button(
                onClick = {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        viewModel.submitRating(shopId, userId, rating)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && FirebaseAuth.getInstance().currentUser != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Submit Rating")
                }
            }
            
            if (FirebaseAuth.getInstance().currentUser == null) {
                Text(
                    text = "You must be logged in to rate",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
