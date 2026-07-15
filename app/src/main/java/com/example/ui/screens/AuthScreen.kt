package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.FloatingHeartsBackground
import com.example.ui.components.SharedProfileImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    isDark: Boolean,
    coupleProfileUri: String?,
    onImageSelected: (String) -> Unit,
    onAuthSuccess: () -> Unit,
    onRegister: (String, String, String) -> Unit,
    onLoginSimulated: (String) -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var appPin by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(Color(0xFF2C191E), Color(0xFF150A0C))
                    } else {
                        listOf(Color.White, Color(0xFFFFF0F3))
                    }
                )
            )
            .testTag("auth_screen")
    ) {
        FloatingHeartsBackground(heartColor = Color(0x33FF4F87))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Back button style header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { /* Demo visual back button */ },
                    modifier = Modifier.background(Color(0x1AFF4F87), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFFF4F87)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (isSignUpMode) "Create Account" else "Login",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFFFF4F87),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SharedProfileImage(
                imageUri = coupleProfileUri,
                onImageSelected = onImageSelected,
                size = 130.dp,
                borderSize = 1.dp,
                paddingSize = 3.dp,
                innerBorderSize = 1.5.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Glassmorphic Input Form Container
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0x1AFF4F87) else Color(0xCCFFFFFF)
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0x33FF4F87)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isSignUpMode) {
                        // Full Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Your Name") },
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = Color(0xFFFF4F87)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF4F87),
                                focusedLabelColor = Color(0xFFFF4F87),
                                cursorColor = Color(0xFFFF4F87)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Email Address
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = Color(0xFFFF4F87)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF4F87),
                            focusedLabelColor = Color(0xFFFF4F87),
                            cursorColor = Color(0xFFFF4F87)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Create Password") },
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color(0xFFFF4F87)) },
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(icon, contentDescription = null, tint = Color(0xFFFF4F87))
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF4F87),
                            focusedLabelColor = Color(0xFFFF4F87),
                            cursorColor = Color(0xFFFF4F87)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        singleLine = true
                    )

                    if (isSignUpMode) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Confirm Password
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color(0xFFFF4F87)) },
                            trailingIcon = {
                                val icon = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(icon, contentDescription = null, tint = Color(0xFFFF4F87))
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF4F87),
                                focusedLabelColor = Color(0xFFFF4F87),
                                cursorColor = Color(0xFFFF4F87)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_password_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // APP PIN lock (6 digit lock code)
                        OutlinedTextField(
                            value = appPin,
                            onValueChange = { if (it.length <= 6) appPin = it },
                            label = { Text("App 6-Digit Lock PIN") },
                            placeholder = { Text("Default: 123456") },
                            leadingIcon = { Icon(Icons.Outlined.Dialpad, contentDescription = null, tint = Color(0xFFFF4F87)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF4F87),
                                focusedLabelColor = Color(0xFFFF4F87),
                                cursorColor = Color(0xFFFF4F87)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("app_pin_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Main Action button (Create Account / Login)
                    Button(
                        onClick = {
                            if (email.isEmpty() || password.isEmpty()) {
                                errorMessage = "Please fill in email and password."
                                return@Button
                            }
                            if (isSignUpMode) {
                                if (name.isEmpty()) {
                                    errorMessage = "Please enter your name."
                                    return@Button
                                }
                                if (password != confirmPassword) {
                                    errorMessage = "Passwords do not match."
                                    return@Button
                                }
                                if (appPin.length != 6) {
                                    errorMessage = "PIN must be exactly 6 digits."
                                    return@Button
                                }
                            }

                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null
                                delay(1500) // Beautiful simulated network loading delay
                                isLoading = false
                                if (isSignUpMode) {
                                    onRegister(name, email, appPin)
                                } else {
                                    onLoginSimulated(email)
                                }
                                onAuthSuccess()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4F87)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("submit_auth_button"),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isSignUpMode) "Create Account" else "Login",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Google login
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                delay(1000)
                                isLoading = false
                                if (isSignUpMode) {
                                    onRegister("Google Partner", "google.couple@gmail.com", "123456")
                                } else {
                                    onLoginSimulated("google.couple@gmail.com")
                                }
                                onAuthSuccess()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0x33FF4F87)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF4F87)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite, // Google icon substitute with elegant couple heart
                                contentDescription = "Google Icon",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSignUpMode) "Sign up with Google" else "Log in with Google",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Switch sign-in mode link
            Text(
                text = if (isSignUpMode) "Already Have Account? Login" else "Don't have an account? Sign Up",
                color = Color(0xFFFF4F87),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = null
                ),
                modifier = Modifier
                    .clickable {
                        isSignUpMode = !isSignUpMode
                        errorMessage = null
                    }
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "This app is for couples only. 💕",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFFF8FA3),
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
