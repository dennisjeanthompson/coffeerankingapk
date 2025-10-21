package com.example.coffeerankingapk.data.repository

import com.example.coffeerankingapk.data.model.Cafe
import com.example.coffeerankingapk.data.model.Review
import kotlinx.coroutines.flow.Flow

interface CafeRepository {
    fun observeCafes(): Flow<List<Cafe>>
    fun observeCafe(cafeId: String): Flow<Cafe?>
    fun observeReviews(cafeId: String, limit: Int = 20): Flow<List<Review>>
    fun observeFavoriteIds(): Flow<Set<String>>

    suspend fun toggleFavorite(cafeId: String)
    suspend fun submitReview(
        cafeId: String,
        rating: Int,
        comment: String,
        quickNote: String?,
        isCoffeeHot: Boolean?,
        photoUrl: String?
    ): Result<Unit>

    suspend fun claimReward(cafeId: String): Result<Unit>
}