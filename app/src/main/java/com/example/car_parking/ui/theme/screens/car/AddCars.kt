package com.example.car_parking.ui.theme.screens.car

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.car_parking.data.CarViewModel
import com.example.car_parking.models.CarModel
import java.util.UUID

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val BgDeep       = Color(0xFF16161F)
private val BgCard       = Color(0xFF24243E)
private val BgField      = Color(0xFF2E2E4A)
private val AccentPurple = Color(0xFF7F5AF0)
private val AccentGreen  = Color(0xFF2CB67D)
private val TextPrimary  = Color.White
private val TextMuted    = Color(0xFFAAAAAA)
private val ErrorRed     = Color(0xFFFF6B6B)

private val fuelTypes  = listOf("Petrol", "Diesel", "Electric", "Hybrid")
private val colorNames = listOf("White", "Black", "Silver", "Red", "Blue", "Grey", "Other")
private val colorMap   = mapOf(
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
fun AddCarScreen(
    navController: NavHostController,
    userId: String,
    carViewModel: CarViewModel = viewModel()
) {
    val context = LocalContext.current
    var make          by remember { mutableStateOf("") }
    var model         by remember { mutableStateOf("") }
    var year          by remember { mutableStateOf("") }
    var licensePlate  by remember { mutableStateOf("") }
    var notes         by remember { mutableStateOf("") }
    var selectedFuel  by remember { mutableStateOf("Petrol") }
    var selectedColor by remember { mutableStateOf("White") }
    var imageUri      by remember { mutableStateOf<Uri?>(null) }
    var driverName    by remember { mutableStateOf("") }
    var phoneNumber   by remember { mutableStateOf("") }

    var makeError    by remember { mutableStateOf(false) }
    var modelError   by remember { mutableStateOf(false) }
    var yearError    by remember { mutableStateOf(false) }
    var plateError   by remember { mutableStateOf(false) }
    var driverError  by remember { mutableStateOf(false) }
    var phoneError   by remember { mutableStateOf(false) }

    var isSaving     by remember { mutableStateOf(false) }
    var saveError    by remember { mutableStateOf<String?>(null) }

    val carId = remember { UUID.randomUUID().toString() }
    val scrollState = rememberScrollState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    fun validate(): Boolean {
        makeError   = make.isBlank()
        modelError  = model.isBlank()
        yearError   = year.isBlank() || year.toIntOrNull() == null
        plateError  = licensePlate.isBlank()
        driverError = driverName.isBlank()
        phoneError  = phoneNumber.isBlank()
        return !makeError && !modelError && !yearError && !plateError && !driverError && !phoneError
    }


    fun saveCar() {
        if (!validate() || isSaving) return
        isSaving  = true
        saveError = null

        val car = CarModel(
            carId        = carId,
            userId       = userId,
            make         = make.trim(),
            model        = model.trim(),
            year         = year.toInt(),
            licensePlate = licensePlate.trim().uppercase(),
            color        = selectedColor,
            fuelType     = selectedFuel,
            photoUrl     = "",
            notes        = notes.trim(),
            driverName   = driverName.trim(),
            phoneNumber  = phoneNumber.trim(),
            createdAt    = System.currentTimeMillis()
        )

        carViewModel.saveCar(
            car       = car,
            imageUri  = imageUri,
            context   = context,          // ← pass context for Cloudinary
            onSuccess = {
                isSaving = false
                navController.popBackStack()
            },
            onError = { errorMsg ->
                isSaving  = false
                saveError = errorMsg
            }
        )
    }

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add a Car",
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
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Photo picker ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCard)
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(AccentPurple, AccentGreen)),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model              = imageUri,
                        contentDescription = "Car photo",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    Brush.verticalGradient(listOf(AccentPurple, AccentGreen)),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Upload photo",
                                tint     = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Upload car photo (optional)", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }

            // ── Fields ────────────────────────────────────────────────────────
            CarTextField(
                label         = "Make",
                value         = make,
                onValueChange = { make = it; makeError = false },
                placeholder   = "e.g. Toyota",
                isError       = makeError,
                errorMessage  = "Make is required",
                icon          = Icons.Default.Star
            )

            CarTextField(
                label          = "Driver Name",
                value          = driverName,
                onValueChange  = { driverName = it; driverError = false },
                placeholder    = "John Doe",
                isError        = driverError,
                errorMessage   = "Driver name is required",
                icon           = Icons.Default.Person,
                capitalization = KeyboardCapitalization.Words
            )

            CarTextField(
                label          = "Phone Number",
                value          = phoneNumber,
                onValueChange  = { phoneNumber = it; phoneError = false },
                placeholder    = "07XXXXXXXX",
                isError        = phoneError,
                errorMessage   = "Phone number is required",
                icon           = Icons.Default.Call,
                keyboardType   = KeyboardType.Phone
            )

            CarTextField(
                label         = "Model",
                value         = model,
                onValueChange = { model = it; modelError = false },
                placeholder   = "e.g. Camry",
                isError       = modelError,
                errorMessage  = "Model is required",
                icon          = Icons.Default.Info
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CarTextField(
                    label         = "Year",
                    value         = year,
                    onValueChange = { year = it; yearError = false },
                    placeholder   = "2022",
                    isError       = yearError,
                    errorMessage  = "Valid year required",
                    icon          = Icons.Default.DateRange,
                    keyboardType  = KeyboardType.Number,
                    modifier      = Modifier.weight(1f)
                )

                Column(modifier = Modifier.weight(1f)) {
                    SectionLabel("Color")
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded         = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value         = selectedColor,
                            onValueChange = {},
                            readOnly      = true,
                            leadingIcon   = {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(colorMap[selectedColor] ?: Color.Gray, CircleShape)
                                        .border(1.dp, Color.White.copy(0.3f), CircleShape)
                                )
                            },
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            colors        = fieldColors(),
                            shape         = RoundedCornerShape(14.dp),
                            modifier      = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded         = expanded,
                            onDismissRequest = { expanded = false },
                            modifier         = Modifier.background(BgCard)
                        ) {
                            colorNames.forEach { c ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment     = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .background(colorMap[c] ?: Color.Gray, CircleShape)
                                                    .border(0.5.dp, Color.White.copy(0.3f), CircleShape)
                                            )
                                            Text(c, color = TextPrimary, fontSize = 14.sp)
                                        }
                                    },
                                    onClick = { selectedColor = c; expanded = false }
                                )
                            }
                        }
                    }
                }
            }

            CarTextField(
                label          = "License Plate",
                value          = licensePlate,
                onValueChange  = { licensePlate = it.uppercase(); plateError = false },
                placeholder    = "KAA 001X",
                isError        = plateError,
                errorMessage   = "License plate is required",
                icon           = Icons.Default.MailOutline,
                capitalization = KeyboardCapitalization.Characters
            )

            // ── Fuel type ─────────────────────────────────────────────────────
            Column {
                SectionLabel("Fuel Type")
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    fuelTypes.forEach { fuel ->
                        val selected = fuel == selectedFuel
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected)
                                        Brush.verticalGradient(listOf(AccentPurple, Color(0xFF5A3ED9)))
                                    else
                                        Brush.verticalGradient(listOf(BgCard, BgCard))
                                )
                                .border(
                                    width = if (selected) 0.dp else 0.5.dp,
                                    color = if (selected) Color.Transparent else Color.White.copy(0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedFuel = fuel },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                fuel,
                                fontSize   = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color      = if (selected) Color.White else TextMuted
                            )
                        }
                    }
                }
            }

            // ── Notes ─────────────────────────────────────────────────────────
            Column {
                SectionLabel("Notes (optional)")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = notes,
                    onValueChange = { notes = it },
                    placeholder   = {
                        Text("e.g. transmission type, mileage…", color = TextMuted, fontSize = 14.sp)
                    },
                    colors   = fieldColors(),
                    shape    = RoundedCornerShape(14.dp),
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Error ─────────────────────────────────────────────────────────
            saveError?.let {
                Text(it, color = ErrorRed, fontSize = 12.sp)
            }

            // ── Save button ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSaving)
                            Brush.horizontalGradient(listOf(Color.Gray, Color.Gray))
                        else
                            Brush.horizontalGradient(listOf(AccentPurple, AccentGreen))
                    )
                    .clickable(enabled = !isSaving) { saveCar() },
                contentAlignment = Alignment.Center
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Text("Add Car", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Text(
                "Your vehicle info is stored securely",
                color    = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color         = TextMuted,
        fontSize      = 11.sp,
        fontWeight    = FontWeight.Medium,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun CarTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String = "",
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Words,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Column(modifier = modifier) {
        SectionLabel(label)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value           = value,
            onValueChange   = onValueChange,
            placeholder     = { Text(placeholder, color = TextMuted, fontSize = 14.sp) },
            leadingIcon     = { Icon(icon, contentDescription = null, tint = AccentPurple) },
            isError         = isError,
            supportingText  = if (isError) {
                { Text(errorMessage, color = ErrorRed, fontSize = 11.sp) }
            } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType   = keyboardType,
                capitalization = capitalization
            ),
            colors   = fieldColors(),
            shape    = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = AccentPurple,
    unfocusedBorderColor    = Color.White.copy(alpha = 0.12f),
    focusedTextColor        = TextPrimary,
    unfocusedTextColor      = TextPrimary,
    cursorColor             = AccentPurple,
    focusedContainerColor   = BgField,
    unfocusedContainerColor = BgField,
    errorBorderColor        = ErrorRed,
    errorContainerColor     = BgField,
    errorTextColor          = TextPrimary
)
