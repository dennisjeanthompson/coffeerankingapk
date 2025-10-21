package com.example.coffeerankingapk.data.model

data class Reward(
    val id: String = "",
    val cafeId: String = "",
    val title: String = "",
    val description: String = "",
    val pointsRequired: Int = 0,
    val claimedAt: Long? = null
)