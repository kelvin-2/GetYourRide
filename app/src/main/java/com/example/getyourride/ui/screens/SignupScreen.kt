package com.example.getyourride.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview //feat : feature
import com.example.getyourride.ui.theme.*
import com.example.getyourride.ui.components.* // Fix: Import shared components

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit = {},
    onSignUpClick: (fullName: String, studentNumber: String, email: String, password: String, isNsfasFunded: Boolean) -> Unit = { _, _, _, _, _ -> },
    onBecomeDriverClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
) {
    var fullName by remember { mutableStateOf("") }
    var studentNumber by remember { mutableStateOf("") }
    var universityEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isNsfasFunded by remember { mutableStateOf<Boolean?>(null) }   // null = not selected yet
    var agreedToTerms by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sign Up",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = NavyPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = NavyPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceGrey),
            )
        },
        containerColor = SurfaceGrey,
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {

            // ── Page heading ──────────────────────────────────────────────────
            Text(
                text = "Create Account",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "No Waiting. No Queues. Just Rides",
                fontSize = 13.sp,
                color = OrangeAccent,
                fontWeight = FontWeight.Medium,
            )

            Spacer(Modifier.height(24.dp))

            // ── Form Card ─────────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {

                    // Full Name
                    GyrTextField(
                        label = "Full Name",
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "John Doe",
                        leadingIcon = Icons.Outlined.Person,
                    )

                    // Student Number
                    GyrTextField(
                        label = "Student Number",
                        value = studentNumber,
                        onValueChange = { studentNumber = it },
                        placeholder = "8-digit ID",
                        leadingIcon = Icons.Outlined.Badge,
                        keyboardType = KeyboardType.Number,
                    )

                    // University Email
                    GyrTextField(
                        label = "University Email",
                        value = universityEmail,
                        onValueChange = { universityEmail = it },
                        placeholder = "name@mandela.ac.za",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardType = KeyboardType.Email,
                    )

                    // Password
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "PASSWORD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                            color = NavyPrimary,
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = {
                                Text("Min. 8 characters", color = TextHint, fontSize = 14.sp)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = IconTint,
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Outlined.Visibility
                                        else Icons.Outlined.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = IconTint,
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = gyrOutlinedTextFieldColors(),
                        )
                    }

                    // ── NSFAS Funding ─────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "ARE YOU NSFAS FUNDED?",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                            color = NavyPrimary,
                        )
                        NsfasRadioOption(
                            label = "Yes, I am NSFAS Funded",
                            selected = isNsfasFunded == true,
                            onSelect = { isNsfasFunded = true },
                        )
                        NsfasRadioOption(
                            label = "No, I am self-funded",
                            selected = isNsfasFunded == false,
                            onSelect = { isNsfasFunded = false },
                        )
                    }

                    // ── Terms checkbox ────────────────────────────────────────
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Checkbox(
                            checked = agreedToTerms,
                            onCheckedChange = { agreedToTerms = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NavyPrimary,
                                checkmarkColor = Color.White,
                            ),
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(10.dp))

                        val termsText = buildAnnotatedString {
                            append("I agree to the ")
                            withStyle(
                                SpanStyle(
                                    color = NavyPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline,
                                )
                            ) { append("Terms of Service") }
                            append(" and ")
                            withStyle(
                                SpanStyle(
                                    color = NavyPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline,
                                )
                            ) { append("Privacy Policy") }
                            append(" of GetYourRide.")
                        }
                        Text(
                            text = termsText,
                            fontSize = 13.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Sign Up button (Navy primary)
                    Button(
                        onClick = {
                            onSignUpClick(
                                fullName,
                                studentNumber,
                                universityEmail,
                                password,
                                isNsfasFunded == true,
                            )
                        },
                        enabled = agreedToTerms && isNsfasFunded != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyPrimary,
                            disabledContainerColor = NavyPrimary.copy(alpha = 0.4f),
                        ),
                    ) {
                        Text(
                            text = "Sign Up",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Become a Driver promo ─────────────────────────────────────────
            val driverText = buildAnnotatedString {
                append("Want to earn while you drive? ")
                withStyle(
                    SpanStyle(
                        color = NavyPrimary,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                    )
                ) { append("Become a Driver") }
            }
            Text(
                text = driverText,
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBecomeDriverClick() },
            )

            Spacer(Modifier.height(12.dp))

            // ── Already have an account ───────────────────────────────────────
            val loginText = buildAnnotatedString {
                append("Already have an account? ")
                withStyle(
                    SpanStyle(
                        color = OrangeAccent,
                        fontWeight = FontWeight.SemiBold,
                    )
                ) { append("Login") }
            }
            Text(
                text = loginText,
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLoginClick() },
            )

            Spacer(Modifier.height(16.dp))

        }

        Spacer(Modifier.height(32.dp))
    }

}

@Preview(showBackground = true, showSystemUi = true, name = "Sign Up Screen")
@Composable
fun SignUpScreenPreview() {
    GetYourRideTheme {
        SignUpScreen()
    }
}


