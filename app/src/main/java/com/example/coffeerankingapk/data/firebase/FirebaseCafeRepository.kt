package com.example.coffeerankingapk.data.firebase

import com.example.coffeerankingapk.data.model.Cafe
import com.example.coffeerankingapk.data.model.Review
import com.example.coffeerankingapk.data.repository.CafeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseCafeRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : CafeRepository {

    private val cafesFlow = MutableStateFlow<List<Cafe>>(emptyList())
    private val favoritesFlow = MutableStateFlow<Set<String>>(emptySet())
    private var cafesListener: ListenerRegistration? = null
    private var favoritesListener: ListenerRegistration? = null

    init {
        subscribeToCafes()
        subscribeToFavorites()
        auth.addAuthStateListener {
            subscribeToFavorites()
        }
    }

    override fun observeCafes(): Flow<List<Cafe>> = cafesFlow

    override fun observeCafe(cafeId: String): Flow<Cafe?> {
        return callbackFlow {
            val registration = firestore.collection(CAFES_COLLECTION)
                .document(cafeId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(null)
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.toCafe())
                }
            awaitClose { registration.remove() }
        }
    }

    override fun observeReviews(cafeId: String, limit: Int): Flow<List<Review>> {
        return callbackFlow {
            val registration = firestore.collection(CAFES_COLLECTION)
                .document(cafeId)
                .collection(REVIEWS_COLLECTION)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val reviews = snapshot?.documents.orEmpty().mapNotNull { it.toReview() }
                    trySend(reviews)
                }
            awaitClose { registration.remove() }
        }
    }

    override fun observeFavoriteIds(): Flow<Set<String>> = favoritesFlow

    override suspend fun toggleFavorite(cafeId: String) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in")
        val favoriteRef = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(FAVORITES_COLLECTION)
            .document(cafeId)

        val snapshot = favoriteRef.get().await()
        if (snapshot.exists()) {
            favoriteRef.delete().await()
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .update("favoritesCount", FieldValue.increment(-1))
        } else {
            favoriteRef.set(mapOf("createdAt" to System.currentTimeMillis())).await()
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .update("favoritesCount", FieldValue.increment(1))
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
            val firebaseUser = auth.currentUser ?: throw IllegalStateException("User must be signed in")
            val reviewData = mapOf(
                "userId" to firebaseUser.uid,
                "userName" to (firebaseUser.displayName ?: firebaseUser.email?.substringBefore('@') ?: "Coffee Lover"),
                "rating" to rating,
                "comment" to comment,
                "quickNote" to quickNote,
                "isCoffeeHot" to isCoffeeHot,
                "photoUrl" to photoUrl,
                "createdAt" to System.currentTimeMillis()
            )

            val cafeRef = firestore.collection(CAFES_COLLECTION).document(cafeId)

            firestore.runTransaction { transaction ->
                val cafeSnapshot = transaction.get(cafeRef)
                val currentReviewCount = cafeSnapshot.getLong("reviewCount")?.toInt() ?: 0
                val currentAverage = cafeSnapshot.getDouble("rating") ?: 0.0

                val newReviewCount = currentReviewCount + 1
                val newAverage = if (currentReviewCount == 0) {
                    rating.toDouble()
                } else {
                    ((currentAverage * currentReviewCount) + rating) / newReviewCount
                }

                val reviewsCollection = cafeRef.collection(REVIEWS_COLLECTION)
                transaction.set(reviewsCollection.document(), reviewData)
                transaction.update(
                    cafeRef,
                    mapOf(
                        "rating" to newAverage,
                        "reviewCount" to newReviewCount
                    )
                )

                val userRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
                transaction.update(userRef, "reviewsCount", FieldValue.increment(1))
            }.await()
        }
    }

    override suspend fun claimReward(cafeId: String): Result<Unit> {
        return runCatching {
            val firebaseUser = auth.currentUser ?: throw IllegalStateException("User must be signed in")
            val rewardsRef = firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .collection(REWARDS_COLLECTION)
                .document(cafeId)

            rewardsRef.set(
                mapOf(
                    "cafeId" to cafeId,
                    "claimedAt" to System.currentTimeMillis()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        }
    }

    private fun subscribeToCafes() {
        cafesListener?.remove()
        cafesListener = firestore.collection(CAFES_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    cafesFlow.value = emptyList()
                    return@addSnapshotListener
                }
                val cafes = snapshot?.documents.orEmpty().mapNotNull { it.toCafe() }
                cafesFlow.value = cafes
            }
    }

    private fun subscribeToFavorites() {
        favoritesListener?.remove()
        favoritesListener = null
        val uid = auth.currentUser?.uid ?: run {
            favoritesFlow.value = emptySet()
            return
        }
        favoritesListener = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(FAVORITES_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    favoritesFlow.value = emptySet()
                    return@addSnapshotListener
                }
                val favorites = snapshot?.documents.orEmpty().map { it.id }.toSet()
                favoritesFlow.value = favorites
            }
    }

    private fun DocumentSnapshot.toCafe(): Cafe? {
        if (!exists()) return null
        return Cafe(
            id = id,
            name = getString("name") ?: "",
            description = getString("description") ?: "",
            address = getString("address") ?: "",
            rating = getDouble("rating") ?: 0.0,
            imageUrl = getString("imageUrl") ?: "",
            latitude = getDouble("latitude") ?: 0.0,
            longitude = getDouble("longitude") ?: 0.0,
            favoriteCount = getLong("favoriteCount")?.toInt() ?: 0,
            reviewCount = getLong("reviewCount")?.toInt() ?: 0
        )
    }

    private fun DocumentSnapshot.toReview(): Review? {
        if (!exists()) return null
        return Review(
            id = id,
            userId = getString("userId") ?: "",
            userName = getString("userName") ?: "Anonymous",
            rating = getLong("rating")?.toInt() ?: 0,
            comment = getString("comment") ?: "",
            quickNote = getString("quickNote"),
            isCoffeeHot = getBoolean("isCoffeeHot"),
            photoUrl = getString("photoUrl"),
            createdAt = getLong("createdAt") ?: 0L
        )
    }

    companion object {
        private const val CAFES_COLLECTION = "cafes"
        private const val USERS_COLLECTION = "users"
        private const val FAVORITES_COLLECTION = "favorites"
        private const val REVIEWS_COLLECTION = "reviews"
        private const val REWARDS_COLLECTION = "rewards"
    }
}