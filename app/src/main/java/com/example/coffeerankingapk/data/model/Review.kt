package com.example.coffeerankingapk.data.model

data class Review(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val quickNote: String? = null,
    val isCoffeeHot: Boolean? = null,
    val photoUrl: String? = null,
    val createdAt: Long = 0L
)