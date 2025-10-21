package com.example.coffeerankingapk.data.fake

import com.example.coffeerankingapk.data.model.UserProfile
import com.example.coffeerankingapk.data.model.UserRole
import com.example.coffeerankingapk.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeAuthRepository : AuthRepository {

    private val profileFlow = MutableStateFlow<UserProfile?>(loverProfile)

    override val isSignedIn: Boolean
        get() = profileFlow.value != null

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return runCatching {
            val normalizedEmail = email.trim().ifBlank { profileFlow.value?.email ?: loverProfile.email }
            val selectedProfile = when {
                normalizedEmail.equals(ownerProfile.email, ignoreCase = true) -> ownerProfile
                normalizedEmail.equals(loverProfile.email, ignoreCase = true) -> loverProfile
                else -> {
                    val displayName = normalizedEmail.substringBefore('@', normalizedEmail)
                        .replaceFirstChar { char -> char.uppercase() }
                    (profileFlow.value ?: loverProfile).copy(
                        email = normalizedEmail,
                        name = displayName
                    )
                }
            }
            profileFlow.value = selectedProfile
        }
    }

    override suspend fun signOut() {
        profileFlow.value = loverProfile
    }

    override fun observeUserProfile(): Flow<UserProfile?> = profileFlow

    override suspend fun refreshUserProfile(): UserProfile? = profileFlow.value

    override suspend fun updateUserRole(role: UserRole) {
        profileFlow.update { current ->
            (current ?: loverProfile).copy(role = role)
        }
    }

    override suspend fun updateNotificationPreferences(pushEnabled: Boolean, emailEnabled: Boolean) {
        profileFlow.update { current ->
            (current ?: loverProfile).copy(
                pushNotificationsEnabled = pushEnabled,
                emailUpdatesEnabled = emailEnabled
            )
        }
    }

    companion object {
        private val loverProfile = UserProfile(
            id = "demo-user",
            name = "Coffee Lover",
            email = "lover@example.com",
            role = UserRole.LOVER,
            rank = "#127",
            points = 2_847,
            reviewsCount = 24,
            favoritesCount = 12,
            pushNotificationsEnabled = true,
            emailUpdatesEnabled = false,
            savedCafeIds = setOf("1", "2", "3")
        )

        private val ownerProfile = UserProfile(
            id = "demo-owner",
            name = "Cafe Owner",
            email = "owner@example.com",
            role = UserRole.OWNER,
            rank = "Local Favorite",
            points = 4_200,
            reviewsCount = 0,
            favoritesCount = 0,
            pushNotificationsEnabled = true,
            emailUpdatesEnabled = true,
            savedCafeIds = emptySet()
        )
    }
}
