package com.example.car_parking.ui.theme.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.car_parking.data.AuthViewModel
import com.example.car_parking.data.AuthViewModel.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = viewModel()
) {
    var selectedItem by rememberSaveable { mutableStateOf(0) }

    val authState by viewModel.authState.collectAsStateWithLifecycle()

    // ── Only navigate away on logout (Success → ROUTE_LOGIN) ─────────────────
    // Login Success (ROUTE_HOME) is what brought us here — ignore it
    LaunchedEffect(authState) {
        val state = authState
        if (state is AuthState.Success && state.route != "home") {
            navController.navigate(state.route) { popUpTo(0) { inclusive = true } }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Parking Dashboard",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        style = TextStyle(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF7F5AF0), Color(0xFF2CB67D))
                            )
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {  // ← actually calls signOut()
                        Icon(Icons.Default.ExitToApp,
                            contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0C29),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF24243E)) {
                listOf(
                    Triple("Home",    Icons.Default.Home,          0),
                    Triple("Profile", Icons.Default.AccountCircle, 1),
                    Triple("Settings",Icons.Default.Settings,      2)
                ).forEach { (label, icon, index) ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick  = { selectedItem = index },
                        icon     = { Icon(icon, contentDescription = label) },
                        label    = { Text(label, color = Color.Cyan) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Smart Parking System", fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp))

            // ── Status card ───────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF24243E))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // TODO: drive these from a ParkingViewModel reading Firebase
                    Column {
                        Text("Available", color = Color.White)
                        Text("18 Slots", fontSize = 20.sp, color = Color.White)
                    }
                    Column {
                        Text("Occupied", color = Color.White)
                        Text("32 Slots", fontSize = 20.sp, color = Color.White)
                    }
                }
            }

            // ── Action cards ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionCard(
                    label    = "Add Car",
                    subtitle = "Register new vehicle",
                    gradient = Brush.verticalGradient(
                        listOf(Color(0xFF7F5AF0), Color(0xFF5A3ED9))),
                    icon     = { Icon(Icons.Default.Add, "Add Car",
                        tint = Color.White, modifier = Modifier.size(28.dp)) },
                    onClick  = { /* TODO: navigate to add car screen */ },
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    label    = "View Cars",
                    subtitle = "Browse all vehicles",
                    gradient = Brush.verticalGradient(
                        listOf(Color(0xFF2CB67D), Color(0xFF1A8A5A))),
                    icon     = { Icon(Icons.AutoMirrored.Filled.List, "View Cars",
                        tint = Color.White, modifier = Modifier.size(28.dp)) },
                    onClick  = { /* TODO: navigate to car list screen */ },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ActionCard(
    label: String,
    subtitle: String,
    gradient: Brush,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier.height(140.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick   = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color.White.copy(0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { icon() }
                Spacer(modifier = Modifier.height(10.dp))
                Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(rememberNavController())
}