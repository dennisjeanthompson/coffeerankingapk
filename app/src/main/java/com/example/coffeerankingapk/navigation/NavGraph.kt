package com.example.coffeerankingapk.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.coffeerankingapk.ui.screens.lover.CafeDetailScreen
import com.example.coffeerankingapk.ui.screens.lover.LoverDiscoverScreen
import com.example.coffeerankingapk.ui.screens.lover.LoverMainScreen
import com.example.coffeerankingapk.ui.screens.lover.MapScreen
import com.example.coffeerankingapk.ui.screens.lover.RateScreen
import com.example.coffeerankingapk.ui.screens.lover.RewardsScreen
import com.example.coffeerankingapk.ui.screens.AuthScreen
import com.example.coffeerankingapk.ui.screens.RoleSelectScreen
import com.example.coffeerankingapk.ui.screens.owner.OwnerMainScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "role_select"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("role_select") {
            RoleSelectScreen(
                onOwnerSelected = {
                    navController.navigate("owner") {
                        popUpTo("role_select") { inclusive = true }
                    }
                },
                onLoverSelected = {
                    navController.navigate("lover") {
                        popUpTo("role_select") { inclusive = true }
                    }
                }
            )
        }
        
        // Owner flow - main screen with bottom navigation
        composable("owner") {
            OwnerMainScreen(
                onLogout = {
                    navController.navigate("role_select") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Lover flow - main screen with bottom navigation
        composable("lover") {
            LoverMainScreen(
                onCafeClick = { cafeId ->
                    navController.navigate("cafe_detail/$cafeId")
                },
                onNavigateToRewards = {
                    // Rewards is handled within LoverMainScreen
                },
                onLogout = {
                    navController.navigate("role_select") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Optional auth route retained for later reactivation
        composable("auth") {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate("role_select") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        
        composable("cafe_detail/{cafeId}") { backStackEntry ->
            val cafeId = backStackEntry.arguments?.getString("cafeId") ?: ""
            CafeDetailScreen(
                cafeId = cafeId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToRate = {
                    navController.navigate("rate_cafe/$cafeId")
                }
            )
        }
        
        composable("rate_cafe/{cafeId}") { backStackEntry ->
            val cafeId = backStackEntry.arguments?.getString("cafeId") ?: ""
            RateScreen(
                cafeId = cafeId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("map") {
            MapScreen(
                onNavigateToCafe = { cafeId ->
                    navController.navigate("cafe_detail/$cafeId")
                }
            )
        }
    }
}