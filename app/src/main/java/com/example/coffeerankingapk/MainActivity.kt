package com.example.coffeerankingapk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.coffeerankingapk.navigation.NavGraph
import com.example.coffeerankingapk.ui.theme.AppTheme

/**
 * Main Activity hosting Compose navigation
 * Includes TomTom map integration and permission handling
 * 
 * Note: Toast messages for route loading are displayed in MapScreen.kt
 * when routes are successfully calculated and loaded
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize any TomTom SDK components if needed
        // TomTom SDK initialization would go here if required
        
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        startDestination = "auth"
                    )
                }
            }
        }
    }
}