package com.example.getyourride.ui.screens

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
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.getyourride.ui.theme.*

@Composable
fun LoginScreen(
    onLoginClick: (studentIdOrEmail: String, password: String) -> Unit = { _, _ -> },
    onCreateAccountClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
) {
    var studentIdOrEmail by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceGrey)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(Modifier.height(56.dp))

        // ── App Icon ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(NavyPrimary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.DirectionsBus,
                contentDescription = "GetYourRide logo",
                tint = Color.White,
                modifier = Modifier.size(40.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── App Name ──────────────────────────────────────────────────────────
        Text(
            text = "GET YOUR RIDE",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = NavyPrimary,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Reliable campus mobility for everyone.",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))

        // ── Login Card ────────────────────────────────────────────────────────
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

                // Student ID / Email field
                GyrTextField(
                    label     = "Student ID or Email",
                    value     = studentIdOrEmail,
                    onValueChange = { studentIdOrEmail = it },
                    placeholder   = "e.g. 12345678",
                    leadingIcon   = Icons.Outlined.Person,
                    keyboardType  = KeyboardType.Email,
                )

                // Password field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text       = "Password",
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                            color      = NavyPrimary,
                        )
                        Text(
                            text     = "Forgot Password?",
                            fontSize = 12.sp,
                            color    = OrangeAccent,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onForgotPasswordClick() },
                        )
                    }

                    OutlinedTextField(
                        value         = password,
                        onValueChange = { password = it },
                        placeholder   = {
                            Text("Enter your password", color = TextHint, fontSize = 14.sp)
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
                                    imageVector = if (passwordVisible)
                                        Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = if (passwordVisible)
                                        "Hide password" else "Show password",
                                    tint = IconTint,
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier        = Modifier.fillMaxWidth(),
                        shape           = RoundedCornerShape(10.dp),
                        singleLine      = true,
                        colors          = gyrOutlinedTextFieldColors(),
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Login button (Orange accent)
                Button(
                    onClick  = { onLoginClick(studentIdOrEmail, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape  = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                ) {
                    Text(
                        text       = "Login",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White,
                    )
                }

                // OR divider
                OrDivider()

                // Create Account button (Outlined navy)
                OutlinedButton(
                    onClick  = onCreateAccountClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape  = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NavyPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
                ) {
                    Text(
                        text       = "Create Account",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Footer legal text ─────────────────────────────────────────────────
        val footerText = buildAnnotatedString {
            append("By logging in, you agree to the University Transit ")
            withStyle(
                SpanStyle(
                    color          = NavyPrimary,
                    fontWeight     = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                )
            ) { append("Terms of Service") }
            append(" and ")
            withStyle(
                SpanStyle(
                    color          = NavyPrimary,
                    fontWeight     = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                )
            ) { append("Privacy Policy") }
            append(".")
        }

        Text(
            text      = footerText,
            fontSize  = 12.sp,
            color     = TextMuted,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 8.dp),
        )

        Spacer(Modifier.height(32.dp))
    }
}

// ─── Shared sub-components ────────────────────────────────────────────────────

@Composable
fun GyrTextField(
    label         : String,
    value         : String,
    onValueChange : (String) -> Unit,
    placeholder   : String,
    leadingIcon   : androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType  : KeyboardType = KeyboardType.Text,
    modifier      : Modifier = Modifier,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier) {
        Text(
            text          = label.uppercase(),
            fontSize      = 11.sp,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            color         = NavyPrimary,
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = { Text(placeholder, color = TextHint, fontSize = 14.sp) },
            leadingIcon   = {
                Icon(leadingIcon, contentDescription = null, tint = IconTint, modifier = Modifier.size(20.dp))
            },
            modifier        = Modifier.fillMaxWidth(),
            shape           = RoundedCornerShape(10.dp),
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors          = gyrOutlinedTextFieldColors(),
        )
    }
}

@Composable
fun gyrOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = NavyPrimary,
    unfocusedBorderColor = BorderLight,
    cursorColor          = NavyPrimary,
    focusedTextColor     = TextPrimary,
    unfocusedTextColor   = TextPrimary,
    focusedContainerColor   = CardWhite,
    unfocusedContainerColor = SurfaceGrey,
)

@Composable
fun OrDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderLight)
        Text(
            text     = "OR",
            fontSize = 12.sp,
            color    = TextMuted,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderLight)
    }
}