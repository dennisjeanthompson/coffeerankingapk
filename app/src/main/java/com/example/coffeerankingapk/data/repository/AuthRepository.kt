package com.example.coffeerankingapk.data.repository

import com.example.coffeerankingapk.data.model.UserProfile
import com.example.coffeerankingapk.data.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isSignedIn: Boolean

    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun signOut()

    fun observeUserProfile(): Flow<UserProfile?>
    suspend fun refreshUserProfile(): UserProfile?
    suspend fun updateUserRole(role: UserRole)
    suspend fun updateNotificationPreferences(
        pushEnabled: Boolean,
        emailEnabled: Boolean
    )
}