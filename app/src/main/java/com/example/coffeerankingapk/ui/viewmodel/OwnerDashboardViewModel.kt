package com.example.coffeerankingapk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeerankingapk.data.model.Coupon
import com.example.coffeerankingapk.data.model.OwnerDashboard
import com.example.coffeerankingapk.data.repository.OwnerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OwnerDashboardState(
    val isLoading: Boolean = true,
    val dashboard: OwnerDashboard? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val activeCoupons: List<Coupon> = emptyList()
)

class OwnerDashboardViewModel(
    private val ownerRepository: OwnerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerDashboardState())
    val uiState: StateFlow<OwnerDashboardState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ownerRepository.observeDashboard().collect { dashboard ->
                _uiState.update {
                    it.copy(isLoading = false, dashboard = dashboard, errorMessage = null)
                }
            }
        }

        viewModelScope.launch {
            ownerRepository.observeCoupons().collect { coupons ->
                val activeCoupons = coupons.filter { coupon ->
                    coupon.isActive && !coupon.isExpired()
                }.sortedBy { coupon ->
                    coupon.expiryDate?.time ?: Long.MAX_VALUE
                }.take(3)

                _uiState.update { state ->
                    state.copy(activeCoupons = activeCoupons)
                }
            }
        }
    }

    fun createCoupon(
        title: String,
        description: String,
        discountPercent: Int,
        durationDays: Int?
    ) {
        viewModelScope.launch {
            val expiryTimestamp = durationDays?.let { days ->
                System.currentTimeMillis() + days * 24L * 60L * 60L * 1000L
            }

            val result = ownerRepository.createCoupon(title, description, discountPercent, expiryTimestamp)

            _uiState.update { state ->
                if (result.isSuccess) {
                    state.copy(successMessage = "Coupon created successfully", errorMessage = null)
                } else {
                    state.copy(
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Unable to create coupon",
                        successMessage = null
                    )
                }
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    private fun Coupon.isExpired(referenceTime: Long = System.currentTimeMillis()): Boolean {
        return expiryDate?.time?.let { it < referenceTime } == true
    }
}