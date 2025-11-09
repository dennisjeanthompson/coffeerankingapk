package com.example.coffeerankingapk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeerankingapk.data.model.CoffeeShop
import com.example.coffeerankingapk.data.repository.CoffeeShopRepository
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddCoffeeShopFormState(
    val name: String = "",
    val type: String = "Coffee Shop",
    val address: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val nameError: String? = null,
    val addressError: String? = null,
    val locationError: String? = null
)

class AddCoffeeShopViewModel : ViewModel() {
    private val repository = CoffeeShopRepository()
    
    private val _formState = MutableStateFlow(AddCoffeeShopFormState())
    val formState: StateFlow<AddCoffeeShopFormState> = _formState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()
    
    fun updateName(name: String) {
        _formState.value = _formState.value.copy(
            name = name,
            nameError = null
        )
    }
    
    fun updateType(type: String) {
        _formState.value = _formState.value.copy(type = type)
    }
    
    fun updateAddress(address: String) {
        _formState.value = _formState.value.copy(
            address = address,
            addressError = null
        )
    }
    
    fun updateDescription(description: String) {
        _formState.value = _formState.value.copy(description = description)
    }
    
    fun updateImageUrl(imageUrl: String) {
        _formState.value = _formState.value.copy(imageUrl = imageUrl)
    }
    
    fun updateLocation(latitude: Double, longitude: Double) {
        _formState.value = _formState.value.copy(
            latitude = latitude,
            longitude = longitude,
            locationError = null
        )
        android.util.Log.d("AddCoffeeShopViewModel", "Location updated: $latitude, $longitude")
    }
    
    private fun validateForm(): Boolean {
        val state = _formState.value
        var isValid = true
        
        // Validate name
        if (state.name.isBlank()) {
            _formState.value = state.copy(nameError = "Shop name is required")
            isValid = false
        }
        
        // Validate address
        if (state.address.isBlank()) {
            _formState.value = _formState.value.copy(addressError = "Address is required")
            isValid = false
        }
        
        // Validate location
        if (state.latitude == null || state.longitude == null) {
            _formState.value = _formState.value.copy(locationError = "Please select a location on the map")
            isValid = false
        }
        
        return isValid
    }
    
    fun saveCoffeeShop(ownerId: String) {
        if (!validateForm()) {
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val state = _formState.value
            val coffeeShop = CoffeeShop(
                name = state.name.trim(),
                type = state.type.trim().ifBlank { "Coffee Shop" },
                address = state.address.trim(),
                description = state.description.trim(),
                imageUrl = state.imageUrl.trim(),
                location = GeoPoint(state.latitude!!, state.longitude!!),
                ownerId = ownerId,
                averageRating = 0.0,
                totalRatings = 0
            )
            
            android.util.Log.d("AddCoffeeShopViewModel", "Saving coffee shop: ${coffeeShop.name} at ${coffeeShop.location}")
            
            repository.addCoffeeShop(coffeeShop)
                .onSuccess { shopId ->
                    android.util.Log.d("AddCoffeeShopViewModel", "Coffee shop saved successfully with ID: $shopId")
                    _success.value = true
                }
                .onFailure { e ->
                    android.util.Log.e("AddCoffeeShopViewModel", "Error saving coffee shop", e)
                    _error.value = e.message ?: "Failed to save coffee shop"
                }
            
            _isLoading.value = false
        }
    }
    
    fun resetSuccess() {
        _success.value = false
    }
}
