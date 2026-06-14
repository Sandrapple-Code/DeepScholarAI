package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ResearchProject::class,
        SavedReport::class,
        Note::class,
        ResearchPaper::class,
        PlannerTask::class,
        ChatThread::class,
        ChatMessage::class,
        User::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun researchProjectDao(): ResearchProjectDao
    abstract fun savedReportDao(): SavedReportDao
    abstract fun noteDao(): NoteDao
    abstract fun researchPaperDao(): ResearchPaperDao
    abstract fun plannerTaskDao(): PlannerTaskDao
    abstract fun chatThreadDao(): ChatThreadDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "deepscholar_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
