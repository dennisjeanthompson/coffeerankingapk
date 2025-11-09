package com.example.coffeerankingapk.data.repository

import android.util.Log
import com.example.coffeerankingapk.data.model.CoffeeShop
import com.example.coffeerankingapk.data.model.Rating
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await

class CoffeeShopRepository {
    private val db = FirebaseFirestore.getInstance()
    private val coffeeShopsCollection = db.collection("coffeeShops")
    
    companion object {
        private const val TAG = "CoffeeShopRepository"
    }
    
    // Fetch all coffee shops - NO server-side filtering, returns all shops with location
    suspend fun getAllCoffeeShops(): Result<List<CoffeeShop>> {
        return try {
            // Fetch ALL documents - let ViewModel do the filtering
            val snapshot = coffeeShopsCollection
                .get()
                .await()
            
            val shops = snapshot.documents.mapNotNull { doc ->
                try {
                    val name = doc.getString("name") ?: ""
                    val type = doc.getString("type") ?: ""
                    val location = doc.getGeoPoint("location") ?: return@mapNotNull null
                    val description = doc.getString("description") ?: ""
                    val address = doc.getString("address") ?: ""
                    val ownerId = doc.getString("ownerId") ?: ""  // Explicit handling for missing field
                    
                    Log.v(TAG, "Fetched doc: ${doc.id} name='$name' type='$type'")
                    
                    CoffeeShop(
                        id = doc.id,
                        name = name,
                        type = type,
                        location = location,
                        averageRating = doc.getDouble("averageRating") ?: 0.0,
                        totalRatings = doc.getLong("totalRatings")?.toInt() ?: 0,
                        ownerId = ownerId,
                        description = description,
                        address = address,
                        imageUrl = doc.getString("imageUrl") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing shop document: ${doc.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "Fetched ${shops.size} total shops with locations (no filtering)")
            Result.success(shops)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching coffee shops", e)
            Result.failure(e)
        }
    }
    
    // Get single coffee shop by ID
    suspend fun getCoffeeShop(shopId: String): Result<CoffeeShop> {
        return try {
            val doc = coffeeShopsCollection.document(shopId).get().await()
            if (doc.exists()) {
                val shop = CoffeeShop(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    type = doc.getString("type") ?: "",
                    location = doc.getGeoPoint("location"),
                    averageRating = doc.getDouble("averageRating") ?: 0.0,
                    totalRatings = doc.getLong("totalRatings")?.toInt() ?: 0,
                    ownerId = doc.getString("ownerId") ?: "",  // Explicit handling for missing field
                    description = doc.getString("description") ?: "",
                    address = doc.getString("address") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
                Result.success(shop)
            } else {
                Result.failure(Exception("Shop not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching shop: $shopId", e)
            Result.failure(e)
        }
    }
    
    // TASK 3: Submit rating using Firestore Transaction
    suspend fun submitRating(
        shopId: String,
        userId: String,
        newRatingValue: Double
    ): Result<Unit> {
        return try {
            db.runTransaction { transaction ->
                // Reference to main shop document
                val shopRef = coffeeShopsCollection.document(shopId)
                
                // Reference for new rating document
                val newRatingId = "${userId}_${System.currentTimeMillis()}"
                val ratingRef = shopRef.collection("ratings").document(newRatingId)
                
                // Read current shop data
                val shopSnapshot = transaction.get(shopRef)
                
                if (!shopSnapshot.exists()) {
                    throw Exception("Shop not found")
                }
                
                // Get current values
                val oldAvg = shopSnapshot.getDouble("averageRating") ?: 0.0
                val oldTotal = shopSnapshot.getLong("totalRatings")?.toInt() ?: 0
                
                // Calculate new values
                val newTotal = oldTotal + 1
                val newAvg = ((oldAvg * oldTotal) + newRatingValue) / newTotal
                
                // Update main shop document
                transaction.update(shopRef, mapOf(
                    "averageRating" to newAvg,
                    "totalRatings" to newTotal
                ))
                
                // Create new rating document in subcollection
                val ratingData = mapOf(
                    "userId" to userId,
                    "rating" to newRatingValue,
                    "timestamp" to System.currentTimeMillis()
                )
                transaction.set(ratingRef, ratingData)
                
                Log.d(TAG, "Rating submitted successfully: $shopId, newAvg: $newAvg, newTotal: $newTotal")
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting rating", e)
            Result.failure(e)
        }
    }
    
    // Get ratings for a shop
    suspend fun getShopRatings(shopId: String): Result<List<Rating>> {
        return try {
            val snapshot = coffeeShopsCollection
                .document(shopId)
                .collection("ratings")
                .get()
                .await()
            
            val ratings = snapshot.documents.mapNotNull { doc ->
                try {
                    Rating(
                        userId = doc.getString("userId") ?: "",
                        rating = doc.getDouble("rating") ?: 0.0,
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        comment = doc.getString("comment") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing rating document", e)
                    null
                }
            }
            
            Result.success(ratings)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching ratings", e)
            Result.failure(e)
        }
    }
    
    // Add a new coffee shop
    suspend fun addCoffeeShop(shop: CoffeeShop): Result<String> {
        return try {
            val docRef = coffeeShopsCollection.document()
            val shopData = shop.copy(
                id = docRef.id,
                type = shop.type.ifBlank { "Coffee Shop" } // Use provided type or default
            ).toMap()
            docRef.set(shopData).await()
            Log.d(TAG, "Coffee shop added: ${docRef.id}, name: ${shop.name}, location: ${shop.location}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding coffee shop", e)
            Result.failure(e)
        }
    }
}
