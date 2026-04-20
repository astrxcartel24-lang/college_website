package com.example.car_parking

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.example.car_parking.data.ParkingPreferencesManager
import com.example.car_parking.navigation.AppNavGraph
import com.example.car_parking.ui.theme.Car_parkingTheme
import kotlinx.coroutines.delay

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val prefsManager = remember { ParkingPreferencesManager(context) }
            val prefs by prefsManager.userPreferencesFlow.collectAsState(initial = null)

            var isLoading by remember { mutableStateOf(true) }
            var progress by remember { mutableFloatStateOf(0f) }

            // Simulate 30s loading
            LaunchedEffect(Unit) {
                val totalTime = 30000L
                val interval = 100L
                val steps = totalTime / interval
                for (i in 1..steps) {
                    delay(interval)
                    progress = i.toFloat() / steps
                }
                isLoading = false
            }

            val darkTheme = when (prefs?.themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            Car_parkingTheme(darkTheme = darkTheme) {
                if (isLoading) {
                    CustomLoadingScreen(progress)
                } else {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}

@Composable
fun CustomLoadingScreen(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF16161F)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // App Title
            Text(
                text = "SMART PARKING",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Progress percentage
            Text(
                text = "${(progress * 100).toInt()}%",
                color = Color(0xFF7F5AF0),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Custom Progress Bar
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF7F5AF0), Color(0xFF2CB67D))
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Loading parking spots...",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}
