package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ResearchUiState

@Composable
fun ResearchScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val researchUiState by viewModel.researchUiState.collectAsState()
    val projects by viewModel.allProjects.collectAsState()
    val selectedProject by viewModel.selectedProjectForResearch.collectAsState()

    val isLightTheme by viewModel.isLightTheme.collectAsState()
    val workspacesList by viewModel.workspaces.collectAsState()
    val TextPrimary = if (isLightTheme) Color(0xFF1F2937) else Color(0xFFF1F5F9)
    val TextSecondary = if (isLightTheme) Color(0xFF4B5563) else Color(0xFF94A3B8)
    val TextMuted = if (isLightTheme) Color(0xFF6B7280) else Color(0xFF64748B)
    val SlateSurface = if (isLightTheme) Color(0xFFE5E7EB) else Color(0x0FFFFFFF)
    val SlateBorder = if (isLightTheme) Color(0x11000000) else Color(0x1CFFFFFF)

    var topicInput by remember { mutableStateOf("") }
    var activeReportTab by remember { mutableStateOf("Overview") }
    var showProjectDropdown by remember { mutableStateOf(false) }

    // Dialog to save to notes
    var showSaveNoteDialog by remember { mutableStateOf(false) }
    var selectedWorkspace by remember { mutableStateOf("Artificial Intelligence") }
    var selectedFolder by remember { mutableStateOf("LLMs") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "AI RESEARCH ASSISTANT",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = NeonPurple
        )
        Text(
            text = "System Scholarly Engine",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Project selector for auto-saving
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = "Project",
                tint = SoftCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Workspace Sync:",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                AssistChip(
                    onClick = { showProjectDropdown = true },
                    label = { Text(selectedProject?.name ?: "Personal Sandbox (Unsaved)") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = SlateSurface,
                        labelColor = if (selectedProject != null) SoftCyan else TextSecondary
                    ),
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.testTag("project_selector")
                )
                DropdownMenu(
                    expanded = showProjectDropdown,
                    onDismissRequest = { showProjectDropdown = false },
                    modifier = Modifier.background(DarkGreyBG)
                ) {
                    DropdownMenuItem(
                        text = { Text("No active project (Unsaved Report)", color = TextSecondary) },
                        onClick = {
                            viewModel.selectProjectForResearch(null)
                            showProjectDropdown = false
                        }
                    )
                    projects.forEach { proj ->
                        DropdownMenuItem(
                            text = { Text(proj.name, color = TextPrimary) },
                            onClick = {
                                viewModel.selectProjectForResearch(proj)
                                showProjectDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Topic research inputs
        OutlinedTextField(
            value = topicInput,
            onValueChange = { topicInput = it },
            label = { Text("Enter topic, question, technology, concept, or historical timeline") },
            placeholder = { Text("E.g., Quantum Computing hardware or Ancient cuneiform origins") },
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonPurple,
                unfocusedBorderColor = SlateBorder,
                focusedContainerColor = SlateSurface.copy(alpha = 0.3f),
                unfocusedContainerColor = SlateSurface.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("research_topic_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Execute deep research button
        GradientButton(
            text = "Generate Academic Report",
            onClick = {
                if (topicInput.isNotBlank()) {
                    viewModel.performResearch(topicInput)
                }
            },
            enabled = topicInput.isNotBlank() && researchUiState !is ResearchUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
            tag = "perform_research_button"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Result boundaries
        AnimatedContent(targetState = researchUiState, label = "ResearchState") { state ->
            when (state) {
                is ResearchUiState.Idle -> {
                    // Display recommendations (Topic suggestions chips)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = SoftCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Popular Scholarly Recommendations",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        val suggestions = listOf(
                            "Multi-Head Attention vs State Space Models",
                            "Commercializing Quantum Cryptography",
                            "Bronze Age Collapse Factors",
                            "AI Alignment Reinforcement Learning",
                            "Bioluminescence Genetics & Applications"
                        )
                        for (sug in suggestions) {
                            Card(
                                onClick = {
                                    topicInput = sug
                                    viewModel.performResearch(sug)
                                },
                                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                                border = BorderStroke(1.dp, SlateBorder),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(sug, fontSize = 13.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
                is ResearchUiState.Loading -> {
                    // Pulsing animated loading page
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulsing"
                        )

                        CircularProgressIndicator(color = NeonPurple)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Synthesizing Knowledge Nodes...",
                            color = TextPrimary.copy(alpha = alpha),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Drafting executive summary, timeline reports, and related academic metrics.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                is ResearchUiState.Success -> {
                    val report = state.report
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RESEARCH OUTPUT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SoftCyan
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(onClick = { showSaveNoteDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Description,
                                        contentDescription = "Save to Notes",
                                        tint = NeonPurple
                                    )
                                }
                                IconButton(onClick = { viewModel.clearResearchState() }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Restart",
                                        tint = TextMuted
                                    )
                                }
                            }
                        }
                        Text(
                            text = report.topic,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Professional Report Sections Tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val tabs = listOf("Overview", "Detailed Analysis", "Key Insights", "Important Concepts", "Further Reading", "Conclusion")
                            for (tb in tabs) {
                                Row(
                                    modifier = Modifier
                                        .background(
                                            if (activeReportTab == tb) NeonPurple.copy(alpha = 0.25f) else SlateSurface,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (activeReportTab == tb) NeonPurple else SlateBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { activeReportTab = tb }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tb, fontSize = 11.sp, color = if (activeReportTab == tb) NeonPurple else TextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Display active tab's report contents
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            val activeText = when (activeReportTab) {
                                "Overview" -> report.overview
                                "Detailed Analysis" -> report.detailedAnalysis
                                "Key Insights" -> report.keyInsights
                                "Important Concepts" -> report.importantConcepts
                                "Further Reading" -> report.furtherReading
                                else -> report.conclusion
                            }
                            Text(
                                text = activeText,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp
                            )
                        }

                        // Save notes suggestion notice
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SlateSurface, RoundedCornerShape(24.dp))
                                .border(1.dp, SlateBorder, RoundedCornerShape(24.dp))
                                .clickable { showSaveNoteDialog = true }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SaveAlt, contentDescription = null, tint = SoftCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Save this AI report directly into your notes foldering workspace.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
                is ResearchUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(state.message, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { viewModel.performResearch(topicInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateSurface)
                        ) {
                            Text("Retry", color = NeonPurple)
                        }
                    }
                }
            }
        }
    }

    // Dialogue to confirm Workspace folder details for note conversion
    if (showSaveNoteDialog && researchUiState is ResearchUiState.Success) {
        val activeSuccReport = (researchUiState as ResearchUiState.Success).report

        AlertDialog(
            onDismissRequest = { showSaveNoteDialog = false },
            title = { Text("Save AI Report as Note", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Note Workspace:", fontSize = 12.sp, color = TextSecondary)
                    for (wk in workspacesList) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedWorkspace = wk }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedWorkspace == wk,
                                onClick = { selectedWorkspace = wk }
                            )
                            Text(wk, fontSize = 14.sp, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Define Folder Name (E.g., LLMs, Relativity):", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = selectedFolder,
                        onValueChange = { selectedFolder = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = SlateBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveResearchReportToNotes(
                            activeSuccReport,
                            selectedWorkspace,
                            selectedFolder
                        )
                        showSaveNoteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Save to Notes Archive", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveNoteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = if (isLightTheme) Color.White else DarkGreyBG
        )
    }
}
