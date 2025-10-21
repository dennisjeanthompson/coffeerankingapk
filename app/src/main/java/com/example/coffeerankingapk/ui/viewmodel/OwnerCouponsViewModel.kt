package com.example.coffeerankingapk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeerankingapk.data.model.Coupon
import com.example.coffeerankingapk.data.repository.OwnerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OwnerCouponsState(
    val isLoading: Boolean = true,
    val coupons: List<Coupon> = emptyList(),
    val errorMessage: String? = null,
    val creationSuccess: Boolean = false
)

class OwnerCouponsViewModel(
    private val ownerRepository: OwnerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerCouponsState())
    val uiState: StateFlow<OwnerCouponsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ownerRepository.observeCoupons().collect { coupons ->
                _uiState.update {
                    it.copy(isLoading = false, coupons = coupons, errorMessage = null)
                }
            }
        }
    }

    fun createCoupon(
        title: String,
        description: String,
        discountPercent: Int,
        expiryTimestamp: Long?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(creationSuccess = false) }
            val result = ownerRepository.createCoupon(title, description, discountPercent, expiryTimestamp)
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(errorMessage = null, creationSuccess = true)
                } else {
                    it.copy(
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Unable to create coupon",
                        creationSuccess = false
                    )
                }
            }
        }
    }

    fun dismissCreationSuccess() {
        _uiState.update { it.copy(creationSuccess = false) }
    }

    fun deactivateCoupon(couponId: String) {
        viewModelScope.launch {
            val result = ownerRepository.deactivateCoupon(couponId)
            if (result.isFailure) {
                _uiState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.localizedMessage)
                }
            }
        }
    }
}