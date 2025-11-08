package com.example.coffeerankingapk.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.R
import com.example.coffeerankingapk.ui.components.OutlineButton
import com.example.coffeerankingapk.ui.components.PrimaryButton
import com.example.coffeerankingapk.ui.theme.BgCream
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSignUpMode by remember { mutableStateOf(false) }
    
    val isFormValid = email.isNotBlank() && password.isNotBlank()
    
    // Configure Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }
    
    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                
                // Debug log
                android.util.Log.d("AuthScreen", "Google Sign-In account: ${account?.email}")
                android.util.Log.d("AuthScreen", "ID Token: ${account?.idToken?.take(20)}...")
                
                if (account?.idToken == null) {
                    val error = "Failed to get ID token. Add SHA-1 to Firebase Console."
                    errorMessage = error
                    isLoading = false
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    return@rememberLauncherForActivityResult
                }
                
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                
                // Sign in to Firebase with Google credential
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        isLoading = false
                        if (authTask.isSuccessful) {
                            val user = auth.currentUser
                            android.util.Log.d("AuthScreen", "Firebase sign-in successful: ${user?.email}")
                            Toast.makeText(
                                context,
                                "Welcome ${user?.displayName ?: user?.email}!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onLoginSuccess()
                        } else {
                            val error = authTask.exception?.message ?: "Google sign-in failed"
                            android.util.Log.e("AuthScreen", "Firebase sign-in failed: $error", authTask.exception)
                            errorMessage = error
                            Toast.makeText(
                                context,
                                "Sign-in failed: $error",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } catch (e: ApiException) {
                isLoading = false
                val errorMsg = "Google sign-in failed: ${e.statusCode} - ${e.message}"
                android.util.Log.e("AuthScreen", errorMsg, e)
                errorMessage = errorMsg
                Toast.makeText(
                    context,
                    errorMsg,
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            isLoading = false
            android.util.Log.d("AuthScreen", "Google Sign-In cancelled or failed with result code: ${result.resultCode}")
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
            // App logo/title
            Text(
                text = if (isSignUpMode) "Create Account" else "Welcome Back",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = if (isSignUpMode) "Sign up to get started" else "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Info else Icons.Default.Lock,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
            
            // Error message display
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Login/Sign Up button
            PrimaryButton(
                text = if (isLoading) {
                    if (isSignUpMode) "Creating Account..." else "Signing in..."
                } else {
                    if (isSignUpMode) "Sign Up" else "Sign In"
                },
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all fields"
                        return@PrimaryButton
                    }
                    
                    if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters"
                        return@PrimaryButton
                    }
                    
                    isLoading = true
                    errorMessage = null
                    
                    if (isSignUpMode) {
                        // Sign Up
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Account created! Welcome!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onLoginSuccess()
                                } else {
                                    errorMessage = task.exception?.message ?: "Failed to create account"
                                }
                            }
                    } else {
                        // Sign In
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    Toast.makeText(
                                        context,
                                        "Welcome back ${user?.email}!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onLoginSuccess()
                                } else {
                                    errorMessage = task.exception?.message ?: "Authentication failed"
                                }
                            }
                    }
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            )
            
            // Google sign in button
            OutlineButton(
                text = "Continue with Google",
                onClick = {
                    isLoading = true
                    errorMessage = null
                    // Launch Google Sign-In
                    val signInIntent = googleSignInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
            
            // Skip login button for testing
            OutlineButton(
                text = "Skip Login (Testing)",
                onClick = {
                    Toast.makeText(
                        context,
                        "Skipping authentication - Demo mode",
                        Toast.LENGTH_SHORT
                    ).show()
                    onLoginSuccess()
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            
            // Toggle between Sign In and Sign Up
            Row(
                modifier = Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isSignUpMode) "Already have an account? " else "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ClickableText(
                    text = AnnotatedString(
                        if (isSignUpMode) "Sign In" else "Sign Up"
                    ),
                    onClick = {
                        isSignUpMode = !isSignUpMode
                        errorMessage = null
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            // Note about Firebase setup
            Text(
                text = "Add SHA-1: 60:3B:F7:B2:77:16:A0:4B:0D:97:8C:F7:43:33:CE:2D:84:F5:C5:A0\nto Firebase Console for Google Sign-In",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}