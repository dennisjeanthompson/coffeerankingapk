package com.example.coffeerankingapk.data.repository

import android.util.Log
import com.example.coffeerankingapk.data.model.Cafe
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class CafeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val cafesCollection = db.collection("cafes")
    
    companion object {
        private const val TAG = "CafeRepository"
    }
    
    // Add a new cafe
    suspend fun addCafe(cafe: Cafe): Result<String> {
        return try {
            val docRef = cafesCollection.document()
            val cafeWithId = cafe.copy(id = docRef.id)
            docRef.set(cafeWithId.toMap()).await()
            Log.d(TAG, "Cafe added successfully: ${cafeWithId.name}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding cafe", e)
            Result.failure(e)
        }
    }
    
    // Get all cafes
    suspend fun getAllCafes(): Result<List<Cafe>> {
        return try {
            val snapshot = cafesCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val cafes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Cafe::class.java)
            }
            Log.d(TAG, "Retrieved ${cafes.size} cafes")
            Result.success(cafes)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cafes", e)
            Result.failure(e)
        }
    }
    
    // Get cafes by owner
    suspend fun getCafesByOwner(ownerId: String): Result<List<Cafe>> {
        return try {
            val snapshot = cafesCollection
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            
            val cafes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Cafe::class.java)
            }
            Log.d(TAG, "Retrieved ${cafes.size} cafes for owner: $ownerId")
            Result.success(cafes)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cafes by owner", e)
            Result.failure(e)
        }
    }
    
    // Get cafes near location (basic implementation)
    suspend fun getCafesNearLocation(
        centerLat: Double,
        centerLng: Double,
        radiusInKm: Double = 10.0
    ): Result<List<Cafe>> {
        return try {
            // Get all cafes and filter by distance
            // For production, use GeoFirestore for efficient geoqueries
            val allCafes = getAllCafes().getOrThrow()
            
            val nearbyCafes = allCafes.filter { cafe ->
                cafe.location?.let { location ->
                    val distance = calculateDistance(
                        centerLat, centerLng,
                        location.latitude, location.longitude
                    )
                    distance <= radiusInKm
                } ?: false
            }
            
            Log.d(TAG, "Found ${nearbyCafes.size} cafes within ${radiusInKm}km")
            Result.success(nearbyCafes)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting nearby cafes", e)
            Result.failure(e)
        }
    }
    
    // Update cafe
    suspend fun updateCafe(cafe: Cafe): Result<Unit> {
        return try {
            cafesCollection.document(cafe.id)
                .update(cafe.toMap())
                .await()
            Log.d(TAG, "Cafe updated: ${cafe.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating cafe", e)
            Result.failure(e)
        }
    }
    
    // Delete cafe
    suspend fun deleteCafe(cafeId: String): Result<Unit> {
        return try {
            cafesCollection.document(cafeId).delete().await()
            Log.d(TAG, "Cafe deleted: $cafeId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting cafe", e)
            Result.failure(e)
        }
    }
    
    // Helper function to calculate distance between two points (Haversine formula)
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // km
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return earthRadius * c
    }
}
