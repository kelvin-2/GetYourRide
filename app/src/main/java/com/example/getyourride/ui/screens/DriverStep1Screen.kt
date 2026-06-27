package com.example.getyourride.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.GetYourRideTheme

private val DriverBackground = Color(0xFFFBF8FD)
private val DriverPrimary = Color(0xFF011844)
private val DriverTopBar = Color(0xFF1A2E5A)
private val DriverAccent = Color(0xFFFC820C)
private val DriverFieldBackground = Color(0xFFE3E2E6)
private val DriverText = Color(0xFF1B1B1F)
private val DriverTextMuted = Color(0xFF44464F)
private val DriverOutline = Color(0xFF757780)
private val DriverInactiveProgress = Color(0xFFE3E2E6)
private val DriverError = Color(0xFFC62828)

data class DriverStep1Data(
    val surname: String,
    val firstName: String,
    val studentNumber: String,
    val contactNumber: String,
    val universityEmail: String,
    val password: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverStep1Screen(
    onBackClick: () -> Unit = {},
    onNextClick: (DriverStep1Data) -> Unit = {},
    errorMessage: String? = null
) {
    var surname by rememberSaveable {
        mutableStateOf("")
    }

    var firstName by rememberSaveable {
        mutableStateOf("")
    }

    var studentNumber by rememberSaveable {
        mutableStateOf("")
    }

    var contactNumber by rememberSaveable {
        mutableStateOf("")
    }

    var universityEmail by rememberSaveable {
        mutableStateOf("")
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GetYourRide",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Text(
                        text = "STEP 1 OF 3",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DriverTopBar
                )
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
                Button(
                    onClick = {
                        onNextClick(
                            DriverStep1Data(
                                surname = surname,
                                firstName = firstName,
                                studentNumber = studentNumber,
                                contactNumber = contactNumber,
                                universityEmail = universityEmail,
                                password = password
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DriverAccent
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Next Step",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Icon(
                        imageVector = Icons.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        },
        containerColor = DriverBackground
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DriverBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            content = {
                DriverProgressIndicator(
                    currentStep = 1,
                    totalSteps = 3
                )

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                Text(
                    text = "Become a Driver",
                    color = DriverPrimary,
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Verify your student details to begin your application journey.",
                    color = DriverTextMuted,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    content = {
                        DriverFormField(
                            label = "Surname",
                            value = surname,
                            placeholder = "Example: Alexander",
                            onValueChange = { surname = it },
                            icon = Icons.Outlined.Person
                        )

                        DriverFormField(
                            label = "First Name",
                            value = firstName,
                            placeholder = "Example: Julian",
                            onValueChange = { firstName = it },
                            icon = Icons.Outlined.Person
                        )

                        DriverFormField(
                            label = "Student Number",
                            value = studentNumber,
                            placeholder = "Example: 214 968 951",
                            onValueChange = { studentNumber = it },
                            icon = Icons.Outlined.Badge,
                            keyboardType = KeyboardType.Number
                        )

                        DriverFormField(
                            label = "Contact Number",
                            value = contactNumber,
                            placeholder = "Example: +27 12 345 6789",
                            onValueChange = { contactNumber = it },
                            icon = Icons.Outlined.Phone,
                            keyboardType = KeyboardType.Phone
                        )

                        DriverFormField(
                            label = "University Email",
                            value = universityEmail,
                            placeholder = "Example: s245987147@mandela.ac.za",
                            onValueChange = { universityEmail = it },
                            icon = Icons.Outlined.Email,
                            keyboardType = KeyboardType.Email
                        )

                        DriverFormField(
                            label = "Password",
                            value = password,
                            placeholder = "Create a login password",
                            onValueChange = { password = it },
                            icon = Icons.Outlined.Lock,
                            keyboardType = KeyboardType.Password,
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        passwordVisible = !passwordVisible
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) {
                                            Icons.Outlined.VisibilityOff
                                        } else {
                                            Icons.Outlined.Visibility
                                        },
                                        contentDescription = if (passwordVisible) {
                                            "Hide password"
                                        } else {
                                            "Show password"
                                        },
                                        tint = DriverOutline
                                    )
                                }
                            }
                        )

                        if (!errorMessage.isNullOrBlank()) {
                            DriverStepErrorText(
                                text = errorMessage
                            )
                        }
                    }
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }
        )
    }
}

@Composable
private fun DriverProgressIndicator(
    currentStep: Int,
    totalSteps: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
        content = {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (index < currentStep) {
                                DriverAccent
                            } else {
                                DriverInactiveProgress
                            }
                        )
                )
            }
        }
    )
}

@Composable
private fun DriverFormField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = {
            Text(
                text = label.uppercase(),
                color = DriverTextMuted,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.24.sp,
                modifier = Modifier.padding(start = 4.dp)
            )

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = DriverOutline
                    )
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = DriverOutline,
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = trailingIcon,
                visualTransformation = visualTransformation,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DriverText,
                    unfocusedTextColor = DriverText,
                    focusedPlaceholderColor = DriverOutline,
                    unfocusedPlaceholderColor = DriverOutline,
                    cursorColor = DriverPrimary,
                    focusedBorderColor = DriverPrimary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = DriverFieldBackground,
                    unfocusedContainerColor = DriverFieldBackground
                )
            )
        }
    )
}

@Composable
private fun DriverStepErrorText(
    text: String
) {
    Text(
        text = text,
        color = DriverError,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DriverStep1ScreenPreview() {
    GetYourRideTheme(dynamicColor = false) {
        DriverStep1Screen()
    }
}
