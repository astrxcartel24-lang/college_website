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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.car_parking.data.CarViewModel
import com.example.car_parking.models.CarModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

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

    // Entry Date & Time states
    var entryDate by remember { mutableStateOf(Calendar.getInstance()) }
    var entryTime by remember { mutableStateOf(Calendar.getInstance()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var entryDateError by remember { mutableStateOf(false) }
    var entryTimeError by remember { mutableStateOf(false) }

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

    // Date and Time formatters
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

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
        entryDateError = false
        entryTimeError = false
        return !makeError && !modelError && !yearError && !plateError && !driverError && !phoneError
    }

    fun saveCar() {
        if (!validate() || isSaving) return
        isSaving  = true
        saveError = null

        // Combine date and time into a single timestamp
        val combinedDateTime = Calendar.getInstance().apply {
            set(entryDate.get(Calendar.YEAR), entryDate.get(Calendar.MONTH), entryDate.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, entryTime.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, entryTime.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
        }

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
              entryDateTime = combinedDateTime.timeInMillis, // Add this to CarModel
            createdAt    = System.currentTimeMillis()
        )

        carViewModel.saveCar(
            car       = car,
            imageUri  = imageUri,
            context   = context,
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

            // ── Entry Date & Time Row ─────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Entry Date Field
                DateTimeTextField(
                    label = "Entry Date",
                    value = dateFormat.format(entryDate.time),
                    placeholder = "DD/MM/YYYY",
                    isError = entryDateError,
                    errorMessage = "Please select a valid date",
                    icon = Icons.Default.DateRange,
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                )

                // Entry Time Field
                DateTimeTextField(
                    label = "Entry Time",
                    value = timeFormat.format(entryTime.time),
                    placeholder = "HH:MM AM/PM",
                    isError = entryTimeError,
                    errorMessage = "Please select a valid time",
                    icon = Icons.Default.AccessTime,
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                )
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

    // ── Date Picker Dialog ────────────────────────────────────────────────────
    if (showDatePicker) {
        DatePickerDialog(
            currentDate = entryDate,
            onDateSelected = { date ->
                entryDate = date
                entryDateError = false
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // ── Time Picker Dialog ────────────────────────────────────────────────────
    if (showTimePicker) {
        CustomTimePickerDialog(
            currentTime = entryTime,
            onTimeSelected = { time ->
                entryTime = time
                entryTimeError = false
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

// ── DateTime TextField (clickable with trailing icon) ────────────────────────
@Composable
private fun DateTimeTextField(
    label: String,
    value: String,
    placeholder: String,
    isError: Boolean,
    errorMessage: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionLabel(label)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {},
            placeholder = { Text(placeholder, color = TextMuted, fontSize = 14.sp) },
            readOnly = true,
            isError = isError,
            trailingIcon = {
                IconButton(onClick = onClick) {
                    Icon(icon, contentDescription = null, tint = AccentPurple)
                }
            },
            supportingText = {
                if (isError) {
                    Text(errorMessage, color = ErrorRed, fontSize = 11.sp)
                }
            },
            colors = fieldColors(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Date Picker Dialog ───────────────────────────────────────────────────────
@Composable
private fun DatePickerDialog(
    currentDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit
) {
    var year by remember { mutableStateOf(currentDate.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(currentDate.get(Calendar.MONTH)) }
    var day by remember { mutableStateOf(currentDate.get(Calendar.DAY_OF_MONTH)) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BgCard)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Entry Date",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Year Picker
                Text("Year", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                CustomNumberPicker(
                    value = year,
                    onValueChange = { year = it },
                    range = 2000..2030,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Month Picker
                Text("Month", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                CustomNumberPicker(
                    value = month + 1,
                    onValueChange = { month = it - 1 },
                    range = 1..12,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Day Picker
                Text("Day", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                val maxDays = getDaysInMonth(month, year)
                CustomNumberPicker(
                    value = day.coerceIn(1, maxDays),
                    onValueChange = { day = it },
                    range = 1..maxDays,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance().apply {
                                set(year, month, day)
                            }
                            onDateSelected(calendar)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

// ── Custom Time Picker Dialog ────────────────────────────────────────────────
@Composable
private fun CustomTimePickerDialog(
    currentTime: Calendar,
    onTimeSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit
) {
    var currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
    val currentMinute = currentTime.get(Calendar.MINUTE)

    var hour12 = if (currentHour == 0) 12 else if (currentHour > 12) currentHour - 12 else currentHour
    var isPM = currentHour >= 12

    var minute by remember { mutableStateOf(currentMinute - (currentMinute % 5)) }
    var selectedHour12 by remember { mutableStateOf(hour12) }
    var selectedIsPM by remember { mutableStateOf(isPM) }

    val minuteOptions = (0..55 step 5).toList()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BgCard)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Entry Time",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomNumberPicker(
                        value = selectedHour12,
                        onValueChange = { selectedHour12 = it },
                        range = 1..12,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    CustomNumberPicker(
                        value = minute,
                        onValueChange = { minute = it },
                        range = minuteOptions,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = !selectedIsPM,
                        onClick = { selectedIsPM = false },
                        label = { Text("AM") },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentPurple
                        )
                    )
                    FilterChip(
                        selected = selectedIsPM,
                        onClick = { selectedIsPM = true },
                        label = { Text("PM") },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentPurple
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            var hour24 = selectedHour12
                            when {
                                selectedIsPM && selectedHour12 != 12 -> hour24 = selectedHour12 + 12
                                !selectedIsPM && selectedHour12 == 12 -> hour24 = 0
                                else -> hour24 = selectedHour12
                            }
                            calendar.set(Calendar.HOUR_OF_DAY, hour24)
                            calendar.set(Calendar.MINUTE, minute)
                            calendar.set(Calendar.SECOND, 0)
                            onTimeSelected(calendar)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

// ── Custom Number Picker (Up/Down buttons) ───────────────────────────────────
@Composable
private fun CustomNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier
) {
    CustomNumberPicker(
        value = value,
        onValueChange = onValueChange,
        range = range.toList(),
        modifier = modifier
    )
}

@Composable
private fun CustomNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: List<Int>,
    modifier: Modifier = Modifier
) {
    val items = range
    val initialIndex = items.indexOf(value).coerceAtLeast(0)
    var selectedIndex by remember { mutableStateOf(initialIndex) }

    LaunchedEffect(selectedIndex) {
        onValueChange(items[selectedIndex])
    }

    Column(
        modifier = modifier
            .height(140.dp)
            .width(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = { if (selectedIndex > 0) selectedIndex-- },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = AccentPurple)
        }

        Card(
            modifier = Modifier
                .size(70.dp, 50.dp),
            colors = CardDefaults.cardColors(containerColor = BgDeep),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%02d", items[selectedIndex]),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
            }
        }

        IconButton(
            onClick = { if (selectedIndex < items.size - 1) selectedIndex++ },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = AccentPurple)
        }
    }
}

// ── Helper Functions ─────────────────────────────────────────────────────────
private fun getDaysInMonth(month: Int, year: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

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