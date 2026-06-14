package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun GlobalSearchOverlay(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack.copy(alpha = 0.95f))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "GLOBAL RESEARCH SEARCH",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeonPurple
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            // High-fidelity search query input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search projects, notes, papers, threads, tasks...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = SlateBorder,
                    focusedContainerColor = SlateSurface,
                    unfocusedContainerColor = SlateSurface
                ),
                trailingIcon = if (searchQuery.isNotBlank()) {
                    {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = TextMuted)
                        }
                    }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("global_search_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (searchQuery.trim().length < 2) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Type at least 2 characters to search across all workspace indexing records.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                } else if (results.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No records found matching '$searchQuery'. Please check spelling or tags.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                } else {
                    // Match Projects
                    if (results.matchedProjects.isNotEmpty()) {
                        SearchSectionHeader(title = "Research Projects (${results.matchedProjects.size})")
                        results.matchedProjects.forEach { proj ->
                            GlassCard(onClick = {
                                viewModel.selectProjectForResearch(proj)
                                viewModel.navigateTo("Research")
                                onDismiss()
                            }) {
                                Text(proj.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(proj.description, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                            }
                        }
                    }

                    // Match Notes
                    if (results.matchedNotes.isNotEmpty()) {
                        SearchSectionHeader(title = "Scholar Notes (${results.matchedNotes.size})")
                        results.matchedNotes.forEach { note ->
                            GlassCard(onClick = {
                                viewModel.navigateTo("Notes")
                                onDismiss()
                            }) {
                                Text(note.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(note.content, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                            }
                        }
                    }

                    // Match Papers
                    if (results.matchedPapers.isNotEmpty()) {
                        SearchSectionHeader(title = "Research Papers (${results.matchedPapers.size})")
                        results.matchedPapers.forEach { paper ->
                            GlassCard(onClick = {
                                viewModel.navigateTo("Profile")
                                onDismiss()
                            }) {
                                Text(paper.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Authors: ${paper.authors}", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }

                    // Match Chat Threads
                    if (results.matchedThreads.isNotEmpty()) {
                        SearchSectionHeader(title = "Chat Conversations (${results.matchedThreads.size})")
                        results.matchedThreads.forEach { th ->
                            GlassCard(onClick = {
                                viewModel.selectThread(th.id)
                                viewModel.navigateTo("Chatbot")
                                onDismiss()
                            }) {
                                Text(th.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }

                    // Match Planner Tasks
                    if (results.matchedTasks.isNotEmpty()) {
                        SearchSectionHeader(title = "Planner Tasks (${results.matchedTasks.size})")
                        results.matchedTasks.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SlateSurface, RoundedCornerShape(10.dp))
                                    .border(1.dp, SlateBorder.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.toggleTaskCompletion(task)
                                        viewModel.navigateTo("Planner")
                                        onDismiss()
                                    }
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.Circle,
                                    contentDescription = null,
                                    tint = if (task.isCompleted) NeonPurple else TextMuted
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = task.taskName,
                                    fontSize = 13.sp,
                                    color = if (task.isCompleted) TextMuted else TextPrimary,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = SoftCyan,
        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
    )
}
