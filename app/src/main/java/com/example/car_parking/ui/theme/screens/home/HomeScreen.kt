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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.car_parking.navigation.ROUTE_LOGIN
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {

    val selectedItem = remember { mutableStateOf(0) }

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

                // ✅ LOGOUT BUTTON
                actions = {
                    IconButton(onClick = {

                        FirebaseAuth.getInstance().signOut()

                        navController.navigate(ROUTE_LOGIN) {
                            popUpTo(0) { inclusive = true } // clears backstack
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
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

                NavigationBarItem(
                    selected = selectedItem.value == 0,
                    onClick = { selectedItem.value = 0 },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home", color = Color.Cyan) }
                )

                NavigationBarItem(
                    selected = selectedItem.value == 1,
                    onClick = { selectedItem.value = 1 },
                    icon = { Icon(Icons.Default.AccountCircle, null) },
                    label = { Text("Profile", color = Color.Cyan) }
                )

                NavigationBarItem(
                    selected = selectedItem.value == 2,
                    onClick = { selectedItem.value = 2 },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Settings", color = Color.Cyan) }
                )
            }
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                text = "Smart Parking System",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // STATUS CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF24243E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

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

            // ACTION CARDS ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ADD CAR
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF7F5AF0), Color(0xFF5A3ED9))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color.White.copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Car",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                "Add Car",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                "Register new vehicle",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                }

                // VIEW CARS
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF2CB67D), Color(0xFF1A8A5A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color.White.copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.List,
                                    contentDescription = "View Cars",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                "View Cars",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                "Browse all vehicles",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(rememberNavController())
}