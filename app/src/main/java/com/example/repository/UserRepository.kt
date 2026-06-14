package com.example.repository

import com.example.data.local.AppDatabase
import com.example.data.local.User

class UserRepository(private val db: AppDatabase) {

    suspend fun getUserByUsername(username: String): User? {
        return db.userDao().getUserByUsername(username)
    }

    suspend fun getUserByPhone(phoneNumber: String): User? {
        return db.userDao().getUserByPhone(phoneNumber)
    }

    suspend fun insertUser(user: User): Long {
        return db.userDao().insertUser(user)
    }

    suspend fun updateUserProfile(username: String, bio: String, interests: String) {
        db.userDao().updateUserProfile(username, bio, interests)
    }
}
