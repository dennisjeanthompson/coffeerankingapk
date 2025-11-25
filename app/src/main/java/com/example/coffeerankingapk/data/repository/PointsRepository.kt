package com.example.coffeerankingapk.data.repository

import android.util.Log
import com.example.coffeerankingapk.data.model.UserPoints
import com.example.coffeerankingapk.data.model.PointTransaction
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class PointsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userPointsCollection = db.collection("userPoints")
    private val transactionsCollection = db.collection("pointTransactions")
    
    companion object {
        private const val TAG = "PointsRepository"
    }
    
    /**
     * Get or create user points document
     */
    suspend fun getUserPoints(userId: String): Result<UserPoints> {
        return try {
            val doc = userPointsCollection.document(userId).get().await()
            
            if (doc.exists()) {
                val userPoints = UserPoints.fromMap(doc.data ?: emptyMap())
                Result.success(userPoints)
            } else {
                // Create new user points document
                val user = auth.currentUser
                val newUserPoints = UserPoints(
                    userId = userId,
                    displayName = user?.displayName ?: "Anonymous",
                    email = user?.email ?: "",
                    photoUrl = user?.photoUrl?.toString() ?: ""
                )
                
                userPointsCollection.document(userId).set(newUserPoints.toMap()).await()
                Result.success(newUserPoints)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user points", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get user points with real-time updates
     */
    fun observeUserPoints(userId: String): Flow<Result<UserPoints>> = callbackFlow {
        val listener = userPointsCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing user points", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val userPoints = UserPoints.fromMap(snapshot.data ?: emptyMap())
                        trySend(Result.success(userPoints))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing user points", e)
                        trySend(Result.failure(e))
                    }
                } else {
                    // Document doesn't exist yet, create it
                    val user = auth.currentUser
                    val newUserPoints = UserPoints(
                        userId = userId,
                        displayName = user?.displayName ?: "Anonymous",
                        email = user?.email ?: "",
                        photoUrl = user?.photoUrl?.toString() ?: ""
                    )
                    
                    // Create the document in Firestore
                    try {
                        userPointsCollection.document(userId).set(newUserPoints.toMap()).addOnSuccessListener {
                            trySend(Result.success(newUserPoints))
                        }.addOnFailureListener { createError ->
                            Log.e(TAG, "Error creating user points document", createError)
                            trySend(Result.failure(createError))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in document creation", e)
                        trySend(Result.success(newUserPoints)) // Still send default data
                    }
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Award points to user for an action
     */
    suspend fun awardPoints(
        userId: String,
        points: Int,
        action: String,
        description: String,
        relatedId: String = ""
    ): Result<Unit> {
        return try {
            // First, ensure user points document exists
            val userPointsRef = userPointsCollection.document(userId)
            val userDoc = userPointsRef.get().await()
            
            if (!userDoc.exists()) {
                // Create new user points document
                val user = auth.currentUser
                val newUserPoints = UserPoints(
                    userId = userId,
                    displayName = user?.displayName ?: "Anonymous",
                    email = user?.email ?: "",
                    photoUrl = user?.photoUrl?.toString() ?: ""
                )
                userPointsRef.set(newUserPoints.toMap()).await()
                Log.d(TAG, "Created new user points document for $userId")
            }
            
            // Now award points in a transaction
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userPointsRef)
                
                if (!snapshot.exists()) {
                    throw Exception("User points document not found after creation")
                }
                
                val currentPoints = (snapshot.getLong("totalPoints") ?: 0).toInt()
                val newTotalPoints = currentPoints + points
                
                // Calculate new level
                val newLevel = UserPoints.calculateLevel(newTotalPoints)
                val pointsToNext = UserPoints.calculatePointsToNextLevel(newTotalPoints)
                
                // Update activity counter based on action
                val updates = mutableMapOf<String, Any>(
                    "totalPoints" to newTotalPoints,
                    "currentLevel" to newLevel,
                    "pointsToNextLevel" to pointsToNext,
                    "updatedAt" to Timestamp(Date()),
                    "lastActivityAt" to Timestamp(Date())
                )
                
                when (action) {
                    "rating" -> updates["totalRatings"] = FieldValue.increment(1)
                    "review" -> updates["totalReviews"] = FieldValue.increment(1)
                    "coupon_redeem" -> updates["totalCouponsRedeemed"] = FieldValue.increment(1)
                    "cafe_visit" -> updates["totalCafesVisited"] = FieldValue.increment(1)
                }
                
                transaction.update(userPointsRef, updates)
                
                // Create transaction record
                val transactionDoc = transactionsCollection.document()
                val pointTransaction = PointTransaction(
                    id = transactionDoc.id,
                    userId = userId,
                    points = points,
                    action = action,
                    description = description,
                    relatedId = relatedId
                )
                transaction.set(transactionDoc, pointTransaction.toMap())
            }.await()
            
            Log.d(TAG, "✅ Awarded $points points to user $userId for $action ($description)")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error awarding points to $userId: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get leaderboard with real-time updates (top users by points)
     */
    fun getLeaderboard(limit: Int = 100): Flow<Result<List<UserPoints>>> = callbackFlow {
        val listener = userPointsCollection
            .orderBy("totalPoints", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error loading leaderboard", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val leaderboard = snapshot.documents.mapIndexedNotNull { index, doc ->
                        try {
                            val userPoints = UserPoints.fromMap(doc.data ?: emptyMap())
                            userPoints.copy(rank = index + 1)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing leaderboard entry: ${doc.id}", e)
                            null
                        }
                    }
                    trySend(Result.success(leaderboard))
                    Log.d(TAG, "Loaded ${leaderboard.size} leaderboard entries")
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get user's rank (position in leaderboard)
     */
    suspend fun getUserRank(userId: String, userPoints: Int): Result<Int> {
        return try {
            val higherScores = userPointsCollection
                .whereGreaterThan("totalPoints", userPoints)
                .get()
                .await()
            
            val rank = higherScores.size() + 1
            
            // Update user's rank in their document
            userPointsCollection.document(userId)
                .update("rank", rank)
                .await()
            
            Result.success(rank)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user rank", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get point transaction history for a user
     */
    fun getUserTransactions(userId: String, limit: Int = 50): Flow<Result<List<PointTransaction>>> = callbackFlow {
        val listener = transactionsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error loading transactions", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val transactions = snapshot.documents.mapNotNull { doc ->
                        try {
                            PointTransaction.fromMap(doc.id, doc.data ?: emptyMap())
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing transaction: ${doc.id}", e)
                            null
                        }
                    }
                    trySend(Result.success(transactions))
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Award badge to user
     */
    suspend fun awardBadge(userId: String, badgeId: String): Result<Unit> {
        return try {
            userPointsCollection.document(userId)
                .update(
                    "badges", FieldValue.arrayUnion(badgeId),
                    "updatedAt", Timestamp(Date())
                )
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error awarding badge", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if user has already been rewarded for a specific action
     */
    suspend fun hasBeenRewarded(userId: String, action: String, relatedId: String): Result<Boolean> {
        return try {
            val existing = transactionsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("action", action)
                .whereEqualTo("relatedId", relatedId)
                .limit(1)
                .get()
                .await()
            
            Result.success(!existing.isEmpty)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking reward status", e)
            Result.failure(e)
        }
    }
}
