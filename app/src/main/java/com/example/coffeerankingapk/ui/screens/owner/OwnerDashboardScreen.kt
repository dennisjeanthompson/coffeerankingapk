package com.example.coffeerankingapk.ui.screens.owner

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.ui.components.AppCard
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
import com.example.coffeerankingapk.ui.theme.Success
import com.example.coffeerankingapk.ui.theme.TextMuted

@Composable
fun OwnerDashboardScreen(
    onNavigateToAnalytics: () -> Unit,
    onNavigateToCoupons: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header with welcome message
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome back!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                        Text(
                            text = "Brew & Beans Cafe",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBrown
                        )
                        Text(
                            text = "Your cafe is performing well today",
                            style = MaterialTheme.typography.bodySmall,
                            color = Success
                        )
                    }
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Cafe",
                        modifier = Modifier.size(48.dp),
                        tint = PrimaryBrown
                    )
                }
            }
        }
        
        // KPI Cards
        item {
            Column {
                Text(
                    text = "Today's Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        KPICard(
                            title = "Total Reviews",
                            value = "248",
                            change = "+12 today",
                            icon = Icons.Default.Star,
                            changePositive = true
                        )
                    }
                    item {
                        KPICard(
                            title = "Avg Rating",
                            value = "4.6",
                            change = "+0.2 this week",
                            icon = Icons.Default.Star,
                            changePositive = true
                        )
                    }
                    item {
                        KPICard(
                            title = "Monthly Visits",
                            value = "1,245",
                            change = "+5.2%",
                            icon = Icons.Default.Person,
                            changePositive = true
                        )
                    }
                    item {
                        KPICard(
                            title = "Revenue",
                            value = "$12,430",
                            change = "+8.1%",
                            icon = Icons.Default.Star,
                            changePositive = true
                        )
                    }
                }
            }
        }
        
        // Recent Activity
        item {
            Column {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                AppCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ActivityItem(
                            icon = Icons.Default.Star,
                            title = "New 5-star review",
                            subtitle = "\"Amazing coffee and service!\" - Sarah M.",
                            time = "2 hours ago"
                        )
                        
                        Divider(color = Color.Gray.copy(alpha = 0.2f))
                        
                        ActivityItem(
                            icon = Icons.Default.LocationOn,
                            title = "Location updated",
                            subtitle = "Your cafe location was verified",
                            time = "1 day ago"
                        )
                        
                        Divider(color = Color.Gray.copy(alpha = 0.2f))
                        
                        ActivityItem(
                            icon = Icons.Default.Settings,
                            title = "New coupon claimed",
                            subtitle = "\"20% off\" coupon used by customer",
                            time = "2 days ago"
                        )
                    }
                }
            }
        }
        
        // Quick Actions
        item {
            Column {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title = "Analytics",
                        subtitle = "View detailed insights",
                        icon = Icons.Default.Star,
                        onClick = onNavigateToAnalytics,
                        modifier = Modifier.weight(1f)
                    )
                    
                    ActionCard(
                        title = "Manage Coupons",
                        subtitle = "Create promotional offers",
                        icon = Icons.Default.Settings,
                        onClick = onNavigateToCoupons,
                        modifier = Modifier.weight(1f)
                    )
                }

                PrimaryButton(
                    text = "Create Coupon",
                    onClick = {
                        Toast.makeText(
                            context,
                            "Coupon creation coming soon!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun KPICard(
    title: String,
    value: String,
    change: String,
    icon: ImageVector,
    changePositive: Boolean,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.width(180.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = PrimaryBrown,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
            
            Text(
                text = change,
                style = MaterialTheme.typography.bodySmall,
                color = if (changePositive) Success else Color.Red
            )
        }
    }
}

@Composable
private fun ActivityItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    time: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = PrimaryBrown,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = PrimaryBrown
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
        
        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = PrimaryBrown,
                modifier = Modifier.size(32.dp)
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}