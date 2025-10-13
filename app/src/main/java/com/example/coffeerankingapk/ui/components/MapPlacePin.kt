package com.example.coffeerankingapk.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.ui.theme.Danger

@Composable
fun MapPlacePin(
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Default.LocationOn,
        contentDescription = "Place pin",
        tint = Danger,
        modifier = modifier.size(32.dp)
    )
}