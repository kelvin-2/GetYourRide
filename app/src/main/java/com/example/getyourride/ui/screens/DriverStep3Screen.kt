package com.example.getyourride.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.GetYourRideTheme

// ─── Color Palette ───────────────────────────────────────────────────────────
private val StepBackground = Color(0xFFF4F6FB)
private val StepPrimary = Color(0xFF1A2E5A)
private val StepPrimaryLight = Color(0xFF2E4A82)
private val StepTopBar = Color(0xFF1A2E5A)
private val StepAccent = Color(0xFFFC820C)
private val StepCardBackground = Color(0xFFFFFFFF)
private val StepText = Color(0xFF1B1B1F)
private val StepTextMuted = Color(0xFF5E6278)
private val StepBorder = Color(0xFFE5E7EB)
private val StepInactiveProgress = Color(0xFFE5E7EB)
private val StepError = Color(0xFFDC2626)
private val StepSuccess = Color(0xFF16A34A)
private val StepSectionBlue = Color(0xFF2563EB)
private val StepPendingBg = Color(0xFFFEF3C7)
private val StepPendingText = Color(0xFF92400E)
private val StepUploadedBg = Color(0xFFDCFCE7)
private val StepUploadedText = Color(0xFF16A34A)

/** Maximum allowed file size: 5 MB */
private const val MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L
private const val MAX_FILE_SIZE_LABEL = "5 MB"

data class DriverStep3Data(
    val driversLicenceFileName: String,
    val driversLicenceUri: String,
    val vehicleRegistrationFileName: String,
    val vehicleRegistrationUri: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverStep3Screen(
    onBackClick: () -> Unit = {},
    onSubmitClick: (DriverStep3Data) -> Unit = {},
    errorMessage: String? = null,
    statusMessage: String? = null,
    isLoading: Boolean = false
) {
    val context = LocalContext.current

    var driversLicenceFileName by rememberSaveable { mutableStateOf("") }
    var driversLicenceUri by rememberSaveable { mutableStateOf("") }
    var vehicleRegistrationFileName by rememberSaveable { mutableStateOf("") }
    var vehicleRegistrationUri by rememberSaveable { mutableStateOf("") }
    var fileValidationError by rememberSaveable { mutableStateOf<String?>(null) }

    val driversLicencePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val permissionGranted = persistReadPermission(context, uri)
            if (!permissionGranted) {
                fileValidationError = "Could not get access to the file. Please try again."
                return@rememberLauncherForActivityResult
            }
            val fileSize = getFileSizeFromUri(context, uri)
            if (fileSize > MAX_FILE_SIZE_BYTES) {
                fileValidationError = "Driver's licence image is too large. Maximum size is $MAX_FILE_SIZE_LABEL."
                return@rememberLauncherForActivityResult
            }
            fileValidationError = null
            driversLicenceUri = uri.toString()
            driversLicenceFileName = getFileNameFromUri(context, uri)
        }
    }

    val vehicleRegistrationPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val permissionGranted = persistReadPermission(context, uri)
            if (!permissionGranted) {
                fileValidationError = "Could not get access to the file. Please try again."
                return@rememberLauncherForActivityResult
            }
            val fileSize = getFileSizeFromUri(context, uri)
            if (fileSize > MAX_FILE_SIZE_BYTES) {
                fileValidationError = "Vehicle registration image is too large. Maximum size is $MAX_FILE_SIZE_LABEL."
                return@rememberLauncherForActivityResult
            }
            fileValidationError = null
            vehicleRegistrationUri = uri.toString()
            vehicleRegistrationFileName = getFileNameFromUri(context, uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsCar,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = "GetYourRide",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StepTopBar)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, StepBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = StepPrimary
                        ),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = "Back",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            onSubmitClick(
                                DriverStep3Data(
                                    driversLicenceFileName = driversLicenceFileName,
                                    driversLicenceUri = driversLicenceUri,
                                    vehicleRegistrationFileName = vehicleRegistrationFileName,
                                    vehicleRegistrationUri = vehicleRegistrationUri
                                )
                            )
                        },
                        modifier = Modifier
                            .weight(2f)
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StepAccent),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Submitting...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Submit Profile",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        containerColor = StepBackground
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ─── Gradient Hero Header ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(StepTopBar, StepPrimaryLight),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, 400f)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Step icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.UploadFile,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Upload Documents",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Upload now or skip and do it later.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        // Step badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = StepAccent
                        ) {
                            Text(
                                text = "Step 3 of 3",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Progress Indicator ──────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (index < 3) StepAccent else StepInactiveProgress
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Documents Card ──────────────────────────────────────────────
            Step3SectionCard(
                title = "Required Documents",
                icon = Icons.Outlined.Description,
                iconTint = StepSectionBlue
            ) {
                // File size info
                Text(
                    text = "Accepted: images only (JPG, PNG). Max size: $MAX_FILE_SIZE_LABEL per file.",
                    color = StepTextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Step3DocumentUploadRow(
                    title = "Driver's Licence",
                    subtitle = "Upload a clear image of your licence",
                    fileName = driversLicenceFileName,
                    icon = Icons.Outlined.Badge,
                    onChooseFileClick = {
                        driversLicencePicker.launch(arrayOf("image/*"))
                    },
                    enabled = !isLoading
                )

                // Thin divider between documents
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(StepBorder)
                )

                Step3DocumentUploadRow(
                    title = "Vehicle Registration",
                    subtitle = "Upload a clear image of your registration",
                    fileName = vehicleRegistrationFileName,
                    icon = Icons.Outlined.Description,
                    onChooseFileClick = {
                        vehicleRegistrationPicker.launch(arrayOf("image/*"))
                    },
                    enabled = !isLoading
                )
            }

            // ─── File Validation Error ───────────────────────────────────────
            AnimatedVisibility(
                visible = !fileValidationError.isNullOrBlank(),
                enter = fadeIn() + slideInVertically()
            ) {
                if (!fileValidationError.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = StepError,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = fileValidationError!!,
                                color = StepError,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ─── Status Message ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = !statusMessage.isNullOrBlank(),
                enter = fadeIn() + slideInVertically()
            ) {
                if (!statusMessage.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = StepSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = statusMessage,
                                color = StepSuccess,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ─── Error Message ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = !errorMessage.isNullOrBlank(),
                enter = fadeIn() + slideInVertically()
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = StepError,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMessage,
                                color = StepError,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ─── Loading Overlay Info ────────────────────────────────────────
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn() + slideInVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = StepSectionBlue,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Submitting your application and uploading documents...",
                            color = StepSectionBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─── Section Card ────────────────────────────────────────────────────────────
@Composable
private fun Step3SectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = StepCardBackground,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section header with icon badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    color = StepPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Thin divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(StepBorder)
            )

            content()
        }
    }
}

// ─── Document Upload Row ─────────────────────────────────────────────────────
@Composable
private fun Step3DocumentUploadRow(
    title: String,
    subtitle: String,
    fileName: String,
    icon: ImageVector,
    onChooseFileClick: () -> Unit,
    enabled: Boolean = true
) {
    val isUploaded = fileName.isNotBlank()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF9FAFB),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isUploaded) StepUploadedBg else StepPendingBg
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isUploaded) StepUploadedText else StepPendingText,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = StepText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        color = StepTextMuted,
                        fontSize = 12.sp
                    )
                }

                // Status badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isUploaded) StepUploadedBg else StepPendingBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isUploaded) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = if (isUploaded) StepUploadedText else StepPendingText,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isUploaded) "Selected" else "Pending",
                            color = if (isUploaded) StepUploadedText else StepPendingText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // File name display
            if (isUploaded) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = StepUploadedBg.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            tint = StepUploadedText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = fileName,
                            color = StepUploadedText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Upload button
            Button(
                onClick = onChooseFileClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUploaded) StepPrimary.copy(alpha = 0.08f) else StepAccent.copy(alpha = 0.1f),
                    contentColor = if (isUploaded) StepPrimary else StepAccent
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Outlined.UploadFile,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isUploaded) "Change Image" else "Choose Image",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Attempts to persist read permission for the URI.
 * Returns true if permission was granted, false if it failed.
 */
private fun persistReadPermission(context: Context, uri: Uri): Boolean {
    return try {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        true
    } catch (e: SecurityException) {
        // Permission denied — the URI might still be readable for this session
        // but won't survive a process restart. We still allow it for immediate use.
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Gets the file size in bytes from a content URI.
 * Returns 0 if size cannot be determined.
 */
private fun getFileSizeFromUri(context: Context, uri: Uri): Long {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex >= 0 && cursor.moveToFirst()) {
                cursor.getLong(sizeIndex)
            } else {
                0L
            }
        } ?: 0L
    } catch (e: Exception) {
        0L
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var fileName: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }
    }
    return fileName ?: uri.lastPathSegment ?: "selected_image"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DriverStep3ScreenPreview() {
    GetYourRideTheme(dynamicColor = false) {
        DriverStep3Screen()
    }
}
