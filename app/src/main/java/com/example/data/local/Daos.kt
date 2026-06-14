package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ResearchProjectDao {
    @Query("SELECT * FROM research_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ResearchProject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ResearchProject): Long

    @Delete
    suspend fun deleteProject(project: ResearchProject)
}

@Dao
interface SavedReportDao {
    @Query("SELECT * FROM saved_reports ORDER BY savedAt DESC")
    fun getAllReports(): Flow<List<SavedReport>>

    @Query("SELECT * FROM saved_reports WHERE projectId = :projectId ORDER BY savedAt DESC")
    fun getReportsByProjectId(projectId: Int): Flow<List<SavedReport>>

    @Query("SELECT * FROM saved_reports WHERE id = :id")
    fun getReportById(id: Int): Flow<SavedReport?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: SavedReport): Long

    @Update
    suspend fun updateReport(report: SavedReport)

    @Delete
    suspend fun deleteReport(report: SavedReport)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY lastModified DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE workspace = :workspace AND folder = :folder ORDER BY lastModified DESC")
    fun getNotesByFolder(workspace: String, folder: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Int): Flow<Note?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchNotes(query: String): Flow<List<Note>>
}

@Dao
interface ResearchPaperDao {
    @Query("SELECT * FROM research_papers ORDER BY savedAt DESC")
    fun getAllPapers(): Flow<List<ResearchPaper>>

    @Query("SELECT * FROM research_papers WHERE category = :category ORDER BY savedAt DESC")
    fun getPapersByCategory(category: String): Flow<List<ResearchPaper>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaper(paper: ResearchPaper): Long

    @Update
    suspend fun updatePaper(paper: ResearchPaper)

    @Delete
    suspend fun deletePaper(paper: ResearchPaper)
}

@Dao
interface PlannerTaskDao {
    @Query("SELECT * FROM planner_tasks ORDER BY targetDate ASC")
    fun getAllTasks(): Flow<List<PlannerTask>>

    @Query("SELECT * FROM planner_tasks WHERE type = :type ORDER BY targetDate ASC")
    fun getTasksByType(type: String): Flow<List<PlannerTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PlannerTask): Long

    @Update
    suspend fun updateTask(task: PlannerTask)

    @Delete
    suspend fun deleteTask(task: PlannerTask)
}

@Dao
interface ChatThreadDao {
    @Query("SELECT * FROM chat_threads ORDER BY isPinned DESC, lastActive DESC")
    fun getAllThreads(): Flow<List<ChatThread>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: ChatThread)

    @Update
    suspend fun updateThread(thread: ChatThread)

    @Delete
    suspend fun deleteThread(thread: ChatThread)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesByThread(threadId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages WHERE threadId = :threadId")
    suspend fun deleteMessagesByThread(threadId: String)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getUserByPhone(phoneNumber: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("UPDATE users SET bio = :bio, interests = :interests WHERE username = :username")
    suspend fun updateUserProfile(username: String, bio: String, interests: String)
}
