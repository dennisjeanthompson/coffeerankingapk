package com.example.coffeerankingapk.data.repository

import com.example.coffeerankingapk.data.model.Coupon
import com.example.coffeerankingapk.data.model.OwnerDashboard
import kotlinx.coroutines.flow.Flow

interface OwnerRepository {
    fun observeDashboard(): Flow<OwnerDashboard>
    fun observeCoupons(): Flow<List<Coupon>>

    suspend fun createCoupon(
        title: String,
        description: String,
        discountPercent: Int,
        expiryTimestamp: Long?
    ): Result<Unit>

    suspend fun deactivateCoupon(couponId: String): Result<Unit>
}