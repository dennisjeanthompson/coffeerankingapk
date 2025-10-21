package com.example.coffeerankingapk.data.model

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.LOVER,
    val rank: String = "",
    val points: Int = 0,
    val reviewsCount: Int = 0,
    val favoritesCount: Int = 0,
    val pushNotificationsEnabled: Boolean = true,
    val emailUpdatesEnabled: Boolean = false,
    val savedCafeIds: Set<String> = emptySet()
)