package com.example.coffeerankingapk.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeerankingapk.data.firebase.ServiceLocator
import com.example.coffeerankingapk.data.model.UserRole
import com.example.coffeerankingapk.ui.components.OutlineButton
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.theme.BgCream
import com.example.coffeerankingapk.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = ServiceLocator.provideDefaultViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile by viewModel.userProfile.collectAsState(initial = null)
    val context = LocalContext.current
    val showToast = remember(context) {
        { message: String -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }

    LaunchedEffect(uiState.loginSuccessful) {
        if (uiState.loginSuccessful) {
            val message = when (profile?.role) {
                UserRole.OWNER -> "Welcome back, cafe owner!"
                else -> "Welcome back, coffee lover!"
            }
            showToast(message)
            viewModel.consumeLoginSuccess()
            onLoginSuccess()
        }
    }

    Scaffold(
        containerColor = BgCream
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Coffee Ranking",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            Text(
                text = "Skip typing credentials—choose a demo profile and jump in.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            PrimaryButton(
                text = if (uiState.isLoading && uiState.pendingRole == UserRole.LOVER) "Signing in..." else "Sign in as Coffee Lover",
                onClick = { viewModel.quickSignIn(UserRole.LOVER) },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            )

            OutlineButton(
                text = if (uiState.isLoading && uiState.pendingRole == UserRole.OWNER) "Signing in..." else "Sign in as Cafe Owner",
                onClick = { viewModel.quickSignIn(UserRole.OWNER) },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Text(
                text = "Demo accounts: lover@example.com or owner@example.com",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}