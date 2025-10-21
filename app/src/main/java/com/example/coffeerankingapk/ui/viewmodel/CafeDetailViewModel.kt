package com.example.coffeerankingapk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeerankingapk.data.model.Cafe
import com.example.coffeerankingapk.data.model.Review
import com.example.coffeerankingapk.data.repository.CafeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CafeDetailState(
    val isLoading: Boolean = true,
    val cafe: Cafe? = null,
    val reviews: List<Review> = emptyList(),
    val isFavorite: Boolean = false,
    val rewardClaimed: Boolean = false,
    val errorMessage: String? = null
)

class CafeDetailViewModel(
    private val cafeId: String,
    private val cafeRepository: CafeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CafeDetailState())
    val uiState: StateFlow<CafeDetailState> = _uiState.asStateFlow()

    init {
        if (cafeId.isNotBlank()) {
            observeCafe()
            observeReviews()
            observeFavorites()
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Cafe not found") }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            runCatching { cafeRepository.toggleFavorite(cafeId) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.localizedMessage) }
                }
        }
    }

    fun claimReward() {
        viewModelScope.launch {
            val result = cafeRepository.claimReward(cafeId)
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(rewardClaimed = true, errorMessage = null)
                } else {
                    it.copy(errorMessage = result.exceptionOrNull()?.localizedMessage)
                }
            }
        }
    }

    private fun observeCafe() {
        viewModelScope.launch {
            cafeRepository.observeCafe(cafeId).collect { cafe ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        cafe = cafe
                    )
                }
            }
        }
    }

    private fun observeReviews() {
        viewModelScope.launch {
            cafeRepository.observeReviews(cafeId, limit = 25).collect { reviews ->
                _uiState.update { it.copy(reviews = reviews) }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            cafeRepository.observeFavoriteIds().collect { favorites ->
                _uiState.update { it.copy(isFavorite = favorites.contains(cafeId)) }
            }
        }
    }
}