package com.example.car_parking.ui.theme.screens.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.car_parking.R
import com.example.car_parking.data.AuthViewModel
import com.example.car_parking.data.AuthViewModel.AuthState
import com.example.car_parking.navigation.ROUTE_REGISTER
import com.example.car_parking.ui.theme.screens.register.*

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = viewModel()
) {
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsStateWithLifecycle()

    // ── Navigate on success only — no resetState() to avoid re-trigger ────────
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val route = (authState as AuthState.Success).route
            navController.navigate(route) { popUpTo(0) { inclusive = true } }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

            Text("Welcome back.", fontSize = 42.sp, fontWeight = FontWeight.Black,
                color = Black, letterSpacing = (-1.5).sp)
            Text("Sign in to continue", fontSize = 42.sp, fontWeight = FontWeight.Black,
                color = AccentBlue, letterSpacing = (-1.5).sp)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Good to see you again.", fontSize = 15.sp, color = LabelGray)
            Spacer(modifier = Modifier.height(48.dp))

            FieldBlock(
                label = "Email Address",
                value = email,
                onValueChange = { email = it },
                placeholder = "you@example.com",
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            FieldBlock(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                placeholder = "Enter your password",
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { /* TODO: forgot password */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot password?", fontSize = 13.sp,
                    color = AccentBlue, fontWeight = FontWeight.SemiBold)
            }

            // ── Error shown inline; persists until next login attempt ─────────
            val errorMsg = (authState as? AuthState.Error)?.message.orEmpty()
            if (errorMsg.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(errorMsg, color = ErrorRed, fontSize = 13.sp,
                    fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            val isLoading = authState is AuthState.Loading

            Button(
                // Setting Loading at start of login() clears any previous error naturally
                onClick = { viewModel.login(email.trim(), password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Black, contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White,
                        strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text("Sign In", fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f),
                    color = SubtleGray, thickness = 1.dp)
                Text("or", fontSize = 13.sp, color = PlaceholderGray,
                    modifier = Modifier.padding(horizontal = 12.dp))
                HorizontalDivider(modifier = Modifier.weight(1f),
                    color = SubtleGray, thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { navController.navigate(ROUTE_REGISTER) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, SubtleGray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Black)
            ) {
                Text("Create an account", fontSize = 16.sp,
                    fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp)
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("By signing in, you agree to our ",
                    fontSize = 12.sp, color = PlaceholderGray)
                Text("Terms & Privacy", fontSize = 12.sp,
                    color = AccentBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(rememberNavController())
}