package com.example.repository

import com.example.data.local.AppDatabase
import com.example.data.local.Note
import kotlinx.coroutines.flow.Flow

class NotesRepository(private val db: AppDatabase) {

    val allNotes: Flow<List<Note>> = db.noteDao().getAllNotes()

    fun getNotesByFolder(workspace: String, folder: String): Flow<List<Note>> =
        db.noteDao().getNotesByFolder(workspace, folder)

    fun getNoteById(id: Int): Flow<Note?> =
        db.noteDao().getNoteById(id)

    fun searchNotes(query: String): Flow<List<Note>> =
        db.noteDao().searchNotes(query)

    suspend fun insertNote(note: Note): Long {
        return db.noteDao().insertNote(note)
    }

    suspend fun updateNote(note: Note) {
        db.noteDao().updateNote(note)
    }

    suspend fun deleteNote(note: Note) {
        db.noteDao().deleteNote(note)
    }
}
