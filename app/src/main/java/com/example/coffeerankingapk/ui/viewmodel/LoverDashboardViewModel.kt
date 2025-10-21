package com.example.coffeerankingapk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeerankingapk.data.model.Cafe
import com.example.coffeerankingapk.data.repository.AuthRepository
import com.example.coffeerankingapk.data.repository.CafeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoverDashboardState(
    val isLoading: Boolean = true,
    val cafes: List<Cafe> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val userName: String = "Coffee Lover",
    val rank: String = "",
    val points: Int = 0,
    val reviewsCount: Int = 0,
    val favoritesCount: Int = 0,
    val errorMessage: String? = null
)

class LoverDashboardViewModel(
    private val cafeRepository: CafeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoverDashboardState())
    val uiState: StateFlow<LoverDashboardState> = _uiState.asStateFlow()

    init {
        observeProfile()
        observeCafes()
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching { authRepository.refreshUserProfile() }
        }
    }

    fun toggleFavorite(cafeId: String) {
        viewModelScope.launch {
            runCatching { cafeRepository.toggleFavorite(cafeId) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.localizedMessage) }
                }
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            authRepository.observeUserProfile().collect { profile ->
                if (profile != null) {
                    _uiState.update {
                        it.copy(
                            userName = profile.name,
                            rank = profile.rank,
                            points = profile.points,
                            reviewsCount = profile.reviewsCount,
                            favoritesCount = profile.favoritesCount
                        )
                    }
                }
            }
        }
    }

    private fun observeCafes() {
        viewModelScope.launch {
            cafeRepository.observeCafes()
                .combine(cafeRepository.observeFavoriteIds()) { cafes, favorites ->
                    cafes to favorites
                }
                .collect { (cafes, favorites) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            cafes = cafes,
                            favorites = favorites,
                            errorMessage = null
                        )
                    }
                }
        }
    }
}