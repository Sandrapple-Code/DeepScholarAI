package com.example.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ChatThread
import com.example.data.local.ChatMessage
import kotlinx.coroutines.flow.Flow

class ChatbotRepository(private val db: AppDatabase) {

    val allThreads: Flow<List<ChatThread>> = db.chatThreadDao().getAllThreads()

    fun getMessagesForThread(threadId: String): Flow<List<ChatMessage>> =
        db.chatMessageDao().getMessagesByThread(threadId)

    suspend fun insertThread(thread: ChatThread) {
        db.chatThreadDao().insertThread(thread)
    }

    suspend fun updateThread(thread: ChatThread) {
        db.chatThreadDao().updateThread(thread)
    }

    suspend fun deleteThread(thread: ChatThread) {
        db.chatThreadDao().deleteThread(thread)
        db.chatMessageDao().deleteMessagesByThread(thread.id)
    }

    suspend fun insertMessage(message: ChatMessage): Long {
        return db.chatMessageDao().insertMessage(message)
    }

    suspend fun clearMessagesForThread(threadId: String) {
        db.chatMessageDao().deleteMessagesByThread(threadId)
    }
}
