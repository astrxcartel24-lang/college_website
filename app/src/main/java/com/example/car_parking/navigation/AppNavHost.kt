package com.example.car_parking.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.car_parking.ui.theme.screens.home.HomeScreen
import com.example.car_parking.ui.theme.screens.login.LoginScreen
import com.example.car_parking.ui.theme.screens.register.RegisterScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = ROUTE_REGISTER
    ) {
        composable(ROUTE_REGISTER) {
            RegisterScreen(navController = navController)
        }
        composable(ROUTE_LOGIN) {
            LoginScreen(navController = navController)
        }
        composable(ROUTE_HOME) {
            HomeScreen(navController = navController)
        }
    }
}

