package com.example.getyourride.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.viewmodel.EditDriverProfileViewModel
import com.example.getyourride.viewmodel.EditProfileLoadState
import com.example.getyourride.viewmodel.EditProfileSaveState

// ── Palette (matches DriverProfileSettingsScreen) ────────────────────────────
private val EditBackground = Color(0xFFF4F6FB)
private val EditPrimary = Color(0xFF1A2E5A)
private val EditTopBar = Color(0xFF1A2E5A)
private val EditAccent = Color(0xFFFC820C)
private val EditCardBackground = Color(0xFFFFFFFF)
private val EditText = Color(0xFF1B1B1F)
private val EditTextMuted = Color(0xFF5E6278)
private val EditError = Color(0xFFDC2626)
private val EditSuccess = Color(0xFF16A34A)
private val EditInfoBg = Color(0xFFFEF3C7)
private val EditInfoText = Color(0xFF92400E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDriverProfileScreen(
    viewModel: EditDriverProfileViewModel,
    onBackClick: () -> Unit = {},
    onPickRegistrationDocument: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(
                        text = "Edit Profile",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EditTopBar)
            )
        },
        containerColor = EditBackground
    ) { innerPadding ->
        when (val load = viewModel.loadState) {
            is EditProfileLoadState.Loading -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EditAccent)
                }
            }
            is EditProfileLoadState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Could not load your profile", color = EditText, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(load.message, color = EditTextMuted, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadProfile() },
                        colors = ButtonDefaults.buttonColors(containerColor = EditAccent)
                    ) { Text("Retry") }
                }
            }
            is EditProfileLoadState.Loaded -> {
                EditForm(
                    viewModel = viewModel,
                    innerPadding = innerPadding,
                    onPickRegistrationDocument = onPickRegistrationDocument,
                    onSave = onSave
                )
            }
        }
    }
}

@Composable
private fun EditForm(
    viewModel: EditDriverProfileViewModel,
    innerPadding: PaddingValues,
    onPickRegistrationDocument: () -> Unit,
    onSave: () -> Unit
) {
    val saveState = viewModel.saveState
    val isSaving = saveState is EditProfileSaveState.Saving

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Re-review info banner ────────────────────────────────────────────
        Surface(color = EditInfoBg, shape = RoundedCornerShape(12.dp)) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = EditInfoText,
                    modifier = Modifier.size(20.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Your changes need re-approval",
                        color = EditInfoText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Saving these changes sends your profile back to the admin for review. " +
                                "Your verified status will be removed and set to \"Pending Review\" " +
                                "until an admin approves your updated details. You won't be able to " +
                                "offer rides as a verified driver until then.",
                        color = EditInfoText,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // ── Read-only identity ───────────────────────────────────────────────
        SectionCard(title = "Personal Details") {
            ReadOnlyRow(label = "First Name", value = viewModel.firstName)
            ReadOnlyRow(label = "Surname", value = viewModel.surname)
            ReadOnlyRow(label = "University Email", value = viewModel.email)
            EditField(
                label = "Contact Number",
                value = viewModel.contactNumber,
                onValueChange = { viewModel.contactNumber = it },
                keyboardType = KeyboardType.Phone,
                enabled = !isSaving
            )
        }

        // ── Vehicle details (all editable) ───────────────────────────────────
        SectionCard(title = "Vehicle Details") {
            EditField(
                label = "Make & Model",
                value = viewModel.vehicleMakeModel,
                onValueChange = { viewModel.vehicleMakeModel = it },
                enabled = !isSaving
            )
            EditField(
                label = "Registration Number",
                value = viewModel.registrationNumber,
                onValueChange = { viewModel.registrationNumber = it },
                enabled = !isSaving
            )
            EditField(
                label = "Colour",
                value = viewModel.vehicleColour,
                onValueChange = { viewModel.vehicleColour = it },
                enabled = !isSaving
            )
            EditField(
                label = "Seating Capacity",
                value = viewModel.seatingCapacity,
                onValueChange = { viewModel.seatingCapacity = it },
                keyboardType = KeyboardType.Number,
                enabled = !isSaving
            )
            EditField(
                label = "Year (optional)",
                value = viewModel.vehicleYear,
                onValueChange = { viewModel.vehicleYear = it },
                keyboardType = KeyboardType.Number,
                enabled = !isSaving
            )
        }

        // ── Vehicle registration document (optional re-upload) ───────────────
        SectionCard(title = "Vehicle Registration Document") {
            Text(
                text = "Optional. Upload a new vehicle registration document only if it has changed. " +
                        "Leave this if your current document is still valid.",
                color = EditTextMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Spacer(Modifier.height(4.dp))
            val picked = viewModel.newRegistrationDocUri != null
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onPickRegistrationDocument,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EditAccent)
                ) {
                    Icon(Icons.Outlined.UploadFile, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(if (picked) "Change file" else "Upload new document")
                }
                if (picked) {
                    Spacer(Modifier.size(10.dp))
                    Icon(
                        Icons.Outlined.CheckCircle,
                        null,
                        tint = EditSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        viewModel.newRegistrationDocFileName ?: "New file selected",
                        color = EditSuccess,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ── Error message ────────────────────────────────────────────────────
        if (saveState is EditProfileSaveState.Error) {
            Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(12.dp)) {
                Text(
                    text = saveState.message,
                    color = EditError,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        // ── Save / Cancel ────────────────────────────────────────────────────
        Button(
            onClick = onSave,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EditAccent)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text("Saving…", color = Color.White, fontWeight = FontWeight.Bold)
            } else {
                Text("Save & Submit for Review", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ── Reusable bits ─────────────────────────────────────────────────────────────
@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = EditCardBackground,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, color = EditPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun ReadOnlyRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label.uppercase(), color = EditTextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Text(value.ifBlank { "—" }, color = EditText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}
