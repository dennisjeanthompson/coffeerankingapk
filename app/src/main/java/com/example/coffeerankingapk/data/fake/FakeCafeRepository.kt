package com.example.coffeerankingapk.data.fake

import com.example.coffeerankingapk.data.MockData
import com.example.coffeerankingapk.data.model.Cafe
import com.example.coffeerankingapk.data.model.Review
import com.example.coffeerankingapk.data.repository.CafeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID

class FakeCafeRepository : CafeRepository {

    private val cafesState = MutableStateFlow(MockData.cafes.associateBy { it.id })
    private val favoritesState = MutableStateFlow(setOf("1", "2"))
    private val reviewsState = MutableStateFlow(initialReviews())

    override fun observeCafes(): Flow<List<Cafe>> = cafesState.map { it.values.toList() }

    override fun observeCafe(cafeId: String): Flow<Cafe?> = cafesState.map { it[cafeId] }

    override fun observeReviews(cafeId: String, limit: Int): Flow<List<Review>> =
        reviewsState.map { reviews ->
            reviews[cafeId].orEmpty()
                .sortedByDescending { it.createdAt }
                .take(limit)
        }

    override fun observeFavoriteIds(): Flow<Set<String>> = favoritesState

    override suspend fun toggleFavorite(cafeId: String) {
        favoritesState.update { favorites ->
            if (favorites.contains(cafeId)) favorites - cafeId else favorites + cafeId
        }
    }

    override suspend fun submitReview(
        cafeId: String,
        rating: Int,
        comment: String,
        quickNote: String?,
        isCoffeeHot: Boolean?,
        photoUrl: String?
    ): Result<Unit> {
        return runCatching {
            var updatedReviews: List<Review> = emptyList()
            reviewsState.update { current ->
                val updated = current.toMutableMap()
                val newReview = Review(
                    id = UUID.randomUUID().toString(),
                    userId = "demo-user",
                    userName = "Coffee Lover",
                    rating = rating,
                    comment = comment.ifBlank { quickNote ?: "Loved the coffee!" },
                    quickNote = quickNote,
                    isCoffeeHot = isCoffeeHot,
                    photoUrl = photoUrl,
                    createdAt = System.currentTimeMillis()
                )
                val list = current[cafeId].orEmpty().toMutableList()
                list.add(0, newReview)
                updated[cafeId] = list
                updatedReviews = list
                updated
            }

            cafesState.update { current ->
                val updated = current.toMutableMap()
                val cafe = current[cafeId]
                if (cafe != null) {
                    val average = updatedReviews.map { it.rating }.average().takeIf { !it.isNaN() } ?: cafe.rating
                    updated[cafeId] = cafe.copy(
                        rating = average,
                        reviewCount = updatedReviews.size
                    )
                }
                updated
            }
        }
    }

    override suspend fun claimReward(cafeId: String): Result<Unit> = Result.success(Unit)

    private fun initialReviews(): Map<String, List<Review>> {
        val now = System.currentTimeMillis()
        return MockData.cafes.associate { cafe ->
            val samples = listOf(
                Review(
                    id = "${cafe.id}-1",
                    userId = "user-a",
                    userName = "Sam Torres",
                    rating = 5,
                    comment = "Incredible espresso and cozy vibe!",
                    createdAt = now - 86_400_000L
                ),
                Review(
                    id = "${cafe.id}-2",
                    userId = "user-b",
                    userName = "Jamie Lee",
                    rating = 4,
                    comment = "Loved the pour-over selection.",
                    createdAt = now - 172_800_000L
                ),
                Review(
                    id = "${cafe.id}-3",
                    userId = "user-c",
                    userName = "Morgan Park",
                    rating = 5,
                    comment = "Friendly staff and perfect latte art!",
                    createdAt = now - 259_200_000L
                )
            )
            cafe.id to samples
        }
    }
}
