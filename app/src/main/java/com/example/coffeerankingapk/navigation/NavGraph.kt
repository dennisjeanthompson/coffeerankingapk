package com.example.coffeerankingapk.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.coffeerankingapk.ui.screens.AuthScreen
import com.example.coffeerankingapk.ui.screens.RoleSelectScreen
import com.example.coffeerankingapk.ui.screens.owner.OwnerAnalyticsScreen
import com.example.coffeerankingapk.ui.screens.owner.OwnerCouponsScreen
import com.example.coffeerankingapk.ui.screens.owner.OwnerDashboardScreen
import com.example.coffeerankingapk.ui.screens.owner.OwnerMapPlaceScreen
import com.example.coffeerankingapk.ui.screens.lover.CafeDetailScreen
import com.example.coffeerankingapk.ui.screens.lover.LoverDiscoverScreen
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
        
        // Owner flow - nested graph
        navigation(
            startDestination = "owner_dashboard",
            route = "owner"
        ) {
            composable("owner_dashboard") {
                OwnerDashboardScreen(
                    onNavigateToAnalytics = {
                        navController.navigate("owner_analytics")
                    },
                    onNavigateToMapPlace = {
                        navController.navigate("owner_map_place")
                    },
                    onNavigateToCoupons = {
                        navController.navigate("owner_coupons")
                    }
                )
            }
            
            composable("owner_analytics") {
                OwnerAnalyticsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("owner_map_place") {
                OwnerMapPlaceScreen(
                    onLocationConfirmed = {
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("owner_coupons") {
                OwnerCouponsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        // Lover flow - nested graph
        navigation(
            startDestination = "lover_discover",
            route = "lover"
        ) {
            composable("lover_discover") {
                LoverDiscoverScreen(
                    onCafeClick = { cafeId ->
                        navController.navigate("cafe_detail/$cafeId")
                    },
                    onNavigateToRewards = {
                        navController.navigate("lover_rewards")
                    }
                )
            }
            
            composable("cafe_detail/{cafeId}") { backStackEntry ->
                val cafeId = backStackEntry.arguments?.getString("cafeId") ?: ""
                CafeDetailScreen(
                    cafeId = cafeId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("lover_rewards") {
                RewardsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}