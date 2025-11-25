package com.example.coffeerankingapk.data.model

data class Favorite(
    val id: String = "",
    val userId: String = "",
    val shopId: String = "",
    val shopName: String = "",
    val shopAddress: String = "",
    val averageRating: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "shopId" to shopId,
            "shopName" to shopName,
            "shopAddress" to shopAddress,
            "averageRating" to averageRating,
            "timestamp" to timestamp
        )
    }
    
    companion object {
        fun fromMap(id: String, data: Map<String, Any>): Favorite {
            return Favorite(
                id = id,
                userId = data["userId"] as? String ?: "",
                shopId = data["shopId"] as? String ?: "",
                shopName = data["shopName"] as? String ?: "",
                shopAddress = data["shopAddress"] as? String ?: "",
                averageRating = (data["averageRating"] as? Number)?.toDouble() ?: 0.0,
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
