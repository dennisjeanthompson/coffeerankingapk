package com.example.coffeerankingapk.data.fake

import com.example.coffeerankingapk.data.model.Coupon
import com.example.coffeerankingapk.data.model.DashboardOverview
import com.example.coffeerankingapk.data.model.OwnerActivity
import com.example.coffeerankingapk.data.model.OwnerDashboard
import com.example.coffeerankingapk.data.model.OwnerReview
import com.example.coffeerankingapk.data.repository.OwnerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import java.util.UUID

class FakeOwnerRepository : OwnerRepository {

    private val dashboardState = MutableStateFlow(sampleDashboard())
    private val couponsState = MutableStateFlow(sampleCoupons())

    override fun observeDashboard(): Flow<OwnerDashboard> = dashboardState

    override fun observeCoupons(): Flow<List<Coupon>> = couponsState

    override suspend fun createCoupon(
        title: String,
        description: String,
        discountPercent: Int,
        expiryTimestamp: Long?
    ): Result<Unit> {
        return runCatching {
            val newCoupon = Coupon(
                id = UUID.randomUUID().toString(),
                ownerId = "owner-demo",
                cafeId = "1",
                title = title,
                description = description,
                discountPercent = discountPercent,
                expiryDate = expiryTimestamp?.let { Date(it) },
                createdAt = System.currentTimeMillis(),
                isActive = true
            )
            couponsState.update { listOf(newCoupon) + it }
            dashboardState.update { current ->
                current.copy(
                    recentActivity = listOf(
                        OwnerActivity(
                            title = "New coupon created",
                            subtitle = title,
                            icon = "🎟️",
                            timestamp = System.currentTimeMillis()
                        )
                    ) + current.recentActivity.take(4)
                )
            }
        }
    }

    override suspend fun deactivateCoupon(couponId: String): Result<Unit> {
        return runCatching {
            couponsState.update { coupons ->
                coupons.map { coupon ->
                    if (coupon.id == couponId) coupon.copy(isActive = false) else coupon
                }
            }
            dashboardState.update { current ->
                current.copy(
                    recentActivity = listOf(
                        OwnerActivity(
                            title = "Coupon deactivated",
                            subtitle = couponsState.value.firstOrNull { it.id == couponId }?.title
                                ?: "Coupon",
                            icon = "⏸️",
                            timestamp = System.currentTimeMillis()
                        )
                    ) + current.recentActivity.take(4)
                )
            }
        }
    }

    private fun sampleDashboard(): OwnerDashboard {
        val now = System.currentTimeMillis()
        return OwnerDashboard(
            cafeName = "Blue Bottle Coffee",
            overview = DashboardOverview(
                totalReviews = 256,
                averageRating = 4.6,
                monthlyVisits = 1_875,
                revenue = 12_850.0,
                reviewsChange = "+12%",
                ratingChange = "+0.3",
                visitsChange = "+8%",
                revenueChange = "+5%"
            ),
            recentActivity = listOf(
                OwnerActivity(
                    title = "New 5★ review",
                    subtitle = "\"Best cold brew in town\"",
                    icon = "⭐",
                    timestamp = now - 3_600_000L
                ),
                OwnerActivity(
                    title = "Reward redeemed",
                    subtitle = "Free pastry reward claimed",
                    icon = "🎁",
                    timestamp = now - 7_200_000L
                ),
                OwnerActivity(
                    title = "Coupon activated",
                    subtitle = "Happy Hour 20% Off",
                    icon = "⚡",
                    timestamp = now - 21_600_000L
                )
            ),
            reviewsOverTime = listOf(4.2, 4.4, 4.5, 4.6, 4.7, 4.8),
            recentReviews = listOf(
                OwnerReview(
                    userName = "Taylor P.",
                    rating = 5,
                    comment = "Loved the seasonal latte and calm playlist!",
                    createdAt = now - 5_400_000L
                ),
                OwnerReview(
                    userName = "Jordan C.",
                    rating = 4,
                    comment = "Great service, would love more vegan options",
                    createdAt = now - 10_800_000L
                )
            )
        )
    }

    private fun sampleCoupons(): List<Coupon> {
        val now = System.currentTimeMillis()
        return listOf(
            Coupon(
                id = UUID.randomUUID().toString(),
                ownerId = "owner-demo",
                cafeId = "1",
                title = "Morning Brew BOGO",
                description = "Buy one latte, get the second 50% off",
                discountPercent = 50,
                expiryDate = Date(now + 7L * 24 * 60 * 60 * 1000),
                createdAt = now - 86_400_000L,
                isActive = true
            ),
            Coupon(
                id = UUID.randomUUID().toString(),
                ownerId = "owner-demo",
                cafeId = "1",
                title = "Pastry Pairing",
                description = "Free croissant with any pour-over",
                discountPercent = 100,
                expiryDate = Date(now + 14L * 24 * 60 * 60 * 1000),
                createdAt = now - 172_800_000L,
                isActive = true
            )
        )
    }
}
