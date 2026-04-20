package com.example.car_parking.ui.theme.screens.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.car_parking.data.UserViewModel
import com.example.car_parking.navigation.ROUTE_EDIT_PROFILE
import com.example.car_parking.navigation.ROUTE_LOGIN
import com.example.car_parking.navigation.ROUTE_HOME

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val BgDeep       = Color(0xFF16161F)
private val BgCard       = Color(0xFF24243E)
private val AccentPurple = Color(0xFF7F5AF0)
private val AccentGreen  = Color(0xFF2CB67D)
private val AccentRed    = Color(0xFFE53935)
private val TextPrimary  = Color.White
private val TextMuted    = Color(0xFFAAAAAA)
private val DividerColor = Color.White.copy(alpha = 0.07f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    userViewModel: UserViewModel = viewModel()
) {
    val user by userViewModel.getCurrentUser().collectAsState(initial = null)
    var showLogoutDialog by remember { mutableStateOf(false) }
    var contentVisible   by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { contentVisible = true }

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Profile",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        style      = TextStyle(
                            brush = Brush.horizontalGradient(listOf(AccentPurple, AccentGreen))
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(ROUTE_HOME) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(ROUTE_EDIT_PROFILE) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = AccentPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep)
            )
        }
    ) { padding ->

        if (user == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentPurple)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Hero / Avatar card ─────────────────────────────────────────────
            AnimatedVisibility(
                visible = contentVisible,
                enter   = fadeIn(tween(400)) + slideInVertically(tween(400)) { -40 }
            ) {
                ProfileHeroCard(
                    avatarUrl   = user!!.avatarUrl,
                    fullName    = user!!.fullName,
                    email       = user!!.email,
                    onEditClick = { navController.navigate(ROUTE_EDIT_PROFILE) }
                )
            }

            // ── Stats row ──────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = contentVisible,
                enter   = fadeIn(tween(400, 100)) + slideInVertically(tween(400, 100)) { 30 }
            ) {
                val totalCars    by userViewModel.getTotalCars().collectAsState(initial = 0)
                val parkedToday  by userViewModel.getParkedToday().collectAsState(initial = 0)
                val totalRevenue by userViewModel.getTotalRevenue().collectAsState(initial = 0.0)

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(label = "Total Cars",    value = "$totalCars",           icon = Icons.Default.DirectionsCar, modifier = Modifier.weight(1f))
                    StatCard(label = "Parked Today",  value = "$parkedToday",          icon = Icons.Default.LocalParking,  modifier = Modifier.weight(1f))
                    StatCard(label = "Revenue",       value = "KES ${totalRevenue.toInt()}", icon = Icons.Default.AttachMoney,   modifier = Modifier.weight(1f))
                }
            }

            // ── Account details ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = contentVisible,
                enter   = fadeIn(tween(400, 160)) + slideInVertically(tween(400, 160)) { 30 }
            ) {
                ProfileSectionCard(title = "Account Details", icon = Icons.Default.Person) {
                    ProfileInfoRow(label = "Full Name",    value = user!!.fullName,          icon = Icons.Default.Badge)
                    HorizontalDivider(color = DividerColor)
                    ProfileInfoRow(label = "Email",        value = user!!.email,             icon = Icons.Default.Email)
                    HorizontalDivider(color = DividerColor)
                    ProfileInfoRow(label = "Phone",        value = user!!.phone ?: "—",      icon = Icons.Default.Phone)
                    HorizontalDivider(color = DividerColor)
                    ProfileInfoRow(label = "Member Since", value = user!!.memberSince ?: "—", icon = Icons.Default.CalendarToday)
                }
            }

            // ── Settings ───────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = contentVisible,
                enter   = fadeIn(tween(400, 220)) + slideInVertically(tween(400, 220)) { 30 }
            ) {
                ProfileSectionCard(title = "Settings", icon = Icons.Default.Settings) {
                    ProfileActionRow(
                        label    = "Edit Profile",
                        icon     = Icons.Default.Edit,
                        iconTint = AccentPurple,
                        onClick  = { navController.navigate(ROUTE_EDIT_PROFILE) }
                    )
                    HorizontalDivider(color = DividerColor)
                    ProfileActionRow(
                        label    = "Change Password",
                        icon     = Icons.Default.Lock,
                        iconTint = AccentPurple,
                        onClick  = { navController.navigate(ROUTE_EDIT_PROFILE) }
                    )
                    HorizontalDivider(color = DividerColor)
                    ProfileActionRow(
                        label    = "Notifications",
                        icon     = Icons.Default.Notifications,
                        iconTint = AccentPurple,
                        onClick  = { /* navigate to notifications settings */ }
                    )
                }
            }

            // ── Danger zone ────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = contentVisible,
                enter   = fadeIn(tween(400, 280)) + slideInVertically(tween(400, 280)) { 30 }
            ) {
                ProfileSectionCard(title = "Account", icon = Icons.Default.ManageAccounts) {
                    ProfileActionRow(
                        label    = "Sign Out",
                        icon     = Icons.AutoMirrored.Filled.Logout,
                        iconTint = AccentRed,
                        labelColor = AccentRed,
                        onClick  = { showLogoutDialog = true }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Logout confirmation ────────────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor   = BgCard,
            title = {
                Text("Sign Out?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("You will be returned to the login screen.", color = TextMuted)
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    userViewModel.signOut()
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Sign Out", color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

// ── Hero card ─────────────────────────────────────────────────────────────────
@Composable
private fun ProfileHeroCard(
    avatarUrl: String?,
    fullName: String,
    email: String,
    onEditClick: () -> Unit
) {
    val initials = fullName.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(BgCard)
            .border(0.5.dp, Color.White.copy(0.07f), RoundedCornerShape(22.dp))
            .padding(24.dp)
    ) {
        // Subtle gradient blob
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(AccentPurple.copy(alpha = 0.18f), Color.Transparent)
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.fillMaxWidth()
        ) {
            // Avatar
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            if (avatarUrl.isNullOrBlank())
                                Brush.linearGradient(listOf(AccentPurple, AccentGreen))
                            else Brush.linearGradient(listOf(BgCard, BgCard))
                        )
                        .border(2.5.dp, AccentPurple.copy(0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model              = avatarUrl,
                            contentDescription = "Avatar",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            initials,
                            color      = Color.White,
                            fontSize   = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                // Edit badge
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(AccentPurple)
                        .border(2.dp, BgDeep, CircleShape)
                        .clickable(onClick = onEditClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint     = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                fullName,
                color      = TextPrimary,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(email, color = TextMuted, fontSize = 13.sp)

            Spacer(Modifier.height(16.dp))

            // Edit button
            OutlinedButton(
                onClick = onEditClick,
                shape   = RoundedCornerShape(10.dp),
                colors  = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurple),
                border  = androidx.compose.foundation.BorderStroke(1.dp, AccentPurple.copy(0.5f)),
                modifier = Modifier.fillMaxWidth(0.55f)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Edit Profile", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Stat card ─────────────────────────────────────────────────────────────────
@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(0.5.dp, Color.White.copy(0.07f), RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AccentPurple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp))
        }
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

// ── Section card ──────────────────────────────────────────────────────────────
@Composable
private fun ProfileSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BgCard)
            .border(0.5.dp, Color.White.copy(0.07f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier              = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        HorizontalDivider(color = DividerColor, modifier = Modifier.padding(bottom = 4.dp))
        content()
    }
}

// ── Info display row ──────────────────────────────────────────────────────────
@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = AccentPurple.copy(0.7f), modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextMuted, fontSize = 11.sp)
            Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ── Tappable action row ───────────────────────────────────────────────────────
@Composable
private fun ProfileActionRow(
    label: String,
    icon: ImageVector,
    iconTint: Color = AccentPurple,
    labelColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Text(label, color = labelColor, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint     = TextMuted.copy(0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}
