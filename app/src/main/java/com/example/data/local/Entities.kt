package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "research_projects")
data class ResearchProject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val category: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_reports")
data class SavedReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val topic: String,
    val overview: String,
    val detailedAnalysis: String,
    val keyInsights: String,
    val importantConcepts: String,
    val furtherReading: String,
    val conclusion: String,
    val savedAt: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workspace: String,   // E.g., "Artificial Intelligence", "Physics"
    val folder: String,      // E.g., "LLMs", "Prompt Engineering", "AI Ethics", "Quantum Mechanics", "Relativity"
    val title: String,
    val content: String,     // Rich text or simple list
    val lastModified: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val tags: String = "",    // Comma-separated tags
    val isPdf: Boolean = false,
    val pdfUriString: String? = null,
    val fileMimeType: String? = null
)

@Entity(tableName = "research_papers")
data class ResearchPaper(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,    // E.g., "AI Papers", "Physics Papers", "Medical Papers", "Business Papers"
    val title: String,
    val authors: String,
    val publishDate: String,
    val personalNotes: String = "",
    val isBookmarked: Boolean = false,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "planner_tasks")
data class PlannerTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskName: String,
    val targetDate: Long,    // timestamp
    val priority: String,    // "HIGH", "MEDIUM", "LOW"
    val isCompleted: Boolean = false,
    val type: String         // "DAILY", "WEEKLY", "MONTHLY", "DEADLINE"
)

@Entity(tableName = "chat_threads")
data class ChatThread(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val lastActive: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val threadId: String,
    val sender: String,      // "USER", "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val bio: String = "Scholarly Researcher",
    val interests: String = "Artificial Intelligence",
    val phoneNumber: String = "",
    val phonePassword: String = ""
)
