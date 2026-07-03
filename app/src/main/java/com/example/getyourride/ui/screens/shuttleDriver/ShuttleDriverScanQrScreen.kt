@file:OptIn(androidx.camera.core.ExperimentalGetImage::class)
package com.example.getyourride.ui.screens.shuttleDriver

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.getyourride.ui.components.ShuttleDriverBottomBar
import com.example.getyourride.ui.components.ShuttleDriverBottomBarItem
import com.example.getyourride.ui.theme.GetYourRideTheme
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/*
 * ShuttleDriverScanQrScreen
 *
 * This screen scans a student's shuttle booking QR code.
 *
 * Expected QR code format for now:
 * booking_id=1;trip_id=1;first_name=Alex;last_name=Thompson;student_number=ST88291
 *
 * Later, the student app QR generator should create QR codes in this format.
 *
 * Database rule:
 * - booking_id maps to trip_booking.booking_id
 * - Mark as Boarded should later update boarding_log.boarded_at
 */

// Screen colours matching the existing GetYourRide design.
private val ScanBackground = Color(0xFFFBF8FD)
private val ScanPrimary = Color(0xFF011844)
private val ScanTopBar = Color(0xFF1A2E5A)
private val ScanAccent = Color(0xFFFC820C)
private val ScanCardBackground = Color(0xFFFFFFFF)
private val ScanFieldBackground = Color(0xFFF5F3F7)
private val ScanText = Color(0xFF1B1B1F)
private val ScanTextMuted = Color(0xFF44464F)
private val ScanPrimaryFixed = Color(0xFFDAE2FF)
private val ScanSuccessBackground = Color(0xFFE8F5E9)
private val ScanSuccessText = Color(0xFF2E7D32)
private val ScanWarningBackground = Color(0xFFFFF3CD)
private val ScanWarningText = Color(0xFF8A5A00)
private val ScanErrorBackground = Color(0xFFFFEBEE)
private val ScanErrorText = Color(0xFFC62828)

/*
 * Student details read from the QR code.
 *
 * These values should come from the QR code that the student shows
 * when entering the shuttle.
 */
@Immutable
data class ShuttleScannedQrStudent(
    val bookingId: Long,
    val tripId: Long,
    val firstName: String,
    val lastName: String,
    val studentNumber: String,
    val boardedAt: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShuttleDriverScanQrScreen(
    /*
     * Later this should call the backend:
     * update boarding_log.boarded_at where booking_id = bookingId.
     */
    onMarkAsBoardedClick: (Long) -> Unit = {},
    onScanQrCodeClick: () -> Unit = {},
    onBoardingClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current

    var hasCameraPermission by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isScanning by rememberSaveable {
        mutableStateOf(false)
    }

    var messageText by rememberSaveable {
        mutableStateOf("Press Scan QR Code and point the camera at the student's QR code.")
    }

    var isErrorMessage by rememberSaveable {
        mutableStateOf(false)
    }

    /*
     * This holds the student details after a QR code has been scanned.
     */
    var scannedStudent by remember {
        mutableStateOf<ShuttleScannedQrStudent?>(null)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted

        if (isGranted) {
            isScanning = true
            isErrorMessage = false
            messageText = "Scanner is active. Point the camera at the student's QR code."
        } else {
            isScanning = false
            isErrorMessage = true
            messageText = "Camera permission is required to scan QR codes."
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ScanTopBar
                )
            )
        },
        bottomBar = {
            ShuttleDriverBottomBar(
                selectedItem = ShuttleDriverBottomBarItem.ScanQrCode,
                onScanQrCodeClick = onScanQrCodeClick,
                onBoardingClick = onBoardingClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = ScanBackground
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ScanBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Scan QR Code",
                    color = ScanPrimary,
                    fontSize = 26.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Scan the student's shuttle booking QR code to quickly mark them as boarded.",
                    color = ScanTextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            if (isScanning && hasCameraPermission) {
                ActiveScannerCard(
                    onQrCodeScanned = { rawQrValue ->
                        /*
                         * QR code has been scanned.
                         * Stop the scanner immediately and try to read student details.
                         */
                        isScanning = false

                        val parsedStudent = parseScannedStudentQr(rawQrValue)

                        if (parsedStudent != null) {
                            scannedStudent = parsedStudent
                            isErrorMessage = false
                            messageText = "QR code scanned successfully. Confirm the student details below."
                        } else {
                            scannedStudent = null
                            isErrorMessage = true
                            messageText = "QR code scanned, but the details could not be read. Use Boarding to mark the student manually."
                        }
                    },
                    onStopScanningClick = {
                        isScanning = false
                        isErrorMessage = false
                        messageText = "Scanner stopped. Press Scan QR Code to scan again."
                    }
                )
            } else {
                ScannerStartCard(
                    onScanClick = {
                        scannedStudent = null

                        if (hasCameraPermission) {
                            isScanning = true
                            isErrorMessage = false
                            messageText = "Scanner is active. Point the camera at the student's QR code."
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )
            }

            ScanMessageCard(
                message = messageText,
                isError = isErrorMessage
            )

            scannedStudent?.let { student ->
                ScannedStudentCard(
                    student = student,
                    onMarkAsBoardedClick = {
                        /*
                         * This behaves like the Mark as Boarded button on the Boarding page.
                         *
                         * For now, it updates local UI state.
                         * Later, this bookingId should be sent to Spring Boot.
                         */
                        onMarkAsBoardedClick(student.bookingId)

                        scannedStudent = student.copy(
                            boardedAt = "Now"
                        )

                        isErrorMessage = false
                        messageText = "Student marked as boarded successfully."
                    }
                )
            }

            BoardingFallbackCard(
                onBoardingClick = onBoardingClick
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/*
 * Card shown before the camera scanner is active.
 */
@Composable
private fun ScannerStartCard(
    onScanClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ScanCardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ScanFieldBackground)
                    .border(
                        BorderStroke(
                            width = 2.dp,
                            color = ScanPrimary.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(ScanPrimaryFixed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.QrCodeScanner,
                            contentDescription = null,
                            tint = ScanPrimary,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Text(
                        text = "Ready to scan",
                        color = ScanPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "The camera will open when you press the button below.",
                        color = ScanTextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Button(
                onClick = onScanClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScanAccent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCodeScanner,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.size(10.dp))

                Text(
                    text = "Scan QR Code",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/*
 * Card shown while the camera scanner is active.
 */
@Composable
private fun ActiveScannerCard(
    onQrCodeScanned: (String) -> Unit,
    onStopScanningClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ScanCardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .border(
                        BorderStroke(
                            width = 2.dp,
                            color = ScanAccent
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                QrCameraScanner(
                    onQrCodeScanned = onQrCodeScanned
                )

                ScannerOverlay()
            }

            OutlinedButton(
                onClick = onStopScanningClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(
                    text = "Stop Scanning",
                    color = ScanPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/*
 * Shows details from the scanned QR code.
 */
@Composable
private fun ScannedStudentCard(
    student: ShuttleScannedQrStudent,
    onMarkAsBoardedClick: () -> Unit
) {
    val isBoarded = student.boardedAt != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ScanCardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Scanned Student Details",
                color = ScanPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ScanPrimaryFixed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = ScanPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "${student.firstName} ${student.lastName}",
                        color = ScanText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Student No: ${student.studentNumber}",
                        color = ScanTextMuted,
                        fontSize = 12.sp
                    )

                    Text(
                        text = "Booking ID: ${student.bookingId}",
                        color = ScanTextMuted,
                        fontSize = 12.sp
                    )

                    Text(
                        text = "Trip ID: ${student.tripId}",
                        color = ScanTextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            if (isBoarded) {
                BoardedSuccessBox()
            } else {
                Button(
                    onClick = onMarkAsBoardedClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScanAccent,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = "Mark as Boarded",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/*
 * Success box shown after Mark as Boarded is pressed.
 */
@Composable
private fun BoardedSuccessBox() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ScanSuccessBackground,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = ScanSuccessText,
                modifier = Modifier.size(22.dp)
            )

            Text(
                text = "Student has been marked as boarded.",
                color = ScanSuccessText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/*
 * CameraX + ML Kit QR scanner.
 */

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun QrCameraScanner(
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnQrCodeScanned by rememberUpdatedState(onQrCodeScanned)

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        BarcodeScanning.getClient(options)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { androidViewContext ->
            val previewView = PreviewView(androidViewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(androidViewContext)

            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = CameraPreview.Builder()
                        .build()
                        .also { cameraPreview ->
                            cameraPreview.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    var isProcessingFrame = false
                    var hasScannedCode = false

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (isProcessingFrame || hasScannedCode) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val mediaImage = imageProxy.image

                        if (mediaImage == null) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        isProcessingFrame = true

                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        processQrFrame(
                            scanner = barcodeScanner,
                            inputImage = inputImage,
                            onQrCodeFound = { qrValue ->
                                if (!hasScannedCode) {
                                    hasScannedCode = true

                                    Handler(Looper.getMainLooper()).post {
                                        latestOnQrCodeScanned(qrValue)
                                    }
                                }
                            },
                            onComplete = {
                                isProcessingFrame = false
                                imageProxy.close()
                            }
                        )
                    }

                    try {
                        cameraProvider.unbindAll()

                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (_: Exception) {
                        // Camera binding failed. We keep the UI open.
                    }
                },
                ContextCompat.getMainExecutor(androidViewContext)
            )

            previewView
        }
    )
}

/*
 * Processes one camera frame using ML Kit.
 */
private fun processQrFrame(
    scanner: BarcodeScanner,
    inputImage: InputImage,
    onQrCodeFound: (String) -> Unit,
    onComplete: () -> Unit
) {
    scanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            val qrValue = barcodes
                .firstOrNull { barcode ->
                    !barcode.rawValue.isNullOrBlank()
                }
                ?.rawValue

            if (!qrValue.isNullOrBlank()) {
                onQrCodeFound(qrValue)
            }
        }
        .addOnCompleteListener {
            onComplete()
        }
}

/*
 * Visual guide over the camera preview.
 */
@Composable
private fun ScannerOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(210.dp)
                .border(
                    BorderStroke(
                        width = 3.dp,
                        color = ScanAccent
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        )
    }
}

/*
 * Message card for scanner feedback.
 */
@Composable
private fun ScanMessageCard(
    message: String,
    isError: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isError) ScanErrorBackground else ScanWarningBackground,
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = message,
            color = if (isError) ScanErrorText else ScanWarningText,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(14.dp)
        )
    }
}

/*
 * If scanning fails, the driver can use the Boarding page to mark students manually.
 */
@Composable
private fun BoardingFallbackCard(
    onBoardingClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ScanSuccessBackground,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "QR not working?",
                color = ScanSuccessText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Use the Boarding page to manually mark students one by one.",
                color = ScanSuccessText,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            OutlinedButton(
                onClick = onBoardingClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text(
                    text = "Go to Boarding",
                    color = ScanPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/*
 * Reads student details from the QR code text.
 *
 * Supported QR format:
 * booking_id=1;trip_id=1;first_name=Alex;last_name=Thompson;student_number=ST88291
 *
 * Also supports:
 * bookingId=1;tripId=1;firstName=Alex;lastName=Thompson;studentNumber=ST88291
 */
private fun parseScannedStudentQr(rawQrValue: String): ShuttleScannedQrStudent? {
    val values = rawQrValue
        .split(";", "\n", ",")
        .mapNotNull { part ->
            val pieces = part.split("=", limit = 2)

            if (pieces.size == 2) {
                pieces[0].trim().lowercase() to pieces[1].trim()
            } else {
                null
            }
        }
        .toMap()

    val bookingId = values["booking_id"]?.toLongOrNull()
        ?: values["bookingid"]?.toLongOrNull()
        ?: return null

    val tripId = values["trip_id"]?.toLongOrNull()
        ?: values["tripid"]?.toLongOrNull()
        ?: return null

    val firstName = values["first_name"]
        ?: values["firstname"]
        ?: return null

    val lastName = values["last_name"]
        ?: values["lastname"]
        ?: return null

    val studentNumber = values["student_number"]
        ?: values["studentnumber"]
        ?: return null

    return ShuttleScannedQrStudent(
        bookingId = bookingId,
        tripId = tripId,
        firstName = firstName,
        lastName = lastName,
        studentNumber = studentNumber,
        boardedAt = null
    )
}

/*
 * Android Studio preview.
 *
 * The actual camera preview only works when running the app.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ShuttleDriverScanQrScreenPreview() {
    GetYourRideTheme(dynamicColor = false) {
        ShuttleDriverScanQrScreen()
    }
}