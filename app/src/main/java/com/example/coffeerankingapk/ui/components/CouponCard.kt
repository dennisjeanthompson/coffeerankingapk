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
    description: String,
    discountPercent: Int,
    expiryDate: Date?,
    isExpired: Boolean = false,
    isActive: Boolean = true,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val expiryLabel = expiryDate?.let { dateFormat.format(it) } ?: "No expiry"

    AppCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    color = when {
                        isExpired -> Danger
                        isActive -> Success
                        else -> TextMuted
                    }
                )
            }

            if (description.isNotBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }

            Text(
                text = "Expires: $expiryLabel",
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    isExpired -> Danger
                    !isActive -> TextMuted
                    else -> TextMuted
                }
            )

            when {
                isExpired || !isActive -> {
                    val statusText = if (isExpired) "Expired" else "Inactive"
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isExpired) Danger else TextMuted,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                actionText != null && onActionClick != null -> {
                    PrimaryButton(
                        text = actionText,
                        onClick = onActionClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}