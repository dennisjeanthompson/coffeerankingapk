package com.example.coffeerankingapk.ui.screens.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.theme.TextMuted
import com.example.coffeerankingapk.viewmodel.CoffeeShopViewModel
import com.example.coffeerankingapk.viewmodel.CouponViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCouponScreen(
    couponId: String? = null,
    onNavigateBack: () -> Unit,
    coffeeShopViewModel: CoffeeShopViewModel = viewModel(),
    couponViewModel: CouponViewModel = viewModel()
) {
    val ownerShops by coffeeShopViewModel.ownerShops.collectAsState()
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    val ownerShop = ownerShops.firstOrNull { it.ownerId == currentUserId } ?: ownerShops.firstOrNull()
    
    val isLoading by couponViewModel.isLoading.collectAsState()
    val successMessage by couponViewModel.operationSuccess.collectAsState()
    
    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf("percent") } // "percent" or "amount"
    var discountPercent by remember { mutableStateOf("") }
    var discountAmount by remember { mutableStateOf("") }
    var minimumPurchase by remember { mutableStateOf("") }
    var maxRedemptions by remember { mutableStateOf("") }
    var isUnlimitedRedemptions by remember { mutableStateOf(true) }
    var code by remember { mutableStateOf("") }
    
    // Dates
    var startDate by remember { mutableStateOf(Date()) }
    var expiryDate by remember { 
        mutableStateOf(Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 30) }.time) 
    }
    
    val startDateDialogState = rememberMaterialDialogState()
    val expiryDateDialogState = rememberMaterialDialogState()
    
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    // Navigate back on success
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            couponViewModel.clearSuccess()
            onNavigateBack()
        }
    }
    
    // Form validation
    val isFormValid = title.isNotBlank() &&
            ownerShop != null &&
            ((discountType == "percent" && discountPercent.toIntOrNull()?.let { it in 1..100 } == true) ||
             (discountType == "amount" && discountAmount.toDoubleOrNull()?.let { it > 0 } == true)) &&
            expiryDate.after(startDate)
    
    Scaffold(
        containerColor = BgCream,
        topBar = {
            TopAppBar(
                title = { Text(if (couponId == null) "Create Coupon" else "Edit Coupon") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Coupon Title") },
                placeholder = { Text("e.g., Weekend Special") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("Describe your offer...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
            
            // Discount Type Selector
            Text(
                text = "Discount Type",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = discountPercent,
                    onValueChange = { 
                        discountPercent = it
                        discountType = "percent"
                        discountAmount = ""
                    },
                    label = { Text("Discount (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = discountAmount,
                    onValueChange = { 
                        discountAmount = it
                        discountType = "amount"
                        discountPercent = ""
                    },
                    label = { Text("Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            // Minimum Purchase
            OutlinedTextField(
                value = minimumPurchase,
                onValueChange = { minimumPurchase = it },
                label = { Text("Minimum Purchase ($) - Optional") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Start Date
            OutlinedTextField(
                value = dateFormat.format(startDate),
                onValueChange = {},
                label = { Text("Start Date") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { startDateDialogState.show() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select start date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Expiry Date
            OutlinedTextField(
                value = dateFormat.format(expiryDate),
                onValueChange = {},
                label = { Text("Expiry Date") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { expiryDateDialogState.show() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select expiry date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Max Redemptions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Unlimited Redemptions",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = isUnlimitedRedemptions,
                    onCheckedChange = { 
                        isUnlimitedRedemptions = it
                        if (it) maxRedemptions = ""
                    }
                )
            }
            
            if (!isUnlimitedRedemptions) {
                OutlinedTextField(
                    value = maxRedemptions,
                    onValueChange = { maxRedemptions = it },
                    label = { Text("Max Redemptions") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            // Coupon Code (Optional)
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase() },
                label = { Text("Coupon Code (Optional)") },
                placeholder = { Text("e.g., WELCOME20") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Create Button
            PrimaryButton(
                text = if (couponId == null) "Create Coupon" else "Save Changes",
                onClick = {
                    ownerShop?.id?.let { shopId ->
                        couponViewModel.createCoupon(
                            shopId = shopId,
                            title = title,
                            description = description,
                            discountPercent = if (discountType == "percent") discountPercent.toIntOrNull() ?: 0 else 0,
                            discountAmount = if (discountType == "amount") discountAmount.toDoubleOrNull() ?: 0.0 else 0.0,
                            minimumPurchase = minimumPurchase.toDoubleOrNull() ?: 0.0,
                            startDate = startDate,
                            expiryDate = expiryDate,
                            maxRedemptions = if (isUnlimitedRedemptions) -1 else maxRedemptions.toIntOrNull() ?: -1,
                            code = code
                        )
                    }
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            
            if (ownerShop == null) {
                Text(
                    text = "Please add a coffee shop first before creating coupons",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
    
    // Start Date Picker Dialog
    MaterialDialog(
        dialogState = startDateDialogState,
        buttons = {
            positiveButton("OK")
            negativeButton("Cancel")
        }
    ) {
        datepicker(
            initialDate = LocalDate.now(),
            title = "Select Start Date"
        ) { selectedDate ->
            startDate = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        }
    }
    
    // Expiry Date Picker Dialog
    MaterialDialog(
        dialogState = expiryDateDialogState,
        buttons = {
            positiveButton("OK")
            negativeButton("Cancel")
        }
    ) {
        datepicker(
            initialDate = LocalDate.now().plusDays(30),
            title = "Select Expiry Date"
        ) { selectedDate ->
            expiryDate = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        }
    }
}
