package com.example.coffeerankingapk.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.coffeerankingapk.ui.screens.AuthScreen
import com.example.coffeerankingapk.ui.screens.RoleSelectScreen
import com.example.coffeerankingapk.ui.screens.owner.OwnerMainScreen
import com.example.coffeerankingapk.ui.screens.lover.LoverMainScreen

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
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Lover flow - main screen with bottom navigation
        composable("lover") {
            LoverMainScreen(
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}