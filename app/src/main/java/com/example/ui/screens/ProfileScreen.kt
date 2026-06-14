package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ProfileScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val projects by viewModel.allProjects.collectAsState()
    val notes by viewModel.allNotes.collectAsState()
    val papers by viewModel.allPapers.collectAsState()
    val stats by viewModel.plannerStats.collectAsState()

    val userName by viewModel.userName.collectAsState()
    val userBio by viewModel.userBio.collectAsState()
    val userInterests by viewModel.userInterests.collectAsState()
    val isLightTheme by viewModel.isLightTheme.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Profile Card Layout Header
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Avatar with Gradient Accent
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Brush.radialGradient(listOf(NeonPurple, BrightBlue)), CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = "Academic avatar",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = userName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Profile",
                            tint = NeonPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = "ACADEMIC RANK: SCHOLAR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftCyan,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = userBio,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tags for research interests
                Text(
                    text = "Interests: $userInterests",
                    fontSize = 11.sp,
                    color = NeonPurple,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Productivity Statistics Widget
        SectionHeader(title = "Research Metric Statistics")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val cellWeight = Modifier.weight(1f)
            StatCell(
                title = "Projects Active",
                value = projects.size.toString(),
                icon = Icons.Default.Folder,
                tint = NeonPurple,
                modifier = cellWeight
            )
            StatCell(
                title = "Total Notes",
                value = notes.size.toString(),
                icon = Icons.Default.Description,
                tint = BrightBlue,
                modifier = cellWeight
            )
            StatCell(
                title = "Papers Library",
                value = papers.size.toString(),
                icon = Icons.Default.LibraryBooks,
                tint = SoftCyan,
                modifier = cellWeight
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress metrics details
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Productivity Score", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text("Weekly Score: ${stats.weeklyProductivityScore}/100", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Box(
                    modifier = Modifier
                        .background(BrightBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("+${stats.activeStreak * 5} XP Boost", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrightBlue)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Achievement Badges Grid
        SectionHeader(title = "Unlockable Research Badges")
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Badge 1: Scholar (always unlocked or unlocked if projects > 0)
            BadgeItem(
                title = "Scholarly Explorer",
                description = "Created your first systematic research project repository.",
                isUnlocked = projects.isNotEmpty(),
                icon = Icons.Default.WorkspacePremium
            )
            // Badge 2: Prompt Architect (unlocked if notes created > 3)
            BadgeItem(
                title = "Knowledge Synthesis Lord",
                description = "Successfully compiled and edited 3 or more expert notes in the vault.",
                isUnlocked = notes.size >= 3,
                icon = Icons.Default.EmojiEvents
            )
            // Badge 3: Quantum entanglement (unlocked if papers > 2 or streak > 3)
            BadgeItem(
                title = "Quantum Entanglement",
                description = "Maintained an active streak of 5 or more productivity days.",
                isUnlocked = stats.activeStreak >= 5,
                icon = Icons.Default.ModelTraining
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Red Accent Sign Out Button (minimum 48.dp target height with Ripple)
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1ADB2777)),
            border = BorderStroke(1.dp, Color(0xFFE11D48)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("logout_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Sign Out",
                    tint = Color(0xFFF43F5E),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out of Scholar Vault",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLightTheme) Color(0xFFBE123C) else Color(0xFFFDA4AF)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Edit Profile details dialog
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(userName) }
        var tempBio by remember { mutableStateOf(userBio) }
        var tempInterests by remember { mutableStateOf(userInterests) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Student Credentials", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Display Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonPurple),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempBio,
                        onValueChange = { tempBio = it },
                        label = { Text("Professional/Academic Bio") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonPurple),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempInterests,
                        onValueChange = { tempInterests = it },
                        label = { Text("Core Research Disciplines") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonPurple),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(tempName, tempBio, tempInterests)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkGreyBG
        )
    }
}

@Composable
fun StatCell(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(SlateSurface, RoundedCornerShape(16.dp))
            .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = title,
            fontSize = 9.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun BadgeItem(
    title: String,
    description: String,
    isUnlocked: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val opacity = if (isUnlocked) 1.0f else 0.4f
    val accentBg = if (isUnlocked) NeonPurple.copy(alpha = 0.15f) else SlateSurface
    val iconTint = if (isUnlocked) NeonPurple else TextMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateSurface, RoundedCornerShape(24.dp))
            .border(1.dp, SlateBorder, RoundedCornerShape(24.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(accentBg, CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else TextMuted
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else TextMuted,
                lineHeight = 16.sp
            )
        }
    }
}
