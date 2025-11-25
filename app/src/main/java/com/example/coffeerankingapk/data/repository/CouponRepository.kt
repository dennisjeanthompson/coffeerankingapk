package com.example.coffeerankingapk.data.repository

import android.util.Log
import com.example.coffeerankingapk.data.model.Coupon
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class CouponRepository {
    private val db = FirebaseFirestore.getInstance()
    private val couponsCollection = db.collection("coupons")
    
    companion object {
        private const val TAG = "CouponRepository"
    }
    
    /**
     * Get all coupons for a specific coffee shop (real-time updates)
     */
    fun getCouponsForShop(shopId: String): Flow<Result<List<Coupon>>> = callbackFlow {
        val listener = couponsCollection
            .whereEqualTo("shopId", shopId)
            // Removed .orderBy to avoid requiring a Firestore index
            // Sorting will be done in the app after fetching
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to coupons", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val coupons = snapshot.documents.mapNotNull { doc ->
                        try {
                            documentToCoupon(doc.id, doc.data ?: emptyMap())
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing coupon document: ${doc.id}", e)
                            null
                        }
                    }
                    trySend(Result.success(coupons))
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get all active coupons for a shop (one-time fetch)
     */
    suspend fun getActiveCouponsForShop(shopId: String): Result<List<Coupon>> {
        return try {
            val now = Timestamp(Date())
            val snapshot = couponsCollection
                .whereEqualTo("shopId", shopId)
                .whereEqualTo("isActive", true)
                // Removed .whereGreaterThan and .orderBy to avoid index requirement
                // Filtering/sorting will be done in the app
                .get()
                .await()
            
            val coupons = snapshot.documents.mapNotNull { doc ->
                try {
                    documentToCoupon(doc.id, doc.data ?: emptyMap())
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing coupon: ${doc.id}", e)
                    null
                }
            }
            .filter { it.expiryDate.after(Date()) } // Filter expired in app
            .sortedBy { it.expiryDate } // Sort by expiry date in app
            
            Log.d(TAG, "Fetched ${coupons.size} active coupons for shop: $shopId")
            Result.success(coupons)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching active coupons", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get ALL active coupons from ALL shops (for lovers/users) with real-time updates
     */
    fun getAllActiveCoupons(): Flow<Result<List<Coupon>>> = callbackFlow {
        val listener = couponsCollection
            .whereEqualTo("isActive", true)
            // No orderBy to avoid index requirement - will sort in app
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to all active coupons", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val coupons = snapshot.documents.mapNotNull { doc ->
                        try {
                            documentToCoupon(doc.id, doc.data ?: emptyMap())
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing coupon document: ${doc.id}", e)
                            null
                        }
                    }
                    .filter { it.expiryDate.after(Date()) } // Filter out expired
                    
                    Log.d(TAG, "Fetched ${coupons.size} active coupons from all shops")
                    trySend(Result.success(coupons))
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get a single coupon by ID
     */
    suspend fun getCoupon(couponId: String): Result<Coupon> {
        return try {
            val doc = couponsCollection.document(couponId).get().await()
            if (doc.exists()) {
                val coupon = documentToCoupon(doc.id, doc.data ?: emptyMap())
                Result.success(coupon)
            } else {
                Result.failure(Exception("Coupon not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching coupon: $couponId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Create a new coupon
     */
    suspend fun createCoupon(coupon: Coupon): Result<String> {
        return try {
            val docRef = couponsCollection.document()
            val now = Date()
            val couponData = coupon.copy(
                id = docRef.id,
                createdAt = now,
                updatedAt = now
            ).toMap()
            
            docRef.set(couponData).await()
            Log.d(TAG, "Coupon created: ${docRef.id}, shop: ${coupon.shopId}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating coupon", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update an existing coupon
     */
    suspend fun updateCoupon(couponId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val updateData = updates.toMutableMap()
            updateData["updatedAt"] = Timestamp(Date())
            
            couponsCollection.document(couponId)
                .update(updateData)
                .await()
            
            Log.d(TAG, "Coupon updated: $couponId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating coupon: $couponId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete a coupon (soft delete by setting isActive to false)
     */
    suspend fun deleteCoupon(couponId: String, hardDelete: Boolean = false): Result<Unit> {
        return try {
            if (hardDelete) {
                couponsCollection.document(couponId).delete().await()
                Log.d(TAG, "Coupon hard deleted: $couponId")
            } else {
                couponsCollection.document(couponId)
                    .update(mapOf(
                        "isActive" to false,
                        "updatedAt" to Timestamp(Date())
                    ))
                    .await()
                Log.d(TAG, "Coupon soft deleted (deactivated): $couponId")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting coupon: $couponId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Toggle coupon active status
     */
    suspend fun toggleCouponStatus(couponId: String, isActive: Boolean): Result<Unit> {
        return try {
            couponsCollection.document(couponId)
                .update(mapOf(
                    "isActive" to isActive,
                    "updatedAt" to Timestamp(Date())
                ))
                .await()
            
            Log.d(TAG, "Coupon status toggled: $couponId, active: $isActive")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling coupon status: $couponId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Redeem a coupon (increment redemption count)
     */
    suspend fun redeemCoupon(couponId: String, userId: String): Result<Unit> {
        return try {
            db.runTransaction { transaction ->
                val couponRef = couponsCollection.document(couponId)
                val snapshot = transaction.get(couponRef)
                
                if (!snapshot.exists()) {
                    throw Exception("Coupon not found")
                }
                
                val currentRedemptions = snapshot.getLong("currentRedemptions")?.toInt() ?: 0
                val maxRedemptions = snapshot.getLong("maxRedemptions")?.toInt() ?: -1
                val isActive = snapshot.getBoolean("isActive") ?: false
                val expiryDate = snapshot.getTimestamp("expiryDate")?.toDate() ?: Date()
                
                // Validate coupon can be redeemed
                if (!isActive) {
                    throw Exception("Coupon is not active")
                }
                
                if (expiryDate.before(Date())) {
                    throw Exception("Coupon has expired")
                }
                
                if (maxRedemptions != -1 && currentRedemptions >= maxRedemptions) {
                    throw Exception("Coupon has reached maximum redemptions")
                }
                
                // Increment redemption count
                transaction.update(couponRef, mapOf(
                    "currentRedemptions" to (currentRedemptions + 1),
                    "updatedAt" to Timestamp(Date())
                ))
                
                // Optionally: Log redemption in a separate collection
                val redemptionRef = couponRef.collection("redemptions").document()
                transaction.set(redemptionRef, mapOf(
                    "userId" to userId,
                    "timestamp" to Timestamp(Date())
                ))
                
                Log.d(TAG, "Coupon redeemed: $couponId by user: $userId")
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error redeeming coupon: $couponId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Helper function to convert Firestore document to Coupon object
     */
    private fun documentToCoupon(id: String, data: Map<String, Any>): Coupon {
        return Coupon(
            id = id,
            shopId = data["shopId"] as? String ?: "",
            ownerId = data["ownerId"] as? String ?: "",
            title = data["title"] as? String ?: "",
            description = data["description"] as? String ?: "",
            discountPercent = (data["discountPercent"] as? Long)?.toInt() ?: 0,
            discountAmount = (data["discountAmount"] as? Number)?.toDouble() ?: 0.0,
            minimumPurchase = (data["minimumPurchase"] as? Number)?.toDouble() ?: 0.0,
            startDate = (data["startDate"] as? Timestamp)?.toDate() ?: Date(),
            expiryDate = (data["expiryDate"] as? Timestamp)?.toDate() ?: Date(),
            maxRedemptions = (data["maxRedemptions"] as? Long)?.toInt() ?: -1,
            currentRedemptions = (data["currentRedemptions"] as? Long)?.toInt() ?: 0,
            isActive = data["isActive"] as? Boolean ?: true,
            code = data["code"] as? String ?: "",
            createdAt = (data["createdAt"] as? Timestamp)?.toDate() ?: Date(),
            updatedAt = (data["updatedAt"] as? Timestamp)?.toDate() ?: Date()
        )
    }
}
