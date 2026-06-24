package com.example.getyourride.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.components.GyrTextField
import com.example.getyourride.ui.theme.*

data class StudentDetailsState(
    val fullName      : String = "",
    val studentNumber : String = "",
    val contactNumber : String = "",
    val email         : String = "",
)

@Composable
fun DriverStep1Verification(
    state               : StudentDetailsState,
    onFullNameChange    : (String) -> Unit,
    onStudentNumChange  : (String) -> Unit,
    onContactChange     : (String) -> Unit,
    onEmailChange       : (String) -> Unit,
) {
    Column(
        modifier            = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        DriverStepHeading(
            title    = "Become a Driver",
            subtitle = "Enter your student details to begin your application journey.",
        )

        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(12.dp),
            colors    = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(2.dp),
        ) {
            Column(
                modifier            = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                GyrTextField(
                    label         = "Full Name",
                    value         = state.fullName,
                    onValueChange = onFullNameChange,
                    placeholder   = "e.g. John Doe",
                    leadingIcon   = Icons.Outlined.Person,
                )
                GyrTextField(
                    label         = "Student Number",
                    value         = state.studentNumber,
                    onValueChange = onStudentNumChange,
                    placeholder   = "8-digit student ID",
                    leadingIcon   = Icons.Outlined.Badge,
                    keyboardType  = KeyboardType.Number,
                )
                GyrTextField(
                    label         = "Contact Number",
                    value         = state.contactNumber,
                    onValueChange = onContactChange,
                    placeholder   = "e.g. +27 81 234 5678",
                    leadingIcon   = Icons.Outlined.Phone,
                    keyboardType  = KeyboardType.Phone,
                )
                GyrTextField(
                    label         = "University Email",
                    value         = state.email,
                    onValueChange = onEmailChange,
                    placeholder   = "name@mandela.ac.za",
                    leadingIcon   = Icons.Outlined.Email,
                    keyboardType  = KeyboardType.Email,
                )
            }
        }

        DriverInfoNotice(
            text = "Your details will be verified against the NMU Registrar records before your driver profile is approved.",
            icon = Icons.Outlined.Info,
            tint = OrangeAccent,
        )
    }
}

// ─── Shared heading + notice (used by all 3 steps) ────────────────────────────

@Composable
fun DriverStepHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text       = title,
            fontSize   = 24.sp,
            fontWeight = FontWeight.Bold,
            color      = NavyPrimary,
        )
        Text(
            text       = subtitle,
            fontSize   = 14.sp,
            color      = TextMuted,
            lineHeight = 20.sp,
        )
    }
}

@Composable
fun DriverInfoNotice(
    text : String,
    icon : androidx.compose.ui.graphics.vector.ImageVector,
    tint : androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                Modifier
                    .background(tint.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                    .border(
                    width = 1.dp,
                    color = tint.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(10.dp)
                )
                    .padding(12.dp)
            ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = androidx.compose.ui.Alignment.Top,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Text(text, fontSize = 12.sp, color = tint, lineHeight = 18.sp)
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Step 1 — Student Details")
@Composable
fun DriverStep1Preview() {
    var state by remember { mutableStateOf(StudentDetailsState()) }
    MaterialTheme {
        DriverStep1Verification(
            state              = state,
            onFullNameChange   = { state = state.copy(fullName      = it) },
            onStudentNumChange = { state = state.copy(studentNumber = it) },
            onContactChange    = { state = state.copy(contactNumber = it) },
            onEmailChange      = { state = state.copy(email         = it) },
        )
    }
}