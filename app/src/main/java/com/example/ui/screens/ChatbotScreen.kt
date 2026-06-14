package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatThread
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatbotScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val threads by viewModel.allThreads.collectAsState()
    val activeThreadId by viewModel.selectedThreadId.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    val activePromptStyle by viewModel.activePromptStyle.collectAsState()
    val isAssistantMode by viewModel.isAssistantMode.collectAsState()
    val chatAttachment by viewModel.chatAttachment.collectAsState()
    val studyPlanProposal by viewModel.latestStudyPlanProposal.collectAsState()
    val isLightTheme by viewModel.isLightTheme.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var userPrompt by remember { mutableStateOf("") }
    var showNewThreadDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showPdfPickDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val chatPdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Ignore if permission not persistable
                }
                val fileName = getFileName(context, uri) ?: "Selected_Device_Document.pdf"
                viewModel.attachFile(
                    name = fileName,
                    type = "application/pdf",
                    uriString = uri.toString()
                )
                showPdfPickDialog = false
            }
        }
    )

    // Style elements matching Light & Dark modes
    val surfaceBg = if (isLightTheme) Color(0xFAF9FAFC) else SlateSurface
    val textPrimaryColor = if (isLightTheme) Color(0xFF1F2937) else TextPrimary
    val textSecondaryColor = if (isLightTheme) Color(0xFF4B5563) else TextSecondary
    val textMutedColor = if (isLightTheme) Color(0xFF9CA3AF) else TextMuted
    val themeBorderColor = if (isLightTheme) Color(0x22111827) else SlateBorder
    val themeAccentColor = if (isLightTheme) Color(0xFF4F46E5) else NeonPurple
    val bubbleSelfColor = if (isLightTheme) Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5))) else Brush.linearGradient(listOf(NeonPurple, AccentGradientStart))
    val bubbleOtherColor = if (isLightTheme) Brush.linearGradient(listOf(Color(0xFFE5E7EB), Color(0xFFE5E7EB))) else Brush.linearGradient(listOf(DarkGreyBG, DarkGreyBG))

    val activeThreadObj = threads.find { it.id == activeThreadId }
    val filteredThreads = threads.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }

    val lazyListState = rememberLazyListState()

    // Scroll to bottom when messages load
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Screen Header title with Actions (History Dialog Trigger & Assistant Mode Toggle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceBg, RoundedCornerShape(24.dp))
                    .border(1.dp, themeBorderColor, RoundedCornerShape(24.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = themeAccentColor
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = activeThreadObj?.title ?: "DeepScholar AI Chat sandbox",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isAssistantMode) "Assistant Mode Active" else "Modular Scholarly Partner",
                                fontSize = 10.sp,
                                color = if (isAssistantMode) SoftCyan else textMutedColor
                            )
                            if (isAssistantMode) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(SoftCyan, CircleShape)
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Assistant Mode Quick Trigger
                    IconButton(
                        onClick = { viewModel.toggleAssistantMode() },
                        modifier = Modifier.testTag("assistant_mode_toggle_header")
                    ) {
                        Icon(
                            imageVector = if (isAssistantMode) Icons.Default.SupportAgent else Icons.Outlined.SupportAgent,
                            contentDescription = "Toggle Assistant Mode",
                            tint = if (isAssistantMode) SoftCyan else textMutedColor
                        )
                    }

                    // Floating History button to toggle saved conversations drawer
                    IconButton(
                        onClick = { showHistoryDialog = true },
                        modifier = Modifier.testTag("chat_history_trigger_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (threads.isNotEmpty()) {
                                    Badge(containerColor = themeAccentColor) {
                                        Text(threads.size.toString(), color = Color.White, fontSize = 8.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Prev Discussions",
                                tint = themeAccentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Horizon Prompting Styles Selector pills row
            ScrollableStylePillsRow(
                activeStyle = activePromptStyle,
                onStyleChange = { viewModel.setPromptStyle(it) },
                isLightTheme = isLightTheme,
                accentColor = themeAccentColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Main chat messages dialogue area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(surfaceBg, RoundedCornerShape(24.dp))
                    .border(1.dp, themeBorderColor, RoundedCornerShape(24.dp))
                    .padding(8.dp)
            ) {
                if (activeThreadId == null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Forum,
                            contentDescription = null,
                            tint = textMutedColor,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Active Scholar Discussion",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondaryColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap on the discussions history book to load or start a thread.",
                            fontSize = 11.sp,
                            color = textMutedColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showHistoryDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = themeAccentColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Discussions", fontSize = 12.sp, color = Color.White)
                        }
                    }
                } else if (messages.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = themeAccentColor, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Awaiting Scholar Synthesis...",
                            fontSize = 11.sp,
                            color = textMutedColor
                        )
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages) { msg ->
                            val isUser = msg.sender == "USER"
                            val bubbleAlign = if (isUser) Alignment.End else Alignment.Start
                            val bubbleCol = if (isUser) bubbleSelfColor else bubbleOtherColor
                            val textCol = if (isUser || !isLightTheme) Color.White else Color(0xFF111827)

                            // Parse researcher follow ups if present
                            val rawText = msg.text
                            val followUpHeader = "## DeepScholar Researcher Follow-ups"
                            val hasFollowUps = rawText.contains(followUpHeader)

                            val mainBodyText = if (hasFollowUps) rawText.substringBefore(followUpHeader).trim() else rawText
                            val followUpSegment = if (hasFollowUps) rawText.substringAfter(followUpHeader).trim() else ""

                            val parsedQuestions = remember(followUpSegment) {
                                if (followUpSegment.isEmpty()) emptyList()
                                else {
                                    followUpSegment.lines()
                                        .filter { it.trim().startsWith("-") || it.trim().startsWith("*") || (it.trim().firstOrNull()?.isDigit() == true && it.contains('.')) }
                                        .map { it.replaceFirst(Regex("^[-*\\sd+0-9.]"), "").trim() }
                                        .filter { it.isNotBlank() && !it.lowercase().contains("do you want me") }
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = bubbleAlign
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                ) {
                                    Text(
                                        text = if (isUser) "You" else "Scholarly Assistant",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUser) themeAccentColor else SoftCyan
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(bubbleCol, RoundedCornerShape(16.dp))
                                        .border(
                                            width = if (isUser) 0.dp else 1.dp,
                                            color = themeBorderColor.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(12.dp)
                                        .widthIn(max = 280.dp)
                                ) {
                                    Text(
                                        text = formatMathDollars(mainBodyText),
                                        color = textCol,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }

                                // Interactive Quick-Click Prompt chips for Follow-up questions
                                if (!isUser && parsedQuestions.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Column(
                                        modifier = Modifier
                                            .widthIn(max = 280.dp)
                                            .padding(top = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "RECOMMENDED ENQUIRIES:",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = themeAccentColor
                                        )
                                        parsedQuestions.take(3).forEach { question ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        color = themeAccentColor.copy(alpha = 0.08f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = themeAccentColor.copy(alpha = 0.25f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable {
                                                        userPrompt = question
                                                        viewModel.sendChatMessage(question)
                                                        userPrompt = ""
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ChatBubbleOutline,
                                                        contentDescription = null,
                                                        tint = themeAccentColor,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Text(
                                                        text = question,
                                                        color = textPrimaryColor,
                                                        fontSize = 10.sp,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // File Attachment preview panel (above the input box if active)
            AnimatedVisibility(
                visible = chatAttachment != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                chatAttachment?.let { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(themeAccentColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .border(1.dp, themeAccentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (file.type.contains("pdf")) Icons.Default.Article else Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = themeAccentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Attached: ${file.name}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimaryColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { viewModel.clearAttachment() },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Clear attachment",
                                tint = textMutedColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Prompt Entry Box & Action triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // File Picker trigger button
                IconButton(
                    onClick = { showPdfPickDialog = true },
                    modifier = Modifier
                        .size(44.dp)
                        .background(surfaceBg, CircleShape)
                        .border(1.dp, themeBorderColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach PDF/Audio",
                        tint = themeAccentColor
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedTextField(
                    value = userPrompt,
                    onValueChange = { userPrompt = it },
                    placeholder = {
                        Text(
                            text = if (isAssistantMode) "Ask Assistant about goals/tasks..." else "Query research intelligence...",
                            fontSize = 12.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeAccentColor,
                        unfocusedBorderColor = themeBorderColor,
                        focusedContainerColor = surfaceBg,
                        unfocusedContainerColor = surfaceBg,
                        focusedTextColor = textPrimaryColor,
                        unfocusedTextColor = textPrimaryColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input"),
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (userPrompt.isNotBlank() && activeThreadId != null) {
                            viewModel.sendChatMessage(userPrompt)
                            userPrompt = ""
                        }
                    },
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(listOf(themeAccentColor, BrightBlue)),
                            CircleShape
                        )
                        .size(46.dp)
                        .testTag("chat_send_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }

        // --- STUDY PLAN PROPOSAL INTERACTIVE CARD FLOATING OVERLAY ---
        AnimatedVisibility(
            visible = studyPlanProposal != null,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp)
        ) {
            studyPlanProposal?.let { proposal ->
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isLightTheme) Color(0xFFFFFFFF) else Color(0xFF1E1E2A),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .border(2.dp, themeAccentColor, RoundedCornerShape(24.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EventNote,
                                    contentDescription = null,
                                    tint = themeAccentColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Automated Study Plan Recommendation",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textPrimaryColor
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Topic: ${proposal.topic} (${proposal.durationWeeks} Weeks Estimated)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftCyan
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            proposal.tasks.take(4).forEachIndexed { idx, tName ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = themeAccentColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tName,
                                        fontSize = 10.sp,
                                        color = textSecondaryColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { viewModel.dismissStudyPlan() }) {
                                    Text("Dismiss Plan", color = textMutedColor, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.approveStudyPlan() },
                                    colors = ButtonDefaults.buttonColors(containerColor = themeAccentColor),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Approve & Schedule Goals", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- DISCUSSIONS LISTING SPACE-SAVING DRAWER OVERLAY ---
        if (showHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showHistoryDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Discussions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textPrimaryColor
                        )
                        IconButton(onClick = { showNewThreadDialog = true }) {
                            Icon(Icons.Default.AddComment, contentDescription = "Add Thread", tint = themeAccentColor)
                        }
                    }
                },
                text = {
                    Column {
                        // Thread Search
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search previous chats...", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = themeAccentColor,
                                unfocusedBorderColor = themeBorderColor
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (filteredThreads.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No discussions found.", color = textMutedColor, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredThreads) { th ->
                                    val isActive = th.id == activeThreadId
                                    val itemBorder = if (isActive) themeAccentColor else Color.Transparent
                                    val itemBg = if (isActive) themeAccentColor.copy(alpha = 0.08f) else Color.Transparent

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(itemBg, RoundedCornerShape(8.dp))
                                            .border(1.dp, itemBorder, RoundedCornerShape(8.dp))
                                            .clickable {
                                                viewModel.selectThread(th.id)
                                                showHistoryDialog = false
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (th.isPinned) {
                                                Icon(
                                                    imageVector = Icons.Default.PushPin,
                                                    contentDescription = "Pinned",
                                                    tint = SoftCyan,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                text = th.title,
                                                fontSize = 12.sp,
                                                color = if (isActive) themeAccentColor else textPrimaryColor,
                                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Row {
                                            IconButton(
                                                onClick = { viewModel.toggleThreadPin(th) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (th.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                                    contentDescription = "Pin Thread",
                                                    tint = if (th.isPinned) SoftCyan else textMutedColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteThread(th) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Delete Thread",
                                                    tint = Color.Red.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showHistoryDialog = false }) {
                        Text("Finish", color = themeAccentColor)
                    }
                },
                containerColor = if (isLightTheme) Color.White else Color(0xFF1F1F2C)
            )
        }

        // --- PDF / AUDIO SELECTION OPTION BUBBLE DIALOG ---
        if (showPdfPickDialog) {
            AlertDialog(
                onDismissRequest = { showPdfPickDialog = false },
                title = { Text("Select Document & Media to Analyze", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Pick a simulated academic resource to attach OR pick a real PDF from your device storage:", fontSize = 11.sp, color = textSecondaryColor)
                        
                        Button(
                            onClick = {
                                chatPdfPickerLauncher.launch("application/pdf")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeAccentColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick Real PDF from Device", fontSize = 11.sp, color = Color.White)
                        }

                        Divider(color = themeBorderColor)

                        // Option 1: PDF
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(themeAccentColor.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.attachFile(
                                        name = "GPT-5_Architectural_Ethics.pdf",
                                        type = "application/pdf",
                                        uriString = "mock://docs/gpt5.pdf"
                                    )
                                    showPdfPickDialog = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Article, contentDescription = null, tint = themeAccentColor)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("GPT-5_Architectural_Ethics.pdf", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textPrimaryColor)
                                Text("Academic paper - 14 pages", fontSize = 9.sp, color = textMutedColor)
                            }
                        }

                        // Option 2: PDF
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(themeAccentColor.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.attachFile(
                                        name = "Astrobiology_Viking_Review.pdf",
                                        type = "application/pdf",
                                        uriString = "mock://docs/astrobiology.pdf"
                                    )
                                    showPdfPickDialog = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Article, contentDescription = null, tint = themeAccentColor)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Astrobiology_Viking_Review.pdf", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textPrimaryColor)
                                Text("Scientific paper - 8 pages", fontSize = 9.sp, color = textMutedColor)
                            }
                        }

                        // Option 3: Sound Track
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BrightBlue.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.attachFile(
                                        name = "Quantum_Lab_Interview.mp3",
                                        type = "audio/mpeg",
                                        uriString = "mock://voice/quantum_interview.mp3"
                                    )
                                    showPdfPickDialog = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Audiotrack, contentDescription = null, tint = BrightBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Quantum_Lab_Interview.mp3", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textPrimaryColor)
                                Text("Voice Transcript - 04:12 mins", fontSize = 9.sp, color = textMutedColor)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPdfPickDialog = false }) {
                        Text("Cancel", color = textMutedColor)
                    }
                },
                containerColor = if (isLightTheme) Color.White else Color(0xFF1F1F2C)
            )
        }

        // New Thread Dialog
        if (showNewThreadDialog) {
            var threadTitle by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showNewThreadDialog = false },
                title = { Text("Start New Scholar Discussion", color = textPrimaryColor, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = threadTitle,
                            onValueChange = { threadTitle = it },
                            label = { Text("Discussion Subject") },
                            placeholder = { Text("E.g., Quantum Gravity notes") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = themeAccentColor,
                                unfocusedBorderColor = themeBorderColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (threadTitle.isNotBlank()) {
                                viewModel.createChatThread(threadTitle)
                                showNewThreadDialog = false
                                showHistoryDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeAccentColor)
                    ) {
                        Text("Start", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewThreadDialog = false }) {
                        Text("Cancel", color = textMutedColor)
                    }
                },
                containerColor = if (isLightTheme) Color.White else Color(0xFF1F1F2C)
            )
        }
    }
}

@Composable
fun ScrollableStylePillsRow(
    activeStyle: String,
    onStyleChange: (String) -> Unit,
    isLightTheme: Boolean,
    accentColor: Color
) {
    val stylesList = listOf(
        "None",
        "Explain like a 5 year old",
        "Summarize text",
        "Generate insights from the uploaded research paper",
        "Research Questions generator"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        stylesList.forEach { style ->
            val isSelected = activeStyle == style
            val chipBg = if (isSelected) accentColor else if (isLightTheme) Color(0xFFE5E7EB) else SlateSurface.copy(alpha = 0.5f)
            val chipTextCol = if (isSelected) Color.White else if (isLightTheme) Color(0xFF1F2937) else TextSecondary
            val chipBorder = if (isSelected) Color.Transparent else if (isLightTheme) Color(0x1B000000) else SlateBorder.copy(alpha = 0.6f)

            Box(
                modifier = Modifier
                    .background(chipBg, RoundedCornerShape(12.dp))
                    .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
                    .clickable { onStyleChange(style) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = style,
                        color = chipTextCol,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatMathDollars(text: String): String {
    val sb = java.lang.StringBuilder()
    var i = 0
    var inDouble = false
    var inSingle = false
    while (i < text.length) {
        if (i + 1 < text.length && text[i] == '$' && text[i+1] == '$') {
            if (inDouble) {
                sb.append(" ]\n")
                inDouble = false
            } else {
                sb.append("\n[ ")
                inDouble = true
            }
            i += 2
        } else if (text[i] == '$') {
            val isCurrency = i + 1 < text.length && (text[i+1].isDigit() || (text[i+1] == ' ' && i + 2 < text.length && text[i+2].isDigit()))
            if (isCurrency) {
                sb.append('$')
                i += 1
            } else {
                if (inSingle) {
                    sb.append("'")
                    inSingle = false
                } else {
                    sb.append("'")
                    inSingle = true
                }
                i += 1
            }
        } else {
            sb.append(text[i])
            i += 1
        }
    }
    return sb.toString()
}

private fun getFileName(context: android.content.Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}
