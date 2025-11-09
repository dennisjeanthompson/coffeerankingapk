package com.example.coffeerankingapk.data.model

import com.google.firebase.firestore.GeoPoint

data class Cafe(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val location: GeoPoint? = null, // Firestore GeoPoint for lat/lng
    val address: String = "",
    val ownerId: String = "",
    val ownerEmail: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isOpen: Boolean = true,
    val phoneNumber: String = "",
    val website: String = ""
) {
    // Convert to Map for Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "description" to description,
            "location" to location,
            "address" to address,
            "ownerId" to ownerId,
            "ownerEmail" to ownerEmail,
            "rating" to rating,
            "reviewCount" to reviewCount,
            "imageUrl" to imageUrl,
            "createdAt" to createdAt,
            "isOpen" to isOpen,
            "phoneNumber" to phoneNumber,
            "website" to website
        )
    }
}
