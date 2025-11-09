package com.example.coffeerankingapk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeerankingapk.data.model.CoffeeShop
import com.example.coffeerankingapk.data.repository.CoffeeShopRepository
import com.example.coffeerankingapk.util.SampleDataSeeder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class CoffeeShopViewModel : ViewModel() {
    private val repository = CoffeeShopRepository()
    
    // Baguio City coordinates
    private val baguioLat = 16.4023
    private val baguioLng = 120.5960
    private val maxRadiusKm = 100.0 // 100km radius to cover nearby areas in Luzon
    
    private val _allCoffeeShops = MutableStateFlow<List<CoffeeShop>>(emptyList())
    
    private val _coffeeShops = MutableStateFlow<List<CoffeeShop>>(emptyList())
    val coffeeShops: StateFlow<List<CoffeeShop>> = _coffeeShops.asStateFlow()
    
    private val _selectedShop = MutableStateFlow<CoffeeShop?>(null)
    val selectedShop: StateFlow<CoffeeShop?> = _selectedShop.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    enum class FilterMode { StrictCoffeeOnly, FlexibleCafeAndCoffee, AllNearby }
    private val _filterMode = MutableStateFlow(FilterMode.FlexibleCafeAndCoffee)
    val filterMode: StateFlow<FilterMode> = _filterMode.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _ratingSubmitted = MutableStateFlow(false)
    val ratingSubmitted: StateFlow<Boolean> = _ratingSubmitted.asStateFlow()

    // Dev-only fallback to seed sample data once when empty
    private var didAttemptSeed: Boolean = false
    
    init {
        loadCoffeeShops()
    }
    
    fun loadCoffeeShops() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getAllCoffeeShops()
                .onSuccess { shops ->
                    // 1) Radius filter (near Baguio)
                    val nearbyShops = shops.filter { shop ->
                        shop.location?.let { loc ->
                            calculateDistance(baguioLat, baguioLng, loc.latitude, loc.longitude) <= maxRadiusKm
                        } ?: false
                    }

                    // 2) If empty and allowed, optional single-time seeding for dev convenience
                    val debugFallback = !didAttemptSeed && nearbyShops.isEmpty() &&
                        java.lang.Boolean.getBoolean("coffeeranking.debug.seed")
                    if (debugFallback) {
                        didAttemptSeed = true
                        android.util.Log.w("CoffeeShopViewModel", "No nearby shops found. Seeding sample data (debug only)...")
                        try { SampleDataSeeder.seedSampleCoffeeShops() } catch (_: Exception) {}
                        delay(1500)
                        val seeded = repository.getAllCoffeeShops().getOrNull()
                        val seededNearby = (seeded ?: emptyList()).filter { shop ->
                            shop.location?.let { loc ->
                                calculateDistance(baguioLat, baguioLng, loc.latitude, loc.longitude) <= maxRadiusKm
                            } ?: false
                        }
                        _allCoffeeShops.value = seededNearby
                        applySearchFilter()
                        android.util.Log.d("CoffeeShopViewModel", "After seeding, loaded ${seededNearby.size} shops")
                        _isLoading.value = false
                        return@onSuccess
                    }

                    _allCoffeeShops.value = nearbyShops
                    applySearchFilter()
                    android.util.Log.d("CoffeeShopViewModel", "Loaded ${nearbyShops.size} nearby shops (pre-filter mode)")
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to load coffee shops"
                    android.util.Log.e("CoffeeShopViewModel", "Error loading shops", e)
                }

            _isLoading.value = false
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applySearchFilter()
    }

    fun updateFilterMode(mode: FilterMode) {
        _filterMode.value = mode
        applySearchFilter()
    }
    
    private fun applySearchFilter() {
        val base = _allCoffeeShops.value.filter { shop ->
            matchesFilterMode(shop, _filterMode.value)
        }
        _coffeeShops.value = if (_searchQuery.value.isEmpty()) base else base.filter { shop ->
            shop.name.contains(_searchQuery.value, ignoreCase = true) ||
            shop.address.contains(_searchQuery.value, ignoreCase = true) ||
            shop.type.contains(_searchQuery.value, ignoreCase = true) ||
            shop.description.contains(_searchQuery.value, ignoreCase = true)
        }
    }

    private fun matchesFilterMode(shop: CoffeeShop, mode: FilterMode): Boolean {
        val name = shop.name
        val type = shop.type
        val desc = shop.description
        
        // Strict: only strong coffee-specific terms
        val strictKeywords = listOf(
            "coffee", "coffee shop", "coffee house", "espresso", "espresso bar", 
            "barista", "roastery", "roaster", "brew"
        )
        
        // Flexible: coffee + cafe variations + drinks (DEFAULT - most permissive while staying on-brand)
        val flexibleKeywords = listOf(
            // Coffee variations
            "coffee", "kape", "kapé", "kopi", "café", "cafe", "caffe", "caffeine",
            "coffee shop", "coffee house", "coffeehouse", "coffee bar",
            // Cafe variations (critical for your use case!)
            "cafe", "café", "caffe", "caffè", "kafé", "kafe", 
            // Drinks
            "espresso", "latte", "cappuccino", "americano", "macchiato", "mocha",
            "flat white", "cortado", "ristretto", "doppio", "lungo",
            // Activities/concepts
            "brew", "brewed", "roast", "roasted", "roastery", "roaster", "barista"
        )
        
        fun containsAny(keywords: List<String>): Boolean = keywords.any { kw ->
            name.contains(kw, ignoreCase = true) || 
            type.contains(kw, ignoreCase = true) || 
            desc.contains(kw, ignoreCase = true)
        }
        
        return when (mode) {
            FilterMode.StrictCoffeeOnly -> containsAny(strictKeywords)
            FilterMode.FlexibleCafeAndCoffee -> containsAny(flexibleKeywords) // This should catch "cafe" in any case
            FilterMode.AllNearby -> true
        }
    }
    
    // Calculate distance between two coordinates using Haversine formula
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return earthRadiusKm * c
    }
    
    fun selectShop(shop: CoffeeShop?) {
        _selectedShop.value = shop
        android.util.Log.d("CoffeeShopViewModel", "Selected shop: ${shop?.name ?: "null"}")
    }
    
    fun submitRating(shopId: String, userId: String, ratingValue: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _ratingSubmitted.value = false
            
            repository.submitRating(shopId, userId, ratingValue)
                .onSuccess {
                    _ratingSubmitted.value = true
                    android.util.Log.d("CoffeeShopViewModel", "Rating submitted successfully")
                    // Immediately reload shops to get updated ratings
                    loadCoffeeShops()
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to submit rating"
                    android.util.Log.e("CoffeeShopViewModel", "Error submitting rating", e)
                }
            
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun resetRatingSubmitted() {
        _ratingSubmitted.value = false
    }
}
