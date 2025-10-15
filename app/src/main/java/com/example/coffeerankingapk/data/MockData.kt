package com.example.coffeerankingapk.data

data class Cafe(
    val id: String,
    val name: String,
    val description: String,
    val address: String,
    val rating: Float,
    val imageUrl: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

object MockData {
    val cafes = listOf(
        Cafe(
            id = "1",
            name = "Brew & Beans",
            description = "Artisanal coffee and fresh pastries",
            address = "123 Coffee Street, Downtown",
            rating = 4.5f,
            imageUrl = "https://via.placeholder.com/300x200/6B3E2A/FFFFFF?text=Brew+%26+Beans",
            latitude = 40.7580,
            longitude = -73.9855
        ),
        Cafe(
            id = "2", 
            name = "The Roastery",
            description = "Premium single-origin coffee",
            address = "456 Bean Avenue, Midtown",
            rating = 4.7f,
            imageUrl = "https://via.placeholder.com/300x200/6B3E2A/FFFFFF?text=The+Roastery",
            latitude = 40.7489,
            longitude = -73.9680
        ),
        Cafe(
            id = "3",
            name = "Coffee Corner",
            description = "Cozy neighborhood cafe",
            address = "789 Espresso Lane, Uptown",
            rating = 4.3f,
            imageUrl = "https://via.placeholder.com/300x200/6B3E2A/FFFFFF?text=Coffee+Corner",
            latitude = 40.7829,
            longitude = -73.9654
        ),
        Cafe(
            id = "4",
            name = "Steam & Grind", 
            description = "Modern coffee experience",
            address = "321 Latte Street, Downtown",
            rating = 4.6f,
            imageUrl = "https://via.placeholder.com/300x200/6B3E2A/FFFFFF?text=Steam+%26+Grind",
            latitude = 40.7614,
            longitude = -73.9776
        ),
        Cafe(
            id = "5",
            name = "Morning Brew",
            description = "Perfect start to your day",
            address = "654 Morning Drive, Westside",
            rating = 4.4f,
            imageUrl = "https://via.placeholder.com/300x200/6B3E2A/FFFFFF?text=Morning+Brew",
            latitude = 40.7505,
            longitude = -74.0027
        ),
        Cafe(
            id = "6",
            name = "Caffeine Fix",
            description = "Strong coffee for busy people",
            address = "987 Rush Street, Business District", 
            rating = 4.2f,
            imageUrl = "https://via.placeholder.com/300x200/6B3E2A/FFFFFF?text=Caffeine+Fix",
            latitude = 40.7549,
            longitude = -73.9840
        )
    )
}