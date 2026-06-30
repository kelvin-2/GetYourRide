package com.example.getyourride.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.components.GyrTextField
import com.example.getyourride.ui.components.NsfasRadioOption
import com.example.getyourride.ui.components.gyrOutlinedTextFieldColors
import com.example.getyourride.ui.theme.CardWhite
import com.example.getyourride.ui.theme.GetYourRideTheme
import com.example.getyourride.ui.theme.IconTint
import com.example.getyourride.ui.theme.NavyPrimary
import com.example.getyourride.ui.theme.OrangeAccent
import com.example.getyourride.ui.theme.SurfaceGrey
import com.example.getyourride.ui.theme.TextHint
import com.example.getyourride.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit = {},
    onSignUpClick: (
        firstName: String,
        lastName: String,
        studentNumber: String,
        email: String,
        password: String,
        isNsfasFunded: Boolean
    ) -> Unit = { _, _, _, _, _, _ -> },
    onBecomeDriverClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var studentNumber by remember { mutableStateOf("") }
    var universityEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isNsfasFunded by remember { mutableStateOf<Boolean?>(null) }
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceGrey
                ),
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

            Text(
                text = "Create Account",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "No Waiting. No Queues. Just Rides",
                fontSize = 13.sp,
                color = OrangeAccent,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardWhite
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {

                    GyrTextField(
                        label = "First Name",
                        value = firstName,
                        onValueChange = { firstName = it },
                        placeholder = "John",
                        leadingIcon = Icons.Outlined.Person,
                    )

                    GyrTextField(
                        label = "Last Name",
                        value = lastName,
                        onValueChange = { lastName = it },
                        placeholder = "Doe",
                        leadingIcon = Icons.Outlined.Person,
                    )

                    GyrTextField(
                        label = "Student Number",
                        value = studentNumber,
                        onValueChange = { studentNumber = it },
                        placeholder = "8-digit ID",
                        leadingIcon = Icons.Outlined.Badge,
                        keyboardType = KeyboardType.Number,
                    )

                    GyrTextField(
                        label = "University Email",
                        value = universityEmail,
                        onValueChange = { universityEmail = it },
                        placeholder = "name@mandela.ac.za",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardType = KeyboardType.Email,
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
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
                                Text(
                                    text = "Min. 8 characters",
                                    color = TextHint,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = IconTint,
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        passwordVisible = !passwordVisible
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) {
                                            Icons.Outlined.Visibility
                                        } else {
                                            Icons.Outlined.VisibilityOff
                                        },
                                        contentDescription = if (passwordVisible) {
                                            "Hide password"
                                        } else {
                                            "Show password"
                                        },
                                        tint = IconTint,
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = gyrOutlinedTextFieldColors(),
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
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
                            onSelect = {
                                isNsfasFunded = true
                            },
                        )

                        NsfasRadioOption(
                            label = "No, I am self-funded",
                            selected = isNsfasFunded == false,
                            onSelect = {
                                isNsfasFunded = false
                            },
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Checkbox(
                            checked = agreedToTerms,
                            onCheckedChange = {
                                agreedToTerms = it
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NavyPrimary,
                                checkmarkColor = Color.White,
                            ),
                            modifier = Modifier.size(20.dp),
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        val termsText = buildAnnotatedString {
                            append("I agree to the ")

                            withStyle(
                                SpanStyle(
                                    color = NavyPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline,
                                )
                            ) {
                                append("Terms of Service")
                            }

                            append(" and ")

                            withStyle(
                                SpanStyle(
                                    color = NavyPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline,
                                )
                            ) {
                                append("Privacy Policy")
                            }

                            append(" of GetYourRide.")
                        }

                        Text(
                            text = termsText,
                            fontSize = 13.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            onSignUpClick(
                                firstName.trim(),
                                lastName.trim(),
                                studentNumber.trim(),
                                universityEmail.trim(),
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

            Spacer(modifier = Modifier.height(20.dp))

            val driverText = buildAnnotatedString {
                append("Want to earn while you drive? ")

                withStyle(
                    SpanStyle(
                        color = NavyPrimary,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                    )
                ) {
                    append("Become a Driver")
                }
            }

            Text(
                text = driverText,
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onBecomeDriverClick()
                    },
            )

            Spacer(modifier = Modifier.height(12.dp))

            val loginText = buildAnnotatedString {
                append("Already have an account? ")

                withStyle(
                    SpanStyle(
                        color = OrangeAccent,
                        fontWeight = FontWeight.SemiBold,
                    )
                ) {
                    append("Login")
                }
            }

            Text(
                text = loginText,
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onLoginClick()
                    },
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Sign Up Screen")
@Composable
fun SignUpScreenPreview() {
    GetYourRideTheme {
        SignUpScreen()
    }
}