package com.example.coffeerankingapk.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
import com.example.coffeerankingapk.ui.theme.TextMuted

@Composable
fun RoleSelectorCard(
    title: String,
    description: String,
    isOwnerRole: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = if (isOwnerRole) Icons.Default.Home else Icons.Default.Person,
                contentDescription = null,
                tint = PrimaryBrown,
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}