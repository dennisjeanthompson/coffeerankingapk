package com.example.coffeerankingapk.data.firebase

import com.example.coffeerankingapk.data.model.Coupon
import com.example.coffeerankingapk.data.model.DashboardOverview
import com.example.coffeerankingapk.data.model.OwnerActivity
import com.example.coffeerankingapk.data.model.OwnerDashboard
import com.example.coffeerankingapk.data.repository.OwnerRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import com.example.coffeerankingapk.data.model.OwnerReview

class FirebaseOwnerRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : OwnerRepository {

    private val dashboardFlow = MutableStateFlow(OwnerDashboard())
    private var dashboardListener: ListenerRegistration? = null
    private var activityListener: ListenerRegistration? = null
    private var reviewsListener: ListenerRegistration? = null

    init {
        subscribeToDashboard()
        auth.addAuthStateListener {
            subscribeToDashboard()
        }
    }

    override fun observeDashboard(): Flow<OwnerDashboard> = dashboardFlow

    override fun observeCoupons(): Flow<List<Coupon>> {
        return callbackFlow {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            val registration = firestore.collection(OWNERS_COLLECTION)
                .document(uid)
                .collection(COUPONS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val coupons = snapshot?.documents.orEmpty().mapNotNull { it.toCoupon(uid) }
                    trySend(coupons)
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun createCoupon(
        title: String,
        description: String,
        discountPercent: Int,
        expiryTimestamp: Long?
    ): Result<Unit> {
        return runCatching {
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("Owner must be signed in")
            val couponRef = firestore.collection(OWNERS_COLLECTION)
                .document(uid)
                .collection(COUPONS_COLLECTION)
                .document()
            val coupon = mapOf(
                "title" to title,
                "description" to description,
                "discountPercent" to discountPercent,
                "expiryDate" to expiryTimestamp,
                "isActive" to true,
                "createdAt" to System.currentTimeMillis(),
                "ownerId" to uid
            )
            couponRef.set(coupon).await()
        }
    }

    override suspend fun deactivateCoupon(couponId: String): Result<Unit> {
        return runCatching {
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("Owner must be signed in")
            firestore.collection(OWNERS_COLLECTION)
                .document(uid)
                .collection(COUPONS_COLLECTION)
                .document(couponId)
                .set(mapOf("isActive" to false), SetOptions.merge())
                .await()
        }
    }

    private fun subscribeToDashboard() {
        dashboardListener?.remove()
    activityListener?.remove()
    reviewsListener?.remove()
        val uid = auth.currentUser?.uid ?: run {
            dashboardFlow.value = OwnerDashboard()
            return
        }

        dashboardListener = firestore.collection(OWNERS_COLLECTION)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    dashboardFlow.value = OwnerDashboard()
                    return@addSnapshotListener
                }
                val current = dashboardFlow.value
                val updated = snapshot?.toOwnerDashboard(
                    existingActivity = current.recentActivity,
                    existingReviews = current.recentReviews
                ) ?: OwnerDashboard()
                dashboardFlow.value = updated
            }

        activityListener = firestore.collection(OWNERS_COLLECTION)
            .document(uid)
            .collection(ACTIVITY_COLLECTION)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    dashboardFlow.value = dashboardFlow.value.copy(recentActivity = emptyList())
                    return@addSnapshotListener
                }
                val activity = snapshot?.documents.orEmpty().mapNotNull { it.toOwnerActivity() }
                dashboardFlow.value = dashboardFlow.value.copy(recentActivity = activity)
            }

        reviewsListener = firestore.collection(OWNERS_COLLECTION)
            .document(uid)
            .collection(REVIEWS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    dashboardFlow.value = dashboardFlow.value.copy(recentReviews = emptyList())
                    return@addSnapshotListener
                }
                val reviews = snapshot?.documents.orEmpty().mapNotNull { it.toOwnerReview() }
                dashboardFlow.value = dashboardFlow.value.copy(recentReviews = reviews)
            }
    }

    private fun DocumentSnapshot.toOwnerDashboard(
        existingActivity: List<OwnerActivity>,
        existingReviews: List<OwnerReview>
    ): OwnerDashboard {
        return OwnerDashboard(
            cafeName = getString("cafeName") ?: "",
            overview = DashboardOverview(
                totalReviews = getLong("totalReviews")?.toInt() ?: 0,
                averageRating = getDouble("averageRating") ?: 0.0,
                monthlyVisits = getLong("monthlyVisits")?.toInt() ?: 0,
                revenue = getDouble("revenue") ?: 0.0,
                reviewsChange = getString("reviewsChange") ?: "",
                ratingChange = getString("ratingChange") ?: "",
                visitsChange = getString("visitsChange") ?: "",
                revenueChange = getString("revenueChange") ?: ""
            ),
            recentActivity = existingActivity,
            reviewsOverTime = (get("reviewsOverTime") as? List<*>)
                ?.mapNotNull {
                    when (it) {
                        is Number -> it.toDouble()
                        is String -> it.toDoubleOrNull()
                        else -> null
                    }
                }
                ?: emptyList(),
            recentReviews = existingReviews
        )
    }

    private fun DocumentSnapshot.toOwnerActivity(): OwnerActivity? {
        if (!exists()) return null
        return OwnerActivity(
            title = getString("title") ?: "",
            subtitle = getString("subtitle") ?: "",
            icon = getString("icon") ?: "",
            timestamp = getLong("timestamp") ?: 0L
        )
    }

    private fun DocumentSnapshot.toCoupon(ownerId: String): Coupon? {
        if (!exists()) return null
        val expiryTimestamp = getLong("expiryDate")
        return Coupon(
            id = id,
            ownerId = ownerId,
            cafeId = getString("cafeId") ?: "",
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            discountPercent = getLong("discountPercent")?.toInt() ?: 0,
            expiryDate = expiryTimestamp?.let { Date(it) },
            createdAt = getLong("createdAt") ?: 0L,
            isActive = getBoolean("isActive") ?: true
        )
    }

    private fun DocumentSnapshot.toOwnerReview(): OwnerReview? {
        if (!exists()) return null
        return OwnerReview(
            userName = getString("userName") ?: "",
            rating = getLong("rating")?.toInt() ?: 0,
            comment = getString("comment") ?: "",
            createdAt = getLong("createdAt") ?: 0L
        )
    }

    companion object {
        private const val OWNERS_COLLECTION = "owners"
        private const val COUPONS_COLLECTION = "coupons"
        private const val ACTIVITY_COLLECTION = "activity"
        private const val REVIEWS_COLLECTION = "reviews"
    }
}