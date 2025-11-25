package com.example.coffeerankingapk.data.model

import com.google.firebase.Timestamp
import java.util.Date

data class Coupon(
    val id: String = "",
    val shopId: String = "", // Owner's coffee shop ID
    val ownerId: String = "",
    val title: String = "",
    val description: String = "",
    val discountPercent: Int = 0,
    val discountAmount: Double = 0.0, // Fixed amount discount (if percent is 0)
    val minimumPurchase: Double = 0.0, // Minimum purchase required
    val startDate: Date = Date(),
    val expiryDate: Date = Date(),
    val maxRedemptions: Int = -1, // -1 means unlimited
    val currentRedemptions: Int = 0,
    val isActive: Boolean = true,
    val code: String = "", // Optional coupon code
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "shopId" to shopId,
            "ownerId" to ownerId,
            "title" to title,
            "description" to description,
            "discountPercent" to discountPercent,
            "discountAmount" to discountAmount,
            "minimumPurchase" to minimumPurchase,
            "startDate" to Timestamp(startDate),
            "expiryDate" to Timestamp(expiryDate),
            "maxRedemptions" to maxRedemptions,
            "currentRedemptions" to currentRedemptions,
            "isActive" to isActive,
            "code" to code,
            "createdAt" to Timestamp(createdAt),
            "updatedAt" to Timestamp(updatedAt)
        )
    }
    
    fun isExpired(): Boolean {
        return expiryDate.before(Date())
    }
    
    fun isRedeemable(): Boolean {
        return isActive && 
               !isExpired() && 
               (maxRedemptions == -1 || currentRedemptions < maxRedemptions) &&
               Date().after(startDate)
    }
    
    fun isMaxedOut(): Boolean {
        return maxRedemptions != -1 && currentRedemptions >= maxRedemptions
    }
}
