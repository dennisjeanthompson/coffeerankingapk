package com.example.coffeerankingapk.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.ui.theme.Danger
import com.example.coffeerankingapk.ui.theme.Success
import com.example.coffeerankingapk.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CouponCard(
    title: String,
    discountPercent: Int,
    expiryDate: Date,
    isExpired: Boolean = false,
    onRedeemClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    AppCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "${discountPercent}% OFF",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isExpired) Danger else Success
                )
            }
            
            Text(
                text = "Expires: ${dateFormat.format(expiryDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isExpired) Danger else TextMuted
            )
            
            if (!isExpired && onRedeemClick != null) {
                PrimaryButton(
                    text = "Redeem",
                    onClick = onRedeemClick,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (isExpired) {
                Text(
                    text = "Expired",
                    style = MaterialTheme.typography.labelMedium,
                    color = Danger,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}