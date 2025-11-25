package com.example.coffeerankingapk.data.repository

import android.util.Log
import com.example.coffeerankingapk.data.model.User
import com.example.coffeerankingapk.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = db.collection("users")
    
    companion object {
        private const val TAG = "UserRepository"
    }
    
    /**
     * Get current user profile from Firestore
     */
    suspend fun getCurrentUser(): Result<User?> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.success(null)
            
            val doc = usersCollection.document(uid).get().await()
            if (doc.exists()) {
                val user = User.fromMap(doc.data ?: emptyMap())
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user", e)
            Result.failure(e)
        }
    }
    
    /**
     * Create or update user profile with role
     */
    suspend fun saveUserProfile(role: UserRole): Result<Unit> {
        return try {
            val firebaseUser = auth.currentUser ?: return Result.failure(Exception("No authenticated user"))
            
            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "User",
                role = role,
                updatedAt = System.currentTimeMillis()
            )
            
            usersCollection.document(firebaseUser.uid)
                .set(user.toMap())
                .await()
            
            Log.d(TAG, "User profile saved: ${user.email} as ${role.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update user role
     */
    suspend fun updateUserRole(role: UserRole): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No authenticated user"))
            
            usersCollection.document(uid)
                .update(
                    mapOf(
                        "role" to role.name,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            
            Log.d(TAG, "User role updated to: ${role.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user role", e)
            Result.failure(e)
        }
    }
}
