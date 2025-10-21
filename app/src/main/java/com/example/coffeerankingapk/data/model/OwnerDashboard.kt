package com.example.coffeerankingapk.data.model

data class OwnerDashboard(
    val cafeName: String = "",
    val overview: DashboardOverview = DashboardOverview(),
    val recentActivity: List<OwnerActivity> = emptyList(),
    val reviewsOverTime: List<Double> = emptyList(),
    val recentReviews: List<OwnerReview> = emptyList()
)

data class DashboardOverview(
    val totalReviews: Int = 0,
    val averageRating: Double = 0.0,
    val monthlyVisits: Int = 0,
    val revenue: Double = 0.0,
    val reviewsChange: String = "",
    val ratingChange: String = "",
    val visitsChange: String = "",
    val revenueChange: String = ""
)

data class OwnerActivity(
    val title: String = "",
    val subtitle: String = "",
    val icon: String = "",
    val timestamp: Long = 0L
)

data class OwnerReview(
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Long = 0L
)