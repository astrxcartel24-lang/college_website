package com.example.car_parking.ui.theme.screens.car



import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
                                                                     import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.car_parking.data.CarViewModel
import com.example.car_parking.models.CarModel
import com.example.car_parking.navigation.ROUTE_ADD_CAR
import com.example.car_parking.navigation.ROUTE_VIEW_CAR
import java.text.SimpleDateFormat
import java.util.*

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val BgDeep       = Color(0xFF16161F)
private val BgCard       = Color(0xFF24243E)
private val AccentPurple = Color(0xFF7F5AF0)
private val AccentGreen  = Color(0xFF2CB67D)
private val TextPrimary  = Color.White
private val TextMuted    = Color(0xFFAAAAAA)

private val colorMap = mapOf(
    "White"  to Color(0xFFF5F5F5),
    "Black"  to Color(0xFF1A1A1A),
    "Silver" to Color(0xFFC0C0C0),
    "Red"    to Color(0xFFE53935),
    "Blue"   to Color(0xFF1E88E5),
    "Grey"   to Color(0xFF757575),
    "Other"  to Color(0xFF7F5AF0)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarListScreen(
    navController: NavHostController,
    carViewModel: CarViewModel = viewModel()
) {
    val cars by carViewModel.getAllCars().collectAsState(initial = emptyList<CarModel>())
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(cars, searchQuery) {
        if (searchQuery.isBlank()) cars
        else cars.filter {
            it.make.contains(searchQuery, ignoreCase = true) ||
                    it.model.contains(searchQuery, ignoreCase = true) ||
                    it.licensePlate.contains(searchQuery, ignoreCase = true) ||
                    it.driverName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "All Vehicles",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        style      = TextStyle(
                            brush = Brush.horizontalGradient(listOf(AccentPurple, AccentGreen))
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick           = { navController.navigate(ROUTE_ADD_CAR) },
                containerColor    = AccentPurple,
                contentColor      = Color.White,
                shape             = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Car")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            // ── Search bar ────────────────────────────────────────────────────
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder   = { Text("Search by make, plate, driver…", color = TextMuted, fontSize = 13.sp) },
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentPurple) },
                trailingIcon  = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                colors  = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = AccentPurple,
                    unfocusedBorderColor    = Color.White.copy(0.12f),
                    focusedTextColor        = TextPrimary,
                    unfocusedTextColor      = TextPrimary,
                    cursorColor             = AccentPurple,
                    focusedContainerColor   = BgCard,
                    unfocusedContainerColor = BgCard
                ),
                shape    = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // ── Count badge ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentPurple.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${filtered.size} vehicle${if (filtered.size != 1) "s" else ""}",
                        color      = AccentPurple,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (searchQuery.isNotBlank()) {
                    Text("matching \"$searchQuery\"", color = TextMuted, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── List / empty state ────────────────────────────────────────────
            if (filtered.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint     = AccentPurple.copy(alpha = 0.3f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (searchQuery.isBlank()) "No vehicles parked yet"
                            else "No results found",
                            color      = TextMuted,
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (searchQuery.isBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Tap + to register a vehicle", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement   = Arrangement.spacedBy(12.dp),
                    modifier              = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(filtered, key = { _, car -> car.carId }) { index, car ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { visible = true }

                        AnimatedVisibility(
                            visible = visible,
                            enter   = fadeIn(tween(300, delayMillis = index * 60)) +
                                    slideInVertically(tween(300, delayMillis = index * 60)) { it / 8 }
                        ) {
                            CarListItem(
                                car         = car,
                                onClick     = { navController.navigate("$ROUTE_VIEW_CAR/${car.carId}") }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(72.dp)) } // FAB clearance
                }
            }
        }
    }
}

// ── Car list card ─────────────────────────────────────────────────────────────
@Composable
private fun CarListItem(
    car: CarModel,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BgCard)
            .border(0.5.dp, Color.White.copy(0.07f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Thumbnail / placeholder ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF16161F)),
            contentAlignment = Alignment.Center
        ) {
            if (!car.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model              = car.photoUrl,
                    contentDescription = "Car photo",
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint     = AccentPurple.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // ── Info ──────────────────────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "${car.make} ${car.model}",
                    color      = TextPrimary,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                // Color dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colorMap[car.color] ?: Color.Gray)
                        .border(1.dp, Color.White.copy(0.2f), CircleShape)
                )
            }

            Text(
                car.licensePlate,
                color      = AccentGreen,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Text(car.driverName, color = TextMuted, fontSize = 12.sp)
            }
        }

        // ── Arrow ─────────────────────────────────────────────────────────────
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted.copy(alpha = 0.5f)
        )
    }
}
