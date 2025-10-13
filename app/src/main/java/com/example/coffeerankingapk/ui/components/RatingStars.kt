package com.example.coffeerankingapk.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.example.coffeerankingapk.ui.theme.Success
import com.example.coffeerankingapk.ui.theme.TextMuted

@Composable
fun RatingStars(
    rating: Float,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    showNumeric: Boolean = true,
    interactive: Boolean = false,
    onRatingChanged: ((Float) -> Unit)? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxStars) { index ->
            val isSelected = index < rating.toInt()
            val isHalfSelected = index == rating.toInt() && rating % 1 != 0f
            
            Icon(
                imageVector = if (isSelected || isHalfSelected) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Star ${index + 1}",
                tint = if (isSelected || isHalfSelected) Success else TextMuted,
                modifier = Modifier
                    .size(16.dp)
                    .clickable(enabled = interactive) {
                        onRatingChanged?.invoke((index + 1).toFloat())
                    }
            )
        }
        
        if (showNumeric) {
            Text(
                text = String.format("%.1f", rating),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun InteractiveRatingStars(
    currentRating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxStars: Int = 5
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxStars) { index ->
            val isSelected = index < currentRating
            
            Icon(
                imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Star ${index + 1}",
                tint = if (isSelected) Success else TextMuted,
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        onRatingChanged(index + 1)
                    }
            )
        }
    }
}