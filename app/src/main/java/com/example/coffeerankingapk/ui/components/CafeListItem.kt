package com.example.coffeerankingapk.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.coffeerankingapk.ui.theme.TextMuted

@Composable
fun CafeListItem(
    name: String,
    description: String,
    rating: Float,
    imageUrl: String,
    onItemClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cafe image thumbnail
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Cafe image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            
            // Cafe details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                RatingStars(
                    rating = rating,
                    showNumeric = true
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    maxLines = 2
                )
            }
            
            // Favorite button
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else TextMuted
                )
            }
        }
    }
}

// Overloaded version that takes a Cafe object and supports distance
@Composable
fun CafeListItem(
    cafe: com.example.coffeerankingapk.data.Cafe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDistance: Boolean = false,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {}
) {
    CafeListItem(
        name = cafe.name,
        description = if (showDistance) {
            "${cafe.description} • ~${(cafe.rating * 2 + 3).toInt()} min"
        } else {
            cafe.description
        },
        rating = cafe.rating,
        imageUrl = cafe.imageUrl,
        isFavorite = isFavorite,
        onItemClick = onClick,
        onFavoriteClick = onFavoriteClick,
        modifier = modifier
    )
}