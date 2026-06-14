package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DashboardScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val projects by viewModel.allProjects.collectAsState()
    val papers by viewModel.allPapers.collectAsState()
    val notes by viewModel.allNotes.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val stats by viewModel.plannerStats.collectAsState()
    val username by viewModel.userName.collectAsState()

    val useCustomChatbot by viewModel.useCustomChatbot.collectAsState()
    val customApiUrl by viewModel.customApiUrl.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()
    val customModelName by viewModel.customModelName.collectAsState()
    val customSystemPrompt by viewModel.customSystemPrompt.collectAsState()
    val assistantMessages by viewModel.assistantChatMessages.collectAsState()
    val assistantIsSending by viewModel.assistantIsSending.collectAsState()

    var showAddProjectDialog by remember { mutableStateOf(false) }
    var showAssistantConfigDialog by remember { mutableStateOf(false) }

    val isLightTheme by viewModel.isLightTheme.collectAsState()
    val TextPrimary = if (isLightTheme) Color(0xFF111827) else Color(0xFFF1F5F9)
    val TextSecondary = if (isLightTheme) Color(0xFF374151) else Color(0xFF94A3B8)
    val TextMuted = if (isLightTheme) Color(0xFF6B7280) else Color(0xFF64748B)
    val SlateSurface = if (isLightTheme) Color(0xFFE5E7EB) else Color(0x0FFFFFFF)
    val SlateBorder = if (isLightTheme) Color(0x22111827) else Color(0x1CFFFFFF)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // AI Workspace Greetings Header
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back,",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Brush.radialGradient(listOf(NeonPurple, BrightBlue)), CircleShape)
                    .clip(CircleShape)
                    .clickable { viewModel.navigateTo("Profile") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI Workspace Quick Shortcuts
        Text(
            text = "Quick Workspace Shortcuts",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val shortcutModifier = Modifier.weight(1f)
            QuickShortcutItem(
                icon = Icons.Outlined.Search,
                label = "Research",
                modifier = shortcutModifier,
                isLightTheme = isLightTheme,
                onClick = { viewModel.navigateTo("Research") }
            )
            QuickShortcutItem(
                icon = Icons.Outlined.Description,
                label = "Notes",
                modifier = shortcutModifier,
                isLightTheme = isLightTheme,
                onClick = { viewModel.navigateTo("Notes") }
            )
            QuickShortcutItem(
                icon = Icons.Outlined.EventNote,
                label = "Planner",
                modifier = shortcutModifier,
                isLightTheme = isLightTheme,
                onClick = { viewModel.navigateTo("Planner") }
            )
            QuickShortcutItem(
                icon = Icons.Outlined.Forum,
                label = "Chat",
                modifier = shortcutModifier,
                isLightTheme = isLightTheme,
                onClick = { viewModel.navigateTo("Chatbot") }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Weekly Productivity Statistics Widget
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = "WEEKLY SYSTEM STATS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Knowledge Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFF97316),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${stats.activeStreak} day active streak",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                // Progress Circular Bar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(76.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { stats.weeklyProductivityScore / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = BrightBlue,
                        strokeWidth = 7.dp,
                        trackColor = SlateBorder.copy(alpha = 0.5f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${stats.weeklyProductivityScore}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Score",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // My AI Assistant Section Header
        SectionHeader(
            title = "My AI Assistant",
            actionText = "Configure",
            onActionClick = { showAssistantConfigDialog = true }
        )

        GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (useCustomChatbot) BrightBlue.copy(alpha = 0.15f) else NeonPurple.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (useCustomChatbot) Icons.Default.Build else Icons.Default.Star,
                                contentDescription = "Assistant State",
                                tint = if (useCustomChatbot) BrightBlue else NeonPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (useCustomChatbot) "Custom Integrated Model" else "Built-in Scholastic Model",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Model: $customModelName",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { viewModel.clearAssistantChat() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear Chat",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { showAssistantConfigDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Chat Area
                val chatScrollState = rememberScrollState()
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(assistantMessages.size) {
                    chatScrollState.animateScrollTo(chatScrollState.maxValue)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0x0A000000), RoundedCornerShape(12.dp))
                        .border(1.dp, SlateBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(chatScrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (msg in assistantMessages) {
                            val isUser = msg.sender == "USER"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (isUser) NeonPurple.copy(alpha = 0.15f) else SlateSurface,
                                            shape = RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (isUser) 12.dp else 0.dp,
                                                bottomEnd = if (isUser) 0.dp else 12.dp
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isUser) NeonPurple.copy(alpha = 0.3f) else SlateBorder,
                                            shape = RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (isUser) 12.dp else 0.dp,
                                                bottomEnd = if (isUser) 0.dp else 12.dp
                                            )
                                        )
                                        .padding(10.dp)
                                        .widthIn(max = 240.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = if (isUser) "You" else "Assistant",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUser) NeonPurple else BrightBlue
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = msg.text,
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        if (assistantIsSending) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(SlateSurface, RoundedCornerShape(12.dp))
                                        .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            color = BrightBlue,
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "Thinking...",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                var chatInputText by remember { mutableStateOf("") }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chatInputText,
                        onValueChange = { chatInputText = it },
                        placeholder = { Text("Ask your custom Assistant...", fontSize = 12.sp, color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = SlateBorder,
                            focusedContainerColor = SlateSurface,
                            unfocusedContainerColor = SlateSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("dashboard_assistant_input"),
                        shape = RoundedCornerShape(24.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                brush = Brush.radialGradient(listOf(NeonPurple, BrightBlue)),
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .clickable(enabled = chatInputText.isNotBlank() && !assistantIsSending) {
                                viewModel.sendAssistantMessage(chatInputText)
                                chatInputText = ""
                            }
                            .testTag("dashboard_assistant_send"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Research Projects with Add Project capability
        SectionHeader(
            title = "Research Projects",
            actionText = "+ New Project",
            onActionClick = { showAddProjectDialog = true }
        )
        if (projects.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No research projects catalogued. Tap '+ New Project' to create your first organized workspace repository.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(projects) { project ->
                    GlassCard(
                        modifier = Modifier
                            .width(200.dp)
                            .height(130.dp),
                        onClick = {
                            viewModel.selectProjectForResearch(project)
                            viewModel.navigateTo("Research")
                        }
                    ) {
                        Text(
                            text = project.category.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = project.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = project.description,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Planner Overview Widget
        SectionHeader(
            title = "Planner Tasks Today",
            actionText = "Open Planner",
            onActionClick = { viewModel.navigateTo("Planner") }
        )
        val todayTasks = tasks.take(2)
        if (todayTasks.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "All caught up! No research tasks scheduled for today.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        } else {
            todayTasks.forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(SlateSurface, RoundedCornerShape(16.dp))
                        .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                        .clickable { viewModel.toggleTaskCompletion(task) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = "Status",
                        tint = if (task.isCompleted) NeonPurple else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = task.taskName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (task.isCompleted) TextMuted else TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                                text = task.priority + " Priority",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Saved Research Papers Quick View
        SectionHeader(title = "Research Paper Library")
        val libraryPapers = papers.take(2)
        if (libraryPapers.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Library is empty. Go to the profile or workspace overview to import catalogued PDF items.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        } else {
            libraryPapers.forEach { paper ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { /* Detail note preview */ }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = paper.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrightBlue
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = paper.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = paper.authors,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { viewModel.togglePaperBookmark(paper) }) {
                            Icon(
                                imageVector = if (paper.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (paper.isBookmarked) SoftCyan else TextMuted
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Add Project Dialog
    if (showAddProjectDialog) {
        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Computer Science") }

        AlertDialog(
            onDismissRequest = { showAddProjectDialog = false },
            title = { Text("Create Research Project", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Project Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Research Description") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Category Selection
                    Text("Select Discipline Area:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        val categories = listOf("Computer Science", "Physics", "History", "Medical", "Business")
                        for (cat in categories) {
                            AssistChip(
                                onClick = { category = cat },
                                label = { Text(cat) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (category == cat) NeonPurple.copy(alpha = 0.2f) else Color.Transparent,
                                    labelColor = if (category == cat) NeonPurple else TextSecondary
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.createProject(name, description, category)
                            showAddProjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Create", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProjectDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkGreyBG
        )
    }

    // Configure Assistant Dialog
    if (showAssistantConfigDialog) {
        var tempUseCustom by remember { mutableStateOf(useCustomChatbot) }
        var tempApiUrl by remember { mutableStateOf(customApiUrl) }
        var tempApiKey by remember { mutableStateOf(customApiKey) }
        var tempModelName by remember { mutableStateOf(customModelName) }
        var tempSystemPrompt by remember { mutableStateOf(customSystemPrompt) }

        AlertDialog(
            onDismissRequest = { showAssistantConfigDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = NeonPurple)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configure Assistant", color = TextPrimary)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Set up your own custom LLM chatbot. Toggle to use your own Custom API (OpenAI/Gemini compatible endpoints), or configure custom options for built-in scholastic Gemini.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    // Toggle Custom Mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempUseCustom = !tempUseCustom }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (tempUseCustom) Icons.Default.Build else Icons.Default.Star,
                                contentDescription = null,
                                tint = if (tempUseCustom) BrightBlue else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use My Custom API", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Switch(
                            checked = tempUseCustom,
                            onCheckedChange = { tempUseCustom = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonPurple,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SlateBorder
                            )
                        )
                    }

                    // Endpoint API URL
                    OutlinedTextField(
                        value = tempApiUrl,
                        onValueChange = { tempApiUrl = it },
                        label = { Text("Custom API Endpoint URL") },
                        placeholder = { Text("e.g. https://api.openai.com/v1/chat/completions") },
                        singleLine = true,
                        enabled = tempUseCustom,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = SlateBorder,
                            disabledBorderColor = SlateBorder.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // API Key
                    OutlinedTextField(
                        value = tempApiKey,
                        onValueChange = { tempApiKey = it },
                        label = { Text(if (tempUseCustom) "API Key / Bearer Token" else "Custom Gemini API Key (Optional)") },
                        placeholder = { Text("Leave blank to use default workspace key") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Model Name
                    OutlinedTextField(
                        value = tempModelName,
                        onValueChange = { tempModelName = it },
                        label = { Text("AI Model Name") },
                        placeholder = { Text("e.g. gemini-3.5-flash or gpt-4o") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // System Instructions
                    OutlinedTextField(
                        value = tempSystemPrompt,
                        onValueChange = { tempSystemPrompt = it },
                        label = { Text("System Instruction Prompt") },
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateCustomChatbotSettings(
                            useCustom = tempUseCustom,
                            apiUrl = tempApiUrl,
                            apiKey = tempApiKey,
                            modelName = tempModelName,
                            systemPrompt = tempSystemPrompt
                        )
                        showAssistantConfigDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Save Configuration", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssistantConfigDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkGreyBG
        )
    }
}

@Composable
fun QuickShortcutItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    isLightTheme: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = if (isLightTheme) Color(0xFF374151) else SlateSurface
    val borderCol = if (isLightTheme) Color(0xFF1F2937) else SlateBorder
    val contentCol = if (isLightTheme) Color.White else TextPrimary
    val iconTint = if (isLightTheme) Color(0xFFE0E7FF) else NeonPurple

    Column(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(1.dp, borderCol, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = contentCol
        )
    }
}
