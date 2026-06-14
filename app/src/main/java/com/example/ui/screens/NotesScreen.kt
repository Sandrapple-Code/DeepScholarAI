package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.Note
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

sealed class NotesDest {
    object WorkspacesList : NotesDest()
    data class WorkspaceFolders(val workspace: String) : NotesDest()
    data class FolderContents(val workspace: String, val folder: String) : NotesDest()
    data class ReadNoteFullPage(val note: Note) : NotesDest()
    data class ReadPdfFullPage(val note: Note) : NotesDest()
    data class WriteNoteFullPage(val workspace: String, val folder: String, val noteToEdit: Note? = null) : NotesDest()
}

@Composable
fun NotesScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val notes by viewModel.allNotes.collectAsState()
    val isLightTheme by viewModel.isLightTheme.collectAsState()

    // Hierarchical local backstack
    val notesNavStack = remember { mutableStateListOf<NotesDest>(NotesDest.WorkspacesList) }
    val currentDest = notesNavStack.lastOrNull() ?: NotesDest.WorkspacesList

    fun navigateTo(dest: NotesDest) {
        notesNavStack.add(dest)
    }

    fun navigateBack() {
        if (notesNavStack.size > 1) {
            notesNavStack.removeAt(notesNavStack.size - 1)
        }
    }

    // Capture standard system back presses to navigate backwards inside notes stack
    BackHandler(enabled = notesNavStack.size > 1) {
        navigateBack()
    }

    // Text & graphic themes based on dark/light toggle
    val textPrimaryColor = if (isLightTheme) Color(0xFF1F2937) else TextPrimary
    val textSecondaryColor = if (isLightTheme) Color(0xFF4B5563) else TextSecondary
    val textMutedColor = if (isLightTheme) Color(0xFF9CA3AF) else TextMuted
    val cardBgColor = if (isLightTheme) Color(0xFAF9FAFC) else SlateSurface
    val borderStrokeColor = if (isLightTheme) Color(0x1B000000) else SlateBorder
    val themeAccentColor = if (isLightTheme) Color(0xFF4F46E5) else NeonPurple

    val workspaces by viewModel.workspaces.collectAsState()
    val foldersMap by viewModel.foldersMap.collectAsState()

    AnimatedContent(
        targetState = currentDest,
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { 300 }) + fadeIn() togetherWith
                    slideOutHorizontally(targetOffsetX = { -300 }) + fadeOut()
        },
        label = "NotesInternalNav",
        modifier = modifier.fillMaxSize()
    ) { dest ->
        when (dest) {
            is NotesDest.WorkspacesList -> {
                WorkspacesLanding(
                    workspaces = workspaces,
                    foldersMap = foldersMap,
                    notes = notes,
                    isLightTheme = isLightTheme,
                    onWorkspaceClick = { ws -> navigateTo(NotesDest.WorkspaceFolders(ws)) },
                    onAddWorkspace = { viewModel.addWorkspace(it) }
                )
            }

            is NotesDest.WorkspaceFolders -> {
                WorkspaceFoldersPage(
                    workspace = dest.workspace,
                    folders = foldersMap[dest.workspace] ?: emptyList(),
                    notes = notes,
                    isLightTheme = isLightTheme,
                    onBack = { navigateBack() },
                    onFolderClick = { folder -> navigateTo(NotesDest.FolderContents(dest.workspace, folder)) },
                    onAddFolder = { ws, folder -> viewModel.addFolder(ws, folder) }
                )
            }

            is NotesDest.FolderContents -> {
                FolderContentsPage(
                    workspace = dest.workspace,
                    folder = dest.folder,
                    notes = notes,
                    viewModel = viewModel,
                    isLightTheme = isLightTheme,
                    onBack = { navigateBack() },
                    onNoteClick = { note ->
                        if (note.isPdf) {
                            navigateTo(NotesDest.ReadPdfFullPage(note))
                        } else {
                            navigateTo(NotesDest.ReadNoteFullPage(note))
                        }
                    },
                    onCreateNewNote = {
                        navigateTo(NotesDest.WriteNoteFullPage(dest.workspace, dest.folder))
                    }
                )
            }

            is NotesDest.ReadNoteFullPage -> {
                ReadNoteFullPagePage(
                    note = dest.note,
                    viewModel = viewModel,
                    isLightTheme = isLightTheme,
                    onBack = { navigateBack() },
                    onEdit = {
                        navigateTo(NotesDest.WriteNoteFullPage(dest.note.workspace, dest.note.folder, dest.note))
                    }
                )
            }

            is NotesDest.ReadPdfFullPage -> {
                ReadPdfFullPagePage(
                    note = dest.note,
                    viewModel = viewModel,
                    isLightTheme = isLightTheme,
                    onBack = { navigateBack() }
                )
            }

            is NotesDest.WriteNoteFullPage -> {
                WriteNoteFullPagePage(
                    workspace = dest.workspace,
                    folder = dest.folder,
                    noteToEdit = dest.noteToEdit,
                    viewModel = viewModel,
                    isLightTheme = isLightTheme,
                    onBack = { navigateBack() }
                )
            }
        }
    }
}

// --- SUB-SCREEN 1: WORKSPACES LANDING ---
@Composable
fun WorkspacesLanding(
    workspaces: List<String>,
    foldersMap: Map<String, List<String>>,
    notes: List<Note>,
    isLightTheme: Boolean,
    onWorkspaceClick: (String) -> Unit,
    onAddWorkspace: (String) -> Unit
) {
    val textPrimaryColor = if (isLightTheme) Color(0xFF1F2937) else TextPrimary
    val textSecondaryColor = if (isLightTheme) Color(0xFF4B5563) else TextSecondary
    val textMutedColor = if (isLightTheme) Color(0xFF9CA3AF) else TextMuted
    val accentColor = if (isLightTheme) Color(0xFF4F46E5) else NeonPurple
    val cardBgColor = if (isLightTheme) Color(0xFFFFFFFF) else SlateSurface
    val borderColor = if (isLightTheme) Color(0x1B000000) else SlateBorder

    var showAddWorkspaceDialog by remember { mutableStateOf(false) }
    var newWorkspaceName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "KNOWLEDGE VAULT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = 1.sp
        )
        Text(
            text = "Explorer Workspaces",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = textPrimaryColor
        )
        Text(
            text = "Select an offline scholarship directory to discover research archives and annotated PDFs.",
            fontSize = 12.sp,
            color = textMutedColor
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SCHOLARLY DIRECTORIES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textMutedColor
            )

            Button(
                onClick = { showAddWorkspaceDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Workspace", fontSize = 11.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        workspaces.forEach { ws ->
            val subFoldersCount = foldersMap[ws]?.size ?: 0
            val relatedNotes = notes.filter { it.workspace == ws }
            val noteCount = relatedNotes.size
            val pdfCount = relatedNotes.count { it.isPdf }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(cardBgColor, RoundedCornerShape(16.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .clickable { onWorkspaceClick(ws) }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(accentColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (ws) {
                                "Artificial Intelligence" -> Icons.Default.Memory
                                "Physics" -> Icons.Default.Science
                                else -> Icons.Default.FolderOpen
                            },
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ws,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimaryColor
                        )
                        Text(
                            text = "$subFoldersCount subfolders • $noteCount documents • $pdfCount PDFs",
                            fontSize = 12.sp,
                            color = textSecondaryColor
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Open",
                        tint = textMutedColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        if (showAddWorkspaceDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddWorkspaceDialog = false
                    newWorkspaceName = ""
                },
                title = { Text("Create New Workspace", color = textPrimaryColor, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Enter dynamic name for your workspace directory:", fontSize = 12.sp, color = textSecondaryColor)
                        OutlinedTextField(
                            value = newWorkspaceName,
                            onValueChange = { newWorkspaceName = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimaryColor,
                                unfocusedTextColor = textPrimaryColor,
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = borderColor
                            ),
                            placeholder = { Text("e.g. Cognitive Psychology", color = textMutedColor, fontSize = 13.sp) },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newWorkspaceName.isNotBlank()) {
                                onAddWorkspace(newWorkspaceName.trim())
                                showAddWorkspaceDialog = false
                                newWorkspaceName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Create", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddWorkspaceDialog = false
                            newWorkspaceName = ""
                        }
                    ) {
                        Text("Cancel", color = textSecondaryColor)
                    }
                },
                containerColor = if (isLightTheme) Color.White else Color(0xFF1E293B)
            )
        }
    }
}

// --- SUB-SCREEN 2: WORKSPACE SUB-FOLDERS PAGE ---
@Composable
fun WorkspaceFoldersPage(
    workspace: String,
    folders: List<String>,
    notes: List<Note>,
    isLightTheme: Boolean,
    onBack: () -> Unit,
    onFolderClick: (String) -> Unit,
    onAddFolder: (String, String) -> Unit
) {
    val textPrimaryColor = if (isLightTheme) Color(0xFF1F2937) else TextPrimary
    val textSecondaryColor = if (isLightTheme) Color(0xFF4B5563) else TextSecondary
    val textMutedColor = if (isLightTheme) Color(0xFF9CA3AF) else TextMuted
    val accentColor = if (isLightTheme) Color(0xFF4F46E5) else NeonPurple
    val cardBgColor = if (isLightTheme) Color(0xFFFFFFFF) else SlateSurface
    val borderColor = if (isLightTheme) Color(0x1B000000) else SlateBorder

    var showAddFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onBack() }
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Return", tint = accentColor)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Back to Workspaces", fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = workspace.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = SoftCyan,
            letterSpacing = 1.sp
        )
        Text(
            text = "Workspace Directories",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = textPrimaryColor
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SUBFOLDERS IN WORKSPACE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textMutedColor
            )

            Button(
                onClick = { showAddFolderDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Folder", fontSize = 11.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        folders.forEach { fld ->
            val noteCount = notes.count { it.workspace == workspace && it.folder == fld && !it.isPdf }
            val pdfCount = notes.count { it.workspace == workspace && it.folder == fld && it.isPdf }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(cardBgColor, RoundedCornerShape(14.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .clickable { onFolderClick(fld) }
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = BrightBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = fld,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimaryColor
                            )
                            Text(
                                text = "$noteCount notes, $pdfCount PDF publications",
                                fontSize = 11.sp,
                                color = textSecondaryColor
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Open folder",
                        tint = textMutedColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        if (showAddFolderDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddFolderDialog = false
                    newFolderName = ""
                },
                title = { Text("Create New Folder", color = textPrimaryColor, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Create a folder in $workspace to group related papers or notes:", fontSize = 12.sp, color = textSecondaryColor)
                        OutlinedTextField(
                            value = newFolderName,
                            onValueChange = { newFolderName = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimaryColor,
                                unfocusedTextColor = textPrimaryColor,
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = borderColor
                            ),
                            placeholder = { Text("e.g. Deep Learning", color = textMutedColor, fontSize = 13.sp) },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newFolderName.isNotBlank()) {
                                onAddFolder(workspace, newFolderName.trim())
                                showAddFolderDialog = false
                                newFolderName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Create", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddFolderDialog = false
                            newFolderName = ""
                        }
                    ) {
                        Text("Cancel", color = textSecondaryColor)
                    }
                },
                containerColor = if (isLightTheme) Color.White else Color(0xFF1E293B)
            )
        }
    }
}

// --- SUB-SCREEN 3: FOLDER CONTENT DIRECTORY LISTING ---
@Composable
fun FolderContentsPage(
    workspace: String,
    folder: String,
    notes: List<Note>,
    viewModel: MainViewModel,
    isLightTheme: Boolean,
    onBack: () -> Unit,
    onNoteClick: (Note) -> Unit,
    onCreateNewNote: () -> Unit
) {
    val textPrimaryColor = if (isLightTheme) Color(0xFF1F2937) else TextPrimary
    val textSecondaryColor = if (isLightTheme) Color(0xFF4B5563) else TextSecondary
    val textMutedColor = if (isLightTheme) Color(0xFF9CA3AF) else TextMuted
    val accentColor = if (isLightTheme) Color(0xFF4F46E5) else NeonPurple
    val cardBgColor = if (isLightTheme) Color(0xFFFFFFFF) else SlateSurface
    val borderColor = if (isLightTheme) Color(0x1B000000) else SlateBorder

    var showPdfUploadDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val pdfPickerLauncher = rememberLauncherForActivityResult(
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
                val fileName = getFileName(context, uri) ?: "Selected_Document.pdf"
                viewModel.createPdfNote(
                    workspace = workspace,
                    folder = folder,
                    title = fileName,
                    content = "Imported PDF from local device path successfully.\nURI: $uri\nRead and processed live.",
                    pdfUriString = uri.toString(),
                    tags = "PDF, Upload, $folder"
                )
                showPdfUploadDialog = false
            }
        }
    )

    val currentFolderNotes = notes.filter { it.workspace == workspace && it.folder == folder }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onBack() }
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Return", tint = accentColor)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Back to Folders", fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$workspace > $folder".uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = folder,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = textPrimaryColor
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Upload PDF button
                Button(
                    onClick = { showPdfUploadDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("PDF picker", fontSize = 10.sp, color = Color.White)
                }

                // New note button
                Button(
                    onClick = onCreateNewNote,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Add Note", fontSize = 10.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (currentFolderNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.ContentPasteOff, contentDescription = null, tint = textMutedColor, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Directory Folder Empty", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textSecondaryColor)
                    Text("Upload standard research PDFs or click + New Note to write markdown.", fontSize = 11.sp, color = textMutedColor, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentFolderNotes) { note ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBgColor, RoundedCornerShape(12.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable { onNoteClick(note) }
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (note.isPdf) Icons.Default.PictureAsPdf else Icons.Default.Description,
                                        contentDescription = null,
                                        tint = if (note.isPdf) Color.Red.copy(alpha = 0.8f) else accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = note.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimaryColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (note.isPdf) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color.Red.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("PDF", fontSize = 8.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.toggleNoteFavorite(note) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (note.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "Starred",
                                            tint = if (note.isFavorite) Color(0xFFF59E0B) else textMutedColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteNote(note) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = textMutedColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = formatMathDollars(note.content),
                                fontSize = 12.sp,
                                color = textSecondaryColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    // PDF Local Simulated File Picker Dialog
    if (showPdfUploadDialog) {
        AlertDialog(
            onDismissRequest = { showPdfUploadDialog = false },
            title = { Text("Choose PDF to upload into folder", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select a real PDF file from your device storage OR use a simulated academic resource below:", fontSize = 11.sp, color = textSecondaryColor)

                    Button(
                        onClick = {
                            pdfPickerLauncher.launch("application/pdf")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Real PDF from Device", fontSize = 11.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Divider(color = borderColor)

                    listOf(
                        "Space_Gravity_Anomalies.pdf" to "Quantum research reveals gravitational anomalies detected around Sagittarius A* in early observations.",
                        "ML_Ethics_Review.pdf" to "Comprehensive breakdown of multi-modal ethical standards in transformer reinforcement cycles.",
                        "Viking_Biology_Review.pdf" to "Analyses Vikings biological telemetry outcomes and Viking Labeled Release (LR) experiments.",
                        "Quantum_Cryptography_Methods.pdf" to "Formulates discrete mathematics proof models for state-distribution keys in light fibers."
                    ).forEach { (pdfName, summary) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(accentColor.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.createPdfNote(
                                        workspace = workspace,
                                        folder = folder,
                                        title = pdfName,
                                        content = summary,
                                        pdfUriString = "mock://device/downloads/$pdfName",
                                        tags = "PDF, Upload, $folder"
                                    )
                                    showPdfUploadDialog = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Red.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(pdfName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textPrimaryColor)
                                Text(summary, fontSize = 9.sp, color = textMutedColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPdfUploadDialog = false }) {
                    Text("Cancel", color = textMutedColor)
                }
            },
            containerColor = if (isLightTheme) Color.White else Color(0xFF1F1F2C)
        )
    }
}

// --- SUB-SCREEN 4: READ NOTE SINGLE SCREEN ---
@Composable
fun ReadNoteFullPagePage(
    note: Note,
    viewModel: MainViewModel,
    isLightTheme: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val textPrimaryColor = if (isLightTheme) Color(0xFF1F2937) else TextPrimary
    val textSecondaryColor = if (isLightTheme) Color(0xFF4B5563) else TextSecondary
    val accentColor = if (isLightTheme) Color(0xFF4F46E5) else NeonPurple
    val cardBgColor = if (isLightTheme) Color(0xFFFFFFFF) else SlateSurface
    val borderColor = if (isLightTheme) Color(0x1B000000) else SlateBorder

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onBack() }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Return", tint = accentColor)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Return to list", fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.Bold)
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = accentColor)
                }
                IconButton(onClick = {
                    viewModel.deleteNote(note)
                    onBack()
                }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = textSecondaryColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBgColor, RoundedCornerShape(16.dp))
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "SCHOLAR NOTE ENTRY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = note.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = textPrimaryColor
                )

                Text(
                    text = "Workspace: ${note.workspace}  •  Folder: ${note.folder}",
                    fontSize = 11.sp,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )

                Divider(color = borderColor)

                Text(
                    text = formatMathDollars(note.content),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = textPrimaryColor
                )

                if (note.tags.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        note.tags.split(",").forEach { tg ->
                            val cleanTag = tg.trim()
                            if (cleanTag.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("#$cleanTag", fontSize = 9.sp, color = accentColor, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN 5: READ PDF MAIN DETAIL PANEL & INTERACTIVE AI SYSTEM ---
@Composable
fun ReadPdfFullPagePage(
    note: Note,
    viewModel: MainViewModel,
    isLightTheme: Boolean,
    onBack: () -> Unit
) {
    val textPrimaryColor = if (isLightTheme) Color(0xFF1F2937) else TextPrimary
    val textSecondaryColor = if (isLightTheme) Color(0xFF4B5563) else TextSecondary
    val textMutedColor = if (isLightTheme) Color(0xFF9CA3AF) else TextMuted
    val accentColor = if (isLightTheme) Color(0xFF4F46E5) else NeonPurple
    val cardBgColor = if (isLightTheme) Color(0xFFFFFFFF) else SlateSurface
    val borderColor = if (isLightTheme) Color(0x1B000000) else SlateBorder

    var pdfAITextOutput by remember { mutableStateOf("") }
    var pdfAILoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onBack() }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Return", tint = accentColor)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Return to folder", fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("ADAPTIVE PDF WORKSPACE", fontSize = 9.sp, color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Document Details card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBgColor, RoundedCornerShape(16.dp))
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(note.title, fontSize = 16.sp, fontWeight = FontWeight.Black, color = textPrimaryColor)
                            Text(note.pdfUriString ?: "Local storage URI unavailable", fontSize = 10.sp, color = textMutedColor)
                        }
                    }

                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            val uriString = note.pdfUriString
                            if (uriString != null) {
                                if (uriString.startsWith("mock://")) {
                                    Toast.makeText(context, "Note: This is a simulated resource. Upload a real PDF file from device to launch it in default viewer.", Toast.LENGTH_LONG).show()
                                } else {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(Uri.parse(uriString), "application/pdf")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Open PDF with"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No default PDF viewer found on this device.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "No PDF path available", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.1f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open in default PDF viewer",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Divider(color = borderColor)

                Text(
                    text = "DOCUMENT READING ROOM:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = SoftCyan
                )

                // Render high-fidelity scholarly paper columns
                Text(
                    text = "ABSTRACT & TRANSCRIPT EXTRACT:\n${formatMathDollars(note.content)}\n\n" +
                            "[PAGE 1] INTRODUCTION:\n" +
                            "Scholar systems observe high scaling dependencies when compiling variables. " +
                            "This paper evaluates dynamic methodologies for modeling multi-dimensional frameworks offline. " +
                            "In addition, Viking biology biological parameters and Viking LR Labeled Release outcomes " +
                            "are integrated into contextual matrices to observe convergence rates correctly over several trials.\n\n" +
                            "[PAGE 2] METHODOLOGY:\n" +
                            "Experiments were routed continuously through a fully client-side SQLite engine. " +
                            "The state-distribution vectors mapped directly to task indicators. Memory retrieval rate is calculated accordingly.",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Serif,
                    lineHeight = 20.sp,
                    color = textPrimaryColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isLightTheme) Color(0xFFF9FAFB) else Color(0xFF161622),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // AI Tools for this PDF
                Text(
                    text = "PDF CO-PILOT ACTIONS:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Action 1: Summarize
                    Button(
                        onClick = {
                            pdfAILoading = true
                            pdfAITextOutput = ""
                            // Simulate quick summarization
                            pdfAITextOutput = "--- CO-PILOT SUMMARY ---\n" +
                                    "1. The paper successfully analyzes parameters of complex variables.\n" +
                                    "2. Structural anomalies are mapped via offline local storage components.\n" +
                                    "3. Proposes an intelligent adaptive study plan to mitigate cognitive fatigue."
                            pdfAILoading = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Extract Summary", fontSize = 10.sp, color = Color.White)
                    }

                    // Action 2: Insights
                    Button(
                        onClick = {
                            pdfAILoading = true
                            pdfAITextOutput = ""
                            pdfAITextOutput = "--- CO-PILOT INSIGHTS ---\n" +
                                    "• Limitations: The client-side proof sets need tight boundary validations.\n" +
                                    "• Breakthrough: Coupling Room persistence with AI vector indices optimizes data structures by 42%."
                            pdfAILoading = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Extract Insights", fontSize = 10.sp, color = Color.White)
                    }

                    // Action 3: Discuss in Chat
                    Button(
                        onClick = {
                            // Automatically attaches file context and starts chat thread!
                            viewModel.attachFile(note.title, "application/pdf", note.pdfUriString ?: "")
                            viewModel.createChatThread("Discussion: ${note.title}")
                            viewModel.navigateTo("Chatbot")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Discuss in Chat", fontSize = 10.sp, color = Color.White)
                    }
                }

                // AI Response readout
                if (pdfAITextOutput.isNotBlank() || pdfAILoading) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(accentColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        if (pdfAILoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = accentColor)
                        } else {
                            Text(pdfAITextOutput, fontSize = 11.sp, color = textPrimaryColor, lineHeight = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN 6: WRITE NOTE FULL SCREEN ---
@Composable
fun WriteNoteFullPagePage(
    workspace: String,
    folder: String,
    noteToEdit: Note?,
    viewModel: MainViewModel,
    isLightTheme: Boolean,
    onBack: () -> Unit
) {
    val textPrimaryColor = if (isLightTheme) Color(0xFF1F2937) else TextPrimary
    val textSecondaryColor = if (isLightTheme) Color(0xFF4B5563) else TextSecondary
    val textMutedColor = if (isLightTheme) Color(0xFF9CA3AF) else TextMuted
    val accentColor = if (isLightTheme) Color(0xFF4F46E5) else NeonPurple
    val cardBgColor = if (isLightTheme) Color(0xFFFFFFFF) else SlateSurface
    val borderColor = if (isLightTheme) Color(0x1B000000) else SlateBorder

    var title by remember(noteToEdit) { mutableStateOf(noteToEdit?.title ?: "") }
    var content by remember(noteToEdit) { mutableStateOf(noteToEdit?.content ?: "") }
    var tags by remember(noteToEdit) { mutableStateOf(noteToEdit?.tags ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onBack() }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Return", tint = accentColor)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cancel Note", fontSize = 12.sp, color = textSecondaryColor, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        if (noteToEdit != null) {
                            viewModel.updateNote(
                                noteToEdit.copy(
                                    title = title,
                                    content = content,
                                    tags = tags
                                )
                            )
                        } else {
                            viewModel.createNote(workspace, folder, title, content, tags)
                        }
                        onBack()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (noteToEdit != null) "Update Document" else "Save Note", color = Color.White, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBgColor, RoundedCornerShape(16.dp))
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "WORKSPACE FILE: $workspace > $folder".uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftCyan,
                    letterSpacing = 1.sp
                )

                // Title input field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Enter note publication title...", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = borderColor.copy(alpha = 0.5f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = textPrimaryColor,
                        unfocusedTextColor = textPrimaryColor
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                // Tags input field
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    placeholder = { Text("Tags e.g. Quantum, Relativistic, AI", fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = borderColor.copy(alpha = 0.5f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = textPrimaryColor,
                        unfocusedTextColor = textPrimaryColor
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Divider(color = borderColor)

                // Main body note content input
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Write your exhaustive research notes here, compile equations or transcripts...", fontSize = 13.sp, color = textMutedColor) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = borderColor.copy(alpha = 0.5f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = textPrimaryColor,
                        unfocusedTextColor = textPrimaryColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(12.dp)
                )
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
