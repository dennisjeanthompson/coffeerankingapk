package com.example.coffeerankingapk.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeerankingapk.data.model.Coupon
import com.example.coffeerankingapk.data.repository.CouponRepository
import com.example.coffeerankingapk.data.repository.PointsRepository
import com.example.coffeerankingapk.data.model.UserPoints
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class CouponViewModel : ViewModel() {
    private val repository = CouponRepository()
    private val pointsRepository = PointsRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _coupons = MutableStateFlow<List<Coupon>>(emptyList())
    val coupons: StateFlow<List<Coupon>> = _coupons.asStateFlow()
    
    private val _allActiveCoupons = MutableStateFlow<List<Coupon>>(emptyList())
    val allActiveCoupons: StateFlow<List<Coupon>> = _allActiveCoupons.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _operationSuccess = MutableStateFlow<String?>(null)
    val operationSuccess: StateFlow<String?> = _operationSuccess.asStateFlow()
    
    companion object {
        private const val TAG = "CouponViewModel"
    }
    
    /**
     * Load coupons for a specific shop with real-time updates
     */
    fun loadCouponsForShop(shopId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getCouponsForShop(shopId).collect { result ->
                result.onSuccess { couponList ->
                    // Sort by createdAt descending (newest first) in the app
                    _coupons.value = couponList.sortedByDescending { it.createdAt }
                    _isLoading.value = false
                    Log.d(TAG, "Loaded ${couponList.size} coupons for shop: $shopId")
                }.onFailure { e ->
                    _error.value = "Failed to load coupons: ${e.message}"
                    _isLoading.value = false
                    Log.e(TAG, "Error loading coupons", e)
                }
            }
        }
    }
    
    /**
     * Load only active coupons (one-time fetch)
     */
    fun loadActiveCoupons(shopId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getActiveCouponsForShop(shopId).onSuccess { couponList ->
                _coupons.value = couponList
                _isLoading.value = false
            }.onFailure { e ->
                _error.value = "Failed to load active coupons: ${e.message}"
                _isLoading.value = false
                Log.e(TAG, "Error loading active coupons", e)
            }
        }
    }
    
    /**
     * Load ALL active coupons from ALL shops (for lovers/users)
     */
    fun loadAllActiveCoupons() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getAllActiveCoupons().collect { result ->
                result.onSuccess { couponList ->
                    // Sort by expiry date (soonest expiring first)
                    _allActiveCoupons.value = couponList.sortedBy { it.expiryDate }
                    _isLoading.value = false
                    Log.d(TAG, "Loaded ${couponList.size} active coupons from all shops")
                }.onFailure { e ->
                    _error.value = "Failed to load coupons: ${e.message}"
                    _isLoading.value = false
                    Log.e(TAG, "Error loading all active coupons", e)
                }
            }
        }
    }
    
    /**
     * Create a new coupon
     */
    fun createCoupon(
        shopId: String,
        title: String,
        description: String,
        discountPercent: Int,
        discountAmount: Double,
        minimumPurchase: Double,
        startDate: Date,
        expiryDate: Date,
        maxRedemptions: Int,
        code: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val ownerId = auth.currentUser?.uid
            if (ownerId.isNullOrEmpty()) {
                _error.value = "Please sign in as a cafe owner"
                _isLoading.value = false
                return@launch
            }
            
            val coupon = Coupon(
                shopId = shopId,
                ownerId = ownerId,
                title = title,
                description = description,
                discountPercent = discountPercent,
                discountAmount = discountAmount,
                minimumPurchase = minimumPurchase,
                startDate = startDate,
                expiryDate = expiryDate,
                maxRedemptions = maxRedemptions,
                code = code,
                isActive = true
            )
            
            repository.createCoupon(coupon).onSuccess { couponId ->
                _operationSuccess.value = "Coupon created successfully"
                _isLoading.value = false
                Log.d(TAG, "Coupon created: $couponId")
            }.onFailure { e ->
                _error.value = "Failed to create coupon: ${e.message}"
                _isLoading.value = false
                Log.e(TAG, "Error creating coupon", e)
            }
        }
    }
    
    /**
     * Update an existing coupon
     */
    fun updateCoupon(couponId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.updateCoupon(couponId, updates).onSuccess {
                _operationSuccess.value = "Coupon updated successfully"
                _isLoading.value = false
                Log.d(TAG, "Coupon updated: $couponId")
            }.onFailure { e ->
                _error.value = "Failed to update coupon: ${e.message}"
                _isLoading.value = false
                Log.e(TAG, "Error updating coupon", e)
            }
        }
    }
    
    /**
     * Delete a coupon (soft delete by default)
     */
    fun deleteCoupon(couponId: String, hardDelete: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.deleteCoupon(couponId, hardDelete).onSuccess {
                _operationSuccess.value = if (hardDelete) {
                    "Coupon deleted permanently"
                } else {
                    "Coupon deactivated"
                }
                _isLoading.value = false
                Log.d(TAG, "Coupon deleted: $couponId")
            }.onFailure { e ->
                _error.value = "Failed to delete coupon: ${e.message}"
                _isLoading.value = false
                Log.e(TAG, "Error deleting coupon", e)
            }
        }
    }
    
    /**
     * Toggle coupon active/inactive status
     */
    fun toggleCouponStatus(couponId: String, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleCouponStatus(couponId, isActive).onSuccess {
                _operationSuccess.value = if (isActive) {
                    "Coupon activated"
                } else {
                    "Coupon deactivated"
                }
            }.onFailure { e ->
                _error.value = "Failed to toggle coupon status: ${e.message}"
                Log.e(TAG, "Error toggling coupon status", e)
            }
        }
    }
    
    /**
     * Redeem a coupon
     */
    fun redeemCoupon(couponId: String) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.redeemCoupon(couponId, userId).onSuccess {
                _operationSuccess.value = "Coupon redeemed successfully!"
                _isLoading.value = false
                Log.d(TAG, "Coupon redeemed: $couponId")
                
                // Award points for redeeming coupon
                val coupon = _allActiveCoupons.value.find { it.id == couponId }
                    ?: _coupons.value.find { it.id == couponId }
                
                coupon?.let {
                    pointsRepository.awardPoints(
                        userId = userId,
                        points = UserPoints.POINTS_PER_COUPON_REDEEM,
                        action = "coupon_redeem",
                        description = "Redeemed: ${it.title}",
                        relatedId = couponId
                    ).onSuccess {
                        Log.d(TAG, "Awarded ${UserPoints.POINTS_PER_COUPON_REDEEM} points for coupon redemption")
                    }
                }
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to redeem coupon"
                _isLoading.value = false
                Log.e(TAG, "Error redeeming coupon", e)
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Clear success message
     */
    fun clearSuccess() {
        _operationSuccess.value = null
    }
}
