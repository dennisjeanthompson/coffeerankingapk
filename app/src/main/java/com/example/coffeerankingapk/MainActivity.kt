package com.example.coffeerankingapk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.coffeerankingapk.navigation.NavGraph
import com.example.coffeerankingapk.ui.navigation.TurnByTurnNavigationActivity
import com.example.coffeerankingapk.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val deepLinkEvents = MutableSharedFlow<DeepLinkDestination>(extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleDeepLinkIntent(intent)

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val context = LocalContext.current

                    LaunchedEffect(navController) {
                        deepLinkEvents.collectLatest { destination ->
                            when (destination) {
                                DeepLinkDestination.OpenMap -> {
                                    navController.navigate("map") {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }

                                is DeepLinkDestination.StartTurnByTurn -> {
                                    navController.navigate("map") {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                    val navigationIntent = TurnByTurnNavigationActivity.createIntent(
                                        context,
                                        destination.latitude,
                                        destination.longitude
                                    )
                                    context.startActivity(navigationIntent)
                                }
                            }
                        }
                    }

                    NavGraph(
                        navController = navController,
                        startDestination = "auth"
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        if (intent == null || intent.action != Intent.ACTION_VIEW) return

        val data = intent.data ?: return
        when (data.host) {
            "map" -> {
                val latitude = data.getQueryParameter("lat")?.toDoubleOrNull()
                val longitude = data.getQueryParameter("lng")?.toDoubleOrNull()
                if (latitude != null && longitude != null) {
                    deepLinkEvents.tryEmit(
                        DeepLinkDestination.StartTurnByTurn(latitude, longitude)
                    )
                } else {
                    deepLinkEvents.tryEmit(DeepLinkDestination.OpenMap)
                }
            }
        }
    }

    private sealed class DeepLinkDestination {
        data object OpenMap : DeepLinkDestination()
        data class StartTurnByTurn(val latitude: Double, val longitude: Double) : DeepLinkDestination()
    }
}
