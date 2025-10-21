package com.example.coffeerankingapk.data.model

import java.util.Date

data class Coupon(
    val id: String = "",
    val ownerId: String = "",
    val cafeId: String = "",
    val title: String = "",
    val description: String = "",
    val discountPercent: Int = 0,
    val expiryDate: Date? = null,
    val createdAt: Long = 0L,
    val isActive: Boolean = true
)