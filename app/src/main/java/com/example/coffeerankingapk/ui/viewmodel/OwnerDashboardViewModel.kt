package com.example.coffeerankingapk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val errorMessage: String? = null
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
    }
}