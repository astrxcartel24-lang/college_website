package com.example.car_parking.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.car_parking.data.AuthViewModel
import com.example.car_parking.ui.theme.screens.car.AddCarScreen
import com.example.car_parking.ui.theme.screens.home.HomeScreen
import com.example.car_parking.ui.theme.screens.login.LoginScreen
import com.example.car_parking.ui.theme.screens.register.RegisterScreen
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = ROUTE_REGISTER
    ) {
        composable(ROUTE_REGISTER) { RegisterScreen(navController = navController) }
        composable(ROUTE_LOGIN) { LoginScreen(navController = navController) }
        composable(ROUTE_HOME) { HomeScreen(navController = navController) }
        
        composable(ROUTE_ADD_CAR) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            AddCarScreen(
                navController = navController,
                userId = userId
            )
        }
    }
}
