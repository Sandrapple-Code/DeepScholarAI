package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlannerTask
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PlannerScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val tasks by viewModel.allTasks.collectAsState()
    val stats by viewModel.plannerStats.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedTaskTab by remember { mutableStateOf("Daily") }

    val isLightTheme by viewModel.isLightTheme.collectAsState()
    val TextPrimary = if (isLightTheme) Color(0xFF111827) else Color(0xFFF1F5F9)
    val TextSecondary = if (isLightTheme) Color(0xFF374151) else Color(0xFF94A3B8)
    val TextMuted = if (isLightTheme) Color(0xFF6B7280) else Color(0xFF64748B)
    val SlateSurface = if (isLightTheme) Color(0xFFE5E7EB) else Color(0x0FFFFFFF)
    val SlateBorder = if (isLightTheme) Color(0x22111827) else Color(0x1CFFFFFF)

    val filteredTasks = tasks.filter { task ->
        when (selectedTaskTab) {
            "Daily" -> task.type == "DAILY"
            "Weekly" -> task.type == "WEEKLY"
            else -> task.type == "DEADLINE"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "PRODUCTIVITY PLANNER",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = NeonPurple
        )
        Text(
            text = "Goals & Deadlines",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Tracking Widget
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TASK COMPLETION RATE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrightBlue
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${stats.completedTasksCount} of ${stats.totalTasksCount} tasks complete",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "${(stats.completionRate * 100).toInt()}%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonPurple
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { stats.completionRate },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NeonPurple,
                trackColor = SlateBorder.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tabs to filter goals
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tabs = listOf("Daily", "Weekly", "Deadlines")
            tabs.forEach { tb ->
                val isSelected = selectedTaskTab == tb
                val containerColor = if (isSelected) NeonPurple.copy(alpha = 0.2f) else SlateSurface
                val contentColor = if (isSelected) NeonPurple else TextSecondary

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(containerColor, RoundedCornerShape(24.dp))
                        .border(1.dp, SlateBorder, RoundedCornerShape(24.dp))
                        .clickable { selectedTaskTab = tb }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tb,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section header
        SectionHeader(
            title = "$selectedTaskTab Tasks",
            actionText = "+ Add Task",
            onActionClick = { showAddTaskDialog = true }
        )

        // Task Items list
        if (filteredTasks.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nice job! Empty workspace here.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            filteredTasks.forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .background(SlateSurface, RoundedCornerShape(16.dp))
                        .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                        .clickable { viewModel.toggleTaskCompletion(task) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = "Complete task",
                        tint = if (task.isCompleted) NeonPurple else TextMuted,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("task_complete_check")
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.taskName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (task.isCompleted) TextMuted else TextPrimary,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val priorityColor = when (task.priority) {
                                "HIGH" -> Color(0xFFEF4444)
                                "MEDIUM" -> Color(0xFFF59E0B)
                                else -> Color(0xFF10B981)
                            }
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(priorityColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Priority: ${task.priority}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.deleteTask(task) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Add Task Dialog Sheet
    if (showAddTaskDialog) {
        var taskName by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("HIGH") }
        var type by remember { mutableStateOf("DAILY") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Log New Task Item", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = taskName,
                        onValueChange = { taskName = it },
                        label = { Text("Task Description") },
                        placeholder = { Text("E.g., Proofread chapter 2 drafts") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Define Priority Level:", fontSize = 12.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val priorities = listOf("HIGH", "MEDIUM", "LOW")
                        for (pr in priorities) {
                            FilterChip(
                                selected = priority == pr,
                                onClick = { priority = pr },
                                label = { Text(pr, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Scope/Interval:", fontSize = 12.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val intervals = listOf("DAILY", "WEEKLY", "DEADLINE")
                        for (ty in intervals) {
                            FilterChip(
                                selected = type == ty,
                                onClick = { type = ty },
                                label = { Text(ty, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskName.isNotBlank()) {
                            viewModel.createTask(taskName, priority, type)
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Add Task", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkGreyBG
        )
    }
}
