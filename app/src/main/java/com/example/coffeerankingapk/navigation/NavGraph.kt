package com.example.coffeerankingapk.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.coffeerankingapk.ui.screens.AuthScreen
import com.example.coffeerankingapk.ui.screens.RoleSelectScreen
import com.example.coffeerankingapk.ui.screens.owner.OwnerMainScreen
import com.example.coffeerankingapk.ui.screens.lover.CafeDetailScreen
import com.example.coffeerankingapk.ui.screens.lover.CoffeeShopMapScreen
import com.example.coffeerankingapk.ui.screens.lover.LoverDiscoverScreen
import com.example.coffeerankingapk.ui.screens.lover.LoverMainScreen
import com.example.coffeerankingapk.ui.screens.lover.MapScreen
import com.example.coffeerankingapk.ui.screens.lover.RateScreen
import com.example.coffeerankingapk.ui.screens.lover.RatingScreen
import com.example.coffeerankingapk.ui.screens.lover.RewardsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "auth"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth flow
        composable("auth") {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate("role_select") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        
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
                onNavigateToAddShop = {
                    navController.navigate("owner_add_shop")
                },
                onNavigateToRating = { shopId ->
                    navController.navigate("coffee_shop_rating/$shopId")
                },
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Owner add coffee shop screen
        composable("owner_add_shop") {
            com.example.coffeerankingapk.ui.screens.owner.AddCoffeeShopScreen(
                onNavigateBack = {
                    navController.popBackStack()
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
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToRating = { shopId ->
                    navController.navigate("coffee_shop_rating/$shopId")
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
        
        // Coffee shop map with ratings
        composable("coffee_shop_map") {
            CoffeeShopMapScreen(
                onNavigateToRating = { shopId ->
                    navController.navigate("coffee_shop_rating/$shopId")
                }
            )
        }
        
        composable("coffee_shop_rating/{shopId}") { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
            RatingScreen(
                shopId = shopId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}