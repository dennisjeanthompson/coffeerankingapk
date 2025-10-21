package com.example.coffeerankingapk.data.firebase

import com.example.coffeerankingapk.data.model.UserProfile
import com.example.coffeerankingapk.data.model.UserRole
import com.example.coffeerankingapk.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private val userProfileFlow = MutableStateFlow<UserProfile?>(null)
    private var profileListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            subscribeToProfile(firebaseAuth.currentUser?.uid)
        }
        subscribeToProfile(auth.currentUser?.uid)
    }

    override val isSignedIn: Boolean
        get() = auth.currentUser != null

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return runCatching {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            ensureUserDocument()
        }
    }

    override suspend fun signOut() {
        profileListener?.remove()
        profileListener = null
        userProfileFlow.value = null
        auth.signOut()
    }

    override fun observeUserProfile(): Flow<UserProfile?> = userProfileFlow

    override suspend fun refreshUserProfile(): UserProfile? {
        ensureUserDocument()
        val uid = auth.currentUser?.uid ?: return null
        val snapshot = firestore.collection(USERS_COLLECTION).document(uid).get().await()
        val profile = snapshot.toUserProfile(uid)
        userProfileFlow.update { profile }
        return profile
    }

    override suspend fun updateUserRole(role: UserRole) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in")
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .set(mapOf("role" to role.name), SetOptions.merge())
            .await()
        refreshUserProfile()
    }

    override suspend fun updateNotificationPreferences(
        pushEnabled: Boolean,
        emailEnabled: Boolean
    ) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in")
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .set(
                mapOf(
                    "pushNotificationsEnabled" to pushEnabled,
                    "emailUpdatesEnabled" to emailEnabled
                ),
                SetOptions.merge()
            ).await()
        refreshUserProfile()
    }

    private fun subscribeToProfile(uid: String?) {
        profileListener?.remove()
        profileListener = null

        if (uid == null) {
            userProfileFlow.value = null
            return
        }

        profileListener = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val profile = snapshot?.toUserProfile(uid)
                userProfileFlow.value = profile
            }
    }

    private suspend fun ensureUserDocument() {
        val firebaseUser = auth.currentUser ?: return
        val userDoc = firestore.collection(USERS_COLLECTION)
            .document(firebaseUser.uid)
            .get()
            .await()

        if (!userDoc.exists()) {
            val newUser = mapOf(
                "name" to (firebaseUser.displayName ?: firebaseUser.email?.substringBefore('@') ?: "Coffee Lover"),
                "email" to (firebaseUser.email ?: ""),
                "role" to UserRole.LOVER.name,
                "rank" to "#1000",
                "points" to 0,
                "reviewsCount" to 0,
                "favoritesCount" to 0,
                "pushNotificationsEnabled" to true,
                "emailUpdatesEnabled" to false,
                "savedCafeIds" to emptyList<String>(),
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .set(newUser)
                .await()
        }
    }

    private fun DocumentSnapshot.toUserProfile(uid: String): UserProfile? {
        if (!exists()) return null
        return UserProfile(
            id = uid,
            name = getString("name") ?: "Coffee Lover",
            email = getString("email") ?: "",
            role = runCatching { UserRole.valueOf(getString("role") ?: UserRole.LOVER.name) }.getOrDefault(UserRole.LOVER),
            rank = getString("rank") ?: "",
            points = getLong("points")?.toInt() ?: 0,
            reviewsCount = getLong("reviewsCount")?.toInt() ?: 0,
            favoritesCount = getLong("favoritesCount")?.toInt() ?: 0,
            pushNotificationsEnabled = getBoolean("pushNotificationsEnabled") ?: true,
            emailUpdatesEnabled = getBoolean("emailUpdatesEnabled") ?: false,
            savedCafeIds = (get("savedCafeIds") as? List<*>)
                ?.mapNotNull { it as? String }
                ?.toSet()
                ?: emptySet()
        )
    }

    companion object {
        private const val USERS_COLLECTION = "users"
    }
}