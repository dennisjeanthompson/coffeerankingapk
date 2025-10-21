package com.example.coffeerankingapk.data.model

data class Cafe(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val rating: Double = 0.0,
    val imageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val favoriteCount: Int = 0,
    val reviewCount: Int = 0
)