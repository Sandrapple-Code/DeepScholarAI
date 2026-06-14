package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AuthScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val authError by viewModel.authError.collectAsState()
    val isLightTheme by viewModel.isLightTheme.collectAsState()

    var isRegisterMode by remember { mutableStateOf(false) }

    // Inputs
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var interests by remember { mutableStateOf("") }

    // Phone alternative credentials
    var isPhoneLoginMode by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    var phonePasswordRaw by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }

    val accentColor = if (isLightTheme) Color(0xFF4F46E5) else NeonPurple
    val secondaryAccent = if (isLightTheme) Color(0xFF0EA5E9) else SoftCyan
    val textBaseColor = if (isLightTheme) Color(0xFF1F2937) else TextPrimary
    val textSubColor = if (isLightTheme) Color(0xFF4B5563) else TextSecondary

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Animated Logo & Header Icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        Brush.radialGradient(listOf(accentColor, secondaryAccent)),
                        CircleShape
                    )
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = "Academic avatar",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DeepScholar",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = textBaseColor,
                letterSpacing = (-1).sp
            )

            Text(
                text = "Dynamic Research Vault & AI Synthesizer",
                fontSize = 13.sp,
                color = secondaryAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Auth Form Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_form_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isRegisterMode) "Create Scholar Account" else if (isPhoneLoginMode) "Phone Vault Identity" else "Scholar Vault Login",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textBaseColor,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Text(
                        text = if (isRegisterMode) 
                            "Register your academic credentials to start tracking projects." 
                            else if (isPhoneLoginMode) "Unlock your session directly with your recovery phone credentials."
                            else "Sign in with your database password.",
                        fontSize = 12.sp,
                        color = textSubColor,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp, bottom = 18.dp)
                    )

                    if (isRegisterMode) {
                        // Registration mode fields
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Vault Username", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = accentColor)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                focusedLabelColor = accentColor,
                                unfocusedBorderColor = if (isLightTheme) Color(0xFFD1D5DB) else Color(0x3BFFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("username_input_field")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = accentColor)
                            },
                            trailingIcon = {
                                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = icon, contentDescription = "Toggle password visibility")
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                focusedLabelColor = accentColor,
                                unfocusedBorderColor = if (isLightTheme) Color(0xFFD1D5DB) else Color(0x3BFFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("password_input_field")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = accentColor)
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                focusedLabelColor = accentColor,
                                unfocusedBorderColor = if (isLightTheme) Color(0xFFD1D5DB) else Color(0x3BFFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("confirm_password_input_field")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Backup Phone for Recovery
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Backup Phone Number (Optional)", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = accentColor)
                            },
                            placeholder = { Text("E.g., +15551234567", fontSize = 12.sp, color = textSubColor) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                focusedLabelColor = accentColor,
                                unfocusedBorderColor = if (isLightTheme) Color(0xFFD1D5DB) else Color(0x3BFFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("phone_input_field_reg")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = phonePasswordRaw,
                            onValueChange = { phonePasswordRaw = it },
                            label = { Text("Alternative Phone Password (Optional)", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = accentColor)
                            },
                            placeholder = { Text("Code/PIN for phone-only login", fontSize = 12.sp, color = textSubColor) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                focusedLabelColor = accentColor,
                                unfocusedBorderColor = if (isLightTheme) Color(0xFFD1D5DB) else Color(0x3BFFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("phone_pwd_input_field_reg")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bio
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Academic Bio (Optional)", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = accentColor)
                            },
                            placeholder = { Text("E.g., Quantum physics explorer", fontSize = 12.sp, color = textSubColor) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                focusedLabelColor = accentColor,
                                unfocusedBorderColor = if (isLightTheme) Color(0xFFD1D5DB) else Color(0x3BFFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("bio_input_field")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Research Interests
                        OutlinedTextField(
                            value = interests,
                            onValueChange = { interests = it },
                            label = { Text("Research Disciplines (Optional)", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Bookmark, contentDescription = null, tint = accentColor)
                            },
                            placeholder = { Text("E.g., AI Ethics, Astrophysics", fontSize = 12.sp, color = textSubColor) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                focusedLabelColor = accentColor,
                                unfocusedBorderColor = if (isLightTheme) Color(0xFFD1D5DB) else Color(0x3BFFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("interests_input_field")
                        )
                    } else {
                        // Login Modes (Username login or Phone password login)
                        if (isPhoneLoginMode) {
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Registered Phone Number", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = accentColor)
                                },
                                placeholder = { Text("Enter your recovery number", fontSize = 12.sp, color = textSubColor) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    focusedLabelColor = accentColor,
                                    unfocusedBorderColor = if (isLightTheme) Color(0xFFD1D5DB) else Color(0x3BFFFFFF)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .testTag("recovery_phone_input_field")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = phonePasswordRaw,
                                onValueChange = { phonePasswordRaw = it },
                                label = { Text("Phone Password / PIN", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = accentColor)
                                },
                                trailingIcon = {
                                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(imageVector = icon, contentDescription = "Toggle password visibility")
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    focusedLabelColor = accentColor,
                                    unfocusedBorderColor = if (isLightTheme) Color(0xFFD1D5DB) else Color(0x3BFFFFFF)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .testTag("phone_password_input_field")
                            )
                        } else {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Vault Username", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = accentColor)
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    focusedLabelColor = accentColor,
                                    unfocusedBorderColor = if (isLightTheme) Color(0xFFD1D5DB) else Color(0x3BFFFFFF)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .testTag("username_input_field")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = accentColor)
                                },
                                trailingIcon = {
                                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(imageVector = icon, contentDescription = "Toggle password visibility")
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    focusedLabelColor = accentColor,
                                    unfocusedBorderColor = if (isLightTheme) Color(0xFFD1D5DB) else Color(0x3BFFFFFF)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .testTag("password_input_field")
                            )
                        }

                        // Toggle Option for Phone Password recovery
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clickable {
                                    isPhoneLoginMode = !isPhoneLoginMode
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isPhoneLoginMode) "Back to Standard Sign In" else "Forgot password? Sign in with Phone",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }

                    // Contextual Error Alerts
                    if (authError != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x33DC2626), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = authError ?: "",
                                    color = if (isLightTheme) Color(0xFFB91C1C) else Color(0xFFFCA5A5),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Dynamic Validation / Submission Button
                    Button(
                        onClick = {
                            if (isRegisterMode) {
                                viewModel.register(
                                    username = username,
                                    passwordRaw = password,
                                    bio = bio,
                                    interests = interests,
                                    phoneNumber = phoneNumber,
                                    phonePasswordRaw = phonePasswordRaw
                                )
                            } else {
                                if (isPhoneLoginMode) {
                                    viewModel.loginByPhone(phoneNumber, phonePasswordRaw)
                                } else {
                                    viewModel.login(username, password)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_submit_button")
                    ) {
                        Text(
                            text = if (isRegisterMode) "Complete Registration" else if (isPhoneLoginMode) "Verify Recovery Phone" else "Unlock Session Vault",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mode Toggle Action with 48.dp minimum touch target
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable {
                                isRegisterMode = !isRegisterMode
                                isPhoneLoginMode = false // reset phone sub mode
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isRegisterMode) "Already have an account? " else "Are you a new scholar? ",
                            fontSize = 12.sp,
                            color = textSubColor
                        )
                        Text(
                            text = if (isRegisterMode) "Sign In" else "Create Account",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Informational Hints Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = textSubColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "End-to-End Local Encrypted SQLite Isolation",
                    fontSize = 11.sp,
                    color = textSubColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
