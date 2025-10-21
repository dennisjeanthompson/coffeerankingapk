package com.example.coffeerankingapk.ui.screens.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCouponDialog(
    onDismiss: () -> Unit,
    onCreateCoupon: (title: String, description: String, discountPercent: Int, durationDays: Int?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var durationDays by remember { mutableStateOf("30") }

    val discountValid = discount.toIntOrNull()?.let { it in 1..100 } ?: false
    val durationValid = durationDays.isBlank() || durationDays.toIntOrNull()?.let { it >= 0 } ?: false
    val isFormValid = title.isNotBlank() && discountValid && durationValid

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
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text("Discount (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = durationDays,
                    onValueChange = { input ->
                        durationDays = input.filter { char -> char.isDigit() }
                    },
                    label = { Text("Valid for (days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Date pickers will arrive in a future update",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Create",
                onClick = {
                    if (isFormValid) {
                        onCreateCoupon(
                            title.trim(),
                            description.trim(),
                            discount.toInt(),
                            durationDays.toIntOrNull()
                        )
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
