package com.example.coffeerankingapk.ui.screens.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.ui.components.CouponCard
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerCouponsScreen(
    onNavigateBack: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var coupons by remember { 
        mutableStateOf(
            listOf(
                CouponData(
                    id = "1",
                    title = "Weekend Special",
                    discountPercent = 20,
                    expiryDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 5) }.time
                ),
                CouponData(
                    id = "2",
                    title = "New Customer",
                    discountPercent = 15,
                    expiryDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 30) }.time
                ),
                CouponData(
                    id = "3",
                    title = "Holiday Treat",
                    discountPercent = 25,
                    expiryDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -2) }.time
                )
            )
        )
    }
    
    Scaffold(
        containerColor = BgCream,
        topBar = {
            TopAppBar(
                title = { Text("Coupons") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgCream
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = PrimaryBrown,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create coupon")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(coupons) { coupon ->
                val isExpired = coupon.expiryDate.before(Date())
                CouponCard(
                    title = coupon.title,
                    discountPercent = coupon.discountPercent,
                    expiryDate = coupon.expiryDate,
                    isExpired = isExpired
                )
            }
        }
    }
    
    if (showCreateDialog) {
        CreateCouponDialog(
            onDismiss = { showCreateDialog = false },
            onCouponCreated = { newCoupon ->
                coupons = coupons + newCoupon
                showCreateDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCouponDialog(
    onDismiss: () -> Unit,
    onCouponCreated: (CouponData) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var isFormValid by remember { mutableStateOf(false) }
    
    // Form validation
    isFormValid = title.isNotBlank() && 
                  discount.toIntOrNull()?.let { it in 1..100 } == true
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Coupon") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Coupon Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text("Discount (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // TODO: Add date pickers for start and end dates
                Text(
                    text = "Date pickers will be implemented with compose-material-dialogs library",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Create",
                onClick = {
                    if (isFormValid) {
                        val newCoupon = CouponData(
                            id = System.currentTimeMillis().toString(),
                            title = title,
                            discountPercent = discount.toInt(),
                            expiryDate = Calendar.getInstance().apply { 
                                add(Calendar.DAY_OF_MONTH, 30) 
                            }.time
                        )
                        onCouponCreated(newCoupon)
                    }
                },
                enabled = isFormValid
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private data class CouponData(
    val id: String,
    val title: String,
    val discountPercent: Int,
    val expiryDate: Date
)