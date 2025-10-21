package com.example.coffeerankingapk.ui.screens.lover

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.coffeerankingapk.data.MockData
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.InteractiveRatingStars
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
import com.example.coffeerankingapk.ui.theme.Success
import com.example.coffeerankingapk.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateScreen(
    cafeId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val showToast: (String) -> Unit = { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    var selectedRating by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    var selectedQuickComment by remember { mutableStateOf<String?>(null) }
    var isCoffeeHot by remember { mutableStateOf<Boolean?>(null) }
    
    // Mock cafe data
    val cafe = MockData.cafes.find { it.id == cafeId } ?: MockData.cafes.first()
    
    val quickComments = listOf(
        "Great coffee!",
        "Amazing atmosphere",
        "Friendly staff",
        "Good value",
        "Perfect location",
        "Love the vibe"
    )
    
    Scaffold(
        containerColor = BgCream,
        topBar = {
            TopAppBar(
                title = { Text("Rate a Cafe") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Cafe header card
            item {
                AppCard {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(cafe.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Cafe image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = cafe.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBrown
                            )
                            Text(
                                text = "Lower Boracay St.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "⭐",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${cafe.rating}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                                Text(
                                    text = "(${(cafe.rating * 20).toInt()} reviews)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
            
            // Rating section
            item {
                Text(
                    text = "How was your experience?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    InteractiveRatingStars(
                        currentRating = selectedRating,
                        onRatingChanged = { rating ->
                            selectedRating = rating
                            showToast("Rated ${cafe.name} $rating stars")
                        }
                    )
                }
            }
            
            // Hot questions
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Was the coffee hot?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryBrown
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val yesSelected = isCoffeeHot == true
                            val noSelected = isCoffeeHot == false
                            Text(
                                text = "Yes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (yesSelected) Color.White else Success,
                                modifier = Modifier
                                    .background(
                                        if (yesSelected) Success else Success.copy(alpha = 0.1f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clickable {
                                        isCoffeeHot = true
                                        showToast("Noted — the coffee was hot!")
                                    }
                            )
                            Text(
                                text = "No",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (noSelected) Color.White else TextMuted,
                                modifier = Modifier
                                    .background(
                                        if (noSelected) PrimaryBrown else Color.Gray.copy(alpha = 0.1f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clickable {
                                        isCoffeeHot = false
                                        showToast("Thanks, we'll help the cafe improve")
                                    }
                            )
                        }
                    }
                }
            }
            
            // Quick comments
            item {
                Text(
                    text = "Quick Comment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryBrown
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickComments) { comment ->
                        val isSelected = selectedQuickComment == comment
                        Text(
                            text = comment,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) Color.White else PrimaryBrown,
                            modifier = Modifier
                                .background(
                                    if (isSelected) PrimaryBrown else PrimaryBrown.copy(alpha = 0.1f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable { 
                                    selectedQuickComment = if (isSelected) null else comment
                                    val message = if (isSelected) {
                                        "Removed quick comment"
                                    } else {
                                        "Selected \"$comment\""
                                    }
                                    showToast(message)
                                }
                        )
                    }
                }
            }
            
            // Add photo section
            item {
                Text(
                    text = "Add a Photo (Optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryBrown
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PhotoOptionCard(
                        icon = Icons.Default.Add,
                        title = "Camera",
                        onClick = {
                            showToast("Camera upload coming soon")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PhotoOptionCard(
                        icon = Icons.Default.Settings,
                        title = "Gallery",
                        onClick = {
                            showToast("Gallery picker coming soon")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Share moment text
            item {
                Text(
                    text = "Share your coffee moment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryBrown
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("What did you think?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBrown,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            // Submit button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                PrimaryButton(
                    text = "Submit Rating",
                    onClick = {
                        val quickNote = selectedQuickComment ?: "No quick comment"
                        showToast("Submitted $selectedRating★ with note: $quickNote")
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = {
                        showToast("Draft saved locally")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryBrown
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PrimaryBrown)
                    )
                ) {
                    Text("Save Draft")
                }
            }
        }
    }
}

@Composable
private fun PhotoOptionCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(PrimaryBrown.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = PrimaryBrown,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBrown,
                fontWeight = FontWeight.Medium
            )
        }
    }
}