package com.example.coffeerankingapk.data.repository

import android.util.Log
import com.example.coffeerankingapk.data.model.Favorite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FavoritesRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val favoritesCollection = db.collection("favorites")
    
    companion object {
        private const val TAG = "FavoritesRepository"
    }
    
    /**
     * Add a shop to favorites
     */
    suspend fun addFavorite(
        shopId: String,
        shopName: String,
        shopAddress: String,
        averageRating: Double
    ): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            
            // Check if already favorited
            val existing = favoritesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("shopId", shopId)
                .get()
                .await()
            
            if (!existing.isEmpty) {
                return Result.failure(Exception("Already in favorites"))
            }
            
            val favorite = Favorite(
                userId = userId,
                shopId = shopId,
                shopName = shopName,
                shopAddress = shopAddress,
                averageRating = averageRating,
                timestamp = System.currentTimeMillis()
            )
            
            val docRef = favoritesCollection.document()
            favoritesCollection.document(docRef.id)
                .set(favorite.copy(id = docRef.id).toMap())
                .await()
            
            Log.d(TAG, "Added to favorites: $shopName")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding favorite", e)
            Result.failure(e)
        }
    }
    
    /**
     * Remove a shop from favorites
     */
    suspend fun removeFavorite(shopId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            
            val snapshot = favoritesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("shopId", shopId)
                .get()
                .await()
            
            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            
            Log.d(TAG, "Removed from favorites: $shopId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing favorite", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if a shop is favorited
     */
    suspend fun isFavorite(shopId: String): Result<Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.success(false)
            
            val snapshot = favoritesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("shopId", shopId)
                .get()
                .await()
            
            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking favorite", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get user's favorites with real-time updates
     */
    fun getUserFavorites(): Flow<Result<List<Favorite>>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(Result.success(emptyList()))
            close()
            return@callbackFlow
        }
        
        val listener = favoritesCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                
                val favorites = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Favorite.fromMap(doc.id, doc.data ?: emptyMap())
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing favorite", e)
                        null
                    }
                } ?: emptyList()
                
                trySend(Result.success(favorites))
            }
        
        awaitClose { listener.remove() }
    }
}
