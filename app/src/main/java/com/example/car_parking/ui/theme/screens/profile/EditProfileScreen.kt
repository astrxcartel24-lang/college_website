package com.example.car_parking.ui.theme.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.car_parking.data.UserViewModel

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val BgDeep       = Color(0xFF16161F)
private val BgCard       = Color(0xFF24243E)
private val BgInput      = Color(0xFF1E1E30)
private val AccentPurple = Color(0xFF7F5AF0)
private val AccentGreen  = Color(0xFF2CB67D)
private val AccentRed    = Color(0xFFE53935)
private val TextPrimary  = Color.White
private val TextMuted    = Color(0xFFAAAAAA)
private val DividerColor = Color.White.copy(alpha = 0.07f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    userViewModel: UserViewModel = viewModel()
) {
    // ── State ──────────────────────────────────────────────────────────────────
    val user by userViewModel.getCurrentUser().collectAsState(initial = null)

    var fullName        by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var phone           by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrentPw  by remember { mutableStateOf(false) }
    var showNewPw      by remember { mutableStateOf(false) }
    var showConfirmPw  by remember { mutableStateOf(false) }

    var avatarUri         by remember { mutableStateOf<Uri?>(null) }
    var existingAvatarUrl by remember { mutableStateOf<String?>(null) }

    var isSaving      by remember { mutableStateOf(false) }
    var showSuccess   by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }

    // ── Validation ─────────────────────────────────────────────────────────────
    val fullNameError      = fullName.isBlank()
    val emailError         = email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val phoneError         = phone.isNotBlank() && phone.length < 9
    val newPwMismatch      = newPassword.isNotBlank() && newPassword != confirmPassword
    val newPwTooShort      = newPassword.isNotBlank() && newPassword.length < 6
    val changePassword     = newPassword.isNotBlank()
    val currentPwRequired  = changePassword && currentPassword.isBlank()
    val isFormValid        = !fullNameError && !emailError && !phoneError &&
            !newPwMismatch && !newPwTooShort && !currentPwRequired

    // ── Populate from loaded user ──────────────────────────────────────────────
    LaunchedEffect(user) {
        user?.let {
            fullName         = it.fullName
            email            = it.email
            phone            = it.phone.orEmpty()
            existingAvatarUrl = it.avatarUrl
        }
    }

    // ── Photo picker ───────────────────────────────────────────────────────────
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> avatarUri = uri }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            snackbarHostState.showSnackbar("Profile updated successfully")
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
                        "Edit Profile",
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

            // ── Avatar ─────────────────────────────────────────────────────────
            var avatarVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { avatarVisible = true }

            AnimatedVisibility(
                visible = avatarVisible,
                enter   = fadeIn(tween(400)) + slideInVertically(tween(400)) { -30 }
            ) {
                AvatarSection(
                    avatarUri         = avatarUri,
                    existingAvatarUrl = existingAvatarUrl,
                    displayName       = fullName.ifBlank { "User" },
                    onPickPhoto       = { photoPicker.launch("image/*") },
                    onRemovePhoto     = { avatarUri = null; existingAvatarUrl = null }
                )
            }

            // ── Personal info card ─────────────────────────────────────────────
            ProfileSectionCard(title = "Personal Info", icon = Icons.Default.Person) {
                ProfileField(
                    label         = "Full Name",
                    value         = fullName,
                    onValueChange = { fullName = it },
                    placeholder   = "Your full name",
                    icon          = Icons.Default.Badge,
                    isError       = attemptedSave && fullNameError,
                    errorMsg      = "Name is required"
                )
                ProfileField(
                    label         = "Email Address",
                    value         = email,
                    onValueChange = { email = it },
                    placeholder   = "you@example.com",
                    icon          = Icons.Default.Email,
                    isError       = attemptedSave && emailError,
                    errorMsg      = "Enter a valid email"
                )
                ProfileField(
                    label         = "Phone Number",
                    value         = phone,
                    onValueChange = { phone = it },
                    placeholder   = "e.g. 0712 345 678",
                    icon          = Icons.Default.Phone,
                    isError       = attemptedSave && phoneError,
                    errorMsg      = "Enter a valid phone number"
                )
            }

            // ── Change password card ───────────────────────────────────────────
            ProfileSectionCard(title = "Change Password", icon = Icons.Default.Lock) {
                Text(
                    "Leave blank to keep your current password.",
                    color    = TextMuted,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(2.dp))

                PasswordField(
                    label         = "Current Password",
                    value         = currentPassword,
                    onValueChange = { currentPassword = it },
                    visible       = showCurrentPw,
                    onToggle      = { showCurrentPw = !showCurrentPw },
                    isError       = attemptedSave && currentPwRequired,
                    errorMsg      = "Required when changing password"
                )
                PasswordField(
                    label         = "New Password",
                    value         = newPassword,
                    onValueChange = { newPassword = it },
                    visible       = showNewPw,
                    onToggle      = { showNewPw = !showNewPw },
                    isError       = attemptedSave && newPwTooShort,
                    errorMsg      = "Minimum 6 characters"
                )
                PasswordField(
                    label         = "Confirm New Password",
                    value         = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    visible       = showConfirmPw,
                    onToggle      = { showConfirmPw = !showConfirmPw },
                    isError       = attemptedSave && newPwMismatch,
                    errorMsg      = "Passwords do not match"
                )

                // Password strength indicator
                AnimatedVisibility(visible = newPassword.isNotBlank()) {
                    PasswordStrengthBar(password = newPassword)
                }
            }

            // ── Save button ────────────────────────────────────────────────────
            Button(
                onClick = {
                    attemptedSave = true
                    if (isFormValid) {
                        isSaving = true
                        userViewModel.updateProfile(
                            fullName    = fullName.trim(),
                            email       = email.trim(),
                            phone       = phone.trim(),
                            avatarUri   = avatarUri,
                            newPassword = if (changePassword) newPassword else null,
                            onSuccess   = { showSuccess = true },
                            onError     = { isSaving = false }
                        )
                    }
                },
                enabled  = !isSaving,
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = AccentPurple,
                    disabledContainerColor = AccentPurple.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color       = Color.White,
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save Changes", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Global validation hint
            AnimatedVisibility(visible = attemptedSave && !isFormValid) {
                Text(
                    "Please fix the errors above before saving.",
                    color    = AccentRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Avatar section ────────────────────────────────────────────────────────────
@Composable
private fun AvatarSection(
    avatarUri: Uri?,
    existingAvatarUrl: String?,
    displayName: String,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    val hasPhoto = avatarUri != null || !existingAvatarUrl.isNullOrBlank()
    val initials = displayName.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .then(
                        if (hasPhoto) Modifier.background(BgCard)
                        else Modifier.background(Brush.linearGradient(listOf(AccentPurple, AccentGreen)))
                    )
                    .border(2.dp, AccentPurple.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (hasPhoto) {
                    AsyncImage(
                        model              = avatarUri ?: existingAvatarUrl,
                        contentDescription = "Avatar",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        initials,
                        color      = Color.White,
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Camera badge
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(AccentPurple)
                    .border(2.dp, BgDeep, CircleShape)
                    .clickable(onClick = onPickPhoto),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Change photo",
                    tint     = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Remove photo option
        if (hasPhoto) {
            TextButton(onClick = onRemovePhoto) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint     = AccentRed.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Remove photo", color = AccentRed.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}

// ── Password strength bar ─────────────────────────────────────────────────────
@Composable
private fun PasswordStrengthBar(password: String) {
    val strength = when {
        password.length >= 12 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() } -> 4
        password.length >= 10 && password.any { it.isDigit() }  -> 3
        password.length >= 6                                      -> 2
        else                                                      -> 1
    }
    val (label, color) = when (strength) {
        4    -> "Strong"   to AccentGreen
        3    -> "Good"     to Color(0xFF80C080)
        2    -> "Fair"     to Color(0xFFFFA726)
        else -> "Weak"     to AccentRed
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i < strength) color else Color.White.copy(0.1f))
                )
            }
        }
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        HorizontalDivider(color = DividerColor)
        content()
    }
}

// ── Text field ────────────────────────────────────────────────────────────────
@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isError: Boolean = false,
    errorMsg: String = ""
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
            placeholder   = { Text(placeholder, color = TextMuted.copy(0.5f), fontSize = 14.sp) },
            leadingIcon   = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint     = if (isError) AccentRed else AccentPurple,
                    modifier = Modifier.size(18.dp)
                )
            },
            isError    = isError,
            shape      = RoundedCornerShape(12.dp),
            colors     = profileFieldColors(),
            singleLine = true,
            modifier   = Modifier.fillMaxWidth()
        )
        if (isError && errorMsg.isNotBlank()) {
            Text(errorMsg, color = AccentRed, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

// ── Password field ────────────────────────────────────────────────────────────
@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggle: () -> Unit,
    isError: Boolean = false,
    errorMsg: String = ""
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            color      = if (isError) AccentRed else TextMuted,
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value                  = value,
            onValueChange          = onValueChange,
            placeholder            = { Text("••••••••", color = TextMuted.copy(0.5f)) },
            leadingIcon            = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint     = if (isError) AccentRed else AccentPurple,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = onToggle) {
                    Icon(
                        if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (visible) "Hide" else "Show",
                        tint = TextMuted
                    )
                }
            },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            isError    = isError,
            shape      = RoundedCornerShape(12.dp),
            colors     = profileFieldColors(),
            singleLine = true,
            modifier   = Modifier.fillMaxWidth()
        )
        if (isError && errorMsg.isNotBlank()) {
            Text(errorMsg, color = AccentRed, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
private fun profileFieldColors() = OutlinedTextFieldDefaults.colors(
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
