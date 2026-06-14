package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.repository.*
import com.example.services.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull

sealed class ResearchUiState {
    object Idle : ResearchUiState()
    object Loading : ResearchUiState()
    data class Success(val report: ResearchReport) : ResearchUiState()
    data class Error(val message: String) : ResearchUiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    // Repositories
    val researchRepository = ResearchRepository(db)
    val notesRepository = NotesRepository(db)
    val plannerRepository = PlannerRepository(db)
    val chatbotRepository = ChatbotRepository(db)
    val userRepository = UserRepository(db)

    // Services
    private val researchService: ResearchService = GeminiResearchService()
    private val chatbotService: ChatbotService = GeminiChatbotService()
    private val notesService: NotesService = NotesServiceImpl()
    private val plannerService: PlannerService = PlannerServiceImpl()

    // UI Navigation State
    private val _currentTab = MutableStateFlow("Dashboard")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // 0. Authentication Session States
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun navigateTo(tab: String) {
        _currentTab.value = tab
    }

    // 1. Core Reactive Flows from Database
    val allProjects: StateFlow<List<ResearchProject>> = researchRepository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReports: StateFlow<List<SavedReport>> = researchRepository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPapers: StateFlow<List<ResearchPaper>> = researchRepository.allPapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<Note>> = notesRepository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<PlannerTask>> = plannerRepository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allThreads: StateFlow<List<ChatThread>> = chatbotRepository.allThreads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic Workspaces and Folders Lists and Actions
    private val defaultWorkspaces = listOf("Artificial Intelligence", "Physics", "General Studies")
    private val defaultFoldersMap = mapOf(
        "Artificial Intelligence" to listOf("LLMs", "Prompt Engineering", "AI Ethics"),
        "Physics" to listOf("Quantum Mechanics", "Relativity"),
        "General Studies" to listOf("Bibliography", "Personal Thoughts")
    )

    private val _customWorkspaces = MutableStateFlow<List<String>>(emptyList())
    val customWorkspaces: StateFlow<List<String>> = _customWorkspaces.asStateFlow()

    private val _customFoldersMap = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val customFoldersMap: StateFlow<Map<String, List<String>>> = _customFoldersMap.asStateFlow()

    val workspaces: StateFlow<List<String>> = combine(allNotes, _customWorkspaces) { notes, custom ->
        val fromNotes = notes.map { it.workspace }.filter { it.isNotBlank() }
        (defaultWorkspaces + fromNotes + custom).distinct()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultWorkspaces)

    val foldersMap: StateFlow<Map<String, List<String>>> = combine(allNotes, _customFoldersMap) { notes, customMap ->
        val result = mutableMapOf<String, List<String>>()
        
        defaultFoldersMap.forEach { (ws, folders) ->
            result[ws] = folders
        }
        
        notes.forEach { note ->
            if (note.workspace.isNotBlank() && note.folder.isNotBlank()) {
                val current = result[note.workspace] ?: emptyList()
                if (note.folder !in current) {
                    result[note.workspace] = current + note.folder
                }
            }
        }
        
        customMap.forEach { (ws, folders) ->
            val current = result[ws] ?: emptyList()
            result[ws] = (current + folders).distinct()
        }

        val allWs = (defaultWorkspaces + customMap.keys + _customWorkspaces.value + notes.map { it.workspace }).distinct()
        allWs.forEach { ws ->
            if (ws !in result) {
                result[ws] = emptyList()
            }
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultFoldersMap)

    fun addWorkspace(workspace: String) {
        if (workspace.isNotBlank() && workspace !in _customWorkspaces.value) {
            _customWorkspaces.value = _customWorkspaces.value + workspace
        }
    }

    fun addFolder(workspace: String, folder: String) {
        if (folder.isNotBlank()) {
            val currentFolders = _customFoldersMap.value[workspace] ?: emptyList()
            if (folder !in currentFolders) {
                val updated = _customFoldersMap.value.toMutableMap()
                updated[workspace] = (currentFolders + folder).distinct()
                _customFoldersMap.value = updated
            }
        }
    }

    // 2. Chat Selection and Messaging Flows
    private val _selectedThreadId = MutableStateFlow<String?>(null)
    val selectedThreadId: StateFlow<String?> = _selectedThreadId.asStateFlow()

    val chatMessages: StateFlow<List<ChatMessage>> = _selectedThreadId
        .flatMapLatest { threadId ->
            if (threadId != null) {
                chatbotRepository.getMessagesForThread(threadId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. User Profile State (Stored in memory and simple reactive setters)
    private val _userName = MutableStateFlow("Dr. Evelyn Vance")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userBio = MutableStateFlow("Cognitive Computing & Astro-physics researcher. Searching for macro patterns in complex systemic networks.")
    val userBio: StateFlow<String> = _userBio.asStateFlow()

    private val _userInterests = MutableStateFlow("Artificial Intelligence, Quantum Mechanics, Astrobiology, Philosophy")
    val userInterests: StateFlow<String> = _userInterests.asStateFlow()

    fun updateProfile(name: String, bio: String, interests: String) {
        _userName.value = name
        _userBio.value = bio
        _userInterests.value = interests
        viewModelScope.launch {
            val current = _currentUser.value
            if (current != null) {
                val updatedUser = current.copy(bio = bio, interests = interests)
                userRepository.updateUserProfile(current.username, bio, interests)
                _currentUser.value = updatedUser
            }
        }
    }

    // Auth operations
    fun login(username: String, passwordRaw: String) {
        _authError.value = null
        if (username.isBlank() || passwordRaw.isBlank()) {
            _authError.value = "Username and password cannot be blank."
            return
        }
        viewModelScope.launch {
            val user = userRepository.getUserByUsername(username.trim())
            if (user != null && user.passwordHash == passwordRaw) {
                _currentUser.value = user
                _userName.value = user.username
                _userBio.value = user.bio
                _userInterests.value = user.interests
                _authError.value = null
            } else {
                _authError.value = "Invalid username or password."
            }
        }
    }

    fun loginByPhone(phoneNumber: String, phonePasswordRaw: String) {
        _authError.value = null
        if (phoneNumber.isBlank() || phonePasswordRaw.isBlank()) {
            _authError.value = "Phone number and phone password cannot be blank."
            return
        }
        viewModelScope.launch {
            val user = userRepository.getUserByPhone(phoneNumber.trim())
            if (user != null && user.phonePassword == phonePasswordRaw.trim()) {
                _currentUser.value = user
                _userName.value = user.username
                _userBio.value = user.bio
                _userInterests.value = user.interests
                _authError.value = null
            } else {
                _authError.value = "Invalid phone credentials, or user not found."
            }
        }
    }

    fun register(
        username: String,
        passwordRaw: String,
        bio: String,
        interests: String,
        phoneNumber: String = "",
        phonePasswordRaw: String = ""
    ) {
        _authError.value = null
        if (username.isBlank() || passwordRaw.isBlank()) {
            _authError.value = "Username and password cannot be blank."
            return
        }
        if (username.trim().length < 3) {
            _authError.value = "Username must be at least 3 characters."
            return
        }
        if (passwordRaw.length < 4) {
            _authError.value = "Password must be at least 4 characters."
            return
        }
        viewModelScope.launch {
            val existing = userRepository.getUserByUsername(username.trim())
            if (existing != null) {
                _authError.value = "Username check failed: account already exists."
                return@launch
            }
            if (phoneNumber.isNotBlank()) {
                val existingPhone = userRepository.getUserByPhone(phoneNumber.trim())
                if (existingPhone != null) {
                    _authError.value = "Phone number is already associated with another scholar."
                    return@launch
                }
            }
            val newUser = User(
                username = username.trim(),
                passwordHash = passwordRaw,
                bio = bio.ifBlank { "Scholarly Researcher" },
                interests = interests.ifBlank { "Artificial Intelligence, Quantum Mechanics" },
                phoneNumber = phoneNumber.trim(),
                phonePassword = phonePasswordRaw.trim()
            )
            userRepository.insertUser(newUser)
            _currentUser.value = newUser
            _userName.value = newUser.username
            _userBio.value = newUser.bio
            _userInterests.value = newUser.interests
            _authError.value = null
        }
    }

    fun logout() {
        _currentUser.value = null
        _authError.value = null
        _userName.value = ""
        _userBio.value = ""
        _userInterests.value = ""
    }

    // 4. Research Assistant UI State
    private val _researchUiState = MutableStateFlow<ResearchUiState>(ResearchUiState.Idle)
    val researchUiState: StateFlow<ResearchUiState> = _researchUiState.asStateFlow()

    private val _selectedProjectForResearch = MutableStateFlow<ResearchProject?>(null)
    val selectedProjectForResearch: StateFlow<ResearchProject?> = _selectedProjectForResearch.asStateFlow()

    fun selectProjectForResearch(project: ResearchProject?) {
        _selectedProjectForResearch.value = project
    }

    // Performs remote or local-mock AI Generation
    fun performResearch(topic: String) {
        if (topic.isBlank()) return
        viewModelScope.launch {
            _researchUiState.value = ResearchUiState.Loading
            try {
                val report = researchService.generateDeepResearch(topic)
                _researchUiState.value = ResearchUiState.Success(report)

                // AUTO-SAVE report if a project is selected
                val activeProj = _selectedProjectForResearch.value
                if (activeProj != null) {
                    researchRepository.insertReport(
                        SavedReport(
                            projectId = activeProj.id,
                            topic = report.topic,
                            overview = report.overview,
                            detailedAnalysis = report.detailedAnalysis,
                            keyInsights = report.keyInsights,
                            importantConcepts = report.importantConcepts,
                            furtherReading = report.furtherReading,
                            conclusion = report.conclusion
                        )
                    )
                }
            } catch (e: Exception) {
                _researchUiState.value = ResearchUiState.Error(e.message ?: "Failed to generate AI report.")
            }
        }
    }

    fun clearResearchState() {
        _researchUiState.value = ResearchUiState.Idle
    }

    // 5. Save report to Notes
    fun saveReportToNotes(report: SavedReport, workspace: String, folder: String) {
        viewModelScope.launch {
            val note = notesService.convertReportToNote(report, workspace, folder)
            notesRepository.insertNote(note)
        }
    }

    fun saveResearchReportToNotes(report: ResearchReport, workspace: String, folder: String) {
        viewModelScope.launch {
            val savedReportDummy = SavedReport(
                projectId = 0,
                topic = report.topic,
                overview = report.overview,
                detailedAnalysis = report.detailedAnalysis,
                keyInsights = report.keyInsights,
                importantConcepts = report.importantConcepts,
                furtherReading = report.furtherReading,
                conclusion = report.conclusion
            )
            val note = notesService.convertReportToNote(savedReportDummy, workspace, folder)
            notesRepository.insertNote(note)
        }
    }

    // 6. Project Actions
    fun createProject(name: String, description: String, category: String) {
        viewModelScope.launch {
            researchRepository.insertProject(
                ResearchProject(name = name, description = description, category = category)
            )
        }
    }

    fun deleteProject(project: ResearchProject) {
        viewModelScope.launch {
            researchRepository.deleteProject(project)
        }
    }

    // 7. Research Paper Actions
    fun createPaper(title: String, authors: String, category: String, publishDate: String, notes: String = "") {
        viewModelScope.launch {
            researchRepository.insertPaper(
                ResearchPaper(
                    title = title,
                    authors = authors,
                    category = category,
                    publishDate = publishDate,
                    personalNotes = notes
                )
            )
        }
    }

    fun togglePaperBookmark(paper: ResearchPaper) {
        viewModelScope.launch {
            researchRepository.updatePaper(paper.copy(isBookmarked = !paper.isBookmarked))
        }
    }

    fun deletePaper(paper: ResearchPaper) {
        viewModelScope.launch {
            researchRepository.deletePaper(paper)
        }
    }

    // 8. Note Actions
    fun createNote(workspace: String, folder: String, title: String, content: String, tags: String = "") {
        viewModelScope.launch {
            notesRepository.insertNote(
                Note(workspace = workspace, folder = folder, title = title, content = content, tags = tags)
            )
        }
    }

    fun createPdfNote(workspace: String, folder: String, title: String, content: String, pdfUriString: String, tags: String = "") {
        viewModelScope.launch {
            notesRepository.insertNote(
                Note(
                    workspace = workspace,
                    folder = folder,
                    title = title,
                    content = content,
                    tags = tags,
                    isPdf = true,
                    pdfUriString = pdfUriString,
                    fileMimeType = "application/pdf"
                )
            )
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            notesRepository.updateNote(note.copy(lastModified = System.currentTimeMillis()))
        }
    }

    fun toggleNoteFavorite(note: Note) {
        viewModelScope.launch {
            notesRepository.updateNote(note.copy(isFavorite = !note.isFavorite))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            notesRepository.deleteNote(note)
        }
    }

    // 9. Planner Task Actions
    fun createTask(name: String, priority: String, type: String, targetDaysOffset: Int = 1) {
        viewModelScope.launch {
            val targetTime = System.currentTimeMillis() + (targetDaysOffset * 24 * 3600 * 1000L)
            plannerRepository.insertTask(
                PlannerTask(taskName = name, priority = priority, type = type, targetDate = targetTime)
            )
        }
    }

    fun toggleTaskCompletion(task: PlannerTask) {
        viewModelScope.launch {
            plannerRepository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: PlannerTask) {
        viewModelScope.launch {
            plannerRepository.deleteTask(task)
        }
    }

    // 10. Custom Chatbot Actions
    fun selectThread(threadId: String?) {
        _selectedThreadId.value = threadId
    }

    fun createChatThread(title: String) {
        viewModelScope.launch {
            val newThread = ChatThread(title = title)
            chatbotRepository.insertThread(newThread)
            _selectedThreadId.value = newThread.id
        }
    }

    fun toggleThreadPin(thread: ChatThread) {
        viewModelScope.launch {
            chatbotRepository.updateThread(thread.copy(isPinned = !thread.isPinned))
        }
    }

    fun deleteThread(thread: ChatThread) {
        viewModelScope.launch {
            if (_selectedThreadId.value == thread.id) {
                _selectedThreadId.value = null
            }
            chatbotRepository.deleteThread(thread)
        }
    }

    fun sendChatMessage(text: String) {
        val threadId = _selectedThreadId.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            // Save User message
            val userMsg = ChatMessage(threadId = threadId, sender = "USER", text = text)
            chatbotRepository.insertMessage(userMsg)

            // Update thread last active
            allThreads.value.find { it.id == threadId }?.let { thread ->
                chatbotRepository.updateThread(thread.copy(lastActive = System.currentTimeMillis()))
            }

            // Generate AI Chat Response
            val currentHistory = chatMessages.value
            
            // Build special context if Attachment exists
            val attachment = _chatAttachment.value
            val enrichedPrompt = if (attachment != null) {
                "[Attached File: ${attachment.name} (${attachment.type})]\n" +
                "Extracted contents of attached file:\n" +
                "${attachment.simulatedContent}\n\n" +
                "User's Question/Instruction:\n$text"
            } else {
                text
            }

            // Compute dynamic instructions representing styles and AssistantMode
            val systemInstruction = getSystemInstructionForChat()
            val replyText = chatbotService.getChatResponse(currentHistory, enrichedPrompt, systemInstruction)

            // Extract study plan proposal if generated
            val plan = extractStudyPlanProposal(replyText)
            if (plan != null) {
                _latestStudyPlanProposal.value = plan
            }

            // Clear active attachment after query
            _chatAttachment.value = null

            // Save AI reply
            val aiMsg = ChatMessage(threadId = threadId, sender = "AI", text = replyText)
            chatbotRepository.insertMessage(aiMsg)
        }
    }

    private fun getSystemInstructionForChat(): String {
        val style = _activePromptStyle.value
        val assistantMode = _isAssistantMode.value

        val base = "You are DeepScholar Chatbot, an expert research partner. You answer academic questions, explain technology, code, or science with rich, structured descriptions. Keep a scholarly, friendly, and analytical tone. " +
                   "Always generate 2-3 possible follow-up questions a researcher might have after reading your response. List them neat and separate at the end under a header called '## DeepScholar Researcher Follow-ups'. " +
                   "At the absolute end of your response, ask the researcher exactly: 'Do you want me to explain these questions or any other question you have in your mind?'"

        val styleText = when (style) {
            "Explain like a 5 year old" -> "\n\nCRITICAL STYLE INSTRUCTION: Explain your response like I am 5 years old, using simple concepts, analogies, and friendly language, but keeping it informative."
            "Summarize text" -> "\n\nCRITICAL STYLE INSTRUCTION: Focus fully on summarizing the provided text, using very concise bullet points and highlighting key takeaways."
            "Generate insights from the uploaded research paper" -> "\n\nCRITICAL STYLE INSTRUCTION: Focus fully on extracting deeply analytical insights, theoretical critiques, limits, and potential future research directions."
            "Research Questions generator" -> "\n\nCRITICAL STYLE INSTRUCTION: Focus on generating a complete comprehensive list of deep, analytical research questions related to the topic, highlighting why they are academically interesting."
            else -> ""
        }

        val assistantModeContext = if (assistantMode) {
            val projects = allProjects.value.joinToString("\n") { "- Project: ${it.name}. Description: ${it.description}. Category: ${it.category}" }
            val tasks = allTasks.value.filter { !it.isCompleted }.joinToString("\n") {
                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(it.targetDate))
                "- Task: ${it.taskName}, Priority: ${it.priority}, Type: ${it.type}, Date: $dateStr"
            }

            "\n\nASSISTANT MODE ACTIVE: You have complete access to the user's active research projects and schedule. " +
            "Here are the active projects:\n$projects\n\nHere is the pending schedule:\n$tasks\n\n" +
            "You should act as their personal assistant, briefly giving details on projects, schedules, or goals when asked. " +
            "IMPORTANT: If the user asks how much time a particular research or topic will take, give a clear estimate (e.g., 'This research on Quantum Mechanics will take approximately 3 weeks'). " +
            "Then, propose a structured Study Plan divided into 3-4 sequential tasks with concrete objectives. " +
            "CRITICAL: If you propose a study plan, you MUST format it as a JSON-block so the UI can parse it and let the user approve & add it to their planner! " +
            "Format the JSON block precisely at the end of your text like this: \n" +
            "```study_plan_proposal\n" +
            "{\n" +
            "  \"topic\": \"Topic Name\",\n" +
            "  \"durationWeeks\": 3,\n" +
            "  \"tasks\": [\"Task 1: Basic concepts\", \"Task 2: Literature review\", \"Task 3: Synthesis\"]\n" +
            "}\n" +
            "```\n" +
            "Make sure the JSON block contains those exact fields!"
        } else ""

        return base + styleText + assistantModeContext
    }

    private fun extractStudyPlanProposal(text: String): StudyPlanProposal? {
        try {
            val startTag = "```study_plan_proposal"
            val endTag = "```"
            val startIdx = text.indexOf(startTag)
            if (startIdx == -1) return null
            val jsonStart = startIdx + startTag.length
            val endIdx = text.indexOf(endTag, jsonStart)
            if (endIdx == -1) return null
            val jsonStr = text.substring(jsonStart, endIdx).trim()

            // Parse simple JSON fields manually so of it fits in one robust block
            val topicQuote = "\"topic\""
            val durationQuote = "\"durationWeeks\""
            val tasksQuote = "\"tasks\""

            val topicIdx = jsonStr.indexOf(topicQuote)
            if (topicIdx == -1) return null
            val topicColon = jsonStr.indexOf(':', topicIdx)
            val topicStartQuote = jsonStr.indexOf('"', topicColon)
            val topicEndQuote = jsonStr.indexOf('"', topicStartQuote + 1)
            val topic = jsonStr.substring(topicStartQuote + 1, topicEndQuote)

            val durationIdx = jsonStr.indexOf(durationQuote)
            val duration = if (durationIdx != -1) {
                val durationColon = jsonStr.indexOf(':', durationIdx)
                val nextCommaStr = jsonStr.indexOf(',', durationColon).let { if (it == -1) jsonStr.indexOf('\n', durationColon) else it }
                jsonStr.substring(durationColon + 1, nextCommaStr).trim().filter { it.isDigit() }.toIntOrNull() ?: 3
            } else {
                3
            }

            val tasksIdx = jsonStr.indexOf(tasksQuote)
            val tasks = mutableListOf<String>()
            if (tasksIdx != -1) {
                val bracketStart = jsonStr.indexOf('[', tasksIdx)
                val bracketEnd = jsonStr.indexOf(']', bracketStart)
                if (bracketStart != -1 && bracketEnd != -1) {
                    val arrayContent = jsonStr.substring(bracketStart + 1, bracketEnd)
                    val parts = arrayContent.split(",")
                    for (part in parts) {
                        val clean = part.trim().trim('"').trim('\'')
                        if (clean.isNotBlank()) {
                            tasks.add(clean)
                        }
                    }
                }
            }

            if (topic.isNotBlank() && tasks.isNotEmpty()) {
                return StudyPlanProposal(topic, duration, tasks)
            }
        } catch (e: Exception) {
            // No-op fallback
        }
        return null
    }

    // 11. Global Search System
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Unified live reactive global filtered search results
    val searchResults: StateFlow<SearchResults> = combine(
        listOf(_searchQuery, allProjects, allNotes, allPapers, allThreads, allTasks)
    ) { array ->
        val query = array[0] as String
        @Suppress("UNCHECKED_CAST")
        val projs = array[1] as List<ResearchProject>
        @Suppress("UNCHECKED_CAST")
        val nts = array[2] as List<Note>
        @Suppress("UNCHECKED_CAST")
        val pprs = array[3] as List<ResearchPaper>
        @Suppress("UNCHECKED_CAST")
        val thrds = array[4] as List<ChatThread>
        @Suppress("UNCHECKED_CAST")
        val tsks = array[5] as List<PlannerTask>

        if (query.trim().length < 2) {
            SearchResults()
        } else {
            val q = query.lowercase()
            SearchResults(
                matchedProjects = projs.filter { it.name.lowercase().contains(q) || it.description.lowercase().contains(q) },
                matchedNotes = nts.filter { it.title.lowercase().contains(q) || it.content.lowercase().contains(q) },
                matchedPapers = pprs.filter { it.title.lowercase().contains(q) || it.authors.lowercase().contains(q) || it.personalNotes.lowercase().contains(q) },
                matchedThreads = thrds.filter { it.title.lowercase().contains(q) },
                matchedTasks = tsks.filter { it.taskName.lowercase().contains(q) }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResults())

    // 12. Dynamic Achievements & Progress Metrics (Calculated dynamically!)
    val plannerStats: StateFlow<PlannerStats> = combine(
        allTasks,
        allNotes,
        allReports
    ) { tsks, nts, rpts ->
        plannerService.calculateStats(tsks, nts, rpts)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PlannerStats(0, 0, 0.0f, 0, 45)
    )

    private val prefs = application.getSharedPreferences("ai_assistant_prefs", android.content.Context.MODE_PRIVATE)

    // Optional Light Mode State
    private val _isLightTheme = MutableStateFlow(prefs.getBoolean("is_light_theme", false))
    val isLightTheme: StateFlow<Boolean> = _isLightTheme.asStateFlow()

    fun toggleTheme() {
        val nextVal = !_isLightTheme.value
        _isLightTheme.value = nextVal
        prefs.edit().putBoolean("is_light_theme", nextVal).apply()
    }

    // Dynamic prompt style states
    private val _activePromptStyle = MutableStateFlow("None")
    val activePromptStyle: StateFlow<String> = _activePromptStyle.asStateFlow()

    fun setPromptStyle(style: String) {
        _activePromptStyle.value = style
    }

    // Assistant Mode states
    private val _isAssistantMode = MutableStateFlow(false)
    val isAssistantMode: StateFlow<Boolean> = _isAssistantMode.asStateFlow()

    fun toggleAssistantMode() {
        _isAssistantMode.value = !_isAssistantMode.value
    }

    // Attached PDF/Audio states
    data class AttachmentInfo(
        val name: String,
        val type: String,
        val uriString: String,
        val simulatedContent: String
    )

    private val _chatAttachment = MutableStateFlow<AttachmentInfo?>(null)
    val chatAttachment: StateFlow<AttachmentInfo?> = _chatAttachment.asStateFlow()

    fun attachFile(name: String, type: String, uriString: String) {
        val simContent = simulateFileExtraction(name, type)
        _chatAttachment.value = AttachmentInfo(name, type, uriString, simContent)
    }

    fun clearAttachment() {
        _chatAttachment.value = null
    }

    fun simulateFileExtraction(fileName: String, mimeType: String): String {
        val cleanName = fileName.substringBeforeLast(".")
        return when {
            mimeType.contains("pdf") || fileName.endsWith(".pdf") -> {
                "--- EXTRACTED STUDY CONTENT: $fileName ---\n" +
                "Abstract: This research paper, titled '$cleanName', explores critical paradigms, empirical analyses, and architectural reviews in this theoretical sector.\n" +
                "Core Concept & Hypotheses: Argues for dynamic validation framework. Introduces significant multi-dimensional parameters.\n" +
                "Theoretical Breakdown: 1. Core variable modeling achieves optimized performance under continuous cycles. 2. Scaling parameters exhibit steady convergent properties.\n" +
                "Conclusion & Scholarly Recommendations: Recommends direct experimental replication, tighter boundary conditions, and further exploratory synthesis in notes."
            }
            mimeType.contains("audio") || fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".m4a") -> {
                "--- EXTRACTED AUDIO TRANSCRIPTION: $fileName ---\n" +
                "Host (Interviewer): Welcome. Let's delve into the core of your latest research project, '$cleanName'.\n" +
                "Expert (Respondent): Thanks. Our primary conclusion is that integration times are reduced exponentially when modules employ unified structures, showing up to a 42% efficiency increase in the planner.\n" +
                "Host: Brilliant. That is a significant milestone for this research topic."
            }
            else -> {
                "--- EXTRACTED CONTENT: $fileName ---\n" +
                "Simulated metadata and details parsed from '$fileName' for scholar analysis and summarization."
            }
        }
    }

    // Study Plan Proposal State
    data class StudyPlanProposal(
        val topic: String,
        val durationWeeks: Int,
        val tasks: List<String>
    )

    private val _latestStudyPlanProposal = MutableStateFlow<StudyPlanProposal?>(null)
    val latestStudyPlanProposal: StateFlow<StudyPlanProposal?> = _latestStudyPlanProposal.asStateFlow()

    fun approveStudyPlan() {
        val proposal = _latestStudyPlanProposal.value ?: return
        viewModelScope.launch {
            proposal.tasks.forEachIndexed { idx, taskName ->
                createTask(
                    name = "[Plan: ${proposal.topic}] $taskName",
                    priority = "MEDIUM",
                    type = "DAILY",
                    targetDaysOffset = (idx + 1) * 3
                )
            }
            _latestStudyPlanProposal.value = null
        }
    }

    fun dismissStudyPlan() {
        _latestStudyPlanProposal.value = null
    }

    private val _useCustomChatbot = MutableStateFlow(prefs.getBoolean("use_custom_chatbot", false))
    val useCustomChatbot: StateFlow<Boolean> = _useCustomChatbot.asStateFlow()

    private val _customApiUrl = MutableStateFlow(prefs.getString("custom_api_url", "") ?: "")
    val customApiUrl: StateFlow<String> = _customApiUrl.asStateFlow()

    private val _customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _customModelName = MutableStateFlow(prefs.getString("custom_model_name", "gemini-3.5-flash") ?: "gemini-3.5-flash")
    val customModelName: StateFlow<String> = _customModelName.asStateFlow()

    private val _customSystemPrompt = MutableStateFlow(prefs.getString("custom_system_prompt", "You are my AI research partner. Answer with structured and deep insights based on search context.") ?: "You are my AI research partner. Answer with structured and deep insights based on search context.")
    val customSystemPrompt: StateFlow<String> = _customSystemPrompt.asStateFlow()

    private val _assistantChatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                threadId = "dashboard_assistant",
                sender = "AI",
                text = "Hello! I am your custom Assistant. Tap the settings icon above to configure my API or prompt. Write anything to start chatting!",
                timestamp = System.currentTimeMillis()
            )
        )
    )
    val assistantChatMessages: StateFlow<List<ChatMessage>> = _assistantChatMessages.asStateFlow()

    private val _assistantIsSending = MutableStateFlow(false)
    val assistantIsSending: StateFlow<Boolean> = _assistantIsSending.asStateFlow()

    fun updateCustomChatbotSettings(
        useCustom: Boolean,
        apiUrl: String,
        apiKey: String,
        modelName: String,
        systemPrompt: String
    ) {
        _useCustomChatbot.value = useCustom
        _customApiUrl.value = apiUrl
        _customApiKey.value = apiKey
        _customModelName.value = modelName
        _customSystemPrompt.value = systemPrompt

        prefs.edit()
            .putBoolean("use_custom_chatbot", useCustom)
            .putString("custom_api_url", apiUrl)
            .putString("custom_api_key", apiKey)
            .putString("custom_model_name", modelName)
            .putString("custom_system_prompt", systemPrompt)
            .apply()
    }

    fun clearAssistantChat() {
        _assistantChatMessages.value = listOf(
            ChatMessage(
                threadId = "dashboard_assistant",
                sender = "AI",
                text = "Assistant chat history cleared. Ready to start over!",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun sendAssistantMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(
            threadId = "dashboard_assistant",
            sender = "USER",
            text = text,
            timestamp = System.currentTimeMillis()
        )

        _assistantChatMessages.value = _assistantChatMessages.value + userMsg
        _assistantIsSending.value = true

        viewModelScope.launch {
            try {
                val responseText = if (_useCustomChatbot.value && _customApiUrl.value.isNotBlank()) {
                    callCustomChatbotApi(_customApiUrl.value, _customApiKey.value, _customModelName.value, _customSystemPrompt.value, _assistantChatMessages.value)
                } else {
                    callCustomGeminiApi(_customApiKey.value, _customModelName.value, _customSystemPrompt.value, _assistantChatMessages.value)
                }

                val aiMsg = ChatMessage(
                    threadId = "dashboard_assistant",
                    sender = "AI",
                    text = responseText,
                    timestamp = System.currentTimeMillis()
                )
                _assistantChatMessages.value = _assistantChatMessages.value + aiMsg
            } catch (e: Exception) {
                val errMessage = ChatMessage(
                    threadId = "dashboard_assistant",
                    sender = "AI",
                    text = "Connection failed: ${e.localizedMessage ?: "Please confirm endpoint and internet connectivity"}.",
                    timestamp = System.currentTimeMillis()
                )
                _assistantChatMessages.value = _assistantChatMessages.value + errMessage
            } finally {
                _assistantIsSending.value = false
            }
        }
    }

    private suspend fun callCustomChatbotApi(
        url: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        history: List<ChatMessage>
    ): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val messagesJson = StringBuilder("[")
        messagesJson.append("{\"role\":\"system\",\"content\":\"${escapeJson(systemPrompt)}\"}")

        val relevant = history.drop(1).takeLast(10)
        for (msg in relevant) {
            val role = if (msg.sender == "USER") "user" else "assistant"
            messagesJson.append(",{\"role\":\"$role\",\"content\":\"${escapeJson(msg.text)}\"}")
        }
        messagesJson.append("]")

        val payload = """
            {
              "model": "${escapeJson(model)}",
              "messages": $messagesJson,
              "temperature": 0.7
            }
        """.trimIndent()

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = okhttp3.RequestBody.Companion.create(mediaType, payload)

        val reqBuilder = okhttp3.Request.Builder()
            .url(url)
            .post(body)

        if (apiKey.isNotBlank()) {
            reqBuilder.addHeader("Authorization", "Bearer $apiKey")
        }

        val request = reqBuilder.build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                throw Exception("HTTP ${response.code}: $errBody")
            }
            val responseBody = response.body?.string() ?: throw Exception("Empty response from custom API.")
            parseContentFromOpenAiJson(responseBody)
        }
    }

    private suspend fun callCustomGeminiApi(
        customKey: String,
        modelName: String,
        systemPrompt: String,
        history: List<ChatMessage>
    ): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val finalKey = if (customKey.isNotBlank()) customKey else com.example.BuildConfig.GEMINI_API_KEY
        val isDummy = finalKey.isBlank() || finalKey == "MY_GEMINI_API_KEY" || finalKey.length < 10

        if (isDummy) {
            // Friendly simulated conversational chatbot response incorporating user settings
            val lcPrompt = (history.lastOrNull { it.sender == "USER" }?.text ?: "").lowercase()
            val suffix = "\n\n*(Notice: Assistant demo fallback since a live API Key is not set)*"
            
            return@withContext when {
                lcPrompt.contains("hello") || lcPrompt.contains("hi") -> {
                    "Greetings! Currently answering as model `$modelName` guided by prompt: \"$systemPrompt\". Let's investigate together!$suffix"
                }
                lcPrompt.contains("help") || lcPrompt.contains("clear") -> {
                    "I am ready. Your system prompt instructs me as: \"$systemPrompt\".$suffix"
                }
                else -> {
                    "Under system instruction of \"$systemPrompt\", I received your message: \"${history.lastOrNull { it.sender == "USER" }?.text ?: ""}\".\n\nI am ready to assist you dynamically once you set your Gemini API Key or a custom API url in the settings!$suffix"
                }
            }
        }

        try {
            val modelToUse = if (modelName.isNotBlank()) modelName else "gemini-3.5-flash"
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelToUse:generateContent?key=$finalKey"

            val contentsJson = StringBuilder("[")
            var count = 0
            val relevant = history.drop(1).takeLast(20)
            for (msg in relevant) {
                if (count > 0) contentsJson.append(",")
                val role = if (msg.sender == "USER") "user" else "model"
                contentsJson.append("{\"role\":\"$role\",\"parts\":[{\"text\":\"${escapeJson(msg.text)}\"}]}")
                count++
            }
            contentsJson.append("]")

            val systemInstructionJson = "{\"parts\":[{\"text\":\"${escapeJson(systemPrompt)}\"}]}"

            val payload = """
                {
                  "contents": $contentsJson,
                  "systemInstruction": $systemInstructionJson
                }
            """.trimIndent()

            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = okhttp3.RequestBody.Companion.create(mediaType, payload)
            val requestBody = okhttp3.Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(requestBody).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    throw Exception("HTTP ${response.code}: $errBody")
                }
                val rawResponse = response.body?.string() ?: throw Exception("Empty response from Gemini API.")
                extractTextFromGeminiJson(rawResponse)
            }
        } catch (e: Exception) {
            "Failed calling Gemini API: ${e.localizedMessage ?: "Unknown connection issue"}"
        }
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun extractTextFromGeminiJson(json: String): String {
        try {
            val searchStr = "\"text\""
            val index = json.indexOf(searchStr)
            if (index == -1) return json
            return extractJsonStringValue(json, index + searchStr.length)
        } catch (e: Exception) {
            return json
        }
    }

    private fun parseContentFromOpenAiJson(json: String): String {
        try {
            val searchStr = "\"content\""
            val index = json.indexOf(searchStr)
            if (index == -1) {
                if (json.contains("\"text\"")) {
                    val textIndex = json.indexOf("\"text\"")
                    return extractJsonStringValue(json, textIndex + "\"text\"".length)
                }
                return json
            }
            return extractJsonStringValue(json, index + searchStr.length)
        } catch (e: Exception) {
            return json
        }
    }

    private fun extractJsonStringValue(json: String, startSearchFrom: Int): String {
        val colonIdx = json.indexOf(':', startSearchFrom)
        if (colonIdx == -1) return json
        val openQuoteIdx = json.indexOf('"', colonIdx)
        if (openQuoteIdx == -1) return json
        val sb = StringBuilder()
        var i = openQuoteIdx + 1
        var escaped = false
        while (i < json.length) {
            val c = json[i]
            if (escaped) {
                when (c) {
                    'n' -> sb.append('\n')
                    't' -> sb.append('\t')
                    'r' -> sb.append('\r')
                    '\\' -> sb.append('\\')
                    '"' -> sb.append('"')
                    else -> sb.append(c)
                }
                escaped = false
            } else if (c == '\\') {
                escaped = true
            } else if (c == '"') {
                break
            } else {
                sb.append(c)
            }
            i++
        }
        return sb.toString()
    }

    init {
        // Run pre-population on thread pool or coroutine scope
        viewModelScope.launch {
            DatabaseInitializer.populateIfEmpty(db)
            // Select first thread if any
            val threads = chatbotRepository.allThreads.first()
            if (threads.isNotEmpty() && _selectedThreadId.value == null) {
                _selectedThreadId.value = threads.first().id
            }
        }
    }
}

data class SearchResults(
    val matchedProjects: List<ResearchProject> = emptyList(),
    val matchedNotes: List<Note> = emptyList(),
    val matchedPapers: List<ResearchPaper> = emptyList(),
    val matchedThreads: List<ChatThread> = emptyList(),
    val matchedTasks: List<PlannerTask> = emptyList()
) {
    fun isEmpty(): Boolean = matchedProjects.isEmpty() &&
            matchedNotes.isEmpty() &&
            matchedPapers.isEmpty() &&
            matchedThreads.isEmpty() &&
            matchedTasks.isEmpty()
}
