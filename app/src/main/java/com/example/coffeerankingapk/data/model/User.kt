package com.example.coffeerankingapk.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.LOVER,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "role" to role.name,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
    
    companion object {
        fun fromMap(data: Map<String, Any>): User {
            return User(
                uid = data["uid"] as? String ?: "",
                email = data["email"] as? String ?: "",
                displayName = data["displayName"] as? String ?: "",
                role = UserRole.valueOf(data["role"] as? String ?: "LOVER"),
                createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
                updatedAt = data["updatedAt"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}

enum class UserRole {
    OWNER,  // Cafe owner
    LOVER,  // Coffee lover/customer
    BOTH    // Can switch between both roles (optional feature)
}
