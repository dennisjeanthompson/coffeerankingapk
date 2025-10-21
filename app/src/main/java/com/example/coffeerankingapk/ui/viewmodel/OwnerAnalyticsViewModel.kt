package com.example.coffeerankingapk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeerankingapk.data.model.DashboardOverview
import com.example.coffeerankingapk.data.model.OwnerReview
import com.example.coffeerankingapk.data.repository.OwnerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OwnerAnalyticsState(
    val isLoading: Boolean = true,
    val overview: DashboardOverview = DashboardOverview(),
    val reviewsOverTime: List<Double> = emptyList(),
    val recentReviews: List<OwnerReview> = emptyList()
)

class OwnerAnalyticsViewModel(
    private val ownerRepository: OwnerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerAnalyticsState())
    val uiState: StateFlow<OwnerAnalyticsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ownerRepository.observeDashboard().collect { dashboard ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        overview = dashboard.overview,
                        reviewsOverTime = dashboard.reviewsOverTime,
                        recentReviews = dashboard.recentReviews
                    )
                }
            }
        }
    }
}