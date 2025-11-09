package com.example.coffeerankingapk.data.model

import com.google.firebase.firestore.GeoPoint

data class CoffeeShop(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val location: GeoPoint? = null,
    val averageRating: Double = 0.0,
    val totalRatings: Int = 0,
    val ownerId: String = "",
    val description: String = "",
    val address: String = "",
    val imageUrl: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "type" to type,
            "location" to location,
            "averageRating" to averageRating,
            "totalRatings" to totalRatings,
            "ownerId" to ownerId,
            "description" to description,
            "address" to address,
            "imageUrl" to imageUrl
        )
    }
}

data class Rating(
    val userId: String = "",
    val rating: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val comment: String = ""
)
