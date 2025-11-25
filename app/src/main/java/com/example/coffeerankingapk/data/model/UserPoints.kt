package com.example.coffeerankingapk.data.model

import com.google.firebase.Timestamp
import java.util.Date

/**
 * User points and gamification data model
 */
data class UserPoints(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    
    // Points & Level
    val totalPoints: Int = 0,
    val currentLevel: Int = 1,
    val pointsToNextLevel: Int = 100,
    
    // Activity tracking
    val totalRatings: Int = 0,
    val totalReviews: Int = 0,
    val totalCouponsRedeemed: Int = 0,
    val totalCafesVisited: Int = 0,
    
    // Achievements
    val badges: List<String> = emptyList(),
    val achievements: Map<String, Boolean> = emptyMap(),
    
    // Ranking
    val rank: Int = 0,
    
    // Metadata
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val lastActivityAt: Date = Date()
) {
    /**
     * Convert to Firestore map
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "displayName" to displayName,
            "email" to email,
            "photoUrl" to photoUrl,
            "totalPoints" to totalPoints,
            "currentLevel" to currentLevel,
            "pointsToNextLevel" to pointsToNextLevel,
            "totalRatings" to totalRatings,
            "totalReviews" to totalReviews,
            "totalCouponsRedeemed" to totalCouponsRedeemed,
            "totalCafesVisited" to totalCafesVisited,
            "badges" to badges,
            "achievements" to achievements,
            "rank" to rank,
            "createdAt" to Timestamp(createdAt),
            "updatedAt" to Timestamp(updatedAt),
            "lastActivityAt" to Timestamp(lastActivityAt)
        )
    }
    
    companion object {
        /**
         * Points awarded for different actions
         */
        const val POINTS_PER_RATING = 10
        const val POINTS_PER_REVIEW = 25
        const val POINTS_PER_DETAILED_REVIEW = 50 // Review with 50+ chars
        const val POINTS_PER_COUPON_REDEEM = 15
        const val POINTS_PER_CAFE_VISIT = 5
        const val POINTS_PER_PHOTO = 20
        
        /**
         * Level thresholds - points needed to reach each level
         */
        val LEVEL_THRESHOLDS = listOf(
            0,      // Level 1
            100,    // Level 2
            250,    // Level 3
            500,    // Level 4
            1000,   // Level 5
            2000,   // Level 6
            3500,   // Level 7
            5500,   // Level 8
            8000,   // Level 9
            12000   // Level 10
        )
        
        /**
         * Calculate level from total points
         */
        fun calculateLevel(points: Int): Int {
            for (level in LEVEL_THRESHOLDS.size - 1 downTo 0) {
                if (points >= LEVEL_THRESHOLDS[level]) {
                    return level + 1
                }
            }
            return 1
        }
        
        /**
         * Calculate points needed for next level
         */
        fun calculatePointsToNextLevel(points: Int): Int {
            val currentLevel = calculateLevel(points)
            return if (currentLevel < LEVEL_THRESHOLDS.size) {
                LEVEL_THRESHOLDS[currentLevel] - points
            } else {
                0 // Max level reached
            }
        }
        
        /**
         * Get level title/name
         */
        fun getLevelTitle(level: Int): String {
            return when (level) {
                1 -> "Coffee Newbie"
                2 -> "Casual Sipper"
                3 -> "Coffee Enthusiast"
                4 -> "Cafe Explorer"
                5 -> "Coffee Connoisseur"
                6 -> "Brew Master"
                7 -> "Coffee Expert"
                8 -> "Espresso Elite"
                9 -> "Coffee Legend"
                10 -> "Barista God"
                else -> "Coffee Master"
            }
        }
        
        /**
         * Parse from Firestore document
         */
        fun fromMap(data: Map<String, Any>): UserPoints {
            return UserPoints(
                userId = data["userId"] as? String ?: "",
                displayName = data["displayName"] as? String ?: "",
                email = data["email"] as? String ?: "",
                photoUrl = data["photoUrl"] as? String ?: "",
                totalPoints = (data["totalPoints"] as? Long)?.toInt() ?: 0,
                currentLevel = (data["currentLevel"] as? Long)?.toInt() ?: 1,
                pointsToNextLevel = (data["pointsToNextLevel"] as? Long)?.toInt() ?: 100,
                totalRatings = (data["totalRatings"] as? Long)?.toInt() ?: 0,
                totalReviews = (data["totalReviews"] as? Long)?.toInt() ?: 0,
                totalCouponsRedeemed = (data["totalCouponsRedeemed"] as? Long)?.toInt() ?: 0,
                totalCafesVisited = (data["totalCafesVisited"] as? Long)?.toInt() ?: 0,
                badges = (data["badges"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                achievements = (data["achievements"] as? Map<*, *>)?.mapKeys { it.key.toString() }?.mapValues { it.value as? Boolean ?: false } ?: emptyMap(),
                rank = (data["rank"] as? Long)?.toInt() ?: 0,
                createdAt = (data["createdAt"] as? Timestamp)?.toDate() ?: Date(),
                updatedAt = (data["updatedAt"] as? Timestamp)?.toDate() ?: Date(),
                lastActivityAt = (data["lastActivityAt"] as? Timestamp)?.toDate() ?: Date()
            )
        }
    }
}

/**
 * Point transaction history
 */
data class PointTransaction(
    val id: String = "",
    val userId: String = "",
    val points: Int = 0,
    val action: String = "", // "rating", "review", "coupon_redeem", "cafe_visit"
    val description: String = "",
    val relatedId: String = "", // Rating ID, Coupon ID, etc.
    val createdAt: Date = Date()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "points" to points,
            "action" to action,
            "description" to description,
            "relatedId" to relatedId,
            "createdAt" to Timestamp(createdAt)
        )
    }
    
    companion object {
        fun fromMap(id: String, data: Map<String, Any>): PointTransaction {
            return PointTransaction(
                id = id,
                userId = data["userId"] as? String ?: "",
                points = (data["points"] as? Long)?.toInt() ?: 0,
                action = data["action"] as? String ?: "",
                description = data["description"] as? String ?: "",
                relatedId = data["relatedId"] as? String ?: "",
                createdAt = (data["createdAt"] as? Timestamp)?.toDate() ?: Date()
            )
        }
    }
}
