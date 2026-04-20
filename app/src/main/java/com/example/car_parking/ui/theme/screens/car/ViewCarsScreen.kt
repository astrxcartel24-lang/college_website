package com.example.car_parking.ui.theme.screens.car

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import java.text.SimpleDateFormat
import java.util.*

// ── Colour tokens (mirrors AddCarScreen) ─────────────────────────────────────
private val BgDeep       = Color(0xFF16161F)
private val BgCard       = Color(0xFF24243E)
private val BgField      = Color(0xFF2E2E4A)
private val AccentPurple = Color(0xFF7F5AF0)
private val AccentGreen  = Color(0xFF2CB67D)
private val TextPrimary  = Color.White
private val TextMuted    = Color(0xFFAAAAAA)
private val ErrorRed     = Color(0xFFFF6B6B)

private val colorMap = mapOf(
    "White"  to Color(0xFFF5F5F5),
    "Black"  to Color(0xFF1A1A1A),
    "Silver" to Color(0xFFC0C0C0),
    "Red"    to Color(0xFFE53935),
    "Blue"   to Color(0xFF1E88E5),
    "Grey"   to Color(0xFF757575),
    "Other"  to Color(0xFF7F5AF0)
)

private val fuelIconMap = mapOf(
    "Petrol"   to Icons.Default.LocalGasStation,
    "Diesel"   to Icons.Default.LocalGasStation,
    "Electric" to Icons.Default.ElectricCar,
    "Hybrid"   to Icons.Default.EnergySavingsLeaf
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewCarScreen(
    navController: NavHostController,
    carId: String,
    carViewModel: CarViewModel = viewModel()
) {
    val car by carViewModel.getCarById(carId).collectAsState(initial = null)
    val scrollState = rememberScrollState()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Car Details",
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
                actions = {
                    car?.let {
                        IconButton(onClick = {
                            navController.navigate("edit_car/${it.carId}")
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentPurple)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep)
            )
        }
    ) { padding ->

        if (car == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentPurple)
            }
            return@Scaffold
        }

        val c = car!!

        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 10 }
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Hero Photo ────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(BgCard)
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(listOf(AccentPurple, AccentGreen)),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    if (!c.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model              = c.photoUrl,
                            contentDescription = "Car photo",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                        // Gradient overlay for text legibility
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                                    )
                                )
                        )
                    } else {
                        Box(
                            modifier         = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint     = AccentPurple.copy(alpha = 0.4f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("No photo available", color = TextMuted, fontSize = 13.sp)
                            }
                        }
                    }

                    // Plate badge overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentPurple.copy(alpha = 0.92f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            c.licensePlate,
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 15.sp,
                            letterSpacing = 2.sp
                        )
                    }

                    // Color dot overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(14.dp)
                            .size(32.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(colorMap[c.color] ?: Color.Gray)
                            .border(2.dp, Color.White.copy(0.35f), CircleShape)
                    )
                }

                // ── Make / Model / Year headline ──────────────────────────────
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${c.make} ${c.model}",
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        c.year.toString(),
                        fontSize = 15.sp,
                        color    = TextMuted
                    )
                }

                // ── Quick stats row ───────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatChip(
                        icon  = fuelIconMap[c.fuelType] ?: Icons.Default.LocalGasStation,
                        label = c.fuelType,
                        modifier = Modifier.weight(1f)
                    )
                    StatChip(
                        icon  = Icons.Default.Circle,
                        label = c.color,
                        iconTint = colorMap[c.color] ?: AccentPurple,
                        modifier = Modifier.weight(1f)
                    )
                }

                // ── Entry Date & Time ─────────────────────────────────────────
                c.entryDateTime?.let { millis ->
                    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
                    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
                    val date = Date(millis)

                    DetailCard(title = "Parking Entry") {
                        DetailRow(
                            icon  = Icons.Default.DateRange,
                            label = "Entry Date",
                            value = dateFormat.format(date)
                        )
                        Divider(color = Color.White.copy(0.06f), thickness = 1.dp)
                        DetailRow(
                            icon  = Icons.Default.AccessTime,
                            label = "Entry Time",
                            value = timeFormat.format(date)
                        )
                    }
                }

                // ── Driver Info ───────────────────────────────────────────────
                DetailCard(title = "Driver Information") {
                    DetailRow(
                        icon  = Icons.Default.Person,
                        label = "Driver Name",
                        value = c.driverName.ifBlank { "—" }
                    )
                    Divider(color = Color.White.copy(0.06f), thickness = 1.dp)
                    DetailRow(
                        icon  = Icons.Default.Call,
                        label = "Phone Number",
                        value = c.phoneNumber.ifBlank { "—" }
                    )
                }

                // ── Vehicle Info ──────────────────────────────────────────────
                DetailCard(title = "Vehicle Information") {
                    DetailRow(
                        icon  = Icons.Default.Star,
                        label = "Make",
                        value = c.make
                    )
                    Divider(color = Color.White.copy(0.06f), thickness = 1.dp)
                    DetailRow(
                        icon  = Icons.Default.Info,
                        label = "Model",
                        value = c.model
                    )
                    Divider(color = Color.White.copy(0.06f), thickness = 1.dp)
                    DetailRow(
                        icon  = Icons.Default.DateRange,
                        label = "Year",
                        value = c.year.toString()
                    )
                    Divider(color = Color.White.copy(0.06f), thickness = 1.dp)
                    DetailRow(
                        icon  = Icons.Default.MailOutline,
                        label = "License Plate",
                        value = c.licensePlate
                    )
                    Divider(color = Color.White.copy(0.06f), thickness = 1.dp)
                    DetailRow(
                        icon  = Icons.Default.Circle,
                        label = "Color",
                        value = c.color,
                        valueColor = colorMap[c.color]
                    )
                    Divider(color = Color.White.copy(0.06f), thickness = 1.dp)
                    DetailRow(
                        icon  = fuelIconMap[c.fuelType] ?: Icons.Default.LocalGasStation,
                        label = "Fuel Type",
                        value = c.fuelType
                    )
                }

                // ── Notes ─────────────────────────────────────────────────────
                if (!c.notes.isNullOrBlank()) {
                    DetailCard(title = "Notes") {
                        Text(
                            c.notes,
                            color    = TextPrimary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }

                // ── Recorded at ───────────────────────────────────────────────
                val recordedFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
                Text(
                    "Recorded on ${recordedFormat.format(Date(c.createdAt))}",
                    color    = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }
}

// ── Quick stat chip ───────────────────────────────────────────────────────────
@Composable
private fun StatChip(
    icon: ImageVector,
    label: String,
    iconTint: Color = AccentPurple,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .border(
                width = 0.5.dp,
                color = Color.White.copy(0.08f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        Text(label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// ── Grouped detail card ───────────────────────────────────────────────────────
@Composable
private fun DetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            title.uppercase(),
            color         = TextMuted,
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Medium,
            letterSpacing = 1.sp,
            modifier      = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(BgCard)
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(0.07f),
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            content()
        }
    }
}

// ── Single info row inside a card ─────────────────────────────────────────────
@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AccentPurple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint     = AccentPurple,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                color      = valueColor ?: TextPrimary,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}