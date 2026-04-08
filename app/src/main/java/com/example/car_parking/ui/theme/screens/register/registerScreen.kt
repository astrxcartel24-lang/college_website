package com.example.car_parking.ui.theme.screens.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.car_parking.R
import com.example.car_parking.data.AuthViewModel
import com.example.car_parking.data.AuthViewModel.AuthState
import com.example.car_parking.navigation.ROUTE_LOGIN
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.LocalTextStyle

// ── Color tokens ──────────────────────────────────────────────────────────────
val OffWhite        = Color(0xFFF8F8F8)
val SurfaceWhite    = Color(0xFFFFFFFF)
val Black           = Color(0xFF111111)
val AccentBlue      = Color(0xFF2563EB)
val LabelGray       = Color(0xFF6B7280)
val PlaceholderGray = Color(0xFF9CA3AF)
val SubtleGray      = Color(0xFFE5E7EB)
val ErrorRed        = Color(0xFFDC2626)

@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = viewModel()
) {
    var fullName           by remember { mutableStateOf("") }
    var email              by remember { mutableStateOf("") }
    var password           by remember { mutableStateOf("") }
    var confirmPass        by remember { mutableStateOf("") }
    var passwordVisible    by remember { mutableStateOf(false) }
    var confirmPassVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsStateWithLifecycle()

    // ── Navigate on success — no resetState() ─────────────────────────────────
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

            Text("Welcome.", fontSize = 42.sp, fontWeight = FontWeight.Black,
                color = Black, letterSpacing = (-1.5).sp)
            Text("Create your account", fontSize = 42.sp, fontWeight = FontWeight.Black,
                color = AccentBlue, letterSpacing = (-1.5).sp)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Join us today. It only takes a minute.", fontSize = 15.sp, color = LabelGray)
            Spacer(modifier = Modifier.height(48.dp))

            FieldBlock(
                label = "Full Name",
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "John Doe",
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
            )
            Spacer(modifier = Modifier.height(20.dp))

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
                placeholder = "Min. 8 characters",
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
            )
            Spacer(modifier = Modifier.height(20.dp))

            FieldBlock(
                label = "Confirm Password",
                value = confirmPass,
                onValueChange = { confirmPass = it },
                placeholder = "Repeat your password",
                isPassword = true,
                passwordVisible = confirmPassVisible,
                onTogglePassword = { confirmPassVisible = !confirmPassVisible },
                leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) }
            )

            val errorMsg = (authState as? AuthState.Error)?.message.orEmpty()
            if (errorMsg.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(errorMsg, color = ErrorRed, fontSize = 13.sp,
                    fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(40.dp))

            val isLoading = authState is AuthState.Loading

            Button(
                onClick = { viewModel.signup(fullName.trim(), email.trim(), password, confirmPass) },
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
                    Text("Create Account", fontSize = 16.sp,
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
                onClick = { navController.navigate(ROUTE_LOGIN) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, SubtleGray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Black)
            ) {
                Text("Log in instead", fontSize = 16.sp,
                    fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp)
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("By registering, you agree to our ",
                    fontSize = 12.sp, color = PlaceholderGray)
                Text("Terms & Privacy", fontSize = 12.sp,
                    color = AccentBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Shared reusable field component ──────────────────────────────────────────
@Composable
fun FieldBlock(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color = Black, letterSpacing = 0.4.sp,
            modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 15.sp, color = PlaceholderGray) },
            leadingIcon = leadingIcon,
            trailingIcon = if (isPassword && onTogglePassword != null) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible)
                                "Hide password" else "Show password"
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Black,
                unfocusedBorderColor = SubtleGray,
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                cursorColor = AccentBlue,
                focusedTextColor = Black,
                unfocusedTextColor = Black,
                focusedLeadingIconColor = Black,
                unfocusedLeadingIconColor = LabelGray,
                focusedTrailingIconColor = Black,
                unfocusedTrailingIconColor = LabelGray
            ),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 15.sp, fontWeight = FontWeight.Normal)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(rememberNavController())
}