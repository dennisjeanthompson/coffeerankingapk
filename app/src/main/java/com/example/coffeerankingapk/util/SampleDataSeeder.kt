package com.example.coffeerankingapk.util

import android.util.Log
import com.example.coffeerankingapk.data.model.CoffeeShop
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

/**
 * Utility to add sample coffee shops to Firestore for testing.
 * Call this once from MainActivity or a debug screen.
 */
object SampleDataSeeder {
    
    fun seedSampleCoffeeShops() {
        val db = FirebaseFirestore.getInstance()
        
        // Coffee shops in Baguio City and nearby areas in Northern Luzon, Philippines
        val sampleShops = listOf(
            CoffeeShop(
                id = "baguio_shop1",
                name = "Baguio Brew",
                type = "coffee shop",
                location = GeoPoint(16.4023, 120.5960), // Baguio City Center
                averageRating = 4.5,
                totalRatings = 89,
                description = "Specialty coffee with a mountain view",
                address = "Session Road, Baguio City, Benguet"
            ),
            CoffeeShop(
                id = "baguio_shop2",
                name = "Hill Station Coffee",
                type = "Coffee Shop", // Capitalized
                location = GeoPoint(16.4113, 120.5927), // Burnham Park area
                averageRating = 4.7,
                totalRatings = 156,
                description = "Cozy café near Burnham Park",
                address = "Harrison Road, Baguio City, Benguet"
            ),
            CoffeeShop(
                id = "baguio_shop3",
                name = "Cordillera Café",
                type = "cafe",
                location = GeoPoint(16.3950, 120.5850), // Lower Baguio
                averageRating = 4.3,
                totalRatings = 67,
                description = "Traditional coffee with local pastries",
                address = "Lower Session Road, Baguio City"
            ),
            CoffeeShop(
                id = "baguio_shop4",
                name = "Pine Ridge Espresso",
                type = "Espresso Bar",
                location = GeoPoint(16.4180, 120.5980), // Upper Baguio
                averageRating = 4.6,
                totalRatings = 112,
                description = "Artisan espresso in the pines",
                address = "Leonard Wood Road, Baguio City"
            ),
            CoffeeShop(
                id = "baguio_shop5",
                name = "Café de Cordillera",
                type = "Café", // With accent
                location = GeoPoint(16.3880, 120.5920), // South Baguio
                averageRating = 4.4,
                totalRatings = 94,
                description = "Mountain-grown arabica coffee",
                address = "South Drive, Baguio City"
            ),
            CoffeeShop(
                id = "baguio_shop6",
                name = "The Baguio Coffee House",
                type = "COFFEE HOUSE", // All caps
                location = GeoPoint(16.4100, 120.6010), // East Baguio
                averageRating = 4.2,
                totalRatings = 78,
                description = "Local favorite since 1980",
                address = "Governor Pack Road, Baguio City"
            ),
            CoffeeShop(
                id = "baguio_shop7",
                name = "Session Road Brew",
                type = "coffee shop",
                location = GeoPoint(16.4050, 120.5975), // Session Road
                averageRating = 4.8,
                totalRatings = 203,
                description = "Popular spot for students and tourists",
                address = "Upper Session Road, Baguio City"
            ),
            CoffeeShop(
                id = "nearby_shop1",
                name = "La Trinidad Coffee Co.",
                type = "coffee shop",
                location = GeoPoint(16.4620, 120.5920), // La Trinidad (nearby town)
                averageRating = 4.5,
                totalRatings = 45,
                description = "Farm-to-cup coffee experience",
                address = "Km 5, La Trinidad, Benguet"
            )
        )
        
        val batch = db.batch()
        
        sampleShops.forEach { shop ->
            val docRef = db.collection("coffeeShops").document(shop.id)
            batch.set(docRef, shop.toMap())
        }
        
        batch.commit()
            .addOnSuccessListener {
                Log.d("SampleDataSeeder", "Successfully added ${sampleShops.size} coffee shops in Baguio area")
            }
            .addOnFailureListener { e ->
                Log.e("SampleDataSeeder", "Error adding sample shops", e)
            }
    }
}
