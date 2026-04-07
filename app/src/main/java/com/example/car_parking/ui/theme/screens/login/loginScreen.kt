package com.example.car_parking.ui.theme.screens.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.car_parking.R
import com.example.car_parking.navigation.ROUTE_HOME
import com.example.car_parking.navigation.ROUTE_REGISTER
import com.example.car_parking.ui.theme.screens.register.AccentBlue
import com.example.car_parking.ui.theme.screens.register.Black
import com.example.car_parking.ui.theme.screens.register.LabelGray
import com.example.car_parking.ui.theme.screens.register.OffWhite
import com.example.car_parking.ui.theme.screens.register.PlaceholderGray
import com.example.car_parking.ui.theme.screens.register.SubtleGray
import com.example.car_parking.ui.theme.screens.register.SurfaceWhite
import com.example.car_parking.ui.theme.screens.register.FieldBlock
import com.example.car_parking.ui.theme.screens.register.ErrorRed
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavHostController) {

    val auth = FirebaseAuth.getInstance()

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf("") }

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
                contentDescription = "logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = "Welcome back.",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Black,
                letterSpacing = (-1.5).sp
            )
            Text(
                text = "Sign in to continue",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = AccentBlue,
                letterSpacing = (-1.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Good to see you again.",
                fontSize = 15.sp,
                color = LabelGray
            )

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
                Text(
                    text = "Forgot password?",
                    fontSize = 13.sp,
                    color = AccentBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // ── Error message ─────────────────────────────────────────────────
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = ErrorRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    when {
                        email.isBlank() -> {
                            errorMessage = "Enter your email"
                        }
                        password.isBlank() -> {
                            errorMessage = "Enter your password"
                        }
                        else -> {
                            isLoading = true
                            errorMessage = ""

                            auth.signInWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        navController.navigate(ROUTE_HOME) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    } else {
                                        errorMessage = task.exception?.message
                                            ?: "Login failed"
                                    }
                                }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Black,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = "Sign In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = SubtleGray, thickness = 1.dp)
                Text(
                    text = "or",
                    fontSize = 13.sp,
                    color = PlaceholderGray,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = SubtleGray, thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    navController.navigate(ROUTE_REGISTER)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, SubtleGray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Black)
            ) {
                Text(
                    text = "Create an account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "By signing in, you agree to our ", fontSize = 12.sp, color = PlaceholderGray)
                Text(text = "Terms & Privacy", fontSize = 12.sp, color = AccentBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}
