package com.example.car_parking.ui.theme.screens.car

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.car_parking.data.CarViewModel
import com.example.car_parking.models.CarModel

// ── Colour tokens (mirrors CarListScreen) ─────────────────────────────────────
private val BgDeep       = Color(0xFF16161F)
private val BgCard       = Color(0xFF24243E)
private val BgInput      = Color(0xFF1E1E30)
private val AccentPurple = Color(0xFF7F5AF0)
private val AccentGreen  = Color(0xFF2CB67D)
private val AccentRed    = Color(0xFFE53935)
private val TextPrimary  = Color.White
private val TextMuted    = Color(0xFFAAAAAA)
private val Divider      = Color.White.copy(alpha = 0.07f)

private val carColors = listOf("White", "Black", "Silver", "Red", "Blue", "Grey", "Other")
private val colorDotMap = mapOf(
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
fun EditCarScreen(
    navController: NavHostController,
    carId: String,
    carViewModel: CarViewModel = viewModel()
) {
    // ── Load existing car ──────────────────────────────────────────────────────
    val car by carViewModel.getCarById(carId).collectAsState(initial = null)

    // ── Form state ─────────────────────────────────────────────────────────────
    var make         by remember { mutableStateOf("") }
    var model        by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var driverName   by remember { mutableStateOf("") }
    var color        by remember { mutableStateOf(carColors.first()) }
    var photoUri     by remember { mutableStateOf<Uri?>(null) }
    var existingPhotoUrl by remember { mutableStateOf<String?>(null) }
    var colorExpanded by remember { mutableStateOf(false) }
    var isSaving     by remember { mutableStateOf(false) }
    var showSuccess  by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // ── Validation ─────────────────────────────────────────────────────────────
    val makeError         = make.isBlank()
    val modelError        = model.isBlank()
    val licensePlateError = licensePlate.isBlank()
    val driverNameError   = driverName.isBlank()
    val isFormValid       = !makeError && !modelError && !licensePlateError && !driverNameError

    // ── Populate from loaded car ───────────────────────────────────────────────
    LaunchedEffect(car) {
        car?.let {
            make             = it.make
            model            = it.model
            licensePlate     = it.licensePlate
            driverName       = it.driverName
            color            = it.color
            existingPhotoUrl = it.photoUrl
        }
    }

    // ── Photo picker ───────────────────────────────────────────────────────────
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> photoUri = uri }

    // ── Snackbar ───────────────────────────────────────────────────────────────
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            snackbarHostState.showSnackbar("Vehicle updated successfully")
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = BgDeep,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Vehicle",
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
                    // Delete button in top bar
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = AccentRed.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep)
            )
        }
    ) { padding ->

        if (car == null) {
            // ── Loading state ──────────────────────────────────────────────────
            Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentPurple)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Photo section ──────────────────────────────────────────────────
            EditPhotoSection(
                photoUri         = photoUri,
                existingPhotoUrl = existingPhotoUrl,
                onPickPhoto      = { photoPicker.launch("image/*") },
                onRemovePhoto    = {
                    photoUri         = null
                    existingPhotoUrl = null
                }
            )

            // ── Vehicle details card ───────────────────────────────────────────
            SectionCard(title = "Vehicle Details", icon = Icons.Default.DirectionsCar) {
                EditField(
                    label       = "Make",
                    value       = make,
                    onValueChange = { make = it },
                    placeholder = "e.g. Toyota",
                    icon        = Icons.Default.Build,
                    isError     = makeError && make.isEmpty().not() || (isSaving && makeError)
                )
                EditField(
                    label       = "Model",
                    value       = model,
                    onValueChange = { model = it },
                    placeholder = "e.g. Corolla",
                    icon        = Icons.Default.DirectionsCar,
                    isError     = isSaving && modelError
                )
                EditField(
                    label       = "License Plate",
                    value       = licensePlate,
                    onValueChange = { licensePlate = it.uppercase() },
                    placeholder = "e.g. KAA 123A",
                    icon        = Icons.Default.CreditCard,
                    isError     = isSaving && licensePlateError
                )

                // ── Color picker ───────────────────────────────────────────────
                Spacer(Modifier.height(4.dp))
                Text("Color", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded        = colorExpanded,
                    onExpandedChange = { colorExpanded = !colorExpanded }
                ) {
                    OutlinedTextField(
                        value         = color,
                        onValueChange = {},
                        readOnly      = true,
                        leadingIcon   = {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(colorDotMap[color] ?: Color.Gray)
                                    .border(1.dp, Color.White.copy(0.25f), CircleShape)
                            )
                        },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = colorExpanded) },
                        shape         = RoundedCornerShape(12.dp),
                        colors        = editFieldColors(),
                        modifier      = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded    = colorExpanded,
                        onDismissRequest = { colorExpanded = false },
                        modifier    = Modifier.background(BgCard)
                    ) {
                        carColors.forEach { c ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(colorDotMap[c] ?: Color.Gray)
                                                .border(1.dp, Color.White.copy(0.2f), CircleShape)
                                        )
                                        Text(c, color = TextPrimary, fontSize = 14.sp)
                                    }
                                },
                                onClick = {
                                    color         = c
                                    colorExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // ── Driver details card ────────────────────────────────────────────
            SectionCard(title = "Driver Details", icon = Icons.Default.Person) {
                EditField(
                    label       = "Driver Name",
                    value       = driverName,
                    onValueChange = { driverName = it },
                    placeholder = "Full name",
                    icon        = Icons.Default.Person,
                    isError     = isSaving && driverNameError
                )
            }

            // ── Save button ────────────────────────────────────────────────────
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    isSaving = true
                    if (isFormValid) {
                        val updated = car!!.copy(
                            make         = make.trim(),
                            model        = model.trim(),
                            licensePlate = licensePlate.trim(),
                            driverName   = driverName.trim(),
                            color        = color,
                            photoUrl     = photoUri?.toString() ?: existingPhotoUrl ?: ""
                        )
                        carViewModel.updateCar(updated) {
                            showSuccess = true
                        }
                    }
                },
                enabled  = !isSaving,
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = AccentPurple,
                    disabledContainerColor = AccentPurple.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isSaving && isFormValid) {
                    CircularProgressIndicator(
                        color    = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Save Changes",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Validation hint
            AnimatedVisibility(visible = isSaving && !isFormValid) {
                Text(
                    "Please fill in all required fields.",
                    color    = AccentRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Delete confirmation dialog ─────────────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest   = { showDeleteDialog = false },
            containerColor     = BgCard,
            title = {
                Text("Delete Vehicle?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will permanently remove ${car?.make} ${car?.model} from your records.",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        carViewModel.deleteCar(carId) {
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Delete", color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

// ── Photo section ─────────────────────────────────────────────────────────────
@Composable
private fun EditPhotoSection(
    photoUri: Uri?,
    existingPhotoUrl: String?,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    val hasPhoto = photoUri != null || !existingPhotoUrl.isNullOrBlank()

    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(BgCard)
            .border(0.5.dp, Color.White.copy(0.07f), RoundedCornerShape(18.dp))
            .clickable(onClick = onPickPhoto),
        contentAlignment = Alignment.Center
    ) {
        if (hasPhoto) {
            AsyncImage(
                model              = photoUri ?: existingPhotoUrl,
                contentDescription = "Car photo",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp))
            )
            // Dark overlay + actions
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clip(RoundedCornerShape(18.dp))
            )
            Row(
                modifier              = Modifier.align(Alignment.BottomCenter).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallChip(icon = Icons.Default.Edit, label = "Change", onClick = onPickPhoto)
                SmallChip(icon = Icons.Default.Delete, label = "Remove", onClick = onRemovePhoto, tint = AccentRed)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint     = AccentPurple.copy(alpha = 0.6f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text("Tap to add photo", color = TextMuted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SmallChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Text(label, color = tint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Section card ──────────────────────────────────────────────────────────────
@Composable
private fun SectionCard(
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        HorizontalDivider(color = Divider)
        content()
    }
}

// ── Labelled input field ──────────────────────────────────────────────────────
@Composable
private fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isError: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            color      = if (isError) AccentRed else TextMuted,
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = { Text(placeholder, color = TextMuted.copy(alpha = 0.5f), fontSize = 14.sp) },
            leadingIcon   = {
                Icon(icon, contentDescription = null, tint = if (isError) AccentRed else AccentPurple, modifier = Modifier.size(18.dp))
            },
            isError    = isError,
            shape      = RoundedCornerShape(12.dp),
            colors     = editFieldColors(),
            singleLine = true,
            modifier   = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun editFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = AccentPurple,
    unfocusedBorderColor    = Color.White.copy(0.12f),
    errorBorderColor        = AccentRed,
    focusedTextColor        = TextPrimary,
    unfocusedTextColor      = TextPrimary,
    cursorColor             = AccentPurple,
    focusedContainerColor   = BgInput,
    unfocusedContainerColor = BgInput,
    errorContainerColor     = AccentRed.copy(alpha = 0.08f)
)